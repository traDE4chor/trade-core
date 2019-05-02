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

package org.trade.core.model.data.instance;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.ILifeCycleInstanceObject;
import org.trade.core.model.lifecycle.DataObjectInstanceLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;
import org.trade.core.utils.events.InstanceEvents;
import org.trade.core.utils.states.InstanceStates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * This class represents an instance of a data object within the middleware.
 * <p>
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataObjectInstances")
public class DataObjectInstance extends ABaseResource implements ILifeCycleInstanceObject {

    private static final long serialVersionUID = 4504379941592896623L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.instance.DataObjectInstance");

    @JsonProperty("creationTimestamp")
    private Date creationTimestamp;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("state")
    @State
    private String state;

    private transient DataObjectInstanceLifeCycle lifeCycle;

    private transient IPersistenceProvider<DataObjectInstance> persistProv;

    @JsonProperty("dataObject")
    @Reference
    private DataObject dataObject;

    @JsonProperty("dataElementInstances")
    @Reference
    private List<DataElementInstance> dataElementInstances;

    @JsonProperty("correlationProperties")
    private HashMap<String, String> correlationProperties;

    public DataObjectInstance(DataObject dataObject, String createdBy, HashMap<String, String>
            correlationProperties) {
        this.createdBy = createdBy;
        this.dataObject = dataObject;

        this.creationTimestamp = new Date();

        if (correlationProperties != null) {
            this.correlationProperties = correlationProperties;
        } else {
            this.correlationProperties = new HashMap<>();
        }

        dataElementInstances = new ArrayList<>();
        this.lifeCycle = new DataObjectInstanceLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObjectInstance
                .class);
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DataObjectInstance() {
        this.lifeCycle = new DataObjectInstanceLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObjectInstance
                .class);
    }

    public Date getCreationTimestamp() {
        return creationTimestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getState() {
        return state;
    }

    public DataObject getDataObject() {
        return this.dataObject;
    }

    public List<DataElementInstance> getDataElementInstances() {
        return dataElementInstances;
    }

    public HashMap<String, String> getCorrelationProperties() {
        return correlationProperties;
    }

    public void addDataElementInstance(DataElementInstance elementInstance) {
        // Check if the element instance belongs to this data object instance
        if (elementInstance.getDataObjectInstance() == this) {
            this.dataElementInstances.add(elementInstance);

            // Persist the changes at the data source
            this.storeToDS();
        }
    }

    public void removeDataElementInstance(DataElementInstance elementInstance) {
        // Check if the element instance belongs to this data object instance
        if (elementInstance.getDataObjectInstance() == this) {
            if (this.dataElementInstances.remove(elementInstance)) {
                // Persist the changes at the data source
                this.storeToDS();
            }
        }
    }

    @Override
    public void initialize() throws Exception {
        if (this.isCreated() || this.isInitialized()) {

            // Check if all related data element instance are initialized or not
            boolean areInitialized = true;
            Iterator<DataElementInstance> iter = this.dataElementInstances.iterator();
            while (areInitialized && iter.hasNext()) {
                if (!iter.next().isInitialized()) {
                    // Found one element instance which is not initialized.
                    // Stop iteration and trigger state changes, if required.
                    areInitialized = false;
                }
            }

            if (areInitialized) {
                // Trigger the initialized event for the data object instance since now all related data element
                // instance are initialized successfully
                this.lifeCycle.triggerEvent(this, InstanceEvents.initialize);
            } else {
                if (this.isInitialized()) {
                    // Trigger the create event for the data object instance since at least one of its related data
                    // element instances is not initialized (anymore)
                    this.lifeCycle.triggerEvent(this, InstanceEvents.create);
                }
            }

            // Persist the changes at the data source
            this.storeToDS();
        } else {
            logger.info("The data object instance ({}) can not be initialized because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data object instance (" + this.getIdentifier() +
                    ") can not be initialized because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void archive() throws Exception {
        // TODO: 24.04.2017 Implement archiving of data object instances

        // Trigger the archive event for the data object instance
        this.lifeCycle.triggerEvent(this, InstanceEvents.archive);

        // Persist the changes at the data source
        this.storeToDS();
    }

    @Override
    public void unarchive() throws Exception {
        // TODO: 24.04.2017 Implement un-archiving of data object instances

        // Trigger the unarchive event for the data object instance
        this.lifeCycle.triggerEvent(this, InstanceEvents.unarchive);

        // Persist the changes at the data source
        this.storeToDS();
    }

    @Override
    public void delete() throws Exception {
        // Remove the data object instance from the data object
        getDataObject().removeDataObjectInstance(this);

        // By convention we also directly delete all related data element instances of the data object instance
        for (Iterator<DataElementInstance> iter = this.dataElementInstances.iterator(); iter.hasNext(); ) {
            DataElementInstance instance = iter.next();

            // Remove the element instance from the list
            iter.remove();

            // Trigger the deletion of the element instance
            instance.delete();
        }

        // Trigger the delete event for the data object instance. This will also trigger the deletion of the
        // corresponding object at the data source through the PersistableHashMap in the corresponding IDataManager
        // instance.
        this.lifeCycle.triggerEvent(this, InstanceEvents.delete);
    }

    @JsonIgnore
    @Override
    public boolean isCreated() {
        return getState() != null && this.getState().equals(InstanceStates
                .CREATED.name());
    }

    @JsonIgnore
    @Override
    public boolean isInitialized() {
        return getState() != null && this.getState().equals(InstanceStates
                .INITIALIZED.name());
    }

    @JsonIgnore
    @Override
    public boolean isArchived() {
        return getState() != null && this.getState().equals(InstanceStates
                .ARCHIVED.name());
    }

    @JsonIgnore
    @Override
    public boolean isDeleted() {
        return getState() != null && this.getState().equals(InstanceStates
                .DELETED.name());
    }

    @Override
    public void storeToDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.storeObject(this);
            } catch (Exception e) {
                logger.error("Storing data object instance '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting data object instance '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataObjectInstanceLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataObjectInstance
                    .class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data object instance '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data object instance.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if(object instanceof DataObjectInstance) {
            DataObjectInstance s = (DataObjectInstance) object;
            return this.identifier.equals(s.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        if (this.dataObject != null) {
            if (this.dataObject.getDataModel() != null) {
                // Create a query optimized hash which can be used to later identify the matching instance faster
                hashCode = Objects.hash(dataObject.getDataModel().getTargetNamespace(), dataObject.getDataModel().getName(),
                        dataObject.getName(), correlationProperties);
            } else {
                hashCode = Objects.hash(identifier, creationTimestamp, createdBy, dataObject.getName(), correlationProperties);
            }
        } else {
            hashCode = Objects.hash(identifier, creationTimestamp, createdBy);
        }

        return hashCode;
    }
}
