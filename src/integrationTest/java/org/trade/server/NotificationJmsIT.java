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
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import javax.jms.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 10.05.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationJmsIT {

    private static IntegrationTestEnvironment env;

    private static String activeMQBrokerURL = "tcp://127.0.0.1:61616";

    private static BrokerService broker;

    @BeforeClass
    public static void setupEnvironment() throws Exception {
        env = new IntegrationTestEnvironment();
        env.setupEnvironment(true);

        // Create a new embedded ActiveMQ broker for receiving test notifications
        broker = new BrokerService();
        broker.setBrokerName("broker");
        broker.setUseShutdownHook(false);

        // configure the broker
        broker.setPersistent(false);
        broker.addConnector("tcp://127.0.0.1:61616");

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

            env.getNotificationApi().deleteNotification(response.getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());

            NotificationArrayWithLinks notifications = env.getNotificationApi().getNotifications(null, null, null, null);
            assertEquals(0, notifications.getNotifications().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        // Stop the broker
        broker.stop();

        env.destroyEnvironment();
        env = null;
    }
}
