package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
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

        this.lifeCycle = new DataElementLifeCycle();
        this.lifeCycle.init(this);
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
     * Provides access to the current state of the data element as {@link DataElementLifeCycle.States}.
     *
     * @return The current state of the data element.
     */
    public DataElementLifeCycle.States getStateAsEnum() {
        return state != null ? DataElementLifeCycle.States.valueOf(state) : null;
    }

    public void initialize() {
        // TODO: 27.10.2016

        try {
            this.lifeCycle.triggerEvent(this, DataElementLifeCycle.Events.ready);
        }  catch (TooBusyException e) {
            logger.error("State transition could not be enacted after maximal amount of retries", e);
        }
    }

    public void archive() {
        // TODO: 27.10.2016
    }

    public void unarchive() {
        // TODO: 27.10.2016
    }

    public void delete() {
        // TODO: 27.10.2016

        // Trigger the delete event
        try {
            this.lifeCycle.triggerEvent(this, DataElementLifeCycle.Events.delete);
        }  catch (TooBusyException e) {
            logger.error("State transition could not be enacted after maximal amount of retries", e);
        }

        urn = null;
        entity = null;
        name = null;
        lifeCycle = null;
    }
}
