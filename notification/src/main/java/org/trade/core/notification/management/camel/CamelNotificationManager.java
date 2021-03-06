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

package org.trade.core.notification.management.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.ATraDEEvent;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.notification.Notification;
import org.trade.core.notification.management.INotificationManager;
import org.trade.core.notification.management.camel.processors.CamelNotificationProcessor;
import org.trade.core.persistence.PersistableHashMap;
import org.trade.core.utils.TraDEProperties;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This singleton provides functionality for the management of notifications.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public enum CamelNotificationManager implements INotificationManager {
    INSTANCE;

    CamelNotificationManager() {
        staticCamel = new DefaultCamelContext();
        modelStateTemplate = staticCamel.createProducerTemplate();

        AuditingServiceFactory.createAuditingService().registerEventListener(this);
    }

    private static final Logger logger = LoggerFactory.getLogger("org.trade.core.notification.management.camel.CamelNotificationManager");

    private CamelContext staticCamel;

    private Map<String, CamelContext> dynamicContexts = new HashMap<>();

    private ProducerTemplate modelStateTemplate;

    private PersistableHashMap<Notification> notifications = new PersistableHashMap<>
            (Notification.class);

    private void loadExistingAndDefaultRoutes() {
        // Create default routes
        try {
            RouteBuilder builder = new RouteBuilder() {
                public void configure() {
                    // Add a dynamic recipient list which distributes the events to all interested notifications
                    // based on their resource filters. The identifier of the notifications are used as endpoint
                    // names to avoid conflicts.
                    // TODO: We use a header to identify the filtered set of notifications to which an event should be
                    // forwarded. Potentially, this will become a bottleneck and we should realize this in a
                    // different manner.
                    from(TraDECamelUtils.ENDPOINT_STATE_CHANGE_EVENTS).recipientList(header(TraDECamelUtils
                            .HEADER_ROUTE_TO), TraDECamelUtils.ENDPOINT_DELIMITER).parallelProcessing();
                }
            };

            // Append the routes to the context
            staticCamel.addRoutes(builder);

            // Start the context
            staticCamel.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create dynamic routes
        if (!this.notifications.isEmpty()) {
            for (Notification notify : this.notifications.values()) {
                createDynamicRoute(notify);
            }
        }
    }

    private void createDynamicRoute(Notification notification) {
        // Create a new camel context for the notification
        CamelContext context = new DefaultCamelContext();

        // Try to setup and add the route to the camel context
        try {
            RouteBuilder builder = new RouteBuilder() {
                public void configure() {
                    errorHandler(deadLetterChannel(TraDECamelUtils.ERROR_LOG_ENDPOINT));

                    // Add a route with a custom processor which triggers the required notifier for each registered
                    // notification based on the filtered messages
                    from(TraDECamelUtils.getDefaultEndpointString(notification.getIdentifier())).process(new
                            CamelNotificationProcessor(notification));
                }
            };

            // Add the route to the context
            context.addRoutes(builder);

            // Remember the context
            dynamicContexts.put(notification.getIdentifier(), context);

            // Start the context
            context.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Notification registerNotification(Notification notification) {
        this.notifications.put(notification.getIdentifier(), notification);

        // Create a new dynamic route for the notification
        createDynamicRoute(notification);

        return notification;
    }

    @Override
    public Notification getNotification(String notificationId) {
        return this.notifications.get(notificationId);
    }

    @Override
    public List<Notification> getAllNotifications(String name, String notifierServiceId) {
        Stream<Notification> stream = this.notifications.values().stream();

        if (name != null && !name.isEmpty()) {
            stream = stream.filter(d -> (d.getName() != null && d.getName().toUpperCase().equals(name
                    .toUpperCase())));
        }
        if (notifierServiceId != null && !notifierServiceId.isEmpty()) {
            stream = stream.filter(d -> (d.getSelectedNotifierServiceId() != null && d.getSelectedNotifierServiceId()
                    .toUpperCase().equals(notifierServiceId.toUpperCase())));
        }

        List<Notification> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    @Override
    public boolean hasNotification(String notificationId) {
        return this.notifications.containsKey(notificationId);
    }

    @Override
    public Notification updateNotification(String notificationId, String name, ABaseResource resourceToObserve,
                                           String notifierServiceId, Map<String, String> notifierParameterValues, Map<String, String> resourceFilters) throws Exception {
        Notification result = null;
        if (hasNotification(notificationId)) {
            result = this.notifications.get(notificationId);

            // Stop the dynamic route of the notification, we do not have to remove the context since it will be
            // replaced by a new one in createDynamicRoute()
            this.dynamicContexts.get(notificationId).stop();

            // Update the notification
            result.setName(name);
            result.setResourceFilters(resourceFilters);
            result.setNotifierParameters(notifierParameterValues);
            result.setSelectedNotifierServiceId(notifierServiceId);
            result.setResource(resourceToObserve);

            // Create a new dynamic route for the updated notification
            createDynamicRoute(result);

            // Persist the changes to the data source
            result.storeToDS();
        }

        return result;
    }

    @Override
    public void deleteNotification(String notificationId) throws Exception {
        if (hasNotification(notificationId)) {
            Notification result = this.notifications.get(notificationId);

            // Stop and remove the dynamic route of the notification
            this.dynamicContexts.remove(result.getIdentifier()).stop();

            // After the notification is successfully deleted, we can remove it from the map
            this.notifications.remove(notificationId);
        }
    }

    @Override
    public void clearCachedObjects() {
        notifications.clear();
    }

    // Implementation of IAuditingService methods
    @Override
    public void onEvent(ATraDEEvent event) {
        // Check if any notifications are registered or if debugging is enabled (forward event messages to log).
        // In any other case we do not need to forward state changes to the static Camel route for their processing.
        if (!this.notifications.isEmpty() || logger.isDebugEnabled()) {
            modelStateTemplate.sendBodyAndHeader(TraDECamelUtils.ENDPOINT_STATE_CHANGE_EVENTS,
                    event, TraDECamelUtils.HEADER_ROUTE_TO, calculateTargetEndpoints(event));
        }
    }

    @Override
    public void startup(TraDEProperties properties) {
        try {
            // Try to load all existing static and dynamic route definitions
            loadExistingAndDefaultRoutes();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdown() {
        try {
            // Stop all dynamic contexts
            for (CamelContext context : this.dynamicContexts.values()) {
                context.stop();
            }

            // Stop the static context
            staticCamel.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String calculateTargetEndpoints(ATraDEEvent event) {
        StringBuilder endpointList = new StringBuilder();

        // Add the log endpoint as static target, if debugging is enabled
        if (logger.isDebugEnabled()) {
            endpointList.append(TraDECamelUtils.ENDPOINT_LOG);
        }

        // Calculate the collection of target destinations for the events
        for (Notification notify : this.notifications.values()) {
            if (checkFilter(notify, event)) {
                endpointList.append(TraDECamelUtils.ENDPOINT_DELIMITER);
                // Use the unique identifier as endpoint to avoid conflicts
                endpointList.append(TraDECamelUtils.getDefaultEndpointString(notify.getIdentifier()));
            }
        }

        return endpointList.toString();
    }

    private boolean checkFilter(Notification notify, ATraDEEvent event) {
        for (String key : notify.getResourceFilters().keySet()) {
            try {
                Object result = resolveValue(event, key);

                if (result == null || !result.toString().equals(notify.getResourceFilters().get(key))) {
                    // If there is no value to check or if the resolved and provided values are not equal, we
                    // directly return false. In any other case isSuccessfullyChecked remains true and will be
                    // finally returned after all filters are checked successfully.
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private Object resolveValue(Object source, String methodName) throws Exception {
        Object result;

        if (methodName.contains(TraDECamelUtils.RESOURCE_FILTER_NESTED_QUERY_SEPARATOR_CHAR)) {
            // Limit the split with an array size of two, so that we get the first method name at index 0 and the
            // rest of the query string at index 1. This is necessary because the rest of the query string can again
            // contain a '#' character and therefore we will invoke this method recursively.
            String[] nestedQueries = methodName.split(TraDECamelUtils.RESOURCE_FILTER_NESTED_QUERY_SEPARATOR_CHAR,
                    2);

            String firstMethodName = nestedQueries[0];
            String nestedQuery = nestedQueries[1];
            Method getter = source.getClass().getMethod("get" + firstMethodName);
            Object firstObject = getter.invoke(source);

            result = resolveValue(firstObject, nestedQuery);
        } else {
            Method getter = source.getClass().getMethod("get" + methodName);

            result = getter.invoke(source);
        }

        return result;
    }
}
