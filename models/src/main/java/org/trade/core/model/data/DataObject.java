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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.instance.DataObjectInstance;
import org.trade.core.model.lifecycle.DataObjectLifeCycle;
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
 * This class represents a data object within the middleware.
 * <p>
 * Created by hahnml on 25.10.2016.
 */
@Entity("dataObjects")
public class DataObject extends ABaseResource implements ILifeCycleModelObject {

    private static final long serialVersionUID = 2549294173554279537L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataObject");

    @JsonProperty("entity")
    private String entity;

    @JsonProperty("name")
    private String name;

    private transient DataObjectLifeCycle lifeCycle;

    private transient IPersistenceProvider<DataObject> persistProv;

    @JsonProperty("state")
    @State
    private String state;

    @JsonProperty("dataModel")
    @Reference
    private DataModel dataModel;

    @JsonProperty("dataElements")
    @Reference
    private List<DataElement> dataElements;

    @JsonProperty("dataObjectInstances")
    @Reference
    private List<DataObjectInstance> dataObjectInstances;

    /**
     * Instantiates a new data object.
     *
     * @param entity the entity to which the data object belongs
     * @param name   the name of the data object
     */
    public DataObject(String entity, String name) {
        this(null, entity, name);
    }

    /**
     * Instantiates a new data object for the given data model.
     *
     * @param model  the data model the data object belongs to
     * @param entity the entity to which the data object belongs
     * @param name   the name of the data object
     */
    public DataObject(DataModel model, String entity, String name) {
        this.dataModel = model;
        this.name = name;
        this.entity = entity;

        this.dataElements = new ArrayList<>();
        this.dataObjectInstances = new ArrayList<>();
        this.lifeCycle = new DataObjectLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObject.class);
    }

    /**
     * Instantiates a new data object with the given identifier for the given data model.
     *
     * @param model      the data model the data object belongs to
     * @param identifier the identifier to use for the data object
     * @param entity     the entity to which the data object belongs
     * @param name       the name of the data object
     */
    public DataObject(DataModel model, String identifier, String entity, String name) {
        this.dataModel = model;
        this.identifier = identifier;
        this.name = name;
        this.entity = entity;

        this.dataElements = new ArrayList<>();
        this.dataObjectInstances = new ArrayList<>();
        this.lifeCycle = new DataObjectLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObject.class);
    }

    /**
     * This constructor is only used by Morphia to load data objects from the database.
     */
    private DataObject() {
        this.lifeCycle = new DataObjectLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObject.class);
    }

    /**
     * Set the entity. Only allowed if the data object is not part of a data model.
     *
     * @param entity the entity to set
     */
    @JsonIgnore
    public void setEntity(String entity) {
        if (this.dataModel == null) {
            this.entity = entity;
        }
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
     * Set the name. Only allowed if the data object is not part of a data model.
     *
     * @param name the name to set
     */
    @JsonIgnore
    public void setName(String name) {
        if (this.dataModel == null) {
            this.name = name;
        }
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
     * Provides the data model to which this data object belongs, if there is one.
     *
     * @return the parent data model or null, if the data object is defined independently.
     */
    public DataModel getDataModel() {
        return dataModel;
    }

    /**
     * Provides the list of data elements associated to this data object.
     *
     * @return An unmodifiable list of data elements.
     */
    @JsonIgnore
    public List<DataElement> getDataElements() {
        return this.dataElements != null ? Collections.unmodifiableList(this.dataElements) : null;
    }

    /**
     * Gets a data element by its name.
     *
     * @param name the name of the data element
     * @return the data element
     */
    public DataElement getDataElement(String name) {
        Optional<DataElement> opt = this.dataElements.stream().filter(s -> s.getName().equals(name)).findFirst();
        return opt.orElse(null);
    }

    /**
     * Gets a data element by its id.
     *
     * @param identifier the identifier of the data element
     * @return the data element with the given id
     */
    public DataElement getDataElementById(String identifier) {
        Optional<DataElement> opt = this.dataElements.stream().filter(s -> s.getIdentifier().equals(identifier)).findFirst();
        return opt.orElse(null);
    }

    /**
     * Provides the list of data object instances of this data object.
     *
     * @return An unmodifiable list of data object instances.
     */
    @JsonIgnore
    public List<DataObjectInstance> getDataObjectInstances() {
        return this.dataObjectInstances != null ? Collections.unmodifiableList(this.dataObjectInstances) : null;
    }

    /**
     * Gets all data object instances of this data object created by the specified entity.
     *
     * @param createdBy the entity which created the data object instances that should be returned
     * @return An unmodifiable list of matching data object instances.
     */
    public List<DataObjectInstance> getDataObjectInstances(String createdBy) {
        List<DataObjectInstance> result = this.dataObjectInstances.stream().filter(s -> s.getCreatedBy().equals(createdBy)).collect
                (Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets a data object instance by id.
     *
     * @param identifier the identifier
     * @return the data object instance by id
     */
    public DataObjectInstance getDataObjectInstanceById(String identifier) {
        Optional<DataObjectInstance> opt = this.dataObjectInstances.stream().filter(s -> s.getIdentifier().equals(identifier)).findFirst();
        return opt.orElse(null);
    }

    /**
     * Gets a data object instance by its correlation properties.
     *
     * @param correlationProperties to search for.
     * @return The data object instance with the given correlation properties or NULL if no matching instance was found.
     */
    public DataObjectInstance getDataObjectInstanceByCorrelationProps(HashMap<String, String> correlationProperties) {
        Optional<DataObjectInstance> opt = this.dataObjectInstances.stream().filter(s -> s.getCorrelationProperties().equals(correlationProperties)).findFirst();
        return opt.orElse(null);
    }

    /**
     * Adds a new data element to the data object. We assume that the data element to be added is already in state
     * READY which means fully prepared for instantiation. If not it won't be added to the data object.
     *
     * @param element The element to add.
     * @throws LifeCycleException the life cycle exception
     */
    void addDataElement(DataElement element) throws LifeCycleException {
        if (element != null) {
            // Only try to add the data element if the data object is not archived or deleted.
            if (this.isInitial() || this.isReady()) {
                // Check if the data element is ready, else reject
                if (element.isReady()) {
                    if (!this.dataElements.contains(element)) {
                        this.dataElements.add(element);
                    }

                    // If the data object is in INITIAL state, we trigger the transition to READY since it is now ready for
                    // instantiation.
                    if (this.isInitial()) {
                        try {
                            this.lifeCycle.triggerEvent(this, ModelEvents.ready);
                        } catch (TooBusyException e) {
                            logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                                    "after maximal " +
                                    "amount of retries", this.getIdentifier(), ModelEvents.ready);
                            throw new LifeCycleException("State transition could not be enacted after maximal amount " +
                                    "of retries", e);
                        }
                    }

                    // Persist the changes at the data source
                    this.storeToDS();
                } else {
                    logger.info("Trying to add a non-ready data element ({}) to data object '{}'", element.getName(),
                            this.getIdentifier());
                    throw new LifeCycleException("Trying to add a non-ready data element (" + element.getName() + ")" +
                            " to data object '" + this.getIdentifier() + "'");
                }
            } else {
                logger.info("No data element can be added because the data object ({}) is in state '{}'.", this.getIdentifier(),
                        getState());
                throw new LifeCycleException("No data element can be added because the data object (" + this.getIdentifier() +
                        ") is in state '" + getState() + "'.");
            }
        }
    }

    /**
     * Deletes an existing data element from the data object.
     *
     * @param element the element to delete
     * @throws LifeCycleException the life cycle exception
     */
    public void deleteDataElement(DataElement element) throws Exception {
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
                        this.lifeCycle.triggerEvent(this, ModelEvents.initial);
                    } catch (TooBusyException e) {
                        logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                                "after maximal " +
                                "amount of retries", this.getIdentifier(), ModelEvents.ready);
                        throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
                    }
                }

                // Persist the changes at the data source
                this.storeToDS();
            } else {
                logger.info("No data element can be deleted because the data object ({}) is in state '{}'.", this
                                .getIdentifier(),
                        getState());

                throw new LifeCycleException("No data element can be deleted because the data object (" + this.getIdentifier() +
                        ") is in state '" + getState() + "'.");
            }
        }
    }

    private void deleteDataElements() throws Exception {
        if (this.isInitial() || this.isReady() || this.isArchived()) {
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
                        this.lifeCycle.triggerEvent(this, ModelEvents.initial);
                    } catch (TooBusyException e) {
                        logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                                "after maximal " +
                                "amount of retries", this.getIdentifier(), ModelEvents.ready);
                        throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
                    }
                }
            }
        } else {
            logger.info("No data element can be deleted because the data object ({}) is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("No data element can be deleted because the data object (" + this.getIdentifier() +
                    ") is in state '" + getState() + "'.");
        }
    }

    private void deleteDataObjectInstances() throws Exception {
        if (this.isInitial() || this.isReady() || this.isArchived()) {
            // Loop over all data object instances
            for (Iterator<DataObjectInstance> iter = this.dataObjectInstances.iterator(); iter.hasNext(); ) {
                DataObjectInstance objInst = iter.next();

                // Remove the data object instance from the list
                iter.remove();

                // Trigger the deletion of the data object instance
                objInst.delete();
            }
        } else {
            logger.info("No data object instance can be deleted because the data object ({}) is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("No data object instance can be deleted because the data object (" + this
                    .getIdentifier() +
                    ") is in state '" + getState() + "'.");
        }
    }

    @Override
    public void archive() throws Exception {
        if (this.isReady()) {
            // Remember changed elements for undoing changes in case of an exception
            List<DataElement> changedElements = new ArrayList<>();

            try {
                for (DataElement element : this.dataElements) {
                    element.archive();
                    changedElements.add(element);
                }

                // Trigger the archive event for the whole data object since all data elements are archived
                // successfully.
                this.lifeCycle.triggerEvent(this, ModelEvents.archive);

                // Persist the changes at the data source
                this.storeToDS();
            } catch (Exception e) {
                logger.warn("Archiving data object '{}' not successful because archiving of one of its data elements " +
                        "caused an exception. Trying to undo all changes.", this.getIdentifier());

                try {
                    // Un-archive the already archived data elements again
                    for (DataElement elm : changedElements) {
                        elm.unarchive();
                    }
                } catch (Exception ex) {
                    logger.error("Rollback of changes triggered by archiving data object '{}' was not successful. " +
                            "MANUAL INTERVENTION REQUIRED.", ex);
                    throw new LifeCycleException("Archiving data object '" + this
                            .getIdentifier() + "' not successful. MANUAL INTERVENTION REQUIRED.", ex);
                }
            }
        } else {
            logger.info("The data object ({}) can not be archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getIdentifier() +
                    ") can not be archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void unarchive() throws Exception {
        if (this.isArchived()) {
            // Remember changed elements for undoing changes in case of an exception
            List<DataElement> changedElements = new ArrayList<>();

            try {
                for (DataElement element : this.dataElements) {
                    element.unarchive();
                    changedElements.add(element);
                }

                // Trigger the unarchive event for the whole data object since all data elements are unarchived
                // successfully.
                this.lifeCycle.triggerEvent(this, ModelEvents.unarchive);

                // Persist the changes at the data source
                this.storeToDS();
            } catch (Exception e) {
                logger.warn("Un-archiving data object '{}' not successful because un-archiving of one of its data " +
                        "elements caused an exception. Trying to undo all changes.", this.getIdentifier());

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
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getIdentifier() +
                    ") can not be un-archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void delete() throws Exception {
        if (this.isReady() || this.isInitial() || this.isArchived()) {

            try {
                // Delete all data object instances
                deleteDataObjectInstances();

                // Delete all data elements
                deleteDataElements();

                // Trigger the delete event for the whole data object since all data elements are deleted
                // successfully. This will also trigger the deletion of the corresponding object at the data source
                // through the PersistableHashMap in the corresponding IDataManager instance.
                this.lifeCycle.triggerEvent(this, ModelEvents.delete);

                // Cleanup variables
                this.identifier = null;
                this.entity = null;
                this.name = null;
                this.lifeCycle = null;
                this.dataElements = null;
            } catch (TooBusyException e) {
                logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                        "after maximal " +
                        "amount of retries", this.getIdentifier(), ModelEvents.ready);
                throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
            } catch (Exception e) {
                logger.error("Deletion data object '{}' not successful because deletion of one of its data " +
                        "elements caused an exception. MANUAL INTERVENTION REQUIRED.", this.getIdentifier());

                // Trigger the initial event to disable the creation of new instances of the corrupted object
                try {
                    this.lifeCycle.triggerEvent(this, ModelEvents.initial);
                } catch (TooBusyException ex) {
                    logger.error("State transition for data object '{}' with event '{}' could not be enacted " +
                            "after maximal " +
                            "amount of retries", this.getIdentifier(), ModelEvents.ready);
                    throw new LifeCycleException("Deletion data object '" + this.getIdentifier() + "' not successful", ex);
                }

                throw new LifeCycleException("Deletion data object '" + this.getIdentifier() + "' not successful", e);
            }
        } else {
            logger.info("The data object ({}) can not be deleted because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getIdentifier() +
                    ") can not be deleted because it is in state '" + getState() + "'.");
        }
    }

    /**
     * Instantiates the data object and returns the created instance.
     *
     * @param createdBy             the entity that triggers the instantiation of the data object. For example, an instance
     *                              ID of a workflow or the name of human user can be used.
     * @param correlationProperties the properties used for identifying a specific instance of a data object
     * @return the created instance of the data object
     * @throws LifeCycleException If the data object is in a state which does not allow its instantiation.
     */
    public DataObjectInstance instantiate(String createdBy, HashMap<String, String>
            correlationProperties) throws LifeCycleException {
        DataObjectInstance result;

        if (this.isReady()) {
            result = new DataObjectInstance(this, createdBy, correlationProperties);

            // Add the new instance to the list of instances
            this.dataObjectInstances.add(result);

            // Persist the changed parent object
            this.storeToDS();
        } else {
            logger.info("The data object ({}) can not be instantiated because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data object (" + this.getIdentifier() +
                    ") can not be instantiated because it is in state '" + getState() + "'.");
        }

        return result;
    }

    /**
     * Removes the data object instance from this data object.
     *
     * @param instance to remove from this data object
     */
    public void removeDataObjectInstance(DataObjectInstance instance) {
        // Check if the object instance belongs to this data object
        if (instance.getDataObject() == this) {
            if (this.dataObjectInstances.remove(instance)) {
                // Persist the changed parent object
                this.storeToDS();
            }
        }
    }

    @JsonIgnore
    public boolean isInitial() {
        return getState() != null && this.getState().equals(ModelStates
                .INITIAL.name());
    }

    @JsonIgnore
    public boolean isReady() {
        return getState() != null && this.getState().equals(ModelStates
                .READY.name());
    }

    @JsonIgnore
    public boolean isArchived() {
        return getState() != null && this.getState().equals(ModelStates
                .ARCHIVED.name());
    }

    @JsonIgnore
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
                logger.error("Storing data object '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting data object '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataObjectLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObject.class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data object '{}'", this.getIdentifier());
            throw new IOException("Class not found during deserialization of data object.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DataObject) {
            DataObject s = (DataObject) object;
            return this.identifier.equals(s.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, name, entity);
    }
}
