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

package org.trade.core.data.management.simple;

import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.InstanceStateChangeEvent;
import org.trade.core.auditing.events.ModelStateChangeEvent;
import org.trade.core.auditing.events.TraDEEvent;
import org.trade.core.data.management.IDataManager;
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.data.*;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;
import org.trade.core.persistence.PersistableHashMap;
import org.trade.core.utils.InstanceEvents;
import org.trade.core.utils.ModelEvents;
import org.trade.core.utils.TraDEProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple implementation of {@link IDataManager} for single-node deployments of the middleware.
 * <p>
 * Created by hahnml on 25.10.2016.
 */
public enum SimpleDataManager implements IDataManager {
    INSTANCE;

    private SimpleDataManager() {
        AuditingServiceFactory.createAuditingService().registerEventListener(this);
    }

    // TODO: Is the use of a singleton object appropriate or will this become a bottleneck in future?
    // But how to access the managers from API service implementations?

    private PersistableHashMap<DataDependencyGraph> dataDependencyGraphs = new PersistableHashMap<>
            (DataDependencyGraph.class);
    private PersistableHashMap<DataModel> dataModels = new PersistableHashMap<>(DataModel.class);
    private PersistableHashMap<DataObject> dataObjects = new PersistableHashMap<>(DataObject.class);
    private PersistableHashMap<DataObjectInstance> dataObjectInstances = new PersistableHashMap<>
            (DataObjectInstance.class);
    private PersistableHashMap<DataElement> dataElements = new PersistableHashMap<>(DataElement.class);
    private PersistableHashMap<DataElementInstance> dataElementInstances = new PersistableHashMap<>
            (DataElementInstance.class);
    private PersistableHashMap<DataValue> dataValues = new PersistableHashMap<>(DataValue.class);

    public DataDependencyGraph registerDataDependencyGraph(DataDependencyGraph graph) {
        this.dataDependencyGraphs.put(graph.getIdentifier(), graph);

        return graph;
    }

    public DataModel registerDataModel(DataModel model) {
        this.dataModels.put(model.getIdentifier(), model);

        return model;
    }

    public DataObject registerDataObject(DataObject dataObject) {
        this.dataObjects.put(dataObject.getIdentifier(), dataObject);

        return dataObject;
    }

    public DataValue registerDataValue(DataValue value) {
        this.dataValues.put(value.getIdentifier(), value);

        return value;
    }

    public DataElement addDataElementToDataObject(String dataObjectId, String entity, String name, String
            contentType, String type) throws Exception {
        DataElement result = null;

        if (hasDataObject(dataObjectId)) {
            // Resolve the data object
            DataObject dataObject = this.dataObjects.get(dataObjectId);

            // Check if the data object belongs to a data model
            if (dataObject.getDataModel() == null) {
                // Create a new data element
                DataElement dataElement = new DataElement(dataObject, entity, name);
                dataElement.setType(type);
                dataElement.setContentType(contentType);

                // Initialize the data element
                dataElement.initialize();

                // Register and return the data element
                this.dataElements.put(dataElement.getIdentifier(), dataElement);

                result = dataElement;
            } else {
                throw new IllegalModificationException("Trying to add a new data element '" + name + "' to data " +
                        "object '" + dataObjectId + "' which belongs to a data model (" + dataObject.getDataModel()
                        .getIdentifier() + ") and is therefore " +
                        "protected against changes.");
            }
        }

        return result;
    }

    public DataObjectInstance instantiateDataObject(String dataObjectId, String createdBy, HashMap<String, String>
            correlationProperties) throws Exception {
        DataObjectInstance result = null;

        if (hasDataObject(dataObjectId)) {
            // Resolve the data object
            DataObject dataObject = this.dataObjects.get(dataObjectId);

            // Create a new data object instance
            DataObjectInstance dataObjectInstance = dataObject.instantiate(createdBy, correlationProperties);

            // By convention we also directly instantiate all related data elements of the data object and associated
            // them to the new data object instance
            // TODO: 15.04.2017 Maybe we will change this behavior in a future version again...
            for (DataElement element : dataObject.getDataElements()) {
                DataElementInstance elmInstance = element.instantiate(dataObjectInstance, createdBy, correlationProperties);

                this.dataElementInstances.put(elmInstance.getIdentifier(), elmInstance);
            }

            // Register and return the data element
            this.dataObjectInstances.put(dataObjectInstance.getIdentifier(), dataObjectInstance);

            result = dataObjectInstance;
        }

        return result;
    }

    public DataDependencyGraph getDataDependencyGraph(String dataDependencyGraphId) {
        return this.dataDependencyGraphs.get(dataDependencyGraphId);
    }

    public DataModel getDataModel(String dataModelId) {
        return this.dataModels.get(dataModelId);
    }

    public DataObject getDataObject(String dataObjectId) {
        return this.dataObjects.get(dataObjectId);
    }

    public DataElement getDataElement(String dataElementId) {
        return this.dataElements.get(dataElementId);
    }

    public DataObjectInstance getDataObjectInstance(String instanceId) {
        return this.dataObjectInstances.get(instanceId);
    }

    public DataElementInstance getDataElementInstance(String instanceId) {
        return this.dataElementInstances.get(instanceId);
    }

    public DataValue getDataValue(String dataValueId) {
        return this.dataValues.get(dataValueId);
    }

    public List<DataDependencyGraph> getAllDataDependencyGraphs(String targetNamespace, String name, String entity) {
        Stream<DataDependencyGraph> stream = dataDependencyGraphs.values().stream();

        if (targetNamespace != null && !targetNamespace.isEmpty()) {
            stream = stream.filter(d -> (d.getTargetNamespace() != null && d.getTargetNamespace().toUpperCase().equals(targetNamespace
                    .toUpperCase())));
        }

        if (name != null && !name.isEmpty()) {
            stream = stream.filter(d -> (d.getName() != null && d.getName().toUpperCase().equals(name
                    .toUpperCase())));
        }

        if (entity != null && !entity.isEmpty()) {
            stream = stream.filter(d -> (d.getEntity() != null && d.getEntity().toUpperCase().equals(entity
                    .toUpperCase())));
        }

        List<DataDependencyGraph> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public List<DataModel> getAllDataModels(String targetNamespace, String name, String entity) {
        Stream<DataModel> stream = dataModels.values().stream();

        if (targetNamespace != null && !targetNamespace.isEmpty()) {
            stream = stream.filter(d -> (d.getTargetNamespace() != null && d.getTargetNamespace().toUpperCase().equals(targetNamespace
                    .toUpperCase())));
        }

        if (name != null && !name.isEmpty()) {
            stream = stream.filter(d -> (d.getName() != null && d.getName().toUpperCase().equals(name
                    .toUpperCase())));
        }

        if (entity != null && !entity.isEmpty()) {
            stream = stream.filter(d -> (d.getEntity() != null && d.getEntity().toUpperCase().equals(entity
                    .toUpperCase())));
        }

        List<DataModel> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public List<DataObject> getAllDataObjectsOfDataModel(String dataModelId) {
        List<DataObject> result = Collections.emptyList();
        if (hasDataModel(dataModelId)) {
            // Return an unmodifiable copy of the list of all data objects of the data model
            result = Collections.unmodifiableList(this.dataModels.get(dataModelId).getDataObjects());
        }

        return result;
    }

    public List<DataElement> getAllDataElementsOfDataObject(String dataObjectId) {
        List<DataElement> result = Collections.emptyList();
        if (hasDataObject(dataObjectId)) {
            // Return an unmodifiable copy of the list of all data elements of the data object
            result = Collections.unmodifiableList(this.dataObjects.get(dataObjectId).getDataElements());
        }

        return result;
    }

    public List<DataObjectInstance> getAllDataObjectInstancesOfDataObject(String dataObjectId) {
        List<DataObjectInstance> result = Collections.emptyList();
        if (hasDataObject(dataObjectId)) {
            // Return an unmodifiable copy of the list of all data object instances of the data object
            result = Collections.unmodifiableList(this.dataObjects.get(dataObjectId).getDataObjectInstances());
        }

        return result;
    }

    public List<DataElementInstance> getAllDataElementInstancesOfDataElement(String dataElementId) {
        List<DataElementInstance> result = Collections.emptyList();
        if (hasDataElement(dataElementId)) {
            // Return an unmodifiable copy of the list of all data element instances of the data element
            result = Collections.unmodifiableList(this.dataElements.get(dataElementId).getDataElementInstances());
        }

        return result;
    }

    public List<DataElementInstance> getAllDataElementInstancesOfDataValue(String dataValueId) {
        List<DataElementInstance> result = Collections.emptyList();
        if (hasDataValue(dataValueId)) {
            // Return an unmodifiable copy of the list of all data element instances using the data value
            result = Collections.unmodifiableList(this.dataValues.get(dataValueId).getDataElementInstances());
        }

        return result;
    }

    public List<DataElement> getAllDataElements(String name, String status) {
        Stream<DataElement> stream = dataElements.values().stream();

        if (name != null && !name.isEmpty()) {
            stream = stream.filter(d -> (d.getName() != null && d.getName().toUpperCase().equals(name
                    .toUpperCase())));
        }

        if (status != null && !status.isEmpty()) {
            stream = stream.filter(d -> (d.getState() != null && d.getState().toUpperCase().equals(status.toUpperCase())));
        }

        List<DataElement> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public List<DataValue> getAllDataValues(String status, String createdBy) {
        Stream<DataValue> stream = dataValues.values().stream();

        if (status != null && !status.isEmpty()) {
            stream = stream.filter(d -> (d.getState() != null && d.getState().toUpperCase().equals(status.toUpperCase())));
        }

        if (createdBy != null && !createdBy.isEmpty()) {
            stream = stream.filter(d -> (d.getOwner() != null && d.getOwner().equals(createdBy)));
        }

        List<DataValue> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public List<DataObject> getAllDataObjects(String name, String entity, String status) {
        Stream<DataObject> stream = dataObjects.values().stream();

        if (name != null && !name.isEmpty()) {
            stream = stream.filter(d -> (d.getName() != null && d.getName().equals(name)));
        }

        if (entity != null && !entity.isEmpty()) {
            stream = stream.filter(d -> (d.getEntity() != null && d.getEntity().equals(entity)));
        }

        if (status != null && !status.isEmpty()) {
            stream = stream.filter(d -> (d.getState() != null && d.getState().toUpperCase().equals(status.toUpperCase())));
        }

        List<DataObject> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public DataElementInstance getDataElementInstanceFromDataObjectInstanceByName(String dataObjectInstanceId, String
            dataElementName) {
        DataElementInstance result = null;

        if (hasDataObjectInstance(dataObjectInstanceId)) {
            Stream<DataElementInstance> stream = dataObjectInstances.get(dataObjectInstanceId).getDataElementInstances().stream();

            result = stream.filter(d -> (d.getDataElement() != null && d.getDataElement().getName().equals
                    (dataElementName))).findAny().orElse(null);
        }

        return result;
    }

    public List<DataElementInstance> getAllDataElementInstancesOfDataObjectInstance(String dataObjectInstanceId, String status) {
        List<DataElementInstance> result = Collections.emptyList();

        if (hasDataObjectInstance(dataObjectInstanceId)) {
            Stream<DataElementInstance> stream = dataObjectInstances.get(dataObjectInstanceId).getDataElementInstances().stream();

            if (status != null && !status.isEmpty()) {
                stream = stream.filter(d -> (d.getState() != null && d.getState().toUpperCase().equals(status.toUpperCase())));
            }

            result = stream.collect(Collectors.toList());
        }

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public List<DataObjectInstance> queryDataObjectInstance(String dataModelNamespace, String dataModelName, String
            dataObjectName, HashMap<String, String> correlationProperties) {
        Stream<DataObjectInstance> stream = dataObjectInstances.values().stream();

        // First, filter the list of instances based on the data object name
        if (dataObjectName != null && !dataObjectName.isEmpty()) {
            stream = stream.filter(d -> (d.getDataObject() != null && d.getDataObject().getName().equals(dataObjectName)));
        }

        // Second, filter the list of instance based on the provided correlation properties
        if (correlationProperties != null && !correlationProperties.isEmpty()) {
            stream = stream.filter(d -> (d.getCorrelationProperties() != null && d.getCorrelationProperties()
                    .equals(correlationProperties)));
        }

        // Last, check if namespace and name of the underlying data models match
        if (dataModelNamespace != null && !dataModelNamespace.isEmpty()) {
            stream = stream.filter(d -> (d.getDataObject().getDataModel() != null && d.getDataObject().getDataModel()
                    .getTargetNamespace().equals(dataModelNamespace)));
        }
        if (dataModelName != null && !dataModelName.isEmpty()) {
            stream = stream.filter(d -> (d.getDataObject().getDataModel() != null && d.getDataObject().getDataModel()
                    .getName().equals(dataModelName)));
        }

        return stream.collect(Collectors.toList());
    }

    public List<DataElementInstance> queryDataElementInstance(String dataModelNamespace, String dataModelName, String dataObjectName, String dataElementName, HashMap<String, String> correlationProperties) {
        Stream<DataElementInstance> stream = dataElementInstances.values().stream();

        // First, filter the list of instances based on the data element name
        if (dataElementName != null && !dataElementName.isEmpty()) {
            stream = stream.filter(d -> (d.getDataElement() != null && d.getDataElement().getName().equals(dataElementName)));
        }

        // Second, filter the list of instances based on the data object name
        if (dataObjectName != null && !dataObjectName.isEmpty()) {
            stream = stream.filter(d -> (d.getDataElement().getParent() != null && d.getDataElement().getParent().getName().equals
                    (dataObjectName)));
        }

        // Third, filter the list of instance based on the provided correlation properties
        if (correlationProperties != null && !correlationProperties.isEmpty()) {
            stream = stream.filter(d -> (d.getCorrelationProperties() != null && d.getCorrelationProperties()
                    .equals(correlationProperties)));
        }

        // Last, check if namespace and name of the underlying data models match
        if (dataModelNamespace != null && !dataModelNamespace.isEmpty()) {
            stream = stream.filter(d -> (d.getDataElement().getParent().getDataModel() != null && d.getDataElement().getParent().getDataModel()
                    .getTargetNamespace().equals(dataModelNamespace)));
        }
        if (dataModelName != null && !dataModelName.isEmpty()) {
            stream = stream.filter(d -> (d.getDataElement().getParent().getDataModel() != null && d.getDataElement().getParent().getDataModel()
                    .getName().equals(dataModelName)));
        }

        return stream.collect(Collectors.toList());
    }

    public boolean hasDataDependencyGraph(String graphId) {
        return this.dataDependencyGraphs.containsKey(graphId);
    }

    public boolean hasDataModel(String dataModelId) {
        return this.dataModels.containsKey(dataModelId);
    }

    public boolean hasDataObject(String dataObjectId) {
        return this.dataObjects.containsKey(dataObjectId);
    }

    public boolean hasDataObjectInstance(String dataObjectInstanceId) {
        return this.dataObjectInstances.containsKey(dataObjectInstanceId);
    }

    public boolean hasDataElement(String dataElementId) {
        return this.dataElements.containsKey(dataElementId);
    }

    public boolean hasDataElementInstance(String dataElementInstanceId) {
        return this.dataElementInstances.containsKey(dataElementInstanceId);
    }

    public boolean hasDataValue(String dataValueId) {
        return this.dataValues.containsKey(dataValueId);
    }

    public DataValue updateDataValue(String dataValueId, String name, String contentType, String type) {
        DataValue result = null;

        if (hasDataValue(dataValueId)) {
            DataValue value = this.dataValues.get(dataValueId);

            if (name != null && !name.isEmpty() && !name.equals(value.getName())) {
                value.setName(name);
            }
            if (type != null && !type.isEmpty() && !type.equals(value.getType())) {
                value.setType(type);
            }
            if (contentType != null && !contentType.isEmpty() && !contentType.equals(value.getContentType())) {
                value.setContentType(contentType);
            }

            result = value;
        }

        return result;
    }

    public DataObject updateDataObject(String dataObjectId, String name, String entity) throws
            IllegalModificationException {
        DataObject result = null;

        if (hasDataObject(dataObjectId)) {
            DataObject value = this.dataObjects.get(dataObjectId);

            // Check if the data object belongs to a data model
            if (value.getDataModel() == null) {
                if (name != null && !name.isEmpty() && !name.equals(value.getName())) {
                    value.setName(name);
                }
                if (entity != null && !entity.isEmpty() && !entity.equals(value.getEntity())) {
                    value.setEntity(entity);
                }
            } else {
                throw new IllegalModificationException("Trying to update data object '" + dataObjectId + "' which " +
                        "belongs to a data model (" + value.getDataModel().getIdentifier() + ") and is therefore " +
                        "protected against changes.");
            }

            result = value;
        }

        return result;
    }

    public DataElement updateDataElement(String dataElementId, String name, String type, String contentType) throws
            Exception {
        DataElement result = null;

        if (hasDataElement(dataElementId)) {
            DataElement value = this.dataElements.get(dataElementId);

            // Check if the data object belongs to a data model
            if (value.getParent().getDataModel() == null) {
                if (name != null && !name.isEmpty() && !name.equals(value.getName())) {
                    value.setName(name);
                }
                if (type != null && !type.isEmpty() && !type.equals(value.getType())) {
                    value.setType(type);
                }
                if (contentType != null && !contentType.isEmpty() && !contentType.equals(value.getContentType())) {
                    value.setContentType(contentType);
                }
            } else {
                throw new IllegalModificationException("Trying to update data element '" + dataElementId + "' which " +
                        "belongs to a data model (" + value.getParent().getDataModel().getIdentifier() + ") and is therefore" +
                        " protected against changes.");
            }

            result = value;
        }

        return result;
    }

    public void setSerializedModelOfDDG(String graphId, byte[] graph) throws Exception {
        if (hasDataDependencyGraph(graphId)) {
            this.dataDependencyGraphs.get(graphId).setSerializedModel(graph);
        }
    }

    public List<CompilationIssue> compileDataDependencyGraph(String graphId, byte[] graph) throws Exception {
        List<CompilationIssue> result = Collections.emptyList();

        if (hasDataDependencyGraph(graphId)) {
            result = this.dataDependencyGraphs.get(graphId).compileDataDependencyGraph(graph);

            registerContentsOfDataDependencyGraph(graphId);
        }

        return result;
    }

    public void setSerializedModelOfDataModel(String dataModelId, byte[] model) throws Exception {
        if (hasDataModel(dataModelId)) {
            this.dataModels.get(dataModelId).setSerializedModel(model);
        }
    }

    public List<CompilationIssue> compileDataModel(String dataModelId, byte[] model) throws Exception {
        List<CompilationIssue> result = Collections.emptyList();

        if (hasDataModel(dataModelId)) {
            result = this.dataModels.get(dataModelId).compileDataModel(model);

            registerContentsOfDataModel(dataModelId);
        }

        return result;
    }

    public void deleteDataDependencyGraph(String graphId) throws Exception {
        if (hasDataDependencyGraph(graphId)) {
            DataDependencyGraph result = this.dataDependencyGraphs.get(graphId);

            // Try to delete the DDG
            result.delete();

            // After the DDG is successfully deleted, we can remove it from the map
            this.dataDependencyGraphs.remove(graphId);
        }
    }

    public void deleteDataModel(String dataModelId) throws Exception {
        if (hasDataModel(dataModelId)) {
            DataModel result = this.dataModels.get(dataModelId);

            // Try to delete the data model
            result.delete();

            // After the data model is successfully deleted, we can remove it from the map
            this.dataModels.remove(dataModelId);
        }
    }

    public void deleteDataObject(String dataObjectId) throws Exception {
        if (hasDataObject(dataObjectId)) {
            DataObject result = this.dataObjects.get(dataObjectId);

            // Check if the data object belongs to a data model
            if (result.getDataModel() == null) {
                // Try to delete the data object
                result.delete();

                // After the data object is successfully deleted, we can remove it from the map
                this.dataObjects.remove(dataObjectId);
            } else {
                throw new IllegalModificationException("Trying to delete data object '" + dataObjectId + "' which " +
                        "belongs to a data model (" + result.getDataModel()
                        .getIdentifier() + ") and is therefore " +
                        "protected against changes.");
            }
        }
    }

    public void deleteDataElement(String dataElementId) throws Exception {
        if (hasDataElement(dataElementId)) {
            DataElement result = this.dataElements.get(dataElementId);

            // Check if the data element belongs to a data model
            if (result.getParent().getDataModel() == null) {
                // Try to delete the element from its parent data object
                result.getParent().deleteDataElement(result);

                // After the data element is successfully deleted, we can remove it from the map
                this.dataElements.remove(dataElementId);
            } else {
                throw new IllegalModificationException("Trying to delete data element '" + dataElementId + "' which " +
                        "belongs to a data model (" + result.getParent().getDataModel()
                        .getIdentifier() + ") and is therefore " +
                        "protected against changes.");
            }
        }
    }

    public void deleteDataObjectInstance(String instanceId) throws Exception {
        if (hasDataObjectInstance(instanceId)) {
            DataObjectInstance result = this.dataObjectInstances.get(instanceId);

            // By convention we also directly delete all related data element instances of the data object instance
            // TODO: 24.04.2017 Maybe we will change this behavior in a future version again...
            for (DataElementInstance elmInstance : result.getDataElementInstances()) {
                // Remove the data element instance from the data object instance
                result.removeDataElementInstance(elmInstance);

                // Try to delete the data element instance
                elmInstance.delete();

                this.dataElementInstances.remove(elmInstance.getIdentifier());
            }

            // Try to delete the data object instance
            result.delete();

            // After the data object instance is successfully deleted, we can remove it from the map
            this.dataObjectInstances.remove(instanceId);
        }
    }

    public void deleteDataValue(String dataValueId) throws Exception {
        if (hasDataValue(dataValueId)) {
            DataValue result = this.dataValues.get(dataValueId);

            // Try to delete the data value
            result.delete();

            // After the data value is successfully deleted, we can remove it from the map
            this.dataValues.remove(dataValueId);
        }
    }

    private void registerContentsOfDataDependencyGraph(String graphId) {
        DataDependencyGraph graph = this.dataDependencyGraphs.get(graphId);

        // Retrieve the data model generated during compilation
        DataModel model = graph.getDataModel();

        // Add the data model to the map
        this.dataModels.put(model.getIdentifier(), model);

        // Register its child elements
        registerContentsOfDataModel(model.getIdentifier());
    }

    private void registerContentsOfDataModel(String dataModelId) {
        DataModel model = this.dataModels.get(dataModelId);

        // Extract all child elements of the model to make them available through the respective maps
        for (DataObject dataObject : model.getDataObjects()) {
            this.dataObjects.put(dataObject.getIdentifier(), dataObject);

            for (DataElement element : dataObject.getDataElements()) {
                this.dataElements.put(element.getIdentifier(), element);
            }
        }
    }

    // Implementation of IAuditingService methods
    @Override
    public void onEvent(TraDEEvent event) {
        // TODO: 21.04.2017 Handle events!
        switch (event.getType()) {
            case dataHandling:
                break;
            case modelLifecycle:
                ModelStateChangeEvent modelStateChangeEvent = (ModelStateChangeEvent) event;
                // Remove all deleted model objects from the corresponding maps if they are still contained
                if (modelStateChangeEvent.getEvent().equals(ModelEvents.delete)) {
                    if (modelStateChangeEvent.getModelClass() == DataDependencyGraph.class) {
                        this.dataDependencyGraphs.remove(modelStateChangeEvent.getIdentifier());
                    } else if (modelStateChangeEvent.getModelClass() == DataModel.class) {
                        this.dataModels.remove(modelStateChangeEvent.getIdentifier());
                    } else if (modelStateChangeEvent.getModelClass() == DataObject.class) {
                        this.dataObjects.remove(modelStateChangeEvent.getIdentifier());
                    } else if (modelStateChangeEvent.getModelClass() == DataElement.class) {
                        this.dataElements.remove(modelStateChangeEvent.getIdentifier());
                    }
                }
                break;
            case instanceLifecycle:
                InstanceStateChangeEvent instanceStateChangeEvent = (InstanceStateChangeEvent) event;
                // Remove all deleted instance objects from the corresponding maps if they are still contained.
                if (instanceStateChangeEvent.getEvent().equals(InstanceEvents.delete)) {
                    if (instanceStateChangeEvent.getModelClass() == DataValue.class) {
                        this.dataValues.remove(instanceStateChangeEvent.getIdentifier());
                    } else if (instanceStateChangeEvent.getModelClass() == DataObjectInstance.class) {
                        this.dataObjectInstances.remove(instanceStateChangeEvent.getIdentifier());
                    } else if (instanceStateChangeEvent.getModelClass() == DataElementInstance.class) {
                        this.dataElementInstances.remove(instanceStateChangeEvent.getIdentifier());
                    }
                }
                break;
        }
    }

    @Override
    public void startup(TraDEProperties properties) {
        // TODO: 21.04.2017 ?
    }

    @Override
    public void shutdown() {
        // TODO: 21.04.2017 ?
    }
}