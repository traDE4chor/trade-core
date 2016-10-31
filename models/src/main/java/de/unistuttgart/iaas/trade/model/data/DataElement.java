package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.LifeCycleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.persistence.annotations.State;

import java.util.UUID;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataElement {

    Logger logger = LoggerFactory.getLogger("de.unistuttgart.trade.model.data.DataElement");

    /**
     * We use Uniform Resource Names (URN) to identify data elements and the entity to which they belong, independent
     * of their concrete location. Examples for such entities are choreography models or users which have defined and
     * registered the data element at TraDE.
     * <p>
     * Therefore, we use the following scheme according to RFC 2141:
     * <URN> ::= "urn:" <Namespace Identifier (NID)> ":" <Namespace Specific String (NSS)>
     * <p>
     * The NID is used to represent the context to which a data element belongs. The NSS is used to
     * represent the name of the parent data object and the data element itself separated by a '$' as delimiter.
     * For example, the URN "urn:cm23:resultData$element1" expresses that the data element with the name "element1"
     * is a child of the data object "resultData" which belongs to the entity "cm23".
     */
    private URN urn = null;

    private String entity = null;

    private String name = null;

    private DataElementLifeCycle lifeCycle = null;

    @State
    private String state;

    /**
     * Instantiates a new data element and associates it to the given data object.
     *
     * @param object the data object to which the data element belongs
     * @param name   the name of the data element
     * @throws URNSyntaxException the urn syntax exception
     */
    public DataElement(DataObject object, String name) throws URNSyntaxException {
        this.name = name;
        this.urn = URN.newInstance(object.getUrn().getNamespaceIdentifier(), object.getUrn()
                .getNamespaceSpecificString() + ModelUtils.URN_NAMESPACE_SPECIFIC_DELIMITER + this.name);

        this.lifeCycle = new DataElementLifeCycle(this);
    }

    /**
     * Instantiates a new data element with an automatically generated random name and associates it to the given
     * data object.
     *
     * @param object the data object to which the data element belongs
     * @throws URNSyntaxException the urn syntax exception
     */
    public DataElement(DataObject object) throws URNSyntaxException {
        this(object, UUID.randomUUID().toString());
    }

    /**
     * Provides the uniform resource name of the data element.
     *
     * @return The URN which identifies the data element.
     */
    public URN getUrn() {
        return this.urn;
    }


    /**
     * Provides the name of the entity the data element belongs to.
     *
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Provides the name of the data element.
     *
     * @return The name of the data element.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Provides access to the current state of the data element through its life cycle object.
     *
     * @return The current state of the data element.
     */
    public String getState() {
        return state;
    }

    /**
     * Initialize.
     *
     * @throws LifeCycleException the life cycle exception
     */
    public void initialize() throws LifeCycleException {
        // TODO: 27.10.2016 Add data element specific parameters and assignments, e.g. data type, type system, etc.

        // Trigger the ready event
        try {
            this.lifeCycle.triggerEvent(this, DataElementLifeCycle.Events.ready);
        } catch (TooBusyException e) {
            logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                    "amount of retries", this.getUrn(), DataElementLifeCycle.Events.ready);
            throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
        }
    }

    /**
     * Archive.
     *
     * @throws LifeCycleException the life cycle exception
     */
    public void archive() throws LifeCycleException {
        if (this.isReady()) {
            // TODO: 27.10.2016

            // Trigger the archive event
            try {
                this.lifeCycle.triggerEvent(this, DataElementLifeCycle.Events.archive);
            } catch (TooBusyException e) {
                logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                        "amount of retries", this.getUrn(), DataElementLifeCycle.Events.archive);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            }
        } else {
            logger.info("The data element ({}) can not be archived because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getUrn() +
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
            // TODO: 27.10.2016

            // Trigger the unarchive event
            try {
                this.lifeCycle.triggerEvent(this, DataElementLifeCycle.Events.unarchive);
            } catch (TooBusyException e) {
                logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                        "amount of retries", this.getUrn(), DataElementLifeCycle.Events.unarchive);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            }
        } else {
            logger.info("The data element ({}) can not be un-archived because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getUrn() +
                    ") can not be un-archived because it is in state '" + getState() + "'.");
        }
    }

    /**
     * Delete.
     *
     * @throws LifeCycleException the life cycle exception
     */
    public void delete() throws LifeCycleException {
        if (this.isReady() || this.isInitial()) {
            // TODO: 27.10.2016

            // Trigger the delete event
            try {
                this.lifeCycle.triggerEvent(this, DataElementLifeCycle.Events.delete);
            } catch (TooBusyException e) {
                logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                        "amount of retries", this.getUrn(), DataElementLifeCycle.Events.delete);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            }

            urn = null;
            entity = null;
            name = null;
            lifeCycle = null;
        } else {
            logger.info("The data element ({}) can not be deleted because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getUrn() +
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
