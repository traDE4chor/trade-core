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
import io.swagger.trade.client.jersey.api.DataValueApi;
import io.swagger.trade.client.jersey.model.DataValue;
import io.swagger.trade.client.jersey.model.DataValueRequest;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 01.02.2017.
 */
public class DataValueTestHelper {

    private static DataValueApi dvApiInstance;

    private String idOfDataValue1 = "";
    private String idOfDataValue2 = "";
    private String idOfDataValue3 = "";

    public DataValueTestHelper(DataValueApi apiInstance) {
        dvApiInstance = apiInstance;
    }

    public void addDataValues() {
        try {
            // Add a new data value
            DataValueRequest value1 = new DataValueRequest();

            value1.setName("inputData");
            value1.setCreatedBy("hahnml");
            value1.setType("binary");
            value1.setContentType("text/plain");

            DataValue result1 = dvApiInstance.addDataValue(value1);
            idOfDataValue1 = result1.getId();

            assertNotNull(result1);
            assertNotNull(result1.getCreated());
            assertNotNull(result1.getHref());
            assertNotNull(result1.getId());
            assertNotNull(result1.getLastModified());
            assertNotNull(result1.getName());
            assertNotNull(result1.getSize());
            assertNotNull(result1.getStatus());

            // Add another data value
            DataValueRequest value2 = new DataValueRequest();

            value2.setName("video");
            value2.setCreatedBy("anotherUser");
            value2.setType("binary");
            value2.setContentType("video/mp4");

            DataValue result2 = dvApiInstance.addDataValue(value2);
            idOfDataValue2 = result2.getId();

            assertNotNull(result2);
            assertNotNull(result2.getCreated());
            assertNotNull(result2.getHref());
            assertNotNull(result2.getId());
            assertNotNull(result2.getLastModified());
            assertNotNull(result2.getName());
            assertNotNull(result2.getSize());
            assertNotNull(result2.getStatus());

            // Add another data value
            DataValueRequest value3 = new DataValueRequest();

            value3.setName("simpleStringValue");
            value3.setCreatedBy("hahnml");
            value3.setType("string");
            value3.setContentType("text/plain");

            DataValue result3 = dvApiInstance.addDataValue(value3);
            idOfDataValue3 = result3.getId();
            assertNotNull(result3);
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#addDataValue");
            e.printStackTrace();
        }
    }

    public void getDataValues() {
        try {
            List<DataValue> result = dvApiInstance.getDataValuesDirectly(null, null);
            assertNotNull(result);
            assertEquals(3, result.size());

            result = dvApiInstance.getDataValuesDirectly(1, null);
            assertNotNull(result);
            assertEquals(1, result.size());

            result = dvApiInstance.getDataValuesDirectly(null, "created");
            assertNotNull(result);
            assertEquals(3, result.size());

            result = dvApiInstance.getDataValuesDirectly(200, "nonExistingState");
            assertNotNull(result);
            assertEquals(0, result.size());
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#getDataValuesDirectly");
            e.printStackTrace();
        }
    }

    public void getDataValue() {
        try {
            DataValue result = dvApiInstance.getDataValueDirectly(idOfDataValue1);
            assertNotNull(result);
            assertEquals(idOfDataValue1, result.getId());

            result = dvApiInstance.getDataValueDirectly(idOfDataValue2);
            assertNotNull(result);
            assertEquals(idOfDataValue2, result.getId());

            result = dvApiInstance.getDataValueDirectly(idOfDataValue3);
            assertNotNull(result);
            assertEquals(idOfDataValue3, result.getId());

            result = dvApiInstance.getDataValueDirectly("aNonExistingId");
            assertNull(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#getDataValueDirectly");
            e.printStackTrace();
        }
    }

    public void pushDataValues() {
        try {
            OffsetDateTime oldTime = dvApiInstance.getDataValueDirectly(idOfDataValue1).getLastModified();
            byte[] data1 = getData("data.dat");

            // Push data to first data value
            dvApiInstance.pushDataValue(idOfDataValue1, new Long(data1.length), data1);

            OffsetDateTime newTime = dvApiInstance.getDataValueDirectly(idOfDataValue1).getLastModified();
            // Check if the lastModified property is updated
            assertNotEquals(oldTime, newTime);

            byte[] data2 = getData("video.mp4");

            // Push data to second data value
            dvApiInstance.pushDataValue(idOfDataValue2, new Long(data2.length), data2);

            // Push data to third data value
            String data = "test value";
            dvApiInstance.pushDataValue(idOfDataValue3, new Long(data.length()), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#pushDataValue");
            e.printStackTrace();
        }
    }

    public void pullDataValues() {
        try {
            // Pull data from first data value
            byte[] result1 = dvApiInstance.pullDataValue(idOfDataValue1);
            assertNotNull(result1);

            // Pull data from second data value
            byte[] result2 = dvApiInstance.pullDataValue(idOfDataValue2);
            assertNotNull(result2);

            // Pull data from third data value
            byte[] result3 = dvApiInstance.pullDataValue(idOfDataValue3);
            assertNotNull(result3);
            assertEquals("test value", new String(result3));
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#pullDataValue");
            e.printStackTrace();
        }
    }

    public void deleteDataValues() {
        try {
            // Delete first data value
            DataValue result = dvApiInstance.deleteDataValue(idOfDataValue1);
            assertNotNull(result);
            assertEquals(idOfDataValue1, result.getId());

            // Delete second data value
            result = dvApiInstance.deleteDataValue(idOfDataValue2);
            assertNotNull(result);
            assertEquals(idOfDataValue2, result.getId());

            // Delete third data value
            result = dvApiInstance.deleteDataValue(idOfDataValue3);
            assertNotNull(result);
            assertEquals(idOfDataValue3, result.getId());
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#deleteDataValue");
            e.printStackTrace();
        }
    }

    private byte[] getData(String fileName) throws IOException {
        InputStream in = getClass().getResourceAsStream("/" + fileName);
        byte[] data = null;

        try {
            data = IOUtils.toByteArray(in);
        } finally {
            in.close();
        }

        return data;
    }
}
