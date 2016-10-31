package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.DataObjectLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.LifeCycleException;
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

        this.lifeCycle = new DataObjectLifeCycle(this);
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
     * Provides the list of data elements associated to this data object.
     *
     * @return An unmodifiable list of data elements.
     */
    public List<DataElement> getDataElements() {
        return this.dataElements != null ? Collections.unmodifiableList(this.dataElements) : null;
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
     * @throws LifeCycleException the life cycle exception
     */
    public void addDataElement(DataElement element) throws LifeCycleException {
        if (element != null) {
            // Only try to add the data element if the data object is not archived or deleted.
            if (this.isInitial() || this.isReady()) {
                // Check if the data element is ready, else reject
                if (element.isReady()) {
                    this.dataElements.add(element);

                    // If the data object is in INITIAL state, we trigger the transition to READY since it is now ready for
                    // instantiation.
                    if (this.isInitial()) {
                        try {
                            this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.ready);
                        } catch (TooBusyException e) {
                            logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                                    "after maximal " +
                                    "amount of retries", this.getUrn(), DataObjectLifeCycle.Events.ready);
                            throw new LifeCycleException("State transition could not be enacted after maximal amount " +
                                    "of retries", e);
                        }
                    }
                } else {
                    logger.info("Trying to add a non-ready data element ({}) to data object '{}'", element.getName(),
                            this.getUrn());
                    throw new LifeCycleException("Trying to add a non-ready data element (" + element.getName() + ")" +
                            " to data object '" + this.getUrn() + "'");
                }
            } else {
                logger.info("No data element can be added because the data object ({}) is in state '{}'.", this.getUrn(),
                        getState());
                throw new LifeCycleException("No data element can be added because the data object (" + this.getUrn() +
                        ") is in state '" + getState() + "'.");
            }
        }
    }

    /**
     * Deletes an existing data element from the data object.
     *
     * @param element the element to delete
     */
    public void deleteDataElement(DataElement element) throws LifeCycleException {
        if (element != null) {
            if (this.isInitial() || this.isReady()) {
                // Remove the element from the list
                this.dataElements.remove(element);

                // Trigger the deletion of the element
                element.delete();

                // Check if the deleted data element was the only child of the data object
                if (this.dataElements.isEmpty()) {
                    // Change the state back to initial to disallow its instantiation
                    try {
                        this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.initial);
                    } catch (TooBusyException e) {
                        logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                                "after maximal " +
                                "amount of retries", this.getUrn(), DataObjectLifeCycle.Events.ready);
                        throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
                    }
                }
            } else {
                logger.info("No data element can be deleted because the data object ({}) is in state '{}'.", this
                                .getUrn(),
                        getState());

                throw new LifeCycleException("No data element can be deleted because the data object (" + this.getUrn() +
                        ") is in state '" + getState() + "'.");
            }
        }
    }

    private void deleteDataElements() throws LifeCycleException {
        if (this.isInitial() || this.isReady()) {
            // Loop over all elements
            for (Iterator<DataElement> iter = this.dataElements.iterator(); iter.hasNext(); ) {
                DataElement element = iter.next();

                // Remove the element from the list
                iter.remove();

                // Trigger the deletion of the element
                element.delete();

                // Check if the deleted data element was the only child of the data object
                if (this.dataElements.isEmpty()) {
                    // Change the state back to initial to disallow its instantiation
                    try {
                        this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.initial);
                    } catch (TooBusyException e) {
                        logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                                "after maximal " +
                                "amount of retries", this.getUrn(), DataObjectLifeCycle.Events.ready);
                        throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
                    }
                }
            }
        } else {
            logger.info("No data element can be deleted because the data object ({}) is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("No data element can be deleted because the data object (" + this.getUrn() +
                    ") is in state '" + getState() + "'.");
        }
    }

    /**
     * Archive.
     *
     * @throws LifeCycleException the life cycle exception
     */
    public void archive() throws LifeCycleException {
        if (this.isReady()) {
            // Remember changed elements for undoing changes in case of an exception
            List<DataElement> changedElements = new ArrayList<DataElement>();

            try {
                for (DataElement element : this.dataElements) {
                    element.archive();
                    changedElements.add(element);
                }

                // Trigger the archive event for the whole data object since all data elements are archived
                // successfully.
                this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.archive);
            } catch (Exception e) {
                logger.warn("Archiving data object '{}' not successful because archiving of one of its data elements " +
                        "caused an exception. Trying to undo all changes.", this.getUrn());

                try {
                    // Un-archive the already archived data elements again
                    for (DataElement elm : changedElements) {
                        elm.unarchive();
                    }
                } catch (Exception ex) {
                    logger.error("Rollback of changes triggered by archiving data object '{}' was not successful. " +
                            "MANUAL INTERVENTION REQUIRED.", ex);
                    throw new LifeCycleException("Archiving data object '" + this
                            .getUrn() + "' not successful. MANUAL INTERVENTION REQUIRED.", ex);
                }
            }
        } else {
            logger.info("The data object ({}) can not be archived because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getUrn() +
                    ") can not be archived because it is in state '" + getState() + "'.");
        }
    }

    /**
     * Unarchive.
     *
     * @throws LifeCycleException the life cycle exception
     */
    public void unarchive() throws LifeCycleException {
        if (this.isArchived()) {
            // Remember changed elements for undoing changes in case of an exception
            List<DataElement> changedElements = new ArrayList<DataElement>();

            try {
                for (DataElement element : this.dataElements) {
                    element.unarchive();
                    changedElements.add(element);
                }

                // Trigger the unarchive event for the whole data object since all data elements are unarchived
                // successfully.
                this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.unarchive);
            } catch (Exception e) {
                logger.warn("Un-archiving data object '{}' not successful because un-archiving of one of its data " +
                        "elements caused an exception. Trying to undo all changes.", this.getUrn());

                try {
                    // Archive the already un-archived data elements again
                    for (DataElement elm : changedElements) {
                        elm.archive();
                    }
                } catch (Exception ex) {
                    logger.error("Rollback of changes triggered by un-archiving data object '{}' was not successful. " +
                            "MANUAL INTERVENTION REQUIRED.", ex);
                }
            }
        } else {
            logger.info("The data object ({}) can not be un-archived because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getUrn() +
                    ") can not be un-archived because it is in state '" + getState() + "'.");
        }
    }

    /**
     * Delete.
     *
     * @throws LifeCycleException the life cycle exception
     */
    public void delete() throws LifeCycleException {
        if (this.isReady() || this.isInitial() || this.isArchived()) {

            try {
                // Delete all data elements
                deleteDataElements();

                // Trigger the delete event for the whole data object since all data elements are deleted
                // successfully.
                this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.delete);
            } catch (TooBusyException e) {
                logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                        "after maximal " +
                        "amount of retries", this.getUrn(), DataObjectLifeCycle.Events.ready);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            } catch (Exception e) {
                logger.error("Deletion data object '{}' not successful because deletion of one of its data " +
                        "elements caused an exception. MANUAL INTERVENTION REQUIRED.", this.getUrn());

                // Trigger the initial event to disable the creation of new instances of the corrupted object
                try {
                    this.lifeCycle.triggerEvent(this, DataObjectLifeCycle.Events.initial);
                } catch (TooBusyException ex) {
                    logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                            "after maximal " +
                            "amount of retries", this.getUrn(), DataObjectLifeCycle.Events.ready);
                    throw new LifeCycleException("Deletion data object '"+this.getUrn()+"' not successful", ex);
                }

                throw new LifeCycleException("Deletion data object '"+this.getUrn()+"' not successful", e);
            }

            // Cleanup variables
            this.urn = null;
            this.entity = null;
            this.name = null;
            this.lifeCycle = null;
            this.dataElements = null;
        } else {
            logger.info("The data object ({}) can not be deleted because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getUrn() +
                    ") can not be deleted because it is in state '" + getState() + "'.");
        }
    }

    public boolean isInitial() {
        return getState() != null && this.getState().equals(DataElementLifeCycle.States
                .INITIAL.name());
    }

    public boolean isReady() {
        return getState() != null && this.getState().equals(DataElementLifeCycle.States
                .READY.name());
    }

    public boolean isArchived() {
        return getState() != null && this.getState().equals(DataElementLifeCycle.States
                .ARCHIVED.name());
    }

    public boolean isDeleted() {
        return getState() != null && this.getState().equals(DataElementLifeCycle.States
                .DELETED.name());
    }
}
