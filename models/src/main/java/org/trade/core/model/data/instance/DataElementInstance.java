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

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.data.BaseResource;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.data.ILifeCycleInstanceObject;
import org.trade.core.model.lifecycle.DataElementInstanceLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.utils.InstanceEvents;
import org.trade.core.utils.InstanceStates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

/**
 * This class represents an instance of a data element within the middleware.
 * <p>
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataElementInstances")
public class DataElementInstance extends BaseResource implements Serializable, ILifeCycleInstanceObject {

    private static final long serialVersionUID = -7695419620536264095L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.instance.DataObjectInstance");

    private transient DataElementInstanceLifeCycle lifeCycle = null;

    private Date timestamp = new Date();

    private String createdBy = "";

    @State
    private String state;

    @Reference
    private DataElement model = null;

    @Reference
    private DataObjectInstance dataObjectInstance = null;

    @Reference
    private DataValue dataValue = null;

    private HashMap<String, String> correlationProperties = new HashMap<>();

    public DataElementInstance(DataElement dataElement, DataObjectInstance dataObjectInstance, String createdBy, HashMap<String, String>
            correlationProperties) {
        this.model = dataElement;
        this.dataObjectInstance = dataObjectInstance;
        this.createdBy = createdBy;
        this.correlationProperties = correlationProperties;

        this.lifeCycle = new DataElementInstanceLifeCycle(this);
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DataElementInstance() {
        this.lifeCycle = new DataElementInstanceLifeCycle(this, false);
    }

    public Date getCreationTimestamp() {
        return timestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getState() {
        return state;
    }

    public DataValue getDataValue() {
        return dataValue;
    }

    public DataElement getDataElement() {
        return this.model;
    }

    public DataObjectInstance getDataObjectInstance() {
        return dataObjectInstance;
    }

    public HashMap<String, String> getCorrelationProperties() {
        return correlationProperties;
    }

    public void setDataValue(DataValue dataValue) throws Exception {
        if (dataValue != null) {
            if (dataValue.isCreated() || dataValue.isInitialized()) {
                // Check if another data value was set before
                if (this.dataValue != null && this.dataValue != dataValue) {
                    // Remove the association between this data element instance and the data value
                    this.dataValue.removeAssociationWithDataElementInstance(this);
                }

                // Set the new data value
                this.dataValue = dataValue;

                // Associate the data element instance with the new data value
                this.dataValue.associateWithDataElementInstance(this);

                this.initialize();
            } else {
                logger.info("The data value ({}) can not be used by data element instance ({}) because it is in state " +
                        "'{}'.", dataValue.getIdentifier(), this.getIdentifier(), dataValue.getState());

                throw new LifeCycleException("The data value (" + dataValue.getIdentifier() +
                        ") can not be used by data element instance (" + this.getIdentifier() + ") because it is in state" +
                        " '" + dataValue.getState() + "'.");
            }
        } else {
            // If a data value was set before, setting a null value means removing the reference between this data
            // element instance and the data value
            if (this.dataValue != null) {
                this.dataValue.removeAssociationWithDataElementInstance(this);
                this.dataValue = null;
            }
        }
    }

    @Override
    public void initialize() throws Exception {
        if (this.isCreated() || this.isInitialized()) {

            boolean hasChanged = false;
            // Check if an initialized data value is present
            if (this.dataValue != null && this.dataValue.isInitialized()) {
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
            }
        } else {
            logger.info("The data element instance ({}) can not be initialized because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data element instance (" + this.getIdentifier() +
                    ") can not be initialized because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void archive() throws Exception {
        // TODO: 24.04.2017 Implement archiving of data element instances
    }

    @Override
    public void unarchive() throws Exception {
        // TODO: 24.04.2017 Implement un-archiving of data element instances
    }

    @Override
    public void delete() throws Exception {
        // Remove the data element instance from the data element
        getDataElement().removeDataElementInstance(this);

        // Remove the data element instance from the data object instance
        dataObjectInstance.removeDataElementInstance(this);

        // If a data value was set before, remove the reference to this data element instance
        if (this.dataValue != null) {
            this.dataValue.removeAssociationWithDataElementInstance(this);
            this.dataValue = null;
        }
    }

    @Override
    public boolean isCreated() {
        return getState() != null && this.getState().equals(InstanceStates
                .CREATED.name());
    }

    @Override
    public boolean isInitialized() {
        return getState() != null && this.getState().equals(InstanceStates
                .INITIALIZED.name());
    }

    @Override
    public boolean isArchived() {
        return getState() != null && this.getState().equals(InstanceStates
                .ARCHIVED.name());
    }

    @Override
    public boolean isDeleted() {
        return getState() != null && this.getState().equals(InstanceStates
                .DELETED.name());
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataElementInstanceLifeCycle(this, false);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data element instance '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data element instance.");
        }
    }
}
