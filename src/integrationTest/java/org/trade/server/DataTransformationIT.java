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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import io.swagger.hdtapps.client.jersey.JSON;
import io.swagger.hdtapps.client.jersey.model.TransformationRequest;
import io.swagger.trade.client.jersey.ApiClient;
import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.*;
import io.swagger.trade.client.jersey.model.*;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 14.02.2018.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataTransformationIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static DataDependencyGraphApi ddgApi;

    private static DataModelApi dataModelApi;

    private static DataObjectApi dataObjectApi;

    private static DataObjectInstanceApi dataObjectInstanceApi;

    private static DataElementInstanceApi dataElementInstanceApi;

    private static DataValueApi dataValueApi;

    private static Server hdtAppsServer;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
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

        ddgApi = new DataDependencyGraphApi(client);
        dataModelApi = new DataModelApi(client);
        dataObjectApi = new DataObjectApi(client);
        dataObjectInstanceApi = new DataObjectInstanceApi(client);
        dataElementInstanceApi = new DataElementInstanceApi(client);
        dataValueApi = new DataValueApi(client);

        // Create a new embedded HTTP server which consumes the requests sent to the HDT API
        // Here we also have to use the default port since the HDT endpoint is read from the properties file
        // in CamelDataTransformationManager
        hdtAppsServer = new Server(new InetSocketAddress("0.0.0.0", 8082));

        // Create a handler to check the transformation requests send to the HDT framework API
        Handler handler = new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                handleHttpRequest(target, request, response);
            }
        };

        hdtAppsServer.setHandler(handler);

        // Start Server
        hdtAppsServer.start();
    }

    @Test
    public void triggerDataTransformationTest() throws Exception {
        // 1. Upload a DDG with data transformations
        String entity = "hahnml";
        String name = "DataTransformationDataDependencyGraph";

        DataDependencyGraphData ddg = new DataDependencyGraphData();
        ddg.setEntity(entity);
        ddg.setName(name);

        DataDependencyGraphWithLinks ddgResponse = ddgApi.addDataDependencyGraph(ddg);

        assertNotNull(ddgResponse);
        assertNotNull(ddgResponse.getDataDependencyGraph());
        assertNotNull(ddgResponse.getDataDependencyGraph().getId());
        assertEquals(entity, ddgResponse.getDataDependencyGraph().getEntity());
        assertEquals(name, ddgResponse.getDataDependencyGraph().getName());

        String graphId = ddgResponse.getDataDependencyGraph().getId();

        byte[] graph = TestUtils.getData("opalDataTransformation.trade");

        // Try to upload and compile a serialized DDG
        ddgApi.uploadGraphModel(graphId, Long.valueOf(graph.length), graph);

        // Query the data dependency graph again to check if all data transformations are compiled as expected
        DataDependencyGraphWithLinks ddgResponseAfterCompile = ddgApi.getDataDependencyGraphDirectly(graphId);
        // Check if the DDG contains two transformations
        assertEquals(2, ddgResponseAfterCompile.getDataDependencyGraph().getTransformations().size());
        // Check if both transformations specify together two parameters (actually only one of the transformations
        // contains both parameters)
        assertEquals(2, ddgResponseAfterCompile.getDataDependencyGraph().getTransformations().stream().map
                (DataTransformation::getTransformerParameters).filter(params -> params != null).mapToInt(List::size)
                .sum());

        // 2. Prepare an instance (data object instance, data element instances)
        // Resolve the required data object
        DataObjectWithLinks dataObject = resolveDataObject("http://de.uni-stuttgart.iaas/opalChor",
                "OpalSimulationTransformationChoreography", "simResults");
        assertNotNull(dataObject);

        // Instantiate the data object
        String dObjId = dataObject.getDataObject().getId();

        CorrelationPropertyArray correlationProperties = new CorrelationPropertyArray();
        correlationProperties.add(new CorrelationProperty().key("test").value("value"));

        DataObjectInstanceData instanceData = new DataObjectInstanceData();
        instanceData.setCreatedBy("test");
        instanceData.setCorrelationProperties(correlationProperties);

        DataObjectInstanceWithLinks dObjInstance = dataObjectInstanceApi.addDataObjectInstance(dObjId,
                instanceData);
        assertNotNull(dObjInstance);
        assertNotNull(dObjInstance.getInstance());

        String dObjInstanceId = dObjInstance.getInstance().getId();

        DataElementInstanceWithLinks dElementInstance = dataElementInstanceApi
                .getDataElementInstanceByDataElementName(
                        dObjInstanceId, "snapshots[]");
        assertNotNull(dElementInstance);
        assertNotNull(dElementInstance.getInstance());

        // 3. Associate data to the instance so that one of the modeled data transformations is triggered
        // Create and associate a new data value to the data element instance
        DataValue dataValueData = new DataValue();

        dataValueData.setType("binary");
        dataValueData.setContentType("text/plain");
        dataValueData.setName("testDataValue");
        dataValueData.setCreatedBy("test");

        DataValueWithLinks value = dataValueApi.associateDataValueToDataElementInstance(dElementInstance.getInstance()
                .getId(), dataValueData);
        assertNotNull(value);
        assertNotNull(value.getDataValue());

        DataValueArrayWithLinks values = dataValueApi.getDataValuesDirectly(null, null, null, null);
        assertNotNull(values.getDataValues());
        int size = values.getDataValues().size();

        // Upload data to the data value
        dataValueApi.pushDataValue(value.getDataValue().getId(), "TEST-DATA".getBytes(), false,
                9L);

        // Wait some time until the transformation results are stored in a new data value
        Thread.sleep(60000);

        values = dataValueApi.getDataValuesDirectly(null, null, null, null);
        assertNotNull(values.getDataValues());
        assertEquals(size + 1, values.getDataValues().size());

        // Remove the associations between the data values and the data element instances and delete the data values
        for (DataValueWithLinks dataValue : values.getDataValues()) {
            DataElementInstanceArrayWithLinks elementInstances = dataElementInstanceApi
                    .getDataElementInstancesUsingDataValue(dataValue.getDataValue().getId(), null,
                            null);

            // Remove all associations
            for (DataElementInstanceWithLinks elmInstance : elementInstances.getInstances()) {
                dataValueApi.removeDataValueFromDataElementInstance(elmInstance.getInstance().getId(),
                        dataValue.getDataValue().getId());
            }

            // Delete the data object
            dataValueApi.deleteDataValue(dataValue.getDataValue().getId());
        }
    }

    // TODO: 14.02.2018 Add more test cases which target different aspects of the transformation functionality, e.g.,
    // re-triggering of logic by changing data values, etc.

    private static void handleHttpRequest(String target, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (target.equals("/transformations")) {

                String respString = "[]";

                if (request.getParameter("qname") != null && request.getParameter("qname").equals
                        ("opalSnapshotArray2mpg")) {
                    // Reply an available transformation app
                    respString = "[\n" +
                            "  {\n" +
                            "    \"appID\": \"michaelhahn_mlhn_opal3danimatedloopfile_1.0.0\",\n" +
                            "    \"inputFileSets\": [\n" +
                            "      {\n" +
                            "        \"alias\": \"$inputsShapshots\",\n" +
                            "        \"fileSetSize\": \"$numberOfFilesToAnimate\",\n" +
                            "        \"format\": \"dat\",\n" +
                            "        \"name\": \"snapshots\",\n" +
                            "        \"isOptional\": false,\n" +
                            "        \"requiredPath\": \"{r}/\",\n" +
                            "        \"schema\": \"\"\n" +
                            "      }\n" +
                            "    ],\n" +
                            "    \"inputFileSetsCount\": 1,\n" +
                            "    \"inputFiles\": [],\n" +
                            "    \"inputFilesCount\": 0,\n" +
                            "    \"inputNumParamCount\": 1,\n" +
                            "    \"inputOptParamCount\": 0,\n" +
                            "    \"inputParamCount\": 2,\n" +
                            "    \"inputParams\": [\n" +
                            "      {\n" +
                            "        \"alias\": \"$prefixName\",\n" +
                            "        \"name\": \"prefixName\",\n" +
                            "        \"isOptional\": false,\n" +
                            "        \"type\": \"string\",\n" +
                            "        \"value\": \"\"\n" +
                            "      },\n" +
                            "      {\n" +
                            "        \"alias\": \"$numberOfFilesToAnimate\",\n" +
                            "        \"name\": \"numberOfFilesToAnimate\",\n" +
                            "        \"isOptional\": false,\n" +
                            "        \"type\": \"integer\",\n" +
                            "        \"value\": \"\"\n" +
                            "      }\n" +
                            "    ],\n" +
                            "    \"inputStrParamCount\": 1,\n" +
                            "    \"name\": \"snapshots-to-video\",\n" +
                            "    \"outputFiles\": [\n" +
                            "      {\n" +
                            "        \"accessPath\": \"{r}/\",\n" +
                            "        \"alias\": \"$outputVideo\",\n" +
                            "        \"format\": \"mp4\",\n" +
                            "        \"name\": \"opalClusterSnapshots\",\n" +
                            "        \"schema\": \"\"\n" +
                            "      }\n" +
                            "    ],\n" +
                            "    \"outputFilesCount\": 1,\n" +
                            "    \"providers\": [\n" +
                            "      {\n" +
                            "        \"pkgID\": \"sha256:91917bddfefe4f5e8969a2b34071978af641d6bf0e6a1487742ad23aad87b0bd\",\n" +
                            "        \"providerQName\": \"default\"\n" +
                            "      }\n" +
                            "    ],\n" +
                            "    \"qname\": \"opalSnapshotArray2mpg\",\n" +
                            "    \"relaxedSignature\": \"[pi:2][fsi:1:dat][fo:1:mp4]\",\n" +
                            "    \"strictSignature\": \"[pi:2][fsi:1:dat][fo:1:mp4]\",\n" +
                            "    \"transformationID\": \"5a73223b0cbf23000b74d9f9\"\n" +
                            "  }\n" +
                            "]";


                }

                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json");
                response.getWriter().write(respString);
            } else if (target.equals("/tasks")) {
                ServletInputStream reqStream = request.getInputStream();
                int contentLength = request.getContentLength();

                byte[] buffer = new byte[contentLength];
                reqStream.read(buffer);
                reqStream.close();

                String message = new String(buffer);

                System.out.println("Data Transformation Request: " + message);

                JSON json = new JSON();
                ObjectMapper mapper = json.getContext(null);
                TransformationRequest transfRequest = mapper.readValue(message, TransformationRequest.class);

                // Validate the structure of the request message
                assertEquals("michaelhahn_mlhn_opal3danimatedloopfile_1.0.0", transfRequest.getAppID());
                assertEquals("5a73223b0cbf23000b74d9f9", transfRequest.getTransformationID());
                assertEquals(1, transfRequest.getInputFileSets().size());
                assertEquals(1, transfRequest.getInputFileSets().get(0).getLinksToFiles().size());
                assertEquals(2, transfRequest.getInputParams().size());

                // Mimic the behavior of the transformation app by uploading some result data to the specified result data value URL
                String responseUrl = transfRequest.getResultsEndpoint();
                URL url = new URL(responseUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setDoOutput(true);

                byte[] out = "transformationResult".getBytes(StandardCharsets.UTF_8);
                int length = out.length;

                con.setFixedLengthStreamingMode(length);
                con.setRequestProperty("Content-Type", "application/octet-stream");
                con.connect();
                try (OutputStream os = con.getOutputStream()) {
                    os.write(out);
                }

                // Get the result
                int status = con.getResponseCode();

                assertEquals(204, status);

                con.disconnect();

                // Send a response to the client
                String respString = "{\n" +
                        "  \"taskID\": \"someID\",\n" +
                        "  \"state\": \"completed\"\n" +
                        "}";

                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(respString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Request) request).setHandled(true);
    }

    private DataObjectWithLinks resolveDataObject(String dataModelNamespace, String dataModelName, String dataObjectName) throws ApiException {
        DataObjectWithLinks result = null;

        DataModelArrayWithLinks dataModels = dataModelApi.getDataModels(null,
                null, dataModelNamespace,
                dataModelName, null);

        if (dataModels.getDataModels() != null
                && !dataModels.getDataModels().isEmpty()) {

            Iterator<DataModelWithLinks> iter = dataModels.getDataModels()
                    .iterator();

            while (result == null && iter.hasNext()) {
                DataModelWithLinks model = iter.next();

                // Retrieve the list of data objects which should contain
                // the searched one
                DataObjectArrayWithLinks list = dataObjectApi.getDataObjects(
                        model.getDataModel().getId(), null, null);
                Iterator<DataObjectWithLinks> iterObj = list.getDataObjects()
                        .iterator();
                while (result == null && iterObj.hasNext()) {
                    DataObjectWithLinks cur = iterObj.next();

                    // Check if the data object is the one we are looking
                    // for
                    if (cur.getDataObject().getName()
                            .equals(dataObjectName)) {
                        result = cur;
                    }
                }
            }
        }

        return result;
    }

    @AfterClass
    public static void destroy() throws Exception {
        // Stop the server
        hdtAppsServer.stop();

        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_MODEL__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_VALUE__DATA_COLLECTION).drop();

        dataStore.getCollection("dataValues").drop();
        dataStore.getCollection("dataElements").drop();
        dataStore.getCollection("dataElementInstances").drop();
        dataStore.getCollection("dataObjects").drop();
        dataStore.getCollection("dataObjectInstances").drop();
        dataStore.getCollection("dataModels").drop();
        dataStore.getCollection("dataDependencyGraphs").drop();
        dataStore.getCollection("notifications").drop();
        dataStore.getCollection("dataTransformations").drop();

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
