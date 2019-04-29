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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.data.ILifeCycleInstanceObject;
import org.trade.core.model.lifecycle.DataElementInstanceLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;
import org.trade.core.utils.events.InstanceEvents;
import org.trade.core.utils.states.InstanceStates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

/**
 * This class represents an instance of a data element within the middleware.
 * <p>
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataElementInstances")
public class DataElementInstance extends ABaseResource implements ILifeCycleInstanceObject {

    private static final long serialVersionUID = -7695419620536264095L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.instance.DataObjectInstance");

    private transient DataElementInstanceLifeCycle lifeCycle;

    private transient IPersistenceProvider<DataElementInstance> persistProv;

    private Date creationTimestamp;

    private String createdBy;

    @State
    private String state;

    @JsonBackReference(value = "dataElement")
    @Reference
    private DataElement dataElement;

    @JsonBackReference(value = "dataObjectInstance")
    @Reference
    private DataObjectInstance dataObjectInstance;

    @Reference
    private List<DataValue> dataValues;

    private HashMap<String, String> correlationProperties;

    public DataElementInstance(DataElement dataElement, DataObjectInstance dataObjectInstance, String createdBy, HashMap<String, String>
            correlationProperties) {
        this.dataElement = dataElement;
        this.dataObjectInstance = dataObjectInstance;
        this.createdBy = createdBy;

        this.creationTimestamp = new Date();

        if (correlationProperties != null) {
            this.correlationProperties = correlationProperties;
        } else {
            this.correlationProperties = new HashMap<>();
        }

        this.dataValues = new ArrayList<>();
        this.lifeCycle = new DataElementInstanceLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElementInstance.class);
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DataElementInstance() {
        this.dataValues = new ArrayList<>();
        this.lifeCycle = new DataElementInstanceLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElementInstance.class);
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

    public List<DataValue> getDataValues() {
        return dataValues;
    }

    public DataValue getDataValue(int index) {
        return dataValues.get(index);
    }

    public DataElement getDataElement() {
        return this.dataElement;
    }

    public DataObjectInstance getDataObjectInstance() {
        return dataObjectInstance;
    }

    public HashMap<String, String> getCorrelationProperties() {
        return correlationProperties;
    }

    @JsonIgnore
    public int getNumberOfDataValues() {
        return this.dataValues.size();
    }

    public void addDataValue(DataValue dataValue) throws Exception {
        if (dataValue != null) {
            if (dataValue.isCreated() || dataValue.isInitialized()) {

                // TODO: 10.01.2018 Add a type and contentType compatibility check!
                // Only data values having the same type and contentType as the data element the instance is
                // created for should be allowed to be associated.

                // Check if the data element is a simple or collection element and if it's a simple element if
                // already a data value was set before
                if (this.dataElement.getIsCollectionElement() && !this.dataValues.contains(dataValue)) {
                    // Add the new data value
                    this.dataValues.add(dataValue);
                } else {
                    if (!this.dataValues.isEmpty() && this.dataValues.get(0) != dataValue) {
                        // Remove the association between this data element instance and the data value
                        this.dataValues.get(0).removeAssociationWithDataElementInstance(this);
                        this.dataValues.clear();
                    }

                    // Set the new data value
                    this.dataValues.add(dataValue);
                }

                // Associate the data element instance with the new data value
                dataValue.associateWithDataElementInstance(this);

                this.initialize();
            } else {
                logger.info("The data value ({}) can not be used by data element instance ({}) because it is in state " +
                        "'{}'.", dataValue.getIdentifier(), this.getIdentifier(), dataValue.getState());

                throw new LifeCycleException("The data value (" + dataValue.getIdentifier() +
                        ") can not be used by data element instance (" + this.getIdentifier() + ") because it is in state" +
                        " '" + dataValue.getState() + "'.");
            }
        }
    }

    public void removeDataValue(DataValue dataValue) throws Exception {
        if (dataValue != null) {
            if (dataValue.getDataElementInstances().contains(this)) {
                // Check if this data value was associated to this data element instance

                // Remove the association between this data element instance and the data value
                dataValue.removeAssociationWithDataElementInstance(this);

                // Remove the data value from the list
                this.dataValues.remove(dataValue);

                this.initialize();
            } else {
                logger.info("The data value ({}) is not associated to the data element instance ({}) and therefore " +
                        "cannot be removed.", dataValue.getIdentifier(), this.getIdentifier());

                throw new LifeCycleException("The data value (" + dataValue.getIdentifier() +
                        ") is not associated to the data element instance (" + this.getIdentifier() + ") and therefore " +
                        "cannot be removed.");
            }
        }
    }

    public void removeAllDataValues() throws Exception {
        for (DataValue dataValue : this.dataValues) {
            dataValue.removeAssociationWithDataElementInstance(this);
        }

        this.dataValues.clear();
    }

    @Override
    public void initialize() throws Exception {
        if (this.isCreated() || this.isInitialized()) {

            boolean hasChanged = false;
            // Check if an initialized data value is present
            if (!this.dataValues.isEmpty() && areDataValuesInitialized()) {
                // Trigger state change if the data element instance is not initialized already
                if (isCreated()) {
                    // Trigger the initialized event for the data element instance since it has now an associated data value
                    this.lifeCycle.triggerEvent(this, InstanceEvents.initialize);
                    hasChanged = true;
                }
            } else {
                if (isInitialized()) {
                    // Trigger the create event for the data element instance since it does not have any associated data
                    // value and is therefore not initialized anymore
                    this.lifeCycle.triggerEvent(this, InstanceEvents.create);
                    hasChanged = true;
                }
            }

            // If the state has changed propagate it to the parent data object instance
            if (hasChanged) {
                // Trigger the initialize method of the parent data object instance to check if the whole data object
                // instance is initialized or not
                this.getDataObjectInstance().initialize();

                // Persist the changes at the data source
                this.storeToDS();
            }
        } else {
            logger.info("The data element instance ({}) can not be initialized because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data element instance (" + this.getIdentifier() +
                    ") can not be initialized because it is in state '" + getState() + "'.");
        }
    }

    private boolean areDataValuesInitialized() {
        return this.dataValues.stream().allMatch(dataValue -> dataValue.isInitialized());
    }

    @Override
    public void archive() throws Exception {
        // TODO: 24.04.2017 Implement archiving of data element instances

        // Persist the changes at the data source
        this.storeToDS();
    }

    @Override
    public void unarchive() throws Exception {
        // TODO: 24.04.2017 Implement un-archiving of data element instances

        // Persist the changes at the data source
        this.storeToDS();
    }

    @Override
    public void delete() throws Exception {
        // Remove the data element instance from the data element
        getDataElement().removeDataElementInstance(this);

        // Remove the data element instance from the data object instance
        dataObjectInstance.removeDataElementInstance(this);

        // If one or more data value were set before, remove the reference to this data element instance
        if (!this.dataValues.isEmpty()) {
            removeAllDataValues();
        }
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
                logger.error("Storing data element instance '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting data element instance '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataElementInstanceLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataElementInstance
                    .class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data element instance '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data element instance.");
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DataElementInstance) {
            DataElementInstance s = (DataElementInstance) object;
            return this.identifier.equals(s.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = 0;
        if (this.dataElement != null) {
            if (this.dataElement.getParent() != null && this.dataElement.getParent().getDataModel() != null) {
                // Create a query optimized hash which can be used to later identify the matching instance faster
                hashCode = Objects.hash(dataElement.getParent().getDataModel().getTargetNamespace(), dataElement.getParent()
                                .getDataModel().getName(), dataElement.getParent().getName(),
                        dataElement.getName(), correlationProperties);
            } else {
                hashCode = Objects.hash(identifier, creationTimestamp, createdBy, dataElement.getName(), correlationProperties);
            }
        } else {
            hashCode = Objects.hash(identifier, creationTimestamp, createdBy);
        }

        return hashCode;
    }
}
