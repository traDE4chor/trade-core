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
import io.swagger.trade.client.jersey.api.DataDependencyGraphApi;
import io.swagger.trade.client.jersey.api.DataValueApi;
import io.swagger.trade.client.jersey.model.DataDependencyGraphData;
import io.swagger.trade.client.jersey.model.DataDependencyGraphWithLinks;
import io.swagger.trade.client.jersey.model.DataValue;
import io.swagger.trade.client.jersey.model.DataValueData;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 02.02.2017.
 */
public class TestDataUploadManually {

    public static void main(String[] args) {
        testDataValueApi();
        //createAndUploadDataDependencyGraphTest();
    }

    private static void createAndUploadDataDependencyGraphTest() {
        String entity = "hahnml";
        String name = "TestDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        try {
            DataDependencyGraphApi ddgApiInstance = new DataDependencyGraphApi();
            ddgApiInstance.getApiClient().setBasePath("http://127.0.0.1:8081/api");

            DataDependencyGraphWithLinks ddgResponse = ddgApiInstance.addDataDependencyGraph(ddg);

            assertNotNull(ddgResponse);
            assertNotNull(ddgResponse.getDataDependencyGraph());
            assertNotNull(ddgResponse.getDataDependencyGraph().getId());
            assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
            assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

            String graphId = ddgResponse.getDataDependencyGraph().getId();

            byte[] graph = TestUtils.getData("opalData.trade");

            // Try to upload and compile a serialized DDG
            ddgApiInstance.uploadGraphModel(graphId, Long.valueOf(graph.length), graph);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void testDataValueApi() {
        DataValueApi dvApiInstance = new DataValueApi();
        dvApiInstance.getApiClient().setBasePath("http://127.0.0.1:8081/api");

        try {
            // Add a new data value
            DataValueData value = new DataValueData();

            value.setName("inputData");
            value.setCreatedBy("hahnml");
            value.setType("binary");
            value.setContentType("text/plain");

            DataValue result = dvApiInstance.addDataValue(value);
            String idOfDataValue = result.getId();

            byte[] data = TestUtils.getData("video.mp4");

            // Push data to second data value
            dvApiInstance.pushDataValue(idOfDataValue, data, null, new Long(data.length));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApiException e) {
            System.err.println("Exception when calling DataValueApi#addDataValue");
            e.printStackTrace();
        }
    }
}
