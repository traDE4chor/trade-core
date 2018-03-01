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
import io.swagger.trade.client.jersey.api.DataValueApi;
import io.swagger.trade.client.jersey.model.DataValue;
import io.swagger.trade.client.jersey.model.DataValueData;
import io.swagger.trade.client.jersey.model.DataValueWithLinks;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 31.01.2017.
 */
public class DataValueApiIT {

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

        ApiClient client = new ApiClient();

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client.setBasePath("http://127.0.0.1:8080/api");

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

            TestUtils.printLinkArray(updated.getLinks());

            // Check changed properties
            assertNotEquals(result.getLastModified(), updated.getDataValue().getLastModified());
            assertNotEquals(result.getName(), updated.getDataValue().getName());
            assertNotEquals(result.getContentType(), updated.getDataValue().getContentType());
            assertNotEquals(result.getType(), updated.getDataValue().getType());

            dvApiInstance.deleteDataValue(result.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void uploadAndDownloadDataOfDataValueTest() {
        // Add a new data value
        DataValueData request = new DataValueData();

        request.setName("dataValue");
        request.setCreatedBy("hahnml");
        request.setType("binary");
        request.setContentType("text/plain");

        try {
            DataValue dataValue = dvApiInstance.addDataValue(request);

            // Create a String with length = 1000 * 2**13 (8MB of data)
            String value = RandomStringUtils.randomAlphabetic(8192000);
            int length = value.getBytes().length;
            dvApiInstance.pushDataValue(dataValue.getId(), value.getBytes(), false, (long) length);

            byte[] resultData = dvApiInstance.pullDataValue(dataValue.getId());
            assertNotNull(resultData);
            assertFalse(resultData.length == 0);

            assertEquals(length, resultData.length);
            assertEquals(value, new String(resultData));

            dvApiInstance.deleteDataValue(dataValue.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void uploadDataThroughALinkTest() {
        // Add a new data value
        DataValueData request = new DataValueData();

        request.setName("dataValue");
        request.setCreatedBy("hahnml");
        request.setType("binary");
        request.setContentType("text/plain");

        try {
            DataValue dataValue = dvApiInstance.addDataValue(request);

            // Use a link to the Swagger API YAML file provided through the server to test data resolution through links
            String link = "http://127.0.0.1:8080/docs/swagger.yaml";
            dvApiInstance.pushDataValue(dataValue.getId(), link.getBytes(), true, null);

            byte[] resultData = dvApiInstance.pullDataValue(dataValue.getId());
            String result = new String(resultData);
            assertNotNull(resultData);
            assertFalse(resultData.length == 0);

            assertTrue(result.startsWith("swagger: '2.0'"));

            dvApiInstance.deleteDataValue(dataValue.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_VALUE__DATA_COLLECTION).drop();

        dataStore.getCollection("dataValues").drop();
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
