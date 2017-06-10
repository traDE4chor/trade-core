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
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.ILifeCycleInstanceObject;
import org.trade.core.model.lifecycle.DataObjectInstanceLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
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

    private Date timestamp = new Date();

    private String createdBy = "";

    @State
    private String state;

    private transient DataObjectInstanceLifeCycle lifeCycle = null;

    @Reference
    private DataObject model = null;

    @Reference
    private List<DataElementInstance> dataElementInstances = new ArrayList<DataElementInstance>();

    private HashMap<String, String> correlationProperties = new HashMap<>();

    public DataObjectInstance(DataObject dataObject, String createdBy, HashMap<String, String>
            correlationProperties) {
        this.createdBy = createdBy;
        this.model = dataObject;
        this.correlationProperties = correlationProperties;

        this.lifeCycle = new DataObjectInstanceLifeCycle(this);
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DataObjectInstance() {
        this.lifeCycle = new DataObjectInstanceLifeCycle(this, false);
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

    public DataObject getDataObject() {
        return this.model;
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
        }
    }

    public void removeDataElementInstance(DataElementInstance elementInstance) {
        // Check if the element instance belongs to this data object instance
        if (elementInstance.getDataObjectInstance() == this) {
            this.dataElementInstances.remove(elementInstance);
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
                if (isInitialized()) {
                    // Trigger the create event for the data object instance since at least one of its related data
                    // element instances is not initialized (anymore)
                    this.lifeCycle.triggerEvent(this, InstanceEvents.create);
                }
            }
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
    }

    @Override
    public void unarchive() throws Exception {
        // TODO: 24.04.2017 Implement un-archiving of data object instances
    }

    @Override
    public void delete() throws Exception {
        // Remove the data object instance from the data object
        getDataObject().removeDataObjectInstance(this);
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

            lifeCycle = new DataObjectInstanceLifeCycle(this, false);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data object instance '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data object instance.");
        }
    }
}
