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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.persistence.annotations.State;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.ModelConstants;
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.compiler.DDGCompiler;
import org.trade.core.model.dataTransformation.DataTransformation;
import org.trade.core.model.lifecycle.DataDependencyGraphLifeCycle;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;
import org.trade.core.utils.events.ModelEvents;
import org.trade.core.utils.states.ModelStates;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This class represents a data dependency graph within the middleware.
 * <p>
 * Created by hahnml on 10.04.2017.
 */
@Entity("dataDependencyGraphs")
public class DataDependencyGraph extends ABaseResource implements ILifeCycleModelObject {

    private static final long serialVersionUID = 2549294173554279537L;

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataDependencyGraph");

    private String entity;

    private String name;

    private String targetNamespace;

    private transient DataDependencyGraphLifeCycle lifeCycle;

    private transient IPersistenceProvider<DataDependencyGraph> persistProv;

    @State
    private String state;

    @Reference
    private DataModel dataModel;

    @JsonManagedReference
    @JsonProperty("dataTransformations")
    @Reference
    private List<DataTransformation> dataTransformations;

    /**
     * Instantiates a new data dependency graph.
     *
     * @param entity the entity to which the data dependency graph belongs
     * @param name   the name of the data dependency graph
     */
    public DataDependencyGraph(String entity, String name) {
        this.name = name;
        this.entity = entity;

        this.dataTransformations = new ArrayList<>();
        this.lifeCycle = new DataDependencyGraphLifeCycle(this);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataDependencyGraph.class);
    }

    /**
     * This constructor is only used by Morphia to load data dependency graphs from the database.
     */
    private DataDependencyGraph() {
        this.lifeCycle = new DataDependencyGraphLifeCycle(this, false);
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataDependencyGraph.class);
        this.dataTransformations = new ArrayList<>();
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
    @JsonIgnore
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
     * Whether the data dependency graph has specified data transformations or not.
     *
     * @return True, if the data dependency graph specifies on or more data transformations. False, otherwise.
     */
    public boolean hasDataTransformations() {
        return !this.dataTransformations.isEmpty();
    }

    /**
     * Provides the list of data transformations specified by the data dependency graph.
     *
     * @return The list of data transformations
     */
    @JsonIgnore
    public List<DataTransformation> getDataTransformations() {
        return Collections.unmodifiableList(this.dataTransformations);
    }

    /**
     * Sets a list of data transformations to the data dependency graph, e.g., the data transformations resolved
     * during compilation of the graph.
     *
     * @param transformations the list of data transformations
     */
    @JsonIgnore
    public void setDataTransformations(List<DataTransformation> transformations) {
        for (DataTransformation transformation : transformations) {
            this.dataTransformations.add(transformation);

            // Persist the transformation at the data source
            transformation.storeToDS();
        }

        // Persist the changes at the data source
        this.storeToDS();
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

                // Persist the changes at the data source
                this.storeToDS();
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

                // Persist the changes at the data source
                this.storeToDS();
            }
        }
    }

    @JsonIgnore
    public byte[] getSerializedModel() throws Exception {
        return this.persistProv.loadBinaryData(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION,
                getIdentifier());
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
                this.persistProv.storeBinaryData(data, ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION,
                        getIdentifier());
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
     * @param data the serialized data dependency graph to compile
     * @return A list of issues identified during the compilation of the data dependency graph
     * @throws Exception An exception thrown during the execution of this method
     */
    public List<CompilationIssue> compileDataDependencyGraph(byte[] data) throws Exception {
        List<CompilationIssue> issues;

        if (this.isInitial()) {
            // Deserialize and compile the provided data dependency graph, i.e., generate the specified data objects and data
            // elements
            DDGCompiler comp = new DDGCompiler();
            comp.compile(this.identifier, this.entity, data);

            // Set the resulting data model to this DDG
            setDataModel(comp.getCompiledDataModel());

            this.targetNamespace = comp.getTargetNamespace();

            // Set the resulting data transformations to this DDG and vice versa
            List<DataTransformation> transformations = comp.getCompiledDataTransformations();
            for (DataTransformation transf : transformations) {
                transf.setDataDependencyGraph(this);
            }
            setDataTransformations(transformations);

            // Get the list of compilation issues
            issues = comp.getCompilationIssues();

            // Persist the changes at the data source
            this.storeToDS();

            // Trigger the ready event for the data dependency graph after everything was compiled successfully
            try {
                this.lifeCycle.triggerEvent(this, ModelEvents.ready);
            } catch (TooBusyException e) {
                logger.error("State transition for data dependency graph '{}' with event '{}' could not be enacted " +
                        "after maximal " +
                        "amount of retries", this.getIdentifier(), ModelEvents.ready);
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

            this.lifeCycle.triggerEvent(this, ModelEvents.archive);

            // Persist the changes at the data source
            this.storeToDS();
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

            this.lifeCycle.triggerEvent(this, ModelEvents.unarchive);

            // Persist the changes at the data source
            this.storeToDS();
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

            if (dataModel != null) {
                // Remove the association between the data model and the data dependency graph
                dataModel.removeAssociationWithDataDependencyGraph(this);

                // Delete the whole associated data model, if it was created through the compilation of the data
                // dependency graph, i.e., the data model does not have attached serialized model data.
                if (dataModel.getSerializedModel().length == 0) {
                    dataModel.delete();
                }
            }

            // Delete the associated data of the graph and destroy the persistence provider
            this.persistProv.deleteBinaryData(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION, getIdentifier());
            this.persistProv.destroyProvider();

            // Trigger the delete event for the whole data dependency graph since all related model objects are deleted
            // successfully.
            this.lifeCycle.triggerEvent(this, ModelEvents.delete);

            // Cleanup variables
            this.identifier = null;
            this.entity = null;
            this.name = null;
            this.lifeCycle = null;
            this.dataModel = null;

            this.dataTransformations.clear();
            this.dataTransformations = null;
        } else {
            logger.info("The data dependency graph ({}) can not be deleted because it is in state '{}'.", this
                            .getIdentifier(),
                    getState());

            throw new LifeCycleException("The data dependency graph (" + this.getIdentifier() +
                    ") can not be deleted because it is in state '" + getState() + "'.");
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
                logger.error("Storing data dependency graph '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting data dependency graph '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            lifeCycle = new DataDependencyGraphLifeCycle(this, false);
            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataDependencyGraph.class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data dependency graph '{}'", this.getIdentifier());
            throw new IOException("Class not found during deserialization of data dependency graph.");
        }
    }

    // Special implementation for this class since the getSerializedModel() method should not invoked
    public String toString() {
        StringBuilder sb = new StringBuilder(resourceName(this) + ":");

        Method[] methods = getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("get") && !method.getName().equals("getSerializedModel")
                    && method.getParameterTypes().length == 0) {
                try {
                    String field = method.getName().substring(3);
                    Object value = method.invoke(this);
                    if (value == null) {
                        continue;
                    }

                    if (value instanceof ABaseResource) {
                        sb.append("\n\t").append(field).append(" = ")
                                .append(((ABaseResource) value).getIdentifier());
                    } else if (isABaseResourceTypeCollection(value)) {
                        sb.append("\n\t").append(field).append(" = ")
                                .append(translateCollection2String((Collection) value));
                    } else {
                        sb.append("\n\t").append(field).append(" = ")
                                .append(value.toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DataDependencyGraph) {
            DataDependencyGraph s = (DataDependencyGraph) object;
            return this.identifier.equals(s.identifier);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, targetNamespace, name, entity);
    }
}