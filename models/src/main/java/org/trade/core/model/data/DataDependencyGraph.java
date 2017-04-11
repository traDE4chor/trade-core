/* Copyright 2017 Michael Hahn
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
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.compiler.DDGCompiler;
import org.trade.core.model.lifecycle.DataDependencyGraphLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.local.LocalPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Created by hahnml on 10.04.2017.
 */
@Entity("dataDependencyGraphs")
public class DataDependencyGraph extends BaseResource implements Serializable, ILifeCycleModelObject {

    private static final long serialVersionUID = 2549294173554279537L;

    @Transient
    Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataDependencyGraph");

    private String entity = null;

    private String name = null;

    private String targetNamespace = null;

    private transient DataDependencyGraphLifeCycle lifeCycle = null;

    private transient LocalPersistenceProvider persistProv = null;

    @State
    private String state;

    @Reference
    private DataModel dataModel = null;

    /**
     * Instantiates a new data dependency graph.
     *
     * @param entity the entity to which the data dependency graph belongs
     * @param name   the name of the data dependency graph
     */
    public DataDependencyGraph(String entity, String name) {
        this.name = name;
        this.entity = entity;

        this.lifeCycle = new DataDependencyGraphLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider();
    }

    /**
     * This constructor is only used by Morphia to load data dependency graphs from the database.
     */
    private DataDependencyGraph() {
        this.lifeCycle = new DataDependencyGraphLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider();
    }

    /**
     * Provides the name of the entity the data dependency graph belongs to.
     *
     * @return the entity
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Provides the qualified name of the data dependency graph.
     *
     * @return The qualified name of the data dependency graph.
     */
    public QName getQName() {
        return new QName(this.targetNamespace, name);
    }

    /**
     * Provides the name of the data dependency graph.
     *
     * @return The name of the data dependency graph.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets target namespace.
     *
     * @return the target namespace
     */
    public String getTargetNamespace() {
        return targetNamespace;
    }

    /**
     * Provides access to the current state of the data dependency graph.
     *
     * @return The current state of the data dependency graph.
     */
    public String getState() {
        return state;
    }

    /**
     * Provides the data model associated to this data dependency graph.
     *
     * @return The data model
     */
    public DataModel getDataModel() {
        return this.dataModel;
    }

    /**
     * Sets data model.
     *
     * @param dataModel the data model
     * @throws LifeCycleException the life cycle exception
     */
    public void setDataModel(DataModel dataModel) throws LifeCycleException {
        if (dataModel != null) {
            if (dataModel.isReady()) {
                // Remember the data model
                this.dataModel = dataModel;
                // Associate the data dependency graph with the data model
                this.dataModel.associateWithDataDependencyGraph(this);
            } else {
                logger.info("The data model ({}) can not be used by data dependency graph ({}) because it is in state" +
                        " " +
                        "'{}'.", dataModel.getIdentifier(), this.getIdentifier(), dataModel.getState());

                throw new LifeCycleException("The data model (" + dataModel.getIdentifier() +
                        ") can not be used by data dependency graph (" + this.getIdentifier() + ") because it is in state" +
                        " '" + dataModel.getState() + "'.");
            }
        } else {
            // If a data model was set before, setting a null value means removing the reference between this data
            // dependency graph and the data model
            if (this.dataModel != null) {
                this.dataModel.removeAssociationWithDataDependencyGraph(this);
                this.dataModel = null;
            }
        }
    }

    public byte[] getSerializedModel() throws Exception {
        byte[] result = this.persistProv.loadData(ModelConstants.DATA_DEPENDENCY_GRAPH_COLLECTION, getIdentifier());

        return result;
    }

