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

package org.trade.core.data.management;

import org.trade.core.auditing.TraDEEventListener;
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.data.*;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;

import java.util.HashMap;
import java.util.List;

/**
 * This interface defines basic methods a data manager implementation should provide.
 * <p>
 * Created by hahnml on 24.04.2017.
 */
public interface IDataManager extends TraDEEventListener {

    /**
     * Register a new data dependency graph.
     *
     * @param graph the graph
     * @return the registered data dependency graph
     */
    DataDependencyGraph registerDataDependencyGraph(DataDependencyGraph graph);

    /**
     * Register a new data model.
     *
     * @param model the model
     * @return the registered data model
     */
    DataModel registerDataModel(DataModel model);

    /**
     * Register a new data object.
     *
     * @param dataObject the data object
     * @return the registered data object
     */
    DataObject registerDataObject(DataObject dataObject);

    /**
     * Register a new data value.
     *
     * @param value the value
     * @return the registered data value
     */
    DataValue registerDataValue(DataValue value);

    /**
     * Add a new data element to a data object. This is only allowed if the data object does not belong to a data model.
     *
     * @param dataObjectId the ID of the data object
     * @param entity       the entity to which the data element belongs
     * @param name         the name of the data element
     * @param contentType  the content type of the data element
     * @param type         the type of the data element
     * @return the data element
     * @throws Exception the exception
     */
    DataElement addDataElementToDataObject(String dataObjectId, String entity, String name, String
            contentType, String type) throws Exception;

    /**
     * Instantiate the given data object.
     *
     * @param dataObjectId          the ID of the data object to instantiate
     * @param createdBy             the information who created the new instance
     * @param correlationProperties a set of correlation properties
     * @return the data object instance
     * @throws Exception the exception
     */
    DataObjectInstance instantiateDataObject(String dataObjectId, String createdBy, HashMap<String, String>
            correlationProperties) throws Exception;

    /**
     * Gets a data dependency graph.
     *
     * @param dataDependencyGraphId the ID of a data dependency graph
     * @return the data dependency graph
     */
    DataDependencyGraph getDataDependencyGraph(String dataDependencyGraphId);

    /**
     * Gets a data model.
     *
     * @param dataModelId the ID of a data model
     * @return the data model
     */
    DataModel getDataModel(String dataModelId);

    /**
     * Gets a data object.
     *
     * @param dataObjectId the ID of a data object
     * @return the data object
     */
    DataObject getDataObject(String dataObjectId);

    /**
     * Gets a data element.
     *
     * @param dataElementId the ID of a data element
     * @return the data element
     */
    DataElement getDataElement(String dataElementId);

    /**
     * Gets a data object instance.
     *
     * @param instanceId the ID of a data object instance
     * @return the data object instance
     */
    DataObjectInstance getDataObjectInstance(String instanceId);

    /**
     * Gets a data element instance.
     *
     * @param instanceId the ID of a data element instance
     * @return the data element instance
     */
    DataElementInstance getDataElementInstance(String instanceId);

    /**
     * Gets a data value.
     *
     * @param dataValueId the ID of a data value
     * @return the data value
     */
    DataValue getDataValue(String dataValueId);

    /**
     * Gets all data dependency graphs based on the provided criteria.
     *
     * @param targetNamespace the target namespace to search for
     * @param name            the name to search for
     * @param entity          the entity to search for
     * @return a list of all data dependency graphs fulfilling the specified criteria
     */
    List<DataDependencyGraph> getAllDataDependencyGraphs(String targetNamespace, String name, String entity);

    /**
     * Gets all data models based on the provided criteria.
     *
     * @param targetNamespace the target namespace to search for
     * @param name            the name to search for
     * @param entity          the entity to search for
     * @return a list of all data models fulfilling the specified criteria
     */
    List<DataModel> getAllDataModels(String targetNamespace, String name, String entity);

    /**
     * Gets all data objects of a data model.
     *
     * @param dataModelId the ID of a data model
     * @return a list of all data objects which belong to the given data model
     */
    List<DataObject> getAllDataObjectsOfDataModel(String dataModelId);

    /**
     * Gets all data elements of a data object.
     *
     * @param dataObjectId the ID of a data object
     * @return a list of all data elements which belong to the given data object
     */
    List<DataElement> getAllDataElementsOfDataObject(String dataObjectId);

    /**
     * Gets all data object instances of a data object.
     *
     * @param dataObjectId the ID of a data object
     * @return a list of all data object instances which belong to the given data object
     */
    List<DataObjectInstance> getAllDataObjectInstancesOfDataObject(String dataObjectId);

