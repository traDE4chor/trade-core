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

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;
import org.trade.core.model.lifecycle.DataElementLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;
import org.trade.core.utils.events.ModelEvents;
import org.trade.core.utils.states.ModelStates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a data element within the middleware.
 * <p>
 * Created by hahnml on 25.10.2016.
 */
@Entity("dataElements")
public class DataElement extends ABaseResource implements ILifeCycleModelObject {

    private static final long serialVersionUID = -2632920295003689320L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataElement");

    private String entity;

    private String name;

    private transient DataElementLifeCycle lifeCycle;

    private transient IPersistenceProvider<DataElement> persistProv;

    private String type;

    private String contentType;

    @State
    private String state;

    @Reference
    private DataObject parent;

    @Reference
    private List<DataElementInstance> instances;

    /**
     * Instantiates a new data element and associates it to the given data object.
     *
     * @param object the data object to which the data element belongs
     * @param entity the name of the entity the data element belongs to
     * @param name   the name of the data element
     */
    public DataElement(DataObject object, String entity, String name) {
        this.parent = object;
        this.name = name;
        this.entity = entity;

        this.instances = new ArrayList<DataElementInstance>();
        this.lifeCycle = new DataElementLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElement.class);
    }

    /**
     * Instantiates a new data element with the given identifier and associates it to the given data object.
     *
     * @param object     the data object to which the data element belongs
     * @param identifier the identifier to use for the data element
     * @param entity     the name of the entity the data element belongs to
     * @param name       the name of the data element
     */
    public DataElement(DataObject object, String identifier, String entity, String name) {
        this.parent = object;
        this.identifier = identifier;
        this.name = name;
        this.entity = entity;

        this.instances = new ArrayList<DataElementInstance>();
        this.lifeCycle = new DataElementLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElement.class);
    }

    /**
     * Instantiates a new data element with an automatically generated random name and associates it to the given
     * data object while reusing the same entity value as specified for the data value.
     *
     * @param object the data object to which the data element belongs
     */
    public DataElement(DataObject object) {
        this(object, object.getEntity(), UUID.randomUUID().toString());
    }

    /**
     * This constructor is only used by Morphia to load data elements from the database.
     */
    private DataElement() {
        this.lifeCycle = new DataElementLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElement.class);
    }

    /**
     * Set the entity. Only allowed if the data object is not part of a data model.
     *
     * @param entity to set
     */
    public void setEntity(String entity) {
        if (this.parent != null && this.parent.getDataModel() == null) {
            this.entity = entity;
        }
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
     * Set the name. Only allowed if the data object is not part of a data model.
     *
     * @param name to set
     */
    public void setName(String name) {
        if (this.parent != null && this.parent.getDataModel() == null) {
            this.name = name;
        }
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
     * Provides the parent data object to which this data element belongs.
     *
     * @return the parent data object
     */
    public DataObject getParent() {
        return parent;
    }

    /**
     * Provides the list of data element instances of this data element.
     *
     * @return An unmodifiable list of data element instances.
     */
    public List<DataElementInstance> getDataElementInstances() {
        return this.instances != null ? Collections.unmodifiableList(this.instances) : null;
    }

    /**
     * Gets all data element instances of this data element created by the specified entity.
     *
     * @param createdBy the entity which created the data element instances that should be returned
     * @return An unmodifiable list of matching data element instances.
     */
    public List<DataElementInstance> getDataElementInstances(String createdBy) {
        List<DataElementInstance> result = this.instances.stream().filter(s -> s.getCreatedBy().equals(createdBy)).collect
                (Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets a data element instance by id.
     *
     * @param identifier the identifier
     * @return the data element instance by id
     */
    public DataElementInstance getDataElementInstanceById(String identifier) {
        Optional<DataElementInstance> opt = this.instances.stream().filter(s -> s.getIdentifier().equals(identifier)).findFirst();
        return opt.orElse(null);
    }

    public void initialize() throws Exception {
        // TODO: 27.10.2016 Add data element specific parameters and assignments, e.g. data type, type system, etc.

        // Trigger the ready event
        try {
            this.lifeCycle.triggerEvent(this, ModelEvents.ready);

            // Add the data element to the specified data object after it is initialized
            this.getParent().addDataElement(this);

            // Persist the changed parent object
            this.getParent().storeToDS();
        } catch (TooBusyException e) {
            logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                    "amount of retries", this.getIdentifier(), ModelEvents.ready);
            throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
        }
    }

    @Override
    public void archive() throws Exception {
        if (this.isReady()) {
            // TODO: 27.10.2016

            // Trigger the archive event
            try {
                this.lifeCycle.triggerEvent(this, ModelEvents.archive);

                // Persist the changes at the data source
                this.storeToDS();
            } catch (TooBusyException e) {
                logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                        "amount of retries", this.getIdentifier(), ModelEvents.archive);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            }
        } else {
            logger.info("The data element ({}) can not be archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getIdentifier() +
                    ") can not be archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void unarchive() throws Exception {
        if (this.isArchived()) {
            // TODO: 27.10.2016

            // Trigger the unarchive event
            try {
                this.lifeCycle.triggerEvent(this, ModelEvents.unarchive);

                // Persist the changes at the data source
                this.storeToDS();
            } catch (TooBusyException e) {
                logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                        "amount of retries", this.getIdentifier(), ModelEvents.unarchive);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            }
        } else {
            logger.info("The data element ({}) can not be un-archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getIdentifier() +
                    ") can not be un-archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void delete() throws Exception {
        if (this.isReady() || this.isInitial()) {
            // TODO: 27.10.2016

            // Trigger the delete event
            try {
                this.lifeCycle.triggerEvent(this, ModelEvents.delete);
            } catch (TooBusyException e) {
                logger.error("State transition for data element '{}' with event '{}' could not be enacted after maximal " +
                        "amount of retries", this.getIdentifier(), ModelEvents.delete);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            }

            identifier = null;
            entity = null;
            name = null;
            lifeCycle = null;
        } else {
            logger.info("The data element ({}) can not be deleted because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getIdentifier() +
                    ") can not be deleted because it is in state '" + getState() + "'.");
        }
    }

    /**
     * Instantiates the data element and returns the created instance.
     *
     * @param dataObjectInstance    the data object instance to which the new data element instance belongs
     * @param createdBy             the entity that triggers the instantiation of the data element. For example, an instance
     *                              ID of a workflow or the name of human user can be used.
     * @param correlationProperties the properties used for identifying a specific instance of a data element
     * @return the created instance of the data element
     * @throws LifeCycleException If the data element is in a state which does not allow its instantiation.
     */
    public DataElementInstance instantiate(DataObjectInstance dataObjectInstance, String createdBy, HashMap<String, String>
            correlationProperties) throws
            LifeCycleException {
        DataElementInstance result = null;

        if (this.isReady()) {
            result = new DataElementInstance(this, dataObjectInstance, createdBy, correlationProperties);

            // Add the new instance to the list of instances
            this.instances.add(result);

            // Persist the changed object
            this.storeToDS();

            // Associate the data element instance with the data object instance
            dataObjectInstance.addDataElementInstance(result);
        } else {
            logger.info("The data element ({}) can not be instantiated because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data element (" + this.getIdentifier() +
                    ") can not be instantiated because it is in state '" + getState() + "'.");
        }

        return result;
    }

    /**
     * Removes the data element instance from this data element.
     *
     * @param instance an instance of this data element which should be removed from this data element.
     */
    public void removeDataElementInstance(DataElementInstance instance) {
        // Check if the data element instance belongs to this data element
        if (instance.getDataElement() == this) {
            this.instances.remove(instance);

            // Persist the changes at the data source
            this.storeToDS();
        }
    }

    public boolean isInitial() {
        return getState() != null && this.getState().equals(ModelStates
                .INITIAL.name());
    }

    public boolean isReady() {
        return getState() != null && this.getState().equals(ModelStates
                .READY.name());
    }

    public boolean isArchived() {
        return getState() != null && this.getState().equals(ModelStates
                .ARCHIVED.name());
    }

    public boolean isDeleted() {
        return getState() != null && this.getState().equals(ModelStates
                .DELETED.name());
    }

    @Override
    public void storeToDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.storeObject(this);
            } catch (Exception e) {
                logger.error("Storing data element '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting data element '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            this.lifeCycle = new DataElementLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElement.class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data element '{}'", this.getIdentifier());
            throw new IOException("Class not found during deserialization of data element.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DataElement) {
            DataElement s = (DataElement) object;
            return this.identifier.equals(s.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, name, entity, type, contentType);
    }
}
