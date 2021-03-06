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
import org.apache.camel.test.AvailablePortFinder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.*;
import org.junit.runners.MethodSorters;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 10.05.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationHttpIT {

    private static IntegrationTestEnvironment env;

    private static Server httpServer;

    private static int notificationServerPort;

    private static CountDownLatch lock;

    private static String notificationMessage;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        env = new IntegrationTestEnvironment();
        env.setupEnvironment(true);

        // Create a new embedded HTTP server which consumes the HTTP notifications
        notificationServerPort = AvailablePortFinder.getNextAvailable();
        httpServer = new Server(new InetSocketAddress("127.0.0.1", notificationServerPort));

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

    @Before
    public void createCountDownLatch() {
        lock = new CountDownLatch(1);
    }

    @Test
    public void createAndTriggerHttpNotificationTest() throws Exception {
        NotificationData test = new NotificationData();
        test.setName("notifyDataValueInitialized");
        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("http");

        NotifierServiceParameterArray params = new NotifierServiceParameterArray();

        NotifierServiceParameter param = new NotifierServiceParameter();
        param.setParameterName("hostname");
        param.setValue("127.0.0.1");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("port");
        param.setValue(String.valueOf(notificationServerPort));
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
            Notification response = env.getNotificationApi().addNotification(test);
            assertNotNull(response);

            // Trigger the notification by adding a new data value and deleting it again
            DataValueData request = new DataValueData();

            request.setName("inputData");
            request.setCreatedBy("hahnml");
            request.setType("binary");
            request.setContentType("text/plain");

            DataValue result = env.getDataValueApi().addDataValue(request);
            assertNotNull(result);
            env.getDataValueApi().deleteDataValue(result.getId());

            lock.await(2000, TimeUnit.MILLISECONDS);
            assertNotNull(notificationMessage);

            env.getNotificationApi().deleteNotification(response.getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());

            NotificationArrayWithLinks notifications = env.getNotificationApi().getNotifications(null, null, null, null);
            assertEquals(0, notifications.getNotifications().size());
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

            notificationMessage = message;
            lock.countDown();

            System.out.println("HTTP Notification Message: " + message);

            response.setContentType("text/plain");
            response.setStatus(HttpServletResponse.SC_OK);
            ((Request)request).setHandled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ((Request) request).setHandled(true);
    }

    @Test
    public void httpNotificationWithComplexResourceFilterQueryTest() throws Exception {
        NotificationData test = new NotificationData();
        test.setName("notifyDataObjectInstanceCreated");
        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAOBJECTINSTANCE);
        test.setSelectedNotifierServiceId("http");

        NotifierServiceParameterArray params = new NotifierServiceParameterArray();

        NotifierServiceParameter param = new NotifierServiceParameter();
        param.setParameterName("hostname");
        param.setValue("127.0.0.1");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("port");
        param.setValue(String.valueOf(notificationServerPort));
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
        filter.setFilterValue("class org.trade.core.model.data.instance.DataObjectInstance");
        filters.add(filter);

        filter = new ResourceEventFilter();
        filter.setFilterName("NewState");
        filter.setFilterValue("CREATED");
        filters.add(filter);

        HashMap<String, String> correlationProps = new HashMap<>();
        correlationProps.put("chorID", "1234567");

        filter = new ResourceEventFilter();
        filter.setFilterName("EventSource#CorrelationProperties");
        filter.setFilterValue(correlationProps.toString());
        filters.add(filter);

        test.setResourceFilters(filters);

        try {
            // Add the notification
            Notification response = env.getNotificationApi().addNotification(test);
            assertNotNull(response);

            // Trigger the notification by creating a data object with a data element and then instantiating the data
            // object
            DataObject dObject = env.getDataObjectApi().addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = env.getDataElementApi().addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain")).getDataElement();
            assertNotNull(dElement);

            // Use the same correlation property values as above for the registered notification
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            // Create a new data object instance to trigger the notification
            DataObjectInstance doInst = env.getDataObjectInstanceApi().addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            lock.await(2000, TimeUnit.MILLISECONDS);
            assertNotNull(notificationMessage);

            env.getDataObjectApi().deleteDataObject(dObject.getId());
            env.getNotificationApi().deleteNotification(response.getId());

            DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
            assertEquals(0, objects.getDataObjects().size());

            NotificationArrayWithLinks notifications = env.getNotificationApi().getNotifications(null, null, null, null);
            assertEquals(0, notifications.getNotifications().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        // Stop the server
        httpServer.stop();

        env.destroyEnvironment();
        env = null;
    }
}