    /**
     * Gets all data element instances of a data element.
     *
     * @param dataElementId the ID of a data element
     * @return a list of all data element instances which belong to the given data element
     */
    List<DataElementInstance> getAllDataElementInstancesOfDataElement(String dataElementId);

    /**
     * Gets all data element instances associated to a data value.
     *
     * @param dataValueId the ID of a data value
     * @return a list of all data element instances which belong to the given data value
     */
    List<DataElementInstance> getAllDataElementInstancesOfDataValue(String dataValueId);

    /**
     * Gets all data elements based on the provided criteria.
     *
     * @param name   the name to search for
     * @param status the status to search for
     * @return a list of all data elements fulfilling the specified criteria
     */
    List<DataElement> getAllDataElements(String name, String status);

    /**
     * Gets all data values based on the provided criteria.
     *
     * @param status    the status to search for
     * @param createdBy who created a data value to search for
     * @return a list of all data values fulfilling the specified criteria
     */
    List<DataValue> getAllDataValues(String status, String createdBy);

    /**
     * Gets all data objects based on the provided criteria.
     *
     * @param name   the name to search for
     * @param entity the entity to search for
     * @param status the status to search for
     * @return a list of all data objects fulfilling the specified criteria
     */
    List<DataObject> getAllDataObjects(String name, String entity, String status);

    /**
     * Gets a data element instance of a data object instance based on the name of the underlying data element.
     *
     * @param dataObjectInstanceId the ID of a data object instance
     * @param dataElementName      the name of a data element, where the data element has to be defined for the data
     *                             object to which the referenced data object instance belongs.
     * @return the data element instance which belongs to the specified data element
     */
    DataElementInstance getDataElementInstanceFromDataObjectInstanceByName(String dataObjectInstanceId, String
            dataElementName);

    /**
     * Gets all data element instances of a data object instance based on the provided status.
     *
     * @param dataObjectInstanceId the ID of a data object instance
     * @param status               the status to search for
     * @return a list of all data element instances with the specified status of the provided data object instance
     */
    List<DataElementInstance> getAllDataElementInstancesOfDataObjectInstance(String dataObjectInstanceId, String
            status);

    /**
     * Query data object instances based on a set of search criteria.
     *
     * @param dataModelNamespace    the namespace of the underlying data model
     * @param dataModelName         the name of the underlying data model
     * @param dataObjectName        the name of the data object for which instances should be returned
     * @param correlationProperties the set of correlation properties which instances need to be compliant with
     * @return a list of all data object instances fulfilling the specified criteria
     */
    List<DataObjectInstance> queryDataObjectInstance(String dataModelNamespace, String dataModelName, String
            dataObjectName, HashMap<String, String> correlationProperties);

    /**
     * Query data element instance based on a set of search criteria.
     *
     * @param dataModelNamespace    the namespace of the underlying data model
     * @param dataModelName         the name of the underlying data model
     * @param dataObjectName        the name of the underlying parent data object
     * @param dataElementName       the name of the data element for which instances should be returned
     * @param correlationProperties the set of correlation properties which instances need to be compliant with
     * @return a list of all data element instances fulfilling the specified criteria
     */
    List<DataElementInstance> queryDataElementInstance(String dataModelNamespace, String dataModelName, String
            dataObjectName, String dataElementName, HashMap<String, String> correlationProperties);

    /**
     * Whether the {@link IDataManager} implementation knows a data dependency graph with the provided ID or not.
     *
     * @param graphId the graph ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataDependencyGraph(String graphId);

    /**
     * Whether the {@link IDataManager} implementation knows a data model with the provided ID or not.
     *
     * @param dataModelId the data model ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataModel(String dataModelId);

    /**
     * Whether the {@link IDataManager} implementation knows a data object with the provided ID or not.
     *
     * @param dataObjectId the data object ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataObject(String dataObjectId);

    /**
     * Whether the {@link IDataManager} implementation knows a data object instance with the provided ID or not.
     *
     * @param dataObjectInstanceId the data object instance ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataObjectInstance(String dataObjectInstanceId);

    /**
     * Whether the {@link IDataManager} implementation knows a data element instance with the provided ID or not.
     *
     * @param dataElementId the data element ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataElement(String dataElementId);

    /**
     * Whether the {@link IDataManager} implementation knows a data element instance with the provided ID or not.
     *
     * @param dataElementInstanceId the data element instance ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataElementInstance(String dataElementInstanceId);

    /**
     * Whether the {@link IDataManager} implementation knows a data value with the provided ID or not.
     *
     * @param dataValueId the data value ID
     * @return True, if the {@link IDataManager} implementation knows the ID; false, otherwise
     */
    boolean hasDataValue(String dataValueId);

