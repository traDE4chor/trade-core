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

package org.trade.core;

import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.DataValueApi;
import io.swagger.trade.client.jersey.model.DataValue;
import io.swagger.trade.client.jersey.model.DataValueRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
    public void createDataValueTest() {
        try {
            DataValueRequest value1 = new DataValueRequest();
            byte[] data1 = getData("data.dat");

            value1.setCreatedBy("hahnml");
            value1.setType("binary");
            value1.setContentType("plain/text");
            //value1.setData(data1);

            DataValue result1 = dvApiInstance.addDataValue(value1);
            System.out.println(result1);

            // TODO: Push data to data value

            DataValueRequest value2 = new DataValueRequest();
            byte[] data2 = getData("video.mp4");

            value2.setCreatedBy("anotherUser");
            value2.setType("binary");
            value2.setContentType("video/mp4");
            //value2.setData(data2);

            DataValue result2 = dvApiInstance.addDataValue(value2);
            System.out.println(result1);

            // TODO: Push data to data value
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            System.err.println("Exception when calling DataElementApi#addDataElement");
            e.printStackTrace();
        }
    }

    @Test
    public void getDataValuesTest() {

    }

    @Test
    public void pullDataValuesTest() {

    }

    @AfterClass
    public static void destroy() {
        // TODO: Purge storage in future, if data is made persistent (see TraDENodeIT.destroy());

        dvApiInstance = null;

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
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
