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
import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.DataValueApi;
import io.swagger.trade.client.jersey.model.DataValue;
import io.swagger.trade.client.jersey.model.DataValueData;
import io.swagger.trade.client.jersey.model.DataValueWithLinks;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by hahnml on 31.01.2017.
 */
public class TraDEClientServerIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

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

        dvApiInstance = new DataValueApi();
        dvApiInstance.getApiClient().setBasePath("http://localhost:8080/api");
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

            assertEquals(400, e.getCode());
        }
    }

    @AfterClass
    public static void destroy() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection("dataCollection").drop();
        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