    /**
     * Update a data value with the given data. Since the three parameters are mandatory attributes, providing NULL or empty
     * String values for the parameters has no effect. Furthermore, parameters are only applied if the provided values
     * are different compared to the current ones.
     *
     * @param dataValueId the ID of a data value
     * @param name        the new name of the data value
     * @param contentType the new content type of the data value
     * @param type        the new type of the data value
     * @return the updated data value.
     */
    DataValue updateDataValue(String dataValueId, String name, String contentType, String type);

    /**
     * Update a data object with the given data. Since both parameters are mandatory attributes, providing NULL or empty
     * String values for the parameters has no effect. Furthermore, parameters are only applied if the provided values
     * are different compared to the current ones.
     *
     * @param dataObjectId the ID of a data object
     * @param name         the new name of the data object
     * @param entity       the new entity of the data object
     * @return the updated data object
     * @throws IllegalModificationException the illegal modification exception
     */
    DataObject updateDataObject(String dataObjectId, String name, String entity) throws
            IllegalModificationException;

    /**
     * Update a data element with the given data. Since the three parameters are mandatory attributes, providing NULL
     * or empty String values for the parameters has no effect. Furthermore, parameters are only applied if the
     * provided values are different compared to the current ones.
     *
     * @param dataElementId the ID of a data element
     * @param name          the new name of the data element
     * @param type          the new type of the data element
     * @param contentType   the new content type of the data element
     * @return the updated data element
     * @throws Exception the exception
     */
    DataElement updateDataElement(String dataElementId, String name, String type, String contentType) throws
            Exception;

    /**
     * Sets a serialized model to a {@link DataDependencyGraph}.
     *
     * @param graphId the ID of the data dependency graph
     * @param graph   the serialized data dependency graph model as byte[]
     * @throws Exception the exception
     */
    void setSerializedModelOfDDG(String graphId, byte[] graph) throws Exception;

    /**
     * Compiles a serialized data dependency graph model and associates the resulting model objects, e.g., data
     * model, data objects and data elements to the specified data dependency graph.
     *
     * @param graphId the ID of the data dependency graph to associate the compiled model objects to
     * @param graph   the serialized data dependency graph model as byte[]
     * @return a list of {@link CompilationIssue}'s identified during compilation
     * @throws Exception the exception
     */
    List<CompilationIssue> compileDataDependencyGraph(String graphId, byte[] graph) throws Exception;

    /**
     * Sets a serialized model to a {@link DataModel}
     *
     * @param dataModelId the ID of the data model
     * @param model       the serialized data model as byte[]
     * @throws Exception the exception
     */
    void setSerializedModelOfDataModel(String dataModelId, byte[] model) throws Exception;

    /**
     * Compiles a serialized data model and associates the resulting model objects, e.g., data objects and data
     * elements to the specified data model.
     *
     * @param dataModelId the ID of the data model
     * @param model       the serialized data model as byte[]
     * @return a list of {@link CompilationIssue}'s identified during compilation
     * @throws Exception the exception
     */
    List<CompilationIssue> compileDataModel(String dataModelId, byte[] model) throws Exception;

    /**
     * Deletes the data dependency graph with the specified ID.
     *
     * @param graphId the ID of the data dependency graph
     * @throws Exception the exception
     */
    void deleteDataDependencyGraph(String graphId) throws Exception;

    /**
     * Deletes the data model with the specified ID.
     *
     * @param dataModelId the ID of the data model
     * @throws Exception the exception
     */
    void deleteDataModel(String dataModelId) throws Exception;

    /**
     * Deletes the data object with the specified ID.
     *
     * @param dataObjectId the ID of the data object
     * @throws Exception the exception
     */
    void deleteDataObject(String dataObjectId) throws Exception;

    /**
     * Deletes the data element with the specified ID.
     *
     * @param dataElementId the data element ID
     * @throws Exception the exception
     */
    void deleteDataElement(String dataElementId) throws Exception;

    /**
     * Deletes the data object instance with the specified ID.
     *
     * @param instanceId the ID of the data object instance
     * @throws Exception the exception
     */
    void deleteDataObjectInstance(String instanceId) throws Exception;

    /**
     * Deletes the data value with the specified ID.
     *
     * @param dataValueId the ID of the data value
     * @throws Exception the exception
     */
    void deleteDataValue(String dataValueId) throws Exception;

}
