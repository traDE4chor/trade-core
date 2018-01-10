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
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.test.AvailablePortFinder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import javax.jms.*;

import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 10.05.2017.
 */
public class NotificationJmsIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static NotificationApi notificationApi;

    private static DataValueApi dvApiInstance;

    private static String activeMQBrokerURL = "tcp://127.0.0.1:61616";

    private static BrokerService broker;

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

        // Create a new embedded ActiveMQ broker for receiving test notifications
        broker = new BrokerService();

        // configure the broker
        broker.setPersistent(false);
        broker.addConnector("tcp://0.0.0.0:61616");

        broker.start();
    }

    @Test
    public void createAndTriggerJmsNotificationTest() throws Exception {
        String destinationName = "dataValueInitializedQueue";

        NotificationData test = new NotificationData();
        test.setName("notifyDataValueInitialized");
        test.setTypeOfResourceToObserve(ResourceTypeEnum.DATAVALUE);
        test.setSelectedNotifierServiceId("activemq");

        NotifierServiceParameterArray params = new NotifierServiceParameterArray();

        NotifierServiceParameter param = new NotifierServiceParameter();
        param.setParameterName("brokerURL");
        param.setValue(activeMQBrokerURL);
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("channelType");
        param.setValue("queue");
        params.add(param);

        param = new NotifierServiceParameter();
        param.setParameterName("destinationName");
        param.setValue(destinationName);
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

            // Connect to the target queue to receive the triggered notification message
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(activeMQBrokerURL);
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(destinationName);
            MessageConsumer consumer = session.createConsumer(destination);

            Message message = consumer.receive(50000);
            assertNotNull(message);

            connection.close();

            notificationApi.deleteNotification(response.getId());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        // Stop the broker
        broker.stop();

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
