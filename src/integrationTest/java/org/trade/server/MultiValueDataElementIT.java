/*
 * Copyright 2018 Michael Hahn
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

package org.trade.server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import io.swagger.trade.client.jersey.ApiClient;
import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.*;
import io.swagger.trade.client.jersey.model.*;
import org.apache.camel.test.AvailablePortFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 10.05.2017.
 */
public class MultiValueDataElementIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static DataValueApi dvApiInstance;

    private static DataObjectApi doApiInstance;

    private static DataObjectInstanceApi doInstApiInstance;

    private static DataElementApi deApiInstance;

    private static DataElementInstanceApi deInstApiInstance;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        // Load custom properties such as MongoDB url and db name
        properties = new TraDEProperties();

        // Find an unused available port
        int port = AvailablePortFinder.getNextAvailable();

        // Set the port
        properties.setProperty(TraDEProperties.PROPERTY_HTTP_SERVER_PORT, String.valueOf(port));

        // Create a new server
        server = new TraDEServer();

        // Start the server
        try {
            server.startHTTPServer(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiClient client = new ApiClient();

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client.setBasePath("http://127.0.0.1:" + port + "/api");

        dvApiInstance = new DataValueApi(client);

        doApiInstance = new DataObjectApi(client);

        doInstApiInstance = new DataObjectInstanceApi(client);

        deInstApiInstance = new DataElementInstanceApi(client);

        deApiInstance = new DataElementApi(client);
    }

    @Test
    public void createMultiValueDataElementTest() throws Exception {
        try {
            // Create a data object with a multi-value data element
            // object
            DataObject dObject = doApiInstance.addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = deApiInstance.addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain").isCollectionElement
                    (true)).getDataElement();
            assertNotNull(dElement);
            assertTrue(dElement.getIsCollectionElement());

            DataElementWithLinks dElm = deApiInstance.getDataElementDirectly(dElement.getId());
            assertNotNull(dElm.getDataElement());
            assertTrue(dElm.getDataElement().getIsCollectionElement());

            doApiInstance.deleteDataObject(dObject.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void instantiateMultiValueDataElementTest() throws Exception {
        try {
            // Create a data object with a multi-value data element and then instantiating the data
            // object
            DataObject dObject = doApiInstance.addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = deApiInstance.addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain").isCollectionElement
                    (true)).getDataElement();
            assertNotNull(dElement);
            assertTrue(dElement.getIsCollectionElement());

            // Set correlation property
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            // Create a new data object instance
            DataObjectInstance doInst = doInstApiInstance.addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            DataElementInstanceWithLinks elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                            .getId(), dElement.getName());
            assertNotNull(elmInstance);
            assertNotNull(elmInstance.getInstance());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Create and associated two data values to the data element instance
            DataValueWithLinks firstDataValue = dvApiInstance.associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data1").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(firstDataValue);
            assertNotNull(firstDataValue.getDataValue());

            DataValueWithLinks secondDataValue = dvApiInstance.associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data2").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(secondDataValue);
            assertNotNull(secondDataValue.getDataValue());

            // Data element instance should still be in state CREATED
            elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());
            assertTrue(elmInstance.getInstance().getNumberOfDataValues() == 2);

            // Assign data to the first data value
            dvApiInstance.pushDataValue(firstDataValue.getDataValue().getId(), 4L, new String("test").getBytes());

            // Data element instance should still be in state CREATED
            elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Assign data to the second data value
            dvApiInstance.pushDataValue(secondDataValue.getDataValue().getId(), 4L, new String("abcd").getBytes());

            // Data element instance should be be in state INITIALIZED now
            elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.INITIALIZED, elmInstance.getInstance().getStatus());

            // Unset data from the second data value
            dvApiInstance.pushDataValue(secondDataValue.getDataValue().getId(), 0L, null);

            // Data element instance should be be in state CREATED again
            elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Remove the first data value from the element instance
            dvApiInstance.removeDataValueFromDataElementInstance(elmInstance.getInstance().getId(), secondDataValue
                    .getDataValue().getId());

            // Data element instance should be be in state INITIALIZED again and only have one data value
            elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());

            assertTrue(elmInstance.getInstance().getNumberOfDataValues() == 1);
            assertEquals(InstanceStatusEnum.INITIALIZED, elmInstance.getInstance().getStatus());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void querySpecificDataValueFromMultiValueDataElementTest() throws Exception {
        try {
            // Create a data object with a multi-value data element and then instantiating the data
            // object
            DataObject dObject = doApiInstance.addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = deApiInstance.addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain").isCollectionElement
                    (true)).getDataElement();
            assertNotNull(dElement);
            assertTrue(dElement.getIsCollectionElement());

            // Set correlation property
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            // Create a new data object instance
            DataObjectInstance doInst = doInstApiInstance.addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            DataElementInstanceWithLinks elmInstance = deInstApiInstance.getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertNotNull(elmInstance);
            assertNotNull(elmInstance.getInstance());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Create and associated two data values to the data element instance
            DataValueWithLinks firstDataValue = dvApiInstance.associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data1").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(firstDataValue);
            assertNotNull(firstDataValue.getDataValue());

            DataValueWithLinks secondDataValue = dvApiInstance.associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data2").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(secondDataValue);
            assertNotNull(secondDataValue.getDataValue());

            DataValueArrayWithLinks dvArray1 = dvApiInstance.getDataValues(elmInstance.getInstance().getId(), 1);
            assertNotNull(dvArray1.getDataValues());
            assertFalse(dvArray1.getDataValues().isEmpty());
            assertEquals(firstDataValue.getDataValue().getId(), dvArray1.getDataValues().get(0).getDataValue().getId());

            DataValueArrayWithLinks dvArray2 = dvApiInstance.getDataValues(elmInstance.getInstance().getId(), 2);
            assertNotNull(dvArray2.getDataValues());
            assertFalse(dvArray2.getDataValues().isEmpty());
            assertEquals(secondDataValue.getDataValue().getId(), dvArray2.getDataValues().get(0).getDataValue().getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_MODEL__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION).drop();

        dataStore.getCollection("dataValues").drop();
        dataStore.getCollection("dataElements").drop();
        dataStore.getCollection("dataElementInstances").drop();
        dataStore.getCollection("dataObjects").drop();
        dataStore.getCollection("dataObjectInstances").drop();
        dataStore.getCollection("dataModels").drop();
        dataStore.getCollection("dataDependencyGraphs").drop();

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
