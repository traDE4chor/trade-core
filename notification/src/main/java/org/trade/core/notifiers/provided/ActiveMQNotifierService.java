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

package org.trade.core.notifiers.provided;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.trade.core.model.notification.Notification;
import org.trade.core.notifiers.INotifierService;

import java.util.HashMap;
import java.util.Map;

import static org.apache.activemq.camel.component.ActiveMQComponent.activeMQComponent;

/**
 * This class provides an implementation of {@link org.trade.core.notifiers.INotifierService} for sending
 * notifications as JMS messages to an ActiveMQ messaging endpoint, i.e., a queue or a topic.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class ActiveMQNotifierService implements INotifierService {

    private CamelContext camelContext;

    private ProducerTemplate template;

    private static final String NOTIFIER_ID = "activemq";

    private static final String ACTIVEMQ_CAMEL_COMPONENT = "activemq";

    private static final String DESCRIPTION = "description";

    private static final String CHANNEL_TYPE = "channelType";

    private static final String DESTINATION_NAME = "destinationName";

    private static final String BROKER_URL = "brokerURL";

    private static final String MESSAGE = "message";

    private static final String MESSAGE_FORMAT = "messageFormat";

    private static final String MESSAGE_FORMAT_PLAIN = "PLAIN";

    private static final String MESSAGE_FORMAT_XML = "XML";

    private static final String MESSAGE_FORMAT_JSON = "JSON";

    private static final String FURTHER_INFORMATION = "Further Information";

    private static final String FURTHER_INFORMATION_URL = "http://camel.apache.org/activemq.html";

    private String activeBrokerUrl = null;

    @Override
    public String getNotifierServiceId() {
        return NOTIFIER_ID;
    }

    @Override
    public Map<String, String> getAvailableNotifierServiceParametersAndDescriptions() {
        Map<String, String> result = new HashMap<String, String>();

        result.put(DESCRIPTION, "The following parameters are used to build the target ActiveMQ endpoint where the " +
                "notification should be send to. The underlying URI format looks like the following: " +
                "'activemq:[queue: or topic:][destinationName}', where parameters are marked" +
                " with '[]'");
        result.put(BROKER_URL, "Use this parameter to specify the URL of the ActiveMQ broker to use. For example," +
                " 'tcp://somehost:61616'. Further information and hints on how to connect with an ActiveMQ broker " +
                "can be found on the websites of Apache ActiveMQ: http://activemq.apache.org/");
        result.put(CHANNEL_TYPE, "Use this parameter to specify the type of channel ('queue' or 'topic') the " +
                "notification should be send to.");
        result.put(DESTINATION_NAME, "Use this parameter to specify the destination name of the target channel. This " +
                "means the name of the queue or topic the notification should be send to.");
        result.put(MESSAGE, "OPTIONAL: Use this parameter to specify the actual message which should be send to the " +
                "target endpoint. If no message is specified, the middleware will generate a default message.");
        result.put(MESSAGE_FORMAT, "OPTIONAL: Use this parameter to specify a message format which should be used for" +
                " generated messages. By default the middleware supports the following message formats: " +
                MESSAGE_FORMAT_PLAIN + ", " + MESSAGE_FORMAT_XML + ", " + MESSAGE_FORMAT_JSON + ". This parameter " +
                "only has an effect, if the 'message' parameter is not set.");
        result.put(FURTHER_INFORMATION, "Further information on the ActiveMQ component of Apache Camel can be found " +
                "here: " + FURTHER_INFORMATION_URL + ".");

        return result;
    }

    @Override
    public void executeNotification(Notification notification, Object notificationSource) throws Exception {
        Map<String, String> notifierParameters = notification.getNotifierParameters();

        // Set the broker URL for the ActiveMQ component, if it differs from the currently used one
        String brokerURL = notifierParameters.get(BROKER_URL);
        if (!brokerURL.equals(this.activeBrokerUrl)) {
            // Get the component and update the broker URL
            ActiveMQComponent comp = (ActiveMQComponent) this.camelContext.getComponent(ACTIVEMQ_CAMEL_COMPONENT);
            comp.setBrokerURL(brokerURL);

            this.activeBrokerUrl = brokerURL;
        }

        template.sendBody(createEndpoint(notifierParameters), ExchangePattern.InOnly, NotificationMessageFactory
                .INSTANCE.createMessage(this, notifierParameters.get(MESSAGE), notification, notificationSource,
                        notifierParameters.get(MESSAGE_FORMAT)));
    }

    @Override
    public void startup() throws Exception {
        camelContext = new DefaultCamelContext();

        // Add the ActiveMQ component
        camelContext.addComponent(ACTIVEMQ_CAMEL_COMPONENT, activeMQComponent());

        template = camelContext.createProducerTemplate();

        // Start Camel
        camelContext.start();
    }

    @Override
    public void shutdown() throws Exception {
        template.cleanUp();

        // Stop the camel context
        camelContext.stop();
    }

    private String createEndpoint(Map<String, String> notifierParameters) {
        // Build the endpoint URL according to the following schema: "activemq:[queue:|topic:][destinationName}"
        StringBuilder endpoint = new StringBuilder();

        // Add the camel component string
        endpoint.append(ACTIVEMQ_CAMEL_COMPONENT);
        endpoint.append(":");
        // Add the type of channel
        endpoint.append(notifierParameters.get(CHANNEL_TYPE));
        endpoint.append(":");
        // Add the destination name
        endpoint.append(notifierParameters.get(DESTINATION_NAME));

        return endpoint.toString();
    }
}
