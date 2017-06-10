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

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpComponent;
import org.apache.camel.impl.DefaultCamelContext;
import org.trade.core.model.notification.Notification;
import org.trade.core.notifiers.INotifierService;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides an implementation of {@link org.trade.core.notifiers.INotifierService} for sending
 * notifications as HTTP messages to an HTTP endpoint using HTTP POST.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class HttpNotifierService implements INotifierService {

    private CamelContext camelContext = null;

    private ProducerTemplate template;

    private static final String NOTIFIER_ID = "http";

    private static final String HTTP_CAMEL_COMPONENT = "http";

    private static final String DESCRIPTION = "description";

    private static final String HOSTNAME = "hostname";

    private static final String PORT = "port";

    private static final String RESOURCE_URI = "resourceUri";

    private static final String PARAMETERS = "parameters";

    private static final String MESSAGE = "message";

    private static final String MESSAGE_FORMAT = "messageFormat";

    private static final String MESSAGE_FORMAT_PLAIN = "PLAIN";

    private static final String MESSAGE_FORMAT_XML = "XML";

    private static final String MESSAGE_FORMAT_JSON = "JSON";

    private static final String SOAP_ACTION_HEADER = "SOAPAction";

    private static final String FURTHER_INFORMATION = "Further Information and Configuration Options";

    private static final String FURTHER_INFORMATION_URL = "http://camel.apache.org/http.html";

    @Override
    public String getNotifierServiceId() {
        return NOTIFIER_ID;
    }

    @Override
    public Map<String, String> getAvailableNotifierServiceParametersAndDescriptions() {
        Map<String, String> result = new HashMap<String, String>();

        result.put(DESCRIPTION, "The following parameters are used to build the target HTTP endpoint where the " +
                "notification should be send to. The underlying URI format looks like the following: " +
                "'http:[hostname][:port][/resourceUri][?param1=value1&param2=value2...]', where parameters are marked" +
                " with '[]'");
        result.put(HOSTNAME, "Use this parameter to specify the hostname of the target endpoint.");
        result.put(PORT, "OPTIONAL: Use this parameter to specify the port of the target endpoint.");
        result.put(RESOURCE_URI, "Use this parameter to specify the resource URI the target endpoint.");
        result.put(PARAMETERS, "OPTIONAL: Use this parameter to specify a set of parameters in the form " +
                "'?firstParam=value&secondParam=value&...'. If parameters are specified Apache Camel calls the target" +
                " URL with the GET method. Therefore, no parameters (query string) should be specified in order to " +
                "call the endpoint via POST. Further information is provided here: http://camel.apache.org/http.html#HTTP-CallingusingGETorPOST");
        result.put(MESSAGE, "OPTIONAL: Use this parameter to specify the actual message which should be send to the " +
                "target endpoint. If no message is specified, the middleware will generate a default message.");
        result.put(MESSAGE_FORMAT, "OPTIONAL: Use this parameter to specify a message format which should be used for" +
                " generated messages. By default the middleware supports the following message formats: " +
                MESSAGE_FORMAT_PLAIN + ", " + MESSAGE_FORMAT_XML + ", " + MESSAGE_FORMAT_JSON + ". This parameter " +
                "only has an effect, if the 'message' parameter is not set.");
        result.put(SOAP_ACTION_HEADER, "OPTIONAL: Use this parameter to specify a SOAP action header in order to send " +
                "document-style SOAP messages over HTTP to the target endpoint. Therefore, the HTTP message has to be" +
                " a valid SOAP envelope.");
        result.put(FURTHER_INFORMATION, "Apache Camel allows additional configuration options for HTTP connections " +
                "which are described here: " + FURTHER_INFORMATION_URL + ". In order to specify one or more of " +
                "these configuration options, just use the parameter names as key and specify a value for it as a " +
                "notifier parameter.");

        return result;
    }

    @Override
    public void executeNotification(Notification notification, Object notificationSource) throws Exception {
        Map<String, String> notifierParameters = notification.getNotifierParameters();

        String endpoint = createEndpoint(notifierParameters);
        String messageFormat = notifierParameters.get(MESSAGE_FORMAT);
        String message = NotificationMessageFactory.INSTANCE.createMessage(this, notifierParameters.get(MESSAGE),
                notification, notificationSource, messageFormat);

        Map<String, Object> headers = new HashMap<>();
        if (notifierParameters.containsKey(SOAP_ACTION_HEADER)) {
            // Add the SOAPACTION header
            headers.put(SOAP_ACTION_HEADER,
                    notifierParameters.get(SOAP_ACTION_HEADER));
        }

        // Use the message format to resolve the content type
        if (messageFormat != null && !messageFormat.isEmpty()) {
            headers.put(Exchange.CONTENT_TYPE, "text/" + messageFormat.toLowerCase());
        } else {
            headers.put(Exchange.CONTENT_TYPE, "text/plain");
        }

        template.sendBodyAndHeaders(endpoint, ExchangePattern.InOnly, message, headers);
    }

    @Override
    public void startup() throws Exception {
        camelContext = new DefaultCamelContext();

        // Load the HTTP component to the context
        camelContext.getComponent(HTTP_CAMEL_COMPONENT, HttpComponent.class);

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
        // Build the endpoint URL according to the following schema:
        // "http:[hostname][:port][/resourceUri][?param1=value1&param2=value2...]"
        StringBuilder endpoint = new StringBuilder();

        // Add the camel component string
        endpoint.append(HTTP_CAMEL_COMPONENT);
        endpoint.append("://");
        // Add the hostname
        endpoint.append(notifierParameters.get(HOSTNAME));

        // Check if a port is specified
        if (notifierParameters.containsKey(PORT)) {
            // Add the port
            endpoint.append(":");
            endpoint.append(notifierParameters.get(PORT));
        }

        // Check if the resource URI has a leading "/", if not add one
        if (!notifierParameters.get(RESOURCE_URI).startsWith("/")) {
            endpoint.append("/");
        }
        endpoint.append(notifierParameters.get(RESOURCE_URI));

        // Check if parameters are specified
        if (notifierParameters.containsKey(PARAMETERS)) {
            // Check if the parameters string has a leading "?", if not add one
            if (!notifierParameters.get(PARAMETERS).startsWith("?")) {
                endpoint.append("?");
            }

            // Add the parameters string
            endpoint.append(notifierParameters.get(PARAMETERS));
        }

        return endpoint.toString();
    }
}
