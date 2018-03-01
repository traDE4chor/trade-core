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
import io.swagger.trade.client.jersey.api.NotificationApi;
import io.swagger.trade.client.jersey.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 10.05.2017.
 */
public class NotificationApiIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static NotificationApi notificationApi;

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

        notificationApi = new NotificationApi(client);
    }

    @Test
    public void createNotificationTest() throws Exception {
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
        param.setValue("8085");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("resourceUri");
        param.setValue("/testResource");
        params.add(param);

        test.setNotifierParameterValues(params);

        ResourceEventFilterArray filters = new ResourceEventFilterArray();

        ResourceEventFilter filter = new ResourceEventFilter();
        filter.setFilterName("ModelClass");
        filter.setFilterValue("class org.trade.core.model.data.DataValue");
        filters.add(filter);

        filter = new ResourceEventFilter();
        filter.setFilterName("NewState");
        filter.setFilterValue("INITIALIZED");
        filters.add(filter);

        test.setResourceFilters(filters);

        try {
            Notification response = notificationApi.addNotification(test);
            assertNotNull(response);

            notificationApi.deleteNotification(response.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldRejectAddNotificationTest() {
        // Try to add a new notification without 'name' value
        NotificationData test = new NotificationData();

        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("http");

        try {
            notificationApi.addNotification(test);
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(400, e.getCode());
        }
    }

    @Test
    public void shouldRejectDeleteNotificationTest() {
        try {
            // Try to delete a non existing notification
            notificationApi.deleteNotification("Not-Existing-Id");
        } catch (ApiException e) {
            e.printStackTrace();

            assertEquals(404, e.getCode());
        }
    }

    @Test
    public void updateNotificationTest() throws Exception {
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
        param.setValue("8085");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("resourceUri");
        param.setValue("/testResource");
        params.add(param);

        test.setNotifierParameterValues(params);

        ResourceEventFilterArray filters = new ResourceEventFilterArray();

        ResourceEventFilter filter = new ResourceEventFilter();
        filter.setFilterName("ModelClass");
        filter.setFilterValue("class org.trade.core.model.data.DataValue");
        filters.add(filter);

        filter = new ResourceEventFilter();
        filter.setFilterName("NewState");
        filter.setFilterValue("INITIALIZED");
        filters.add(filter);

        test.setResourceFilters(filters);

        Notification response = notificationApi.addNotification(test);
        assertNotNull(response);

        Notification updateRequest = new Notification();
        updateRequest.setName("changedName");
        updateRequest.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        updateRequest.setSelectedNotifierServiceId("http");

        // Add an additional parameter
        param = new NotifierServiceParameter();
        param.setParameterName("messageFormat");
        param.setValue("XML");
        params.add(param);

        updateRequest.setNotifierParameterValues(params);

        updateRequest.setResourceFilters(filters);

        notificationApi.updateNotificationDirectly(response.getId(), updateRequest);

        NotificationWithLinks updated = notificationApi.getNotificationDirectly(response.getId());
        // Check unchanged properties
        assertEquals(response.getId(), updated.getNotification().getId());
        assertEquals(response.getResourceFilters(), updated.getNotification().getResourceFilters());
        assertEquals(response.getTypeOfResourceToObserve(), updated.getNotification().getTypeOfResourceToObserve());
        assertEquals(response.getSelectedNotifierServiceId(), updated.getNotification().getSelectedNotifierServiceId());

        TestUtils.printLinkArray(updated.getLinks());

        // Check changed properties
        assertNotEquals(response.getName(), updated.getNotification().getName());
        assertNotEquals(response.getNotifierParameterValues(), updated.getNotification().getNotifierParameterValues());

        notificationApi.deleteNotification(response.getId());
    }

    @AfterClass
    public static void destroy() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_MODEL__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION).drop();

        dataStore.getCollection("dataElements").drop();
        dataStore.getCollection("dataObjects").drop();
        dataStore.getCollection("dataModels").drop();
        dataStore.getCollection("dataDependencyGraphs").drop();
        dataStore.getCollection("notifications").drop();
        dataStore.getCollection("dataValues").drop();

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
