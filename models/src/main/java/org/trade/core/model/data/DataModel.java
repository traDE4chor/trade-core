/*
 * Copyright 2017 Michael Hahn
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
import org.trade.core.model.ModelConstants;
import org.trade.core.model.compiler.CompilationException;
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.compiler.DataModelCompiler;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.lifecycle.DataModelLifeCycle;
import org.trade.core.model.lifecycle.DataObjectLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.local.LocalPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * Created by hahnml on 07.04.2017.
 */
@Entity("dataModels")
public class DataModel extends BaseResource implements Serializable, ILifeCycleModelObject {

    private static final long serialVersionUID = 2549294173554279537L;

    @Transient
    Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataModel");

    private String entity = null;

    private QName name = null;

    private transient DataModelLifeCycle lifeCycle = null;

    private transient LocalPersistenceProvider persistProv = null;

    @State
    private String state;

    @Reference
    private List<DataDependencyGraph> dataDependencyGraphs = new ArrayList<DataDependencyGraph>();

    @Reference
    private List<DataObject> dataObjects = new ArrayList<DataObject>();

    /**
     * Instantiates a new data model.
     *
     * @param entity the entity to which the data model belongs
     * @param name   the name of the data model
     */
    public DataModel(String entity, QName name) {
        this.name = name;
        this.entity = entity;

        this.lifeCycle = new DataModelLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider();
    }

    /**
     * This constructor is only used by Morphia to load data models from the database.
     */
    private DataModel() {
        this.lifeCycle = new DataModelLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider();
    }

    /**
     * Provides the name of the entity the data model belongs to.
     *
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Provides the qualified name of the data model.
     *
     * @return The qualified name of the data model.
     */
    public QName getName() {
        return this.name;
    }

    /**
     * Provides access to the current state of the data model.
     *
     * @return The current state of the data model.
     */
    public String getState() {
        return state;
    }

    /**
     * Provides the list of data objects associated to this data model.
     *
     * @return An unmodifiable list of data objects.
     */
    public List<DataObject> getDataObjects() {
        return this.dataObjects != null ? Collections.unmodifiableList(this.dataObjects) : null;
    }

    public DataObject getDataObject(String name) {
        Optional<DataObject> opt = this.dataObjects.stream().filter(s -> s.getName().equals(name)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    public DataObject getDataObjectById(String identifier) {
        Optional<DataObject> opt = this.dataObjects.stream().filter(s -> s.getIdentifier().equals(identifier)).findFirst();
        return opt.isPresent() ? opt.get() : null;
    }

    public byte[] getSerializedModel() throws Exception {
        byte[] result = this.persistProv.loadData(ModelConstants.DATA_MODEL_COLLECTION, getIdentifier());

        return result;
    }

    /**
     * Sets and persists the serialized data of this data model. At the moment we do not support updates on data
     * models, i.e., support the migration of data objects and data elements (and their related instances and
     * data values) between different data model versions.
     *
     * @param data the data
     * @throws Exception An exception thrown during execution of this method
     */
    public void setSerializedModel(byte[] data) throws Exception {
        // Check if the data model is created but not initialized (ready) already
        if (this.isInitial()) {
            try {
                // Persist the serialized model
                this.persistProv.storeData(data, ModelConstants.DATA_MODEL_COLLECTION, getIdentifier());
            } catch (Exception e) {
                logger.error("Setting the serialized model data for data model '" + this.getIdentifier() +
                        "' caused an exception.", e);

                throw e;
            }
        } else {
            logger.info("No serialized model data can be set for data model ({}) because it is in state '{}'.",
                    this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("No serialized model data can be set for data model (" + this.getIdentifier() +
                    ") because it is in state '" + getState() + "'.");
        }
    }

    public void associateWithDataDependencyGraph(DataDependencyGraph dataDependencyGraph) {
        if (dataDependencyGraph != null) {
            if (!dataDependencyGraphs.contains(dataDependencyGraph)) {
                dataDependencyGraphs.add(dataDependencyGraph);
            }
        }
    }

    public void removeAssociationWithDataDependencyGraph(DataDependencyGraph dataDependencyGraph) {
        if (dataDependencyGraph != null) {
            if (dataDependencyGraphs.contains(dataDependencyGraph)) {
                dataDependencyGraphs.remove(dataDependencyGraph);
            }
        }
    }

    /**
     * Provides the list of data dependency graphs using this data model.
     *
     * @return An unmodifiable list of data dependency graphs.
     */
    public List<DataDependencyGraph> getDataDependencyGraphs() {
        return this.dataDependencyGraphs != null ? Collections.unmodifiableList(this.dataDependencyGraphs) : null;
    }

    /**
     * Compile the serialized data model to generate and expose the specified data objects and data elements.
     *
     * @return A list of issues identified during the compilation of the data model
     * @throws Exception An exception thrown during the execution of this method
     */
    public List<CompilationIssue> compileDataModel(byte[] data) throws Exception {
        List<CompilationIssue> issues = Collections.emptyList();

        if (this.isInitial()) {
            // Deserialize and compile the provided data model, i.e., generate the specified data objects and data
            // elements
            DataModelCompiler comp = new DataModelCompiler();
            comp.compile(this.identifier, this.entity, data);

            // Add the resulting data objects to this data model
            this.dataObjects.addAll(comp.getCompiledDataObjects());

            // Get the list of compilation issues
            issues = comp.getCompilationIssues();

            // Trigger the ready event for the data model after everything was compiled successfully
            try {
                this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.ready);
            } catch (TooBusyException e) {
                logger.error("State transition for data model '{}' with event '{}' could not be enacted " +
                        "after maximal " +
                        "amount of retries", this.getIdentifier(), DataModelLifeCycle.Events.ready);
                throw new LifeCycleException("State transition could not be enacted after maximal amount " +
                        "of retries", e);
            }
        } else {
            logger.info("The data model ({}) can not be compiled because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data model (" + this.getIdentifier() +
                    ") can not be compiled because it is already in state '" + getState() + "'.");
        }

        return issues;
    }

    public void initialize(List<DataObject> dataObjects) throws LifeCycleException {
        // If the data model is in state INITIAL and contains only data objects which are all in state READY, we
        // trigger a state change to READY
        if (this.isInitial()) {
            boolean everythingReady = true;

            // Set the list of data objects
            this.dataObjects = dataObjects;

            Iterator<DataObject> iter = this.dataObjects.iterator();
            // Stop if one data object is not ready
            while (everythingReady && iter.hasNext()) {
                DataObject obj = iter.next();
                // If 'everythingReady' becomes false, it will never become true anymore. Therefore, we can stop the loop as
                // soon as it is false.
                everythingReady = everythingReady && obj.isReady();
            }

            if (everythingReady) {
                // Trigger the ready event for the data model after everything was initialized successfully
                try {
                    this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.ready);
                } catch (TooBusyException e) {
                    logger.error("State transition for data model '{}' with event '{}' could not be enacted " +
                            "after maximal " +
                            "amount of retries", this.getIdentifier(), DataModelLifeCycle.Events.ready);
                    throw new LifeCycleException("State transition could not be enacted after maximal amount " +
                            "of retries", e);
                }
            } else {
                logger.info("The data model ({}) can not be initialized because one or more of its data objects are " +
                                "not in state 'ready'.", this.getIdentifier(), getState());

                throw new LifeCycleException("The data model (" + this.getIdentifier() +
                        ") because one or more of its data objects are not in state 'ready'.");
            }
        } else {
            logger.info("The data model ({}) can not be initialized because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data model (" + this.getIdentifier() +
                    ") can not be initialized because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void archive() throws Exception {
        if (this.isReady()) {
            // Remember changed objects for undoing changes in case of an exception
            List<DataObject> changedObjects = new ArrayList<DataObject>();

            try {
                for (DataObject object : this.dataObjects) {
                    object.archive();
                    changedObjects.add(object);
                }

                // Trigger the archive event for the whole data model since all data objects are archived
                // successfully.
                this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.archive);
            } catch (Exception e) {
                logger.warn("Archiving data model '{}' not successful because archiving of one of its data objects " +
                        "caused an exception. Trying to undo all changes.", this.getIdentifier());

                try {
                    // Un-archive the already archived data objects again
                    for (DataObject obj : changedObjects) {
                        obj.unarchive();
                    }
                } catch (Exception ex) {
                    logger.error("Rollback of changes triggered by archiving data model '{}' was not successful. " +
                            "MANUAL INTERVENTION REQUIRED.", ex);
                    throw new LifeCycleException("Archiving data model '" + this
                            .getIdentifier() + "' not successful. MANUAL INTERVENTION REQUIRED.", ex);
                }
            }
        } else {
            logger.info("The data model ({}) can not be archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data model (" + this.getIdentifier() +
                    ") can not be archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void unarchive() throws Exception {
        if (this.isArchived()) {
            // Remember changed objects for undoing changes in case of an exception
            List<DataObject> changedElements = new ArrayList<DataObject>();

            try {
                for (DataObject object : this.dataObjects) {
                    object.unarchive();
                    changedElements.add(object);
                }

                // Trigger the unarchive event for the whole data model since all data objects are unarchived
                // successfully.
                this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.unarchive);
            } catch (Exception e) {
                logger.warn("Un-archiving data model '{}' not successful because un-archiving of one of its data " +
                        "objects caused an exception. Trying to undo all changes.", this.getIdentifier());

                try {
                    // Archive the already un-archived data objects again
                    for (DataObject elm : changedElements) {
                        elm.archive();
                    }
                } catch (Exception ex) {
                    logger.error("Rollback of changes triggered by un-archiving data model '{}' was not successful. " +
                            "MANUAL INTERVENTION REQUIRED.", ex);

                    throw new LifeCycleException("Rollback of changes triggered by un-archiving data model (" + this.getIdentifier() +
                            ") was not successful. MANUAL INTERVENTION REQUIRED.", ex);
                }
            }
        } else {
            logger.info("The data model ({}) can not be un-archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data model (" + this.getIdentifier() +
                    ") can not be un-archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void delete() throws Exception {
        if (this.isReady() || this.isInitial() || this.isArchived()) {

            // Check if the data model is used by any data dependency graph, if not we can delete it
            if (this.dataDependencyGraphs.isEmpty()) {
                try {
                    // Delete the associated data
                    this.persistProv.removeData(ModelConstants.DATA_MODEL_COLLECTION, getIdentifier());

                    // Delete all data objects
                    deleteDataObjects();

                    // Trigger the delete event for the whole data model since all data objects are deleted
                    // successfully.
                    this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.delete);
                } catch (TooBusyException e) {
                    logger.error("State transition for data model '{}' with event '{}' could not be enacted " +
                            "after maximal " +
                            "amount of retries", this.getIdentifier(), DataObjectLifeCycle.Events.ready);
                    throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
                } catch (Exception e) {
                    logger.error("Deletion data model '{}' not successful because deletion of one of its data " +
                            "objects caused an exception. MANUAL INTERVENTION REQUIRED.", this.getIdentifier());

                    // Trigger the initial event to disable the creation of new instances of the corrupted model
                    try {
                        this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.initial);
                    } catch (TooBusyException ex) {
                        logger.error("State transition for data model '{}' with event '{}' could not be enacted " +
                                "after maximal " +
                                "amount of retries", this.getIdentifier(), DataObjectLifeCycle.Events.ready);
                        throw new LifeCycleException("Deletion data model '" + this.getIdentifier() + "' not successful", ex);
                    }

                    throw new LifeCycleException("Deletion data model '" + this.getIdentifier() + "' not successful", e);
                }
            } else {
                // If the data value is used by any data element instance, we deny its deletion
                logger.warn("Someone tried to delete data model ({}) which is used by '{}' data dependency graphs." +
                                " " +
                                "Therefore, the deletion attempt is rejected by the system.",
                        this
                                .getIdentifier(),
                        this.dataDependencyGraphs.size());

                throw new LifeCycleException("Someone tried to delete data model (" + this.getIdentifier() +
                        ") which is used by " + this.dataDependencyGraphs.size() + " data dependency graphs. " +
                        "Therefore, the deletion attempt is rejected by the system.");
            }

            // Cleanup variables
            this.identifier = null;
            this.entity = null;
            this.name = null;
            this.lifeCycle = null;
            this.dataObjects = null;
        } else {
            logger.info("The data model ({}) can not be deleted because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data model (" + this.getIdentifier() +
                    ") can not be deleted because it is in state '" + getState() + "'.");
        }
    }

    private void deleteDataObjects() throws Exception {
        if (this.isInitial() || this.isReady()) {
            // Loop over all objects
            for (Iterator<DataObject> iter = this.dataObjects.iterator(); iter.hasNext(); ) {
                DataObject object = iter.next();

                // Remove the object from the list
                iter.remove();

                // Trigger the deletion of the object
                object.delete();

                // Check if the deleted data object was the only child of the data model
                if (this.dataObjects.isEmpty()) {
                    // Change the state back to initial to disallow its instantiation
                    try {
                        this.lifeCycle.triggerEvent(this, DataModelLifeCycle.Events.initial);
                    } catch (TooBusyException e) {
                        logger.error("State transition for data model '{}' with event '{}' could not be enacted " +
                                "after maximal " +
                                "amount of retries", this.getIdentifier(), DataObjectLifeCycle.Events.ready);
                        throw new LifeCycleException("State transition could not be enacted after maximal amount of retries", e);
                    }
                }
            }
        } else {
            logger.info("No data object can be deleted because the data model ({}) is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("No data object can be deleted because the data model (" + this.getIdentifier() +
                    ") is in state '" + getState() + "'.");
        }
    }

    public boolean isInitial() {
        return getState() != null && this.getState().equals(DataModelLifeCycle.States
                .INITIAL.name());
    }

    public boolean isReady() {
        return getState() != null && this.getState().equals(DataModelLifeCycle.States
                .READY.name());
    }

    public boolean isArchived() {
        return getState() != null && this.getState().equals(DataModelLifeCycle.States
                .ARCHIVED.name());
    }

    public boolean isDeleted() {
        return getState() != null && this.getState().equals(DataModelLifeCycle.States
                .DELETED.name());
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataModelLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider();
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data model '{}'", this.getIdentifier());
            throw new IOException("Class not found during deserialization of data model.");
        }
    }

}