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

package org.trade.server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import io.swagger.trade.client.jersey.ApiClient;
import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.*;
import io.swagger.trade.client.jersey.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 31.01.2017.
 */
public class TraDEClientServerIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static DataDependencyGraphApi ddgApiInstance;

    private static DataModelApi dataModelApiInstance;

    private static DataObjectApi dataObjectApiInstance;

    private static DataElementApi dataElementApiInstance;

    private static DataValueApi dvApiInstance;

    @BeforeClass
    public static void setupEnvironment() {
        // Load custom properties such as MongoDB url and db name
        properties = new TraDEProperties();

        // Create a new server
        server = new TraDEServer();

        // Start the server
        try {
            server.startHTTPServer(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiClient client = new ApiClient();
        client.setBasePath("http://localhost:8080/api");

        ddgApiInstance = new DataDependencyGraphApi(client);
        dataModelApiInstance = new DataModelApi(client);
        dataObjectApiInstance = new DataObjectApi(client);
        dataElementApiInstance = new DataElementApi(client);
        dvApiInstance = new DataValueApi(client);
    }

    @Test
    public void dataValueApiRoundTripTest() throws Exception {
        DataValueTestHelper helper = new DataValueTestHelper(dvApiInstance);
        helper.addDataValues();

        helper.getDataValues();

        helper.getDataValue();

        helper.pushDataValues();

        helper.pullDataValues();

        helper.deleteDataValues();
    }

    @Test
    public void shouldRejectAddDataValueRequestTest() {
        // Try to add a new data value without 'createdBy' value
        DataValueData request = new DataValueData();

        request.setName("inputData");
        request.setType("binary");
        request.setContentType("text/plain");

        try {
            DataValue result3 = dvApiInstance.addDataValue(request);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void shouldRejectDeleteDataValueRequestTest() {
        try {
            // Try to delete not existing data value
            dvApiInstance.deleteDataValue("Not-Existing-Id");
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void updateDataValueTest() {
        DataValueData request = new DataValueData();

        request.setName("inputData");
        request.setCreatedBy("hahnml");
        request.setType("binary");
        request.setContentType("text/plain");

        try {
            DataValue result = dvApiInstance.addDataValue(request);

            DataValue updateRequest = new DataValue();
            updateRequest.setName("changedName");
            updateRequest.setContentType("changedContentType");
            updateRequest.setType("changedType");

            dvApiInstance.updateDataValueDirectly(result.getId(), updateRequest);

            DataValueWithLinks updated = dvApiInstance.getDataValueDirectly(result.getId());
            // Check unchanged properties
            assertEquals(result.getId(), updated.getDataValue().getId());
            assertEquals(result.getStatus(), updated.getDataValue().getStatus());
            assertEquals(result.getCreated(), updated.getDataValue().getCreated());
            assertEquals(result.getSize(), updated.getDataValue().getSize());
            assertEquals(result.getCreatedBy(), updated.getDataValue().getCreatedBy());

            // Check changed properties
            assertNotEquals(result.getLastModified(), updated.getDataValue().getLastModified());
            assertNotEquals(result.getName(), updated.getDataValue().getName());
            assertNotEquals(result.getContentType(), updated.getDataValue().getContentType());
            assertNotEquals(result.getType(), updated.getDataValue().getType());

            dvApiInstance.deleteDataValue(result.getId());
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void createAndUploadDataDependencyGraphTest() throws Exception {
        String entity = "hahnml";
        String name = "TestDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        try {
            DataDependencyGraphWithLinks ddgResponse = ddgApiInstance.addDataDependencyGraph(ddg);

            assertNotNull(ddgResponse);
            assertNotNull(ddgResponse.getDataDependencyGraph());
            assertNotNull(ddgResponse.getDataDependencyGraph().getId());
            assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
            assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

            printLinkArray(ddgResponse.getLinks());

            String graphId = ddgResponse.getDataDependencyGraph().getId();

            byte[] graph = TestUtils.INSTANCE.getData("opalData.trade");

            // Try to upload and compile a serialized DDG
            ddgApiInstance.uploadGraphModel(graphId, Long.valueOf(graph.length), graph);

            // Try to query the resulting data objects and data elements
            DataModelWithLinks dataModelResp = dataModelApiInstance.getDataModel(graphId);
            String dataModelId = dataModelResp.getDataModel().getId();

            printLinkArray(dataModelResp.getLinks());

            DataObjectArrayWithLinks dataObjects = dataObjectApiInstance.getDataObjects(dataModelId, null, null);
            assertEquals(4, dataObjects.getDataObjects().size());

            printLinkArray(dataObjects.getLinks());
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }
    }

    @Test
    public void createAndUploadDataDependencyGraphWithFailureTest() {
        String entity = "hahnml";
        String name = "AnotherTestDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        try {
            DataDependencyGraphWithLinks ddgResponse = ddgApiInstance.addDataDependencyGraph(ddg);

            assertNotNull(ddgResponse);
            assertNotNull(ddgResponse.getDataDependencyGraph());
            assertNotNull(ddgResponse.getDataDependencyGraph().getId());
            assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
            assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

            String id = ddgResponse.getDataDependencyGraph().getId();

            byte[] graph = TestUtils.INSTANCE.getData("opalDataFailure.trade");

            // Try to upload and compile a serialized DDG that is not valid
            ddgApiInstance.uploadGraphModel(id, Long.valueOf(graph.length), graph);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(500, e.getCode());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_VALUE_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_MODEL_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH_COLLECTION).drop();
        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printLinkArray(LinkArray links) {
        for (Link link : links) {
            System.out.println("HREF: " + link.getHref());
            System.out.println("REL: " + link.getRel());
            System.out.println("TITLE: " + link.getTitle());
            System.out.println("TYPE: " + link.getType());
            System.out.println("HREF-LANG: " + link.getHreflang());
            System.out.println("LENGTH: " + link.getLength());
        }
    }
}
