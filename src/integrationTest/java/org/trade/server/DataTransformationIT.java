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

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import io.swagger.trade.client.jersey.ApiClient;
import io.swagger.trade.client.jersey.api.*;
import org.apache.camel.test.AvailablePortFinder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by hahnml on 14.02.2018.
 */
public class DataTransformationIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static NotificationApi notificationApi;

    private static DataValueApi dvApiInstance;

    private static DataObjectApi doApiInstance;

    private static DataObjectInstanceApi doInstApiInstance;

    private static DataElementApi deApiInstance;

    private static Server hdtAppsServer;

    private static int hdtAppsServerPort;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
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

        notificationApi = new NotificationApi(client);

        dvApiInstance = new DataValueApi(client);

        doApiInstance = new DataObjectApi(client);

        doInstApiInstance = new DataObjectInstanceApi(client);

        deApiInstance = new DataElementApi(client);

        // Create a new embedded HTTP server which consumes the requests sent to the HDT API
        hdtAppsServerPort = AvailablePortFinder.getNextAvailable();
        hdtAppsServer = new Server(new InetSocketAddress("0.0.0.0", hdtAppsServerPort));

        // Set the resulting HDT endpoint in the properties
        properties.setProperty(TraDEProperties.PROPERTY_HDT_APP_FRAMEWORK_URL, "http://127.0.0.1:" + String.valueOf
                (port));

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
        // TODO: 14.02.2018 Implement a test case
        // 1. Upload a DDG with data transformations
        // 2. Prepare an instance (data object instance, data element instances)
        // 3. Associate data to the instance so that one of the modeled data transformations is triggered
        // 4. Check if the result data is uploaded to the specified target data value

        // TODO: 14.02.2018 Add more test cases which target different aspects of the transformation functionality, e.g., retriggering of logic by changing data values, etc.
    }

    private static void handleHttpRequest(String target, HttpServletRequest request, HttpServletResponse response) {
        try {
            ServletInputStream reqStream = request.getInputStream();
            int contentLength = request.getContentLength();

            byte[] buffer = new byte[contentLength];
            reqStream.read(buffer);
            reqStream.close();

            String message = new String(buffer);

            // TODO: 14.02.2018 Validate the content of the request message 

            // TODO: 14.02.2018 Mimic the behavior of the transformation app by uploading some result data to the specified result data value URL

            System.out.println("Data Transformation Request: " + message);

            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            ((Request) request).setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Request) request).setHandled(true);
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

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