    /**
     * Sets and persists the serialized data of this data dependency graph. At the moment we do not support updates on data
     * dependency graphs, i.e., support the migration of data models, data objects and data elements (and their related
     * instances and data values) between different data dependency graph versions.
     *
     * @param data the data
     * @throws Exception An exception thrown during execution of this method
     */
    public void setSerializedModel(byte[] data) throws Exception {
        // Check if the data dependency graph is created but not initialized (ready) already
        if (this.isInitial()) {
            try {
                // Persist the serialized graph
                this.persistProv.storeData(data, ModelConstants.DATA_DEPENDENCY_GRAPH_COLLECTION, getIdentifier());
            } catch (Exception e) {
                logger.error("Setting the serialized model data for data dependency graph '" + this.getIdentifier() +
                        "' caused an exception.", e);

                throw e;
            }
        } else {
            logger.info("No serialized model data can be set for data dependency graph ({}) because it is in state '{}'.",
                    this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("No serialized model data can be set for data dependency graph (" + this.getIdentifier() +
                    ") because it is in state '" + getState() + "'.");
        }
    }

    /**
     * Compile the serialized data dependency graph to generate and expose the specified data model with its data
     * objects and data elements.
     *
     * @return A list of issues identified during the compilation of the data dependency graph
     * @throws Exception An exception thrown during the execution of this method
     */
    public List<CompilationIssue> compileDataDependencyGraph(byte[] data) throws Exception {
        List<CompilationIssue> issues = Collections.emptyList();

        if (this.isInitial()) {
            // Deserialize and compile the provided data dependency graph, i.e., generate the specified data objects and data
            // elements
            DDGCompiler comp = new DDGCompiler();
            comp.compile(this.identifier, this.entity, data);

            // Set the resulting data model to this DDG
            setDataModel(comp.getCompiledDataModel());

            this.targetNamespace = comp.getTargetNamespace();

            // Get the list of compilation issues
            issues = comp.getCompilationIssues();

            // Trigger the ready event for the data dependency graph after everything was compiled successfully
            try {
                this.lifeCycle.triggerEvent(this, DataDependencyGraphLifeCycle.Events.ready);
            } catch (TooBusyException e) {
                logger.error("State transition for data dependency graph '{}' with event '{}' could not be enacted " +
                        "after maximal " +
                        "amount of retries", this.getIdentifier(), DataDependencyGraphLifeCycle.Events.ready);
                throw new LifeCycleException("State transition could not be enacted after maximal amount " +
                        "of retries", e);
            }
        } else {
            logger.info("The data dependency graph ({}) can not be compiled because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data dependency graph (" + this.getIdentifier() +
                    ") can not be compiled because it is already in state '" + getState() + "'.");
        }

        return issues;
    }

    @Override
    public void archive() throws Exception {
        if (this.isReady()) {
            // TODO: 10.04.2017 Implement 
        } else {
            logger.info("The data dependency graph ({}) can not be archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data dependency graph (" + this.getIdentifier() +
                    ") can not be archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void unarchive() throws Exception {
        if (this.isArchived()) {
            // TODO: 10.04.2017 Implement 
        } else {
            logger.info("The data dependency graph ({}) can not be un-archived because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data dependency graph (" + this.getIdentifier() +
                    ") can not be un-archived because it is in state '" + getState() + "'.");
        }
    }

    @Override
    public void delete() throws Exception {
        if (this.isReady() || this.isInitial() || this.isArchived()) {

            // TODO: 10.04.2017 Implement deletion of data model?

            // At least delete the associated data
            this.persistProv.removeData(ModelConstants.DATA_DEPENDENCY_GRAPH_COLLECTION, getIdentifier());

            // Cleanup variables
            this.identifier = null;
            this.entity = null;
            this.name = null;
            this.lifeCycle = null;
            this.dataModel = null;
        } else {
            logger.info("The data dependency graph ({}) can not be deleted because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data dependency graph (" + this.getIdentifier() +
                    ") can not be deleted because it is in state '" + getState() + "'.");
        }
    }

    public boolean isInitial() {
        return getState() != null && this.getState().equals(DataDependencyGraphLifeCycle.States
                .INITIAL.name());
    }

    public boolean isReady() {
        return getState() != null && this.getState().equals(DataDependencyGraphLifeCycle.States
                .READY.name());
    }

    public boolean isArchived() {
        return getState() != null && this.getState().equals(DataDependencyGraphLifeCycle.States
                .ARCHIVED.name());
    }

    public boolean isDeleted() {
        return getState() != null && this.getState().equals(DataDependencyGraphLifeCycle.States
                .DELETED.name());
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataDependencyGraphLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider();
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data dependency graph '{}'", this.getIdentifier());
            throw new IOException("Class not found during deserialization of data dependency graph.");
        }
    }

}