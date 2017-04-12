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

package org.trade.core.data.management;

import org.trade.core.model.data.*;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataManager {

    private static DataManager instance = new DataManager();

    // TODO: Should we split this to multiple, type-specific DataManager classes?

    // TODO: Is the use of a singleton object appropriate or will this become a bottleneck in future?
    // But how to access the managers from API service implementations?

    // TODO: Use Hazelcast, etc. instead of local maps
    private HashMap<String, DataDependencyGraph> dataDependencyGraphs = new LinkedHashMap<>();
    private HashMap<String, DataModel> dataModels = new LinkedHashMap<>();
    private HashMap<String, DataObject> dataObjects = new LinkedHashMap<>();
    private HashMap<String, DataObjectInstance> dataObjectInstances = new LinkedHashMap<>();
    private HashMap<String, DataElement> dataElements = new LinkedHashMap<>();
    private HashMap<String, DataElementInstance> dataElementInstances = new LinkedHashMap<>();
    private HashMap<String, DataValue> dataValues = new LinkedHashMap<>();

    private DataManager() {
        // Block instantiation
    }

    public static DataManager getInstance() {
        return instance;
    }

    public DataDependencyGraph registerDataDependencyGraph(DataDependencyGraph graph) {
        this.dataDependencyGraphs.put(graph.getIdentifier(), graph);

        return graph;
    }

    public DataModel registerDataModel(DataModel model) {
        this.dataModels.put(model.getIdentifier(), model);

        return model;
    }

    public DataValue registerDataValue(DataValue value) {
        this.dataValues.put(value.getIdentifier(), value);

        return value;
    }

    public DataDependencyGraph getDataDependencyGraph(String dataDependencyGraphId) {
        return this.dataDependencyGraphs.get(dataDependencyGraphId);
    }

    public DataModel getDataModel(String dataModelId) {
        return this.dataModels.get(dataModelId);
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

    public List<DataObject> getAllDataObjectsOfDataModel(String dataModelId) {
        List<DataObject> result = Collections.emptyList();
        if (hasDataModel(dataModelId)) {
            // Return an unmodifiable copy of the list of all data objects of the data model
            result = Collections.unmodifiableList(this.dataModels.get(dataModelId).getDataObjects());
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

    public boolean hasDataDependencyGraph(String graphId) {
        return this.dataDependencyGraphs.containsKey(graphId);
    }

    public boolean hasDataModel(String dataModelId) {
        return this.dataModels.containsKey(dataModelId);
    }

    public boolean hasDataValue(String dataValueId) {
        return this.dataValues.containsKey(dataValueId);
    }

    public DataValue updateDataValue(String dataValueId, String name, String contentType, String type) {
        DataValue result = null;

        if (this.dataValues.containsKey(dataValueId)) {
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

    public void deleteDataDependencyGraph(String graphId) throws Exception {
        if (this.dataDependencyGraphs.containsKey(graphId)) {
            DataDependencyGraph result = this.dataDependencyGraphs.remove(graphId);

            result.delete();
        }
    }

    public void deleteDataValue(String dataValueId) throws Exception {
        if (this.dataValues.containsKey(dataValueId)) {
            DataValue result = this.dataValues.remove(dataValueId);

            result.delete();
        }
    }

    public void registerContentsOfDataDependencyGraph(String graphId) {
        DataDependencyGraph graph = this.dataDependencyGraphs.get(graphId);

        // Retrieve the data model generated during compilation
        DataModel model = graph.getDataModel();

        // Add the data model to the map
        this.dataModels.put(model.getIdentifier(), model);

        // Register its child elements
        registerContentsOfDataModel(model.getIdentifier());
    }

    public void registerContentsOfDataModel(String dataModelId) {
        DataModel model = this.dataModels.get(dataModelId);

        // Extract all child elements of the model to make them available through the respective maps
        for (DataObject dataObject : model.getDataObjects()) {
            this.dataObjects.put(dataObject.getIdentifier(), dataObject);

            for (DataElement element : dataObject.getDataElements()) {
                this.dataElements.put(element.getIdentifier(), element);
            }
        }
    }
}
