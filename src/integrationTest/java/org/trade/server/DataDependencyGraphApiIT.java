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
import io.swagger.trade.client.jersey.api.DataDependencyGraphApi;
import io.swagger.trade.client.jersey.api.DataElementApi;
import io.swagger.trade.client.jersey.api.DataModelApi;
import io.swagger.trade.client.jersey.api.DataObjectApi;
import io.swagger.trade.client.jersey.model.*;
import org.apache.camel.test.AvailablePortFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Integration tests for data dependency graph API methods
 * <p>
 * Created by hahnml on 31.01.2017.
 */
public class DataDependencyGraphApiIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static DataDependencyGraphApi ddgApiInstance;

    private static DataModelApi dataModelApiInstance;

    private static DataObjectApi dataObjectApiInstance;

    private static DataElementApi dataElementApiInstance;

    @BeforeClass
    public static void setupEnvironment() {
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

        ddgApiInstance = new DataDependencyGraphApi(client);
        dataModelApiInstance = new DataModelApi(client);
        dataObjectApiInstance = new DataObjectApi(client);
        dataElementApiInstance = new DataElementApi(client);
    }

    @Test
    public void createAndQueryMultipleDataDependencyGraphsTest() throws Exception {
        String[] entity = {"hahnml", "otherUser", "hahnml"};
        String[] name = {"TestDataDependencyGraph", "DDG1", "DDG_B"};
        String[] graphIds = new String[3];

        // Add three DDGs
        for (int i = 0; i < entity.length; i++) {
            DataDependencyGraphData ddg = new DataDependencyGraphData();
            ddg.setEntity(entity[i]);
            ddg.setName(name[i]);

            DataDependencyGraphWithLinks resp = ddgApiInstance.addDataDependencyGraph(ddg);
            graphIds[i] = resp.getDataDependencyGraph().getId();
        }

        // Upload a graph model to the first DDG to enable namespace-based queries
        byte[] graph = TestUtils.getData("opalData.trade");
        // Try to upload and compile a serialized DDG
        ddgApiInstance.uploadGraphModel(graphIds[0], Long.valueOf(graph.length), graph);

        DataDependencyGraphArrayWithLinks result = ddgApiInstance.getDataDependencyGraphs(null, null, null, null,
                null);
        assertEquals(3, result.getDataDependencyGraphs().size());

        result = ddgApiInstance.getDataDependencyGraphs(null, null, null, null,
                "hahnml");
        assertEquals(2, result.getDataDependencyGraphs().size());

        result = ddgApiInstance.getDataDependencyGraphs(null, null, null, "TestDataDependencyGraph",
                "hahnml");
        assertEquals(1, result.getDataDependencyGraphs().size());

        result = ddgApiInstance.getDataDependencyGraphs(null, null, "http://de.uni-stuttgart.iaas/opalChor",
                null,
                null);
        assertEquals(1, result.getDataDependencyGraphs().size());

        // Delete the three DDGs again
        for (int i = 0; i < entity.length; i++) {
            ddgApiInstance.deleteDataDependencyGraph(graphIds[i]);
        }
    }

    @Test
    public void createAndUploadDataDependencyGraphTest() throws Exception {
        String entity = "hahnml";
        String name = "TestDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        DataDependencyGraphWithLinks ddgResponse = ddgApiInstance.addDataDependencyGraph(ddg);

        assertNotNull(ddgResponse);
        assertNotNull(ddgResponse.getDataDependencyGraph());
        assertNotNull(ddgResponse.getDataDependencyGraph().getId());
        assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
        assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

        TestUtils.printLinkArray(ddgResponse.getLinks());

        String graphId = ddgResponse.getDataDependencyGraph().getId();

        byte[] graph = TestUtils.getData("opalData.trade");

        // Try to upload and compile a serialized DDG
        ddgApiInstance.uploadGraphModel(graphId, Long.valueOf(graph.length), graph);

        // Links should be different, therefore objects should be not equal
        DataDependencyGraphWithLinks ddgResponse2 = ddgApiInstance.getDataDependencyGraphDirectly(graphId);
        assertNotEquals(ddgResponse, ddgResponse2);

        // Try to query the resulting data objects and data elements
        DataModelWithLinks dataModelResp = dataModelApiInstance.getDataModel(graphId);
        String dataModelId = dataModelResp.getDataModel().getId();

        TestUtils.printLinkArray(dataModelResp.getLinks());

        DataObjectArrayWithLinks dataObjects = dataObjectApiInstance.getDataObjects(dataModelId, null, null);
        assertEquals(4, dataObjects.getDataObjects().size());

        TestUtils.printLinkArray(dataObjects.getLinks());

        int[] sizes = {3, 2, 2, 2};
        int index = 0;
        for (DataObjectWithLinks dataObject : dataObjects.getDataObjects()) {
            DataElementArrayWithLinks dataElements = dataElementApiInstance.getDataElements(dataObject
                            .getDataObject().getId(), null, null,
                    null, null);
            assertEquals(sizes[index], dataElements.getDataElements().size());
            index++;
        }

        byte[] graphData = ddgApiInstance.downloadGraphModel(graphId);
        assertEquals(new String(graph, StandardCharsets.UTF_8), new String(graphData, StandardCharsets.UTF_8));

        // Delete the DDG
        ddgApiInstance.deleteDataDependencyGraph(graphId);

        // Check if the DDG is deleted
        DataDependencyGraphArrayWithLinks result = ddgApiInstance.getDataDependencyGraphs(null, null, null, null,
                null);
        assertEquals(0, result.getDataDependencyGraphs().size());

        // And all related resources are also deleted
        // Data Model
        DataModelArrayWithLinks models = dataModelApiInstance.getDataModels(null, null, null, null, null);
        assertEquals(0, models.getDataModels().size());

        // Data Objects
        DataObjectArrayWithLinks objects = dataObjectApiInstance.getAllDataObjects(null, null, null, null,
                null);
        assertEquals(0, objects.getDataObjects().size());

        // Data Elements
        DataElementArrayWithLinks elements = dataElementApiInstance.getAllDataElements(null, null, null, null);
        assertEquals(0, elements.getDataElements().size());
    }

    @Test
    public void createAndUploadDataDependencyGraphWithFailureTest() {
        String entity = "hahnml";
        String name = "AnotherTestDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        String id = null;
        try {
            DataDependencyGraphWithLinks ddgResponse = ddgApiInstance.addDataDependencyGraph(ddg);

            assertNotNull(ddgResponse);
            assertNotNull(ddgResponse.getDataDependencyGraph());
            assertNotNull(ddgResponse.getDataDependencyGraph().getId());
            assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
            assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

            id = ddgResponse.getDataDependencyGraph().getId();

            byte[] graph = TestUtils.getData("opalDataFailure.trade");

            // Try to upload and compile a serialized DDG that is not valid
            ddgApiInstance.uploadGraphModel(id, Long.valueOf(graph.length), graph);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(500, e.getCode());

            if (id != null) {
                try {
                    ddgApiInstance.deleteDataDependencyGraph(id);
                } catch (ApiException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_MODEL__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION).drop();

        dataStore.getCollection("dataElements").drop();
        dataStore.getCollection("dataObjects").drop();
        dataStore.getCollection("dataModels").drop();
        dataStore.getCollection("dataDependencyGraphs").drop();
        dataStore.getCollection("notifications").drop();

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
