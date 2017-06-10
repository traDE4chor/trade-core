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
import io.swagger.trade.client.jersey.api.NotificationApi;
import io.swagger.trade.client.jersey.model.*;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hahnml on 10.05.2017.
 */
public class NotificationHttpIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static NotificationApi notificationApi;

    private static DataValueApi dvApiInstance;

    private static Server httpServer;

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

        client.setBasePath("http://localhost:8080/api");

        notificationApi = new NotificationApi(client);

        dvApiInstance = new DataValueApi(client);

        // Create a new embedded HTTP server which consumes the HTTP notifications
        httpServer = new Server(8085);

        // Create a handler to check the notification message send to the specified HTTP endpoint
        Handler handler = new AbstractHandler() {
            @Override
            public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
                handleHttpRequest(target, request, response);
            }
        };

        httpServer.setHandler(handler);

        // Start Server
        httpServer.start();
    }

    @Test
    public void createAndTriggerHttpNotificationTest() throws Exception {
        String destinationName = "dataValueInitializedQueue";

        NotificationData test = new NotificationData();
        test.setName("notifyDataValueInitialized");
        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("http");

        NotifierServiceParameterArray params = new NotifierServiceParameterArray();

        NotifierServiceParameter param = new NotifierServiceParameter();
        param.setParameterName("hostname");
        param.setValue("localhost");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("port");
        param.setValue("8085");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("resourceUri");
        param.setValue("/testResource");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("messageFormat");
        param.setValue("XML");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("SOAPAction");
        param.setValue("processRequest");
        params.add(param);

        test.setNotifierParameterValues(params);

        ResourceEventFilterArray filters = new ResourceEventFilterArray();

        ResourceEventFilter filter = new ResourceEventFilter();
        filter.setFilterName("ModelClass");
        filter.setFilterValue("class org.trade.core.model.data.DataValue");
        filters.add(filter);

        filter = new ResourceEventFilter();
        filter.setFilterName("NewState");
        filter.setFilterValue("DELETED");
        filters.add(filter);

        test.setResourceFilters(filters);

        try {
            // Add the notification
            Notification response = notificationApi.addNotification(test);
            assertNotNull(response);

            // Trigger the notification by adding a new data value and deleting it again
            DataValueData request = new DataValueData();

            request.setName("inputData");
            request.setCreatedBy("hahnml");
            request.setType("binary");
            request.setContentType("text/plain");

            DataValue result = dvApiInstance.addDataValue(request);
            assertNotNull(result);
            dvApiInstance.deleteDataValue(result.getId());

            notificationApi.deleteNotification(response.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    private static void handleHttpRequest(String target, HttpServletRequest request, HttpServletResponse response) {
        try {
            assertEquals("/testResource", target);

            ServletInputStream reqStream = request.getInputStream();
            int contentLength = request.getContentLength();

            assertTrue(contentLength > 0);

            String soapActionHeader = request.getHeader("SOAPAction");
            assertNotNull(soapActionHeader);
            assertEquals("processRequest", soapActionHeader);

            byte[] buffer = new byte[contentLength];
            reqStream.read(buffer);
            reqStream.close();

            String message = new String(buffer);

            assertNotNull(message);

            System.out.println("HTTP Notification Message: " + message);

            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            ((Request)request).setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Request) request).setHandled(true);
    }

    @AfterClass
    public static void destroy() throws Exception {
        // Stop the server
        httpServer.stop();

        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_MODEL__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION).drop();

        dataStore.getCollection("dataValues").drop();
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
