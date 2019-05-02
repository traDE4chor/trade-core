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
import io.swagger.trade.client.jersey.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * Integration tests for data dependency graph API methods
 * <p>
 * Created by hahnml on 31.01.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataDependencyGraphApiIT {

    private static IntegrationTestEnvironment env;

    @BeforeClass
    public static void setupEnvironment() {
        env = new IntegrationTestEnvironment();

        env.setupEnvironment(true);
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

            DataDependencyGraphWithLinks resp = env.getDdgApi().addDataDependencyGraph(ddg);
            graphIds[i] = resp.getDataDependencyGraph().getId();
        }

        // Upload a graph model to the first DDG to enable namespace-based queries
        byte[] graph = TestUtils.getData("opalData.trade");
        // Try to upload and compile a serialized DDG
        env.getDdgApi().uploadGraphModel(graphIds[0], Long.valueOf(graph.length), graph);

        DataDependencyGraphArrayWithLinks result = env.getDdgApi().getDataDependencyGraphs(null, null, null, null,
                null);
        assertEquals(3, result.getDataDependencyGraphs().size());

        result = env.getDdgApi().getDataDependencyGraphs(null, null, null, null,
                "hahnml");
        assertEquals(2, result.getDataDependencyGraphs().size());

        result = env.getDdgApi().getDataDependencyGraphs(null, null, null, "TestDataDependencyGraph",
                "hahnml");
        assertEquals(1, result.getDataDependencyGraphs().size());

        result = env.getDdgApi().getDataDependencyGraphs(null, null, "http://de.uni-stuttgart.iaas/opalChor",
                null,
                null);
        assertEquals(1, result.getDataDependencyGraphs().size());

        // Delete the three DDGs again
        for (int i = 0; i < entity.length; i++) {
            env.getDdgApi().deleteDataDependencyGraph(graphIds[i]);
        }

        DataDependencyGraphArrayWithLinks ddgs = env.getDdgApi().getDataDependencyGraphs(null, null, null, null, null);
        assertEquals(0, ddgs.getDataDependencyGraphs().size());

        DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
        assertEquals(0, objects.getDataObjects().size());
    }

    @Test
    public void createAndUploadDataDependencyGraphTest() throws Exception {
        String entity = "hahnml";
        String name = "TestDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        DataDependencyGraphWithLinks ddgResponse = env.getDdgApi().addDataDependencyGraph(ddg);

        assertNotNull(ddgResponse);
        assertNotNull(ddgResponse.getDataDependencyGraph());
        assertNotNull(ddgResponse.getDataDependencyGraph().getId());
        assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
        assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

        TestUtils.printLinkArray(ddgResponse.getLinks());

        String graphId = ddgResponse.getDataDependencyGraph().getId();

        byte[] graph = TestUtils.getData("opalData.trade");

        // Try to upload and compile a serialized DDG
        env.getDdgApi().uploadGraphModel(graphId, Long.valueOf(graph.length), graph);

        // Links should be different, therefore objects should be not equal
        DataDependencyGraphWithLinks ddgResponse2 = env.getDdgApi().getDataDependencyGraphDirectly(graphId);
        assertNotEquals(ddgResponse, ddgResponse2);

        // Try to query the resulting data objects and data elements
        DataModelWithLinks dataModelResp = env.getDataModelApi().getDataModel(graphId);
        String dataModelId = dataModelResp.getDataModel().getId();

        TestUtils.printLinkArray(dataModelResp.getLinks());

        DataObjectArrayWithLinks dataObjects = env.getDataObjectApi().getDataObjects(dataModelId, null, null);
        assertEquals(4, dataObjects.getDataObjects().size());

        TestUtils.printLinkArray(dataObjects.getLinks());

        int[] sizes = {3, 2, 2, 2};
        int index = 0;
        for (DataObjectWithLinks dataObject : dataObjects.getDataObjects()) {
            DataElementArrayWithLinks dataElements = env.getDataElementApi().getDataElements(dataObject
                            .getDataObject().getId(), null, null,
                    null, null);
            assertEquals(sizes[index], dataElements.getDataElements().size());
            index++;
        }

        byte[] graphData = env.getDdgApi().downloadGraphModel(graphId);
        assertEquals(new String(graph, StandardCharsets.UTF_8), new String(graphData, StandardCharsets.UTF_8));

        // Delete the DDG
        env.getDdgApi().deleteDataDependencyGraph(graphId);

        // Check if the DDG is deleted
        DataDependencyGraphArrayWithLinks result = env.getDdgApi().getDataDependencyGraphs(null, null, null, null,
                null);
        assertEquals(0, result.getDataDependencyGraphs().size());

        // And all related resources are also deleted
        // Data Model
        DataModelArrayWithLinks models = env.getDataModelApi().getDataModels(null, null, null, null, null);
        assertEquals(0, models.getDataModels().size());

        // Data Objects
        DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null,
                null);
        assertEquals(0, objects.getDataObjects().size());

        // Data Elements
        DataElementArrayWithLinks elements = env.getDataElementApi().getAllDataElements(null, null, null, null);
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
            DataDependencyGraphWithLinks ddgResponse = env.getDdgApi().addDataDependencyGraph(ddg);

            assertNotNull(ddgResponse);
            assertNotNull(ddgResponse.getDataDependencyGraph());
            assertNotNull(ddgResponse.getDataDependencyGraph().getId());
            assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
            assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

            id = ddgResponse.getDataDependencyGraph().getId();

            byte[] graph = TestUtils.getData("opalDataFailure.trade");

            // Try to upload and compile a serialized DDG that is not valid
            env.getDdgApi().uploadGraphModel(id, Long.valueOf(graph.length), graph);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(500, e.getCode());

            if (id != null) {
                try {
                    env.getDdgApi().deleteDataDependencyGraph(id);

                    // Check if the DDG is deleted
                    DataDependencyGraphArrayWithLinks result = env.getDdgApi().getDataDependencyGraphs(null, null, null, null,
                            null);
                    assertEquals(0, result.getDataDependencyGraphs().size());

                    DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
                    assertEquals(0, objects.getDataObjects().size());
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
        env.destroyEnvironment();
        env = null;
    }
}
