package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.DataObjectLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.persistence.annotations.State;

import java.util.*;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataObject {

    Logger logger = LoggerFactory.getLogger("de.unistuttgart.trade.model.data.DataObject");

    /**
     * We use Uniform Resource Names (URN) to identify data objects and the entity to which they belong, independent
     * of their concrete location. Examples for such entities are choreography models or users which have defined and
     * registered the data object at TraDE.
     * <p>
     * Therefore, we use the following scheme according to RFC 2141:
     * <URN> ::= "urn:" <Namespace Identifier (NID)> ":" <Namespace Specific String (NSS)>
     * <p>
     * Where the NID is used to represent the context to which a data object belongs.
     * For example, the URN "urn:cm23:resultData" expresses that the data object with the name "resultData"
     * belongs to the entity "cm23".
     */
    private URN urn = null;

    private String entity = null;

    private String name = null;

    private DataObjectLifeCycle lifeCycle = null;

    @State
    private String state;

    private List<DataElement> dataElements = new ArrayList<DataElement>();

    /**
     * Instantiates a new data object.
     *
     * @param entity the entity to which the data object belongs
     * @param name   the name of the data object
     * @throws URNSyntaxException the urn syntax exception
     */
    public DataObject(String entity, String name) throws URNSyntaxException {
        this.name = name;
        this.urn = URN.newInstance(entity, name);

        this.lifeCycle = new DataObjectLifeCycle();
        this.lifeCycle.init(this);
    }

    /**
     * Instantiates a new data object with an automatically generated random URN.
     *
     * @throws URNSyntaxException the urn syntax exception
     */
    public DataObject() throws URNSyntaxException {
        this(ModelUtils.RANDOM_URN_NAMESPACE_ID, UUID.randomUUID().toString());
    }

    /**
     * Provides the uniform resource name of the data object.
     *
     * @return The URN which identifies the data object.
     */
    public URN getUrn() {
        return this.urn;
    }


    /**
     * Provides the name of the entity the data object belongs to.
     *
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Provides the name of the data object.
     *
     * @return The name of the data object.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Provides access to the current state of the data object.
     *
     * @return The current state of the data object.
     */
    public String getState() {
        return state;
    }

    /**
     * Provides access to the current state of the data object as {@link DataObjectLifeCycle.States}.
     *
     * @return The current state of the data object.
     */
    public DataObjectLifeCycle.States getStateAsEnum() {
        return state != null ? DataObjectLifeCycle.States.valueOf(state) : null;
    }

    /**
     * Provides the list of data elements associated to this data object.
     *
     * @return An unmodifiable list of data elements.
     */
    public List<DataElement> getDataElements() {
        return Collections.unmodifiableList(this.dataElements);
    }

    public DataElement getDataElement(String name) {
        Optional<DataElement> opt = this.dataElements.stream().filter(s -> s.getName().equals(name)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    public DataElement getDataElement(URN identifier) {
        Optional<DataElement> opt = this.dataElements.stream().filter(s -> s.getUrn().equals(identifier)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    /**
     * Adds a new data element to the data object. We assume that the data element to be added is already in state
     * READY which means fully prepared for instantiation. If not it won't be added to the data object.
     *
     * @param element The element to add.
     * @return If the element was added or not.
     */
    public boolean addDataElement(DataElement element) {
        boolean success = false;

        if (element != null) {
            // Only try to add the data element if the data object is not archived or deleted.
            if (getStateAsEnum().equals(DataObjectLifeCycle.States.INITIAL) || getStateAsEnum().equals
                    (DataObjectLifeCycle.States.READY)) {

                // Check if the data element is ready, else reject
                if (element.getState() != null && element.getStateAsEnum().equals(DataElementLifeCycle.States.READY)) {
                    this.dataElements.add(element);
                    success = true;

                    // If the data object is in INITIAL state, we trigger the transition to READY since it is now ready for
                    // instantiation.
                    if (getStateAsEnum().equals(DataObjectLifeCycle.States.INITIAL)) {
                        try {
                            this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.ready);
                        } catch (TooBusyException e) {
                            logger.error("State transition could not be enacted after maximal amount of retries", e);
                        }
                    }
                } else {
                    logger.info("Trying to add a non-ready data element ({}) to data object '{}'", element.getName(),
                            this
                            .getUrn());
                }
            } else {
                logger.info("No data element can be added because the data object ({}) is in state '{}'.", this.getUrn(),
                        getState());
            }
        }

        return success;
    }

    /**
     * Deletes an existing data element from the data object.
     *
     * @param element the element to delete
     * @return If the data element was deleted or not.
     */
    public boolean deleteDataElement(DataElement element) {
        boolean success = false;

        if (element != null) {
            if (getStateAsEnum().equals(DataObjectLifeCycle.States.INITIAL) || getStateAsEnum().equals
                    (DataObjectLifeCycle.States.READY)) {
                // Remove the element from the list
                this.dataElements.remove(element);

                // Trigger the deletion of the element
                element.delete();

                success = true;

                // Check if the deleted data element was the only child of the data object
                if (this.dataElements.size() == 0) {
                    // Change the state back to initial to disallow its instantiation
                    try {
                        this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.initial);
                    } catch (TooBusyException e) {
                        logger.error("State transition could not be enacted after maximal amount of retries", e);
                    }
                }
            } else {
                logger.info("No data element can be deleted because the data object ({}) is in state '{}'.", this
                                .getUrn(),
                        getState());
            }
        }

        return success;
    }

    public void archive() {
        // TODO: 27.10.2016

        // Trigger the archive event
        try {
            this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.archive);
        } catch (TooBusyException e) {
            logger.error("State transition could not be enacted after maximal amount of retries", e);
        }
    }

    public void unarchive() {
        // TODO: 27.10.2016  
    }

    public void delete() {
        for (Iterator<DataElement> i = this.dataElements.iterator(); i.hasNext(); ) {
            deleteDataElement(i.next());
        }

        // TODO: 27.10.2016

        // Trigger the delete event
        try {
            this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.delete);
        } catch (TooBusyException e) {
            logger.error("State transition could not be enacted after maximal amount of retries", e);
        }

        // Cleanup variables
        urn = null;
        entity = null;
        name = null;
        lifeCycle = null;
    }
}
