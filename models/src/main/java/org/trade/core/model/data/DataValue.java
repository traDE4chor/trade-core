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
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.ModelConstants;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.lifecycle.DataValueLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;
import org.trade.core.utils.events.InstanceEvents;
import org.trade.core.utils.states.InstanceStates;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class represents a data value within the middleware.
 * <p>
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataValues")
public class DataValue extends ABaseResource implements ILifeCycleInstanceObject {

    private static final long serialVersionUID = -1774719861199414867L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataValue");

    private String name = null;

    private Date timestamp = new Date();

    private String owner = "";

    private transient DataValueLifeCycle lifeCycle = null;

    private transient IPersistenceProvider<DataValue> persistProv = null;

    private boolean hasData = false;

    @State
    private String state;

    private String type = null;

    private String contentType = null;

    private Date lastModified = timestamp;

    private long size = 0L;

    @Reference
    private List<DataElementInstance> dataElementInstances = new ArrayList<DataElementInstance>();

    /**
     * Instantiates a new data value with the given name and owner.
     *
     * @param owner the owner of the data value
     * @param name  the name of the data value
     */
    public DataValue(String owner, String name) {
        this.owner = owner;
        this.name = name;

        this.lifeCycle = new DataValueLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataValue.class);
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DataValue() {
        this.lifeCycle = new DataValueLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataValue.class);
    }

    /**
     * Provides the name of the data value.
     *
     * @return The name of the data value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Allows to set the name of the data value.
     *
     * @param name the name of the data value
     */
    public void setName(String name) {
        this.name = name;
        this.lastModified = new Date();
    }

    public Date getCreationTimestamp() {
        return timestamp;
    }

    public String getOwner() {
        return owner;
    }

    /**
     * Provides access to the current state of the data value through its life cycle object.
     *
     * @return The current state of the data value.
     */
    public String getState() {
        return state;
    }

    public Date getLastModified() {
        return lastModified;
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
        this.lastModified = new Date();
    }

    /**
     * Gets content type of the data element in form of a MIME type. For example, "text/plain; charset=utf-8",
     * "image/jpeg" or "video/mpeg".
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
        this.lastModified = new Date();
    }

    /**
     * Return the size of the attached data.
     *
     * @return The size of the data attached to the data value object.
     */
    public long getSize() {
        return this.size;
    }

    public boolean hasData() {
        return hasData;
    }

    public byte[] getData() throws Exception {
        return this.persistProv.loadBinaryData(ModelConstants.DATA_VALUE__DATA_COLLECTION, getIdentifier());
    }

    public void setData(byte[] data, long size) throws Exception {
        // Check if the data value is created or already initialized and therefore ready to store data
        if (this.isCreated() || this.isInitialized()) {
            this.size = size;

            try {
                this.persistProv.storeBinaryData(data, ModelConstants.DATA_VALUE__DATA_COLLECTION, getIdentifier());

                // Remember if data is set or not (setting a NULL value deletes the data by convention)
                hasData = data != null;

                this.lastModified = new Date();

                this.initialize();
            } catch (Exception e) {
                logger.error("Setting data on data value '" + this.getIdentifier() + "' caused an exception.", e);

                throw e;
            }
        } else {
            logger.info("No data can be set for the data value ({}) because it is in state '{}'.",
                    this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("No data can be set for the data value (" + this.getIdentifier() +
                    ") because it is in state '" + getState() + "'.");
        }
    }

    public void associateWithDataElementInstance(DataElementInstance dataElementInstance) {
        if (dataElementInstance != null) {
            if (!dataElementInstances.contains(dataElementInstance)) {
                dataElementInstances.add(dataElementInstance);
            }
        }
    }

    public void removeAssociationWithDataElementInstance(DataElementInstance dataElementInstance) {
        if (dataElementInstance != null) {
            if (dataElementInstances.contains(dataElementInstance)) {
                dataElementInstances.remove(dataElementInstance);
            }
        }
    }

    /**
     * Provides the list of data element instances using this data value.
     *
     * @return An unmodifiable list of data element instances.
     */
    public List<DataElementInstance> getDataElementInstances() {
        return this.dataElementInstances != null ? Collections.unmodifiableList(this.dataElementInstances) : null;
    }

    /**
     * Gets all data element instances using this data value created by the specified entity.
     *
     * @param createdBy the entity for which data element instances using this data value should be
     *                  returned
     * @return An unmodifiable list of matching data element instances.
     */
    public List<DataElementInstance> getDataElementInstances(String createdBy) {
        List<DataElementInstance> result = this.dataElementInstances.stream().filter(s -> s.getCreatedBy().equals(createdBy)).collect
                (Collectors.toList());
        return Collections.unmodifiableList(result);
    }

    /**
     * Gets a data element instance (using this data value) by id.
     *
     * @param identifier the identifier
     * @return the data element instance by id
     */
    public DataElementInstance getDataElementInstanceById(String identifier) {
        Optional<DataElementInstance> opt = this.dataElementInstances.stream().filter(s -> s.getIdentifier().equals(identifier)).findFirst();
        return opt.orElse(null);
    }

    @Override
    public void initialize() throws Exception {
        boolean hasChanged = false;

        if (this.isCreated()) {
            // If this is the first time data is set, we trigger the corresponding state change that the data
            // value is now initialized

            // Trigger the initialized event for the data value
            this.lifeCycle.triggerEvent(this, InstanceEvents.initialize);
            hasChanged = true;
        } else if (this.isInitialized() && !hasData()) {
            // If the data value was already initialized (i.e., has associated data) we have to change the state
            // back to created when the associated data is deleted (data==null or hasData()==false)

            // Trigger the created event for the data value
            this.lifeCycle.triggerEvent(this, InstanceEvents.create);
            hasChanged = true;
        }

        // If the state of this data value has changed, we have to inform all related data element instance which are
        // using this data value
        if (hasChanged) {
            for (DataElementInstance elmInstance : this.getDataElementInstances()) {
                elmInstance.initialize();
            }
        }
    }

    @Override
    public void archive() throws Exception {
        // TODO: Add logic for archiving data values. Only archive a data value if it is not used by any existing (non-archived) data element instance!

        this.lifeCycle.triggerEvent(this, InstanceEvents.archive);
    }

    @Override
    public void unarchive() throws Exception {
        // TODO: Add logic for unarchiving data values.

        this.lifeCycle.triggerEvent(this, InstanceEvents.unarchive);
    }

    @Override
    public void delete() throws Exception {
        // Check if the data value is used by any data element, if not delete it
        if (this.dataElementInstances.isEmpty()) {
            // Delete the associated data and destroy the persistence provider
            this.persistProv.deleteBinaryData(ModelConstants.DATA_VALUE__DATA_COLLECTION, getIdentifier());
            this.persistProv.destroyProvider();

            // Trigger the delete event for the data value
            this.lifeCycle.triggerEvent(this, InstanceEvents.delete);
        } else {
            // If the data value is used by any data element instance, we deny its deletion
            logger.warn("Someone tried to delete data value ({}) which is used by '{}' data element instances. " +
                            "Therefore, the deletion attempt is rejected by the system.",
                    this
                            .getIdentifier(),
                    this.dataElementInstances.size());

            throw new LifeCycleException("Someone tried to delete data value (" + this.getIdentifier() +
                    ") which is used by " + this.dataElementInstances.size() + " data element instances. " +
                    "Therefore, the deletion attempt is rejected by the system.");
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

            lifeCycle = new DataValueLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataValue.class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data value '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data value.");
        }
    }

    // Special implementation for this class since the getData() method should not invoked
    public String toString() {
        StringBuilder sb = new StringBuilder(resourceName(this) + ":");

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && !method.getName().equals("getData")
                    && method.getParameterTypes().length == 0) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(this);
                    if (value == null) {
                        continue;
                    }
                    sb.append("\n\t").append(field).append(" = ")
                            .append(value.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
