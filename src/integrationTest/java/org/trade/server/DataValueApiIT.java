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

import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.model.DataValue;
import io.swagger.trade.client.jersey.model.DataValueArrayWithLinks;
import io.swagger.trade.client.jersey.model.DataValueData;
import io.swagger.trade.client.jersey.model.DataValueWithLinks;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 31.01.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataValueApiIT {

    private static IntegrationTestEnvironment env;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        env = new IntegrationTestEnvironment();

        env.setupEnvironment(true);
    }

    @Test
    public void dataValueApiRoundTripTest() throws Exception {
        DataValueTestHelper helper = new DataValueTestHelper(env.getDataValueApi());
        helper.addDataValues();

        helper.getDataValues();

        helper.getDataValue();

        helper.pushDataValues();

        helper.pullDataValues();

        helper.deleteDataValues();

        DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
        assertEquals(0, values.getDataValues().size());
    }

    @Test
    public void shouldRejectAddDataValueRequestTest() {
        // Try to add a new data value without 'createdBy' value
        DataValueData request = new DataValueData();

        request.setName("inputData");
        request.setType("binary");
        request.setContentType("text/plain");

        try {
            DataValue result3 = env.getDataValueApi().addDataValue(request);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void shouldRejectDeleteDataValueRequestTest() {
        try {
            // Try to delete not existing data value
            env.getDataValueApi().deleteDataValue("Not-Existing-Id");
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
            DataValue result = env.getDataValueApi().addDataValue(request);

            DataValue updateRequest = new DataValue();
            updateRequest.setName("changedName");
            updateRequest.setContentType("changedContentType");
            updateRequest.setType("changedType");

            env.getDataValueApi().updateDataValueDirectly(result.getId(), updateRequest);

            DataValueWithLinks updated = env.getDataValueApi().getDataValueDirectly(result.getId());
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

            env.getDataValueApi().deleteDataValue(result.getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());
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
            DataValue dataValue = env.getDataValueApi().addDataValue(request);

            // Create a String with length = 1000 * 2**13 (8MB of data)
            String value = RandomStringUtils.randomAlphabetic(8192000);
            int length = value.getBytes().length;
            env.getDataValueApi().pushDataValue(dataValue.getId(), value.getBytes(), false, (long) length);

            byte[] resultData = env.getDataValueApi().pullDataValue(dataValue.getId());
            assertNotNull(resultData);
            assertNotEquals(0, resultData.length);

            assertEquals(length, resultData.length);
            assertEquals(value, new String(resultData));

            env.getDataValueApi().deleteDataValue(dataValue.getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());
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
            DataValue dataValue = env.getDataValueApi().addDataValue(request);

            int serverPort = env.getProperties().getHttpServerPort();

            // Use a link to the Swagger API YAML file provided through the server to test data resolution through links
            String link = "http://127.0.0.1:" + serverPort + "/docs/swagger.yaml";
            env.getDataValueApi().pushDataValue(dataValue.getId(), link.getBytes(), true, null);

            byte[] resultData = env.getDataValueApi().pullDataValue(dataValue.getId());
            String result = new String(resultData);
            assertNotNull(resultData);
            assertNotEquals(0, resultData.length);

            assertTrue(result.startsWith("swagger: '2.0'"));

            env.getDataValueApi().deleteDataValue(dataValue.getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        env.destroyEnvironment();
        env = null;
    }

}
