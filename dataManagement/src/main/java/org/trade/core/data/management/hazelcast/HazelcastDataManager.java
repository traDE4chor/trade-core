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

package org.trade.core.data.management.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.ATraDEEvent;
import org.trade.core.data.management.IDataManager;
import org.trade.core.model.compiler.CompilationIssue;
import org.trade.core.model.data.*;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.model.data.instance.DataObjectInstance;
import org.trade.core.utils.TraDEProperties;

import java.util.HashMap;
import java.util.List;

/**
 * Implementation of {@link IDataManager} using a Hazelcast for multi-node deployments of the middleware.
 * <p>
 * Created by hahnml on 24.04.2017.
 */
public enum HazelcastDataManager implements IDataManager {
    INSTANCE;

    private HazelcastInstance hInstance;

    HazelcastDataManager() {
        AuditingServiceFactory.createAuditingService().registerEventListener(this);

        // Load properties
        TraDEProperties properties = new TraDEProperties();

        // Apply the properties to the XML config
        XmlConfigBuilder builder = new XmlConfigBuilder();
        builder.setProperties(properties);
        Config config = builder.build();

        this.hInstance = Hazelcast.newHazelcastInstance(config);
    }

    // TODO: Is the use of a singleton object appropriate or will this become a bottleneck in future?
    // But how to access the managers from API service implementations?

    private IMap<String, DataDependencyGraph> dataDependencyGraphs;
    private IMap<String, DataModel> dataModels;
    private IMap<String, DataObject> dataObjects;
    private IMap<String, DataObjectInstance> dataObjectInstances;
    private IMap<String, DataElement> dataElements;
    private IMap<String, DataElementInstance> dataElementInstances;
    private IMap<String, DataValue> dataValues;


    // TODO: 24.04.2017 Implement all methods
    
    @Override
    public DataDependencyGraph registerDataDependencyGraph(DataDependencyGraph graph) {
        return null;
    }

    @Override
    public DataModel registerDataModel(DataModel model) {
        return null;
    }

    @Override
    public DataObject registerDataObject(DataObject dataObject) {
        return null;
    }

    @Override
    public DataValue registerDataValue(DataValue value) {
        return null;
    }

    @Override
    public DataElement addDataElementToDataObject(String dataObjectId, String entity, String name, String contentType, String type) throws Exception {
        return null;
    }

    @Override
    public DataObjectInstance instantiateDataObject(String dataObjectId, String createdBy, HashMap<String, String> correlationProperties) throws Exception {
        return null;
    }

    @Override
    public DataDependencyGraph getDataDependencyGraph(String dataDependencyGraphId) {
        return null;
    }

    @Override
    public DataModel getDataModel(String dataModelId) {
        return null;
    }

    @Override
    public DataObject getDataObject(String dataObjectId) {
        return null;
    }

    @Override
    public DataElement getDataElement(String dataElementId) {
        return null;
    }

    @Override
    public DataObjectInstance getDataObjectInstance(String instanceId) {
        return null;
    }

    @Override
    public DataElementInstance getDataElementInstance(String instanceId) {
        return null;
    }

    @Override
    public DataValue getDataValue(String dataValueId) {
        return null;
    }

    @Override
    public List<DataDependencyGraph> getAllDataDependencyGraphs(String targetNamespace, String name, String entity) {
        return null;
    }

    @Override
    public List<DataModel> getAllDataModels(String targetNamespace, String name, String entity) {
        return null;
    }

    @Override
    public List<DataObject> getAllDataObjectsOfDataModel(String dataModelId) {
        return null;
    }

    @Override
    public List<DataElement> getAllDataElementsOfDataObject(String dataObjectId) {
        return null;
    }

    @Override
    public List<DataObjectInstance> getAllDataObjectInstancesOfDataObject(String dataObjectId) {
        return null;
    }

    @Override
    public List<DataElementInstance> getAllDataElementInstancesOfDataElement(String dataElementId) {
        return null;
    }

    @Override
    public List<DataElementInstance> getAllDataElementInstancesOfDataValue(String dataValueId) {
        return null;
    }

    @Override
    public List<DataElement> getAllDataElements(String name, String status) {
        return null;
    }

    @Override
    public List<DataValue> getAllDataValues(String status, String createdBy) {
        return null;
    }

    @Override
    public List<DataObject> getAllDataObjects(String name, String entity, String status) {
        return null;
    }

    @Override
    public DataElementInstance getDataElementInstanceFromDataObjectInstanceByName(String dataObjectInstanceId, String dataElementName) {
        return null;
    }

    @Override
    public List<DataElementInstance> getAllDataElementInstancesOfDataObjectInstance(String dataObjectInstanceId, String status) {
        return null;
    }

    @Override
    public List<DataObjectInstance> queryDataObjectInstance(String dataModelNamespace, String dataModelName, String dataObjectName, HashMap<String, String> correlationProperties) {
        return null;
    }

    @Override
    public List<DataElementInstance> queryDataElementInstance(String dataModelNamespace, String dataModelName, String dataObjectName, String dataElementName, HashMap<String, String> correlationProperties) {
        return null;
    }

    @Override
    public boolean hasDataDependencyGraph(String graphId) {
        return false;
    }

    @Override
    public boolean hasDataModel(String dataModelId) {
        return false;
    }

    @Override
    public boolean hasDataObject(String dataObjectId) {
        return false;
    }

    @Override
    public boolean hasDataObjectInstance(String dataObjectInstanceId) {
        return false;
    }

    @Override
    public boolean hasDataElement(String dataElementId) {
        return false;
    }

    @Override
    public boolean hasDataElementInstance(String dataElementInstanceId) {
        return false;
    }

    @Override
    public boolean hasDataValue(String dataValueId) {
        return false;
    }

    @Override
    public DataValue updateDataValue(String dataValueId, String name, String contentType, String type) {
        return null;
    }

    @Override
    public DataObject updateDataObject(String dataObjectId, String name, String entity) throws IllegalModificationException {
        return null;
    }

    @Override
    public DataElement updateDataElement(String dataElementId, String name, String type, String contentType) throws Exception {
        return null;
    }

    @Override
    public void setSerializedModelOfDDG(String graphId, byte[] graph) throws Exception {

    }

    @Override
    public List<CompilationIssue> compileDataDependencyGraph(String graphId, byte[] graph) throws Exception {
        return null;
    }

    @Override
    public void setSerializedModelOfDataModel(String dataModelId, byte[] model) throws Exception {

    }

    @Override
    public List<CompilationIssue> compileDataModel(String dataModelId, byte[] model) throws Exception {
        return null;
    }

    @Override
    public void deleteDataDependencyGraph(String graphId) throws Exception {

    }

    @Override
    public void deleteDataModel(String dataModelId) throws Exception {

    }

    @Override
    public void deleteDataObject(String dataObjectId) throws Exception {

    }

    @Override
    public void deleteDataElement(String dataElementId) throws Exception {

    }

    @Override
    public void deleteDataObjectInstance(String instanceId) throws Exception {

    }

    @Override
    public void deleteDataValue(String dataValueId) throws Exception {

    }

    // Implementation of IAuditingService methods
    @Override
    public void onEvent(ATraDEEvent event) {

    }

    @Override
    public void startup(TraDEProperties properties) {

    }

    @Override
    public void shutdown() {

    }
}
