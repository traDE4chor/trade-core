/*
 * Copyright 2016 Michael Hahn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trade.core.model.data;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ModelUtils;
import org.trade.core.model.data.instance.DEInstance;
import org.trade.core.model.lifecycle.DataElementLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by hahnml on 25.10.2016.
 */
@Entity("dataElements")
public class DataElement extends BaseResource implements Serializable {

    private static final long serialVersionUID = -2632920295003689320L;

    @Transient
    Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataElement");

    /**
     * We use Uniform Resource Names (URN) to identify data elements and the entity to which they belong, independent
     * of their concrete location. Examples for such entities are choreography models or users which have defined and
     * registered the data element at TraDE.
     * <p>
     * Therefore, we use the following scheme according to RFC 2141:
     * <URN> ::= "urn:" <Namespace Identifier (NID)> ":" <Namespace Specific String (NSS)>
     * <p>
     * The NID is used to represent the context to which the parent data object belongs. The NSS contains the name of
     * the data object and the name of the data element separated by a ":" symbol.
     * For example, the URN "urn:cm23:resultData:element1" expresses that the data element with the name "element1"
     * is a child of the data object "resultData" which belongs to the entity "cm23".
     */
    private transient URN urn = null;

    private String entity = null;

    private String name = null;

    private transient DataElementLifeCycle lifeCycle = null;

    private String type = null;

    private String contentType = null;

    @State
    private String state;

    @Reference
    private DataObject parent = null;

    /**
     * Instantiates a new data element and associates it to the given data object.
     *
     * @param object the data object to which the data element belongs
     * @param name   the name of the data element
     * @throws URNSyntaxException the urn syntax exception
     */
    public DataElement(DataObject object, String name) throws URNSyntaxException {
        this.parent = object;
        this.name = name;
        this.entity = object.getUrn().getNamespaceIdentifier();

        this.urn = URN.newInstance(object.getUrn().getNamespaceIdentifier(), object.getUrn()
                .getNamespaceSpecificString() + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + this.name);
        this.identifier = urn.toString();

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
     * This constructor is only used by Morphia to load data element from the database.
     */
    private DataElement() {
        this.lifeCycle = new DataElementLifeCycle(this, false);
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
     * Gets the type of the data element. The following basic types are supported by default: "string", "number",
     * "boolean", "xml_element", "xml_element_list", or "binary". This list should be extensible in
     * the future by adding corresponding type (system) plugins.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets content type of the data element in form of a MIME type. For example, "text/plain; charset=utf-8",
     * "image/jpeg" or "application/xml".
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets content type.
     *
     * @param contentType the content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    /**
     * Instantiates the data element and returns the created instance.
     *
     * @param owner      the owner of the instance (entity that triggers the instantiation)
     * @param createdFor the resource for which the instance is created, e.g., for example an instance ID of a
     *                   workflow (information is used for correlation)
     * @return the created instance of the data element
     * @throws LifeCycleException If the data element is in a state which does not allow its instantiation.
     */
    public DEInstance instantiate(String owner, String createdFor) throws LifeCycleException {
        DEInstance result = null;

        if (this.isReady()) {
            try {
                result = new DEInstance(this.getUrn(), owner, createdFor);
            } catch (URNSyntaxException e) {
                logger.error("Instantiation of data element '{}' caused an exception during URN creation.", this.getUrn
                        ());
            }
        } else {
            logger.info("The data element ({}) can not be instantiated because it is in state '{}'.", this
                            .getUrn(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getUrn() +
                    ") can not be instantiated because it is in state '" + getState() + "'.");
        }

        return result;
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

    @PostLoad
    private void postLoad() {
        try {
            this.urn = URN.fromString(this.identifier);
        } catch (URNSyntaxException e) {
            logger.error("Reloading the persisted data element '{}' caused an URNSyntaxException", this.getIdentifier
                    ());
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            this.urn = URN.fromString(this.identifier);

            lifeCycle = new DataElementLifeCycle(this, false);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data element '{}'", this.getUrn().toString());
            throw new IOException("Class not found during deserialization of data element.");
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }
    }
}
