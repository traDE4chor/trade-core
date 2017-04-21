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
import org.trade.core.model.data.BaseResource;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.ILifeCycleInstanceObject;
import org.trade.core.model.lifecycle.DataObjectInstanceLifeCycle;
import org.trade.core.utils.InstanceStates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataObjectInstances")
public class DataObjectInstance extends BaseResource implements Serializable, ILifeCycleInstanceObject {

    private static final long serialVersionUID = 4504379941592896623L;

    @Transient
    Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.instance.DataObjectInstance");

    private Date timestamp = new Date();

    private String createdBy = "";

    private String state = "";

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

    public String getIdentifier() {
        return identifier;
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
    public void create() throws Exception {

    }

    @Override
    public void archive() throws Exception {

    }

    @Override
    public void unarchive() throws Exception {

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
