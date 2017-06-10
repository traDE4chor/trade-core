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

package io.swagger.trade.server.jersey.api.impl;

import io.swagger.trade.server.jersey.api.NotFoundException;
import io.swagger.trade.server.jersey.api.NotificationsApiService;
import io.swagger.trade.server.jersey.api.util.ResourceTransformationUtils;
import io.swagger.trade.server.jersey.model.*;
import org.trade.core.notification.management.NotificationManagerFactory;
import org.trade.core.notifiers.NotifierServiceFactory;

import javax.validation.constraints.Min;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collections;
import java.util.List;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-05-16T14:58:47.393+02:00")
public class NotificationsApiServiceImpl extends NotificationsApiService {
    @Override
    public Response addNotification(NotificationData notificationData, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            // Check if a name is specified since this is a mandatory attribute
            if (notificationData.getName() == null || notificationData.getName().isEmpty()) {
                response = Response.status(Response.Status.BAD_REQUEST).entity(new InvalidInput()
                        .message("The 'name' attribute in parameter 'body' is required but missing in the " +
                                "processed request.").example("{\n" +
                                "  \"name\": \"notifyDataValueInitialized\",\n" +
                                "  \"typeOfResourceToObserve\": \"DataValue\",\n" +
                                "  \"resourceFilters\": [\n" +
                                "    {\n" +
                                "      \"filterName\": \"ModelClass\",\n" +
                                "      \"filterValue\": \"class org.trade.core.model.data.DataValue\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"filterName\": \"NewState\",\n" +
                                "      \"filterValue\": \"INITIALIZED\"\n" +
                                "    }\n" +
                                "  ],\n" +
                                "  \"selectedNotifierServiceId\": \"http\",\n" +
                                "  \"notifierParameterValues\": [\n" +
                                "    {\n" +
                                "      \"parameterName\": \"hostname\",\n" +
                                "      \"value\": \"localhost\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"parameterName\": \"port\",\n" +
                                "      \"value\": \"8085\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"parameterName\": \"resourceUri\",\n" +
                                "      \"value\": \"/testResource\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"parameterName\": \"messageFormat\",\n" +
                                "      \"value\": \"XML\"\n" +
                                "    },\n" +
                                "    {\n" +
                                "      \"parameterName\": \"SOAPAction\",\n" +
                                "      \"value\": \"processRequest\"\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}"))
                        .build();
            } else {
                org.trade.core.model.notification.Notification notification = NotificationManagerFactory
                        .createNotificationManager().registerNotification(ResourceTransformationUtils.resource2Model
                                (uriInfo, notificationData));

                Notification result = ResourceTransformationUtils.model2Resource(notification);

                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getId()).build();

                result.setHref(valueUri.toASCIIString());

                response = Response.created(valueUri).entity(result).build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response deleteNotification(String notificationId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = NotificationManagerFactory.createNotificationManager().hasNotification(notificationId);

            if (exists) {
                NotificationManagerFactory.createNotificationManager().deleteNotification(notificationId);

                response = Response.status(Response.Status.NO_CONTENT).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(notificationId)).message("A Notification with ID='" + notificationId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getNotificationDirectly(String notificationId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        org.trade.core.model.notification.Notification notification = NotificationManagerFactory
                .createNotificationManager().getNotification(notificationId);

        try {
            if (notification != null) {
                NotificationWithLinks result = new NotificationWithLinks();

                result.setNotification(ResourceTransformationUtils.model2Resource(notification));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getNotification().setHref(valueUri.toASCIIString());

                // Set link to referenced notifier service
                result.setLinks(LinkUtils.createNotificationLinks(uriInfo, notification, result.getNotification().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(notificationId)).message("A Notification with ID='" + notificationId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getNotifications(@Min(1) Integer start, @Min(1) Integer size, String name, String notifierServiceId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            List<org.trade.core.model.notification.Notification> notifications = NotificationManagerFactory
                    .createNotificationManager().getAllNotifications(name, notifierServiceId);
            int filteredListSize = notifications.size();

            // Check if the start index and the size are in still the range of the filtered result list, if not
            // respond the whole filtered result list
            if (start > 0 && size > 0 && start <= notifications.size()) {
                // Calculate the two index
                int toIndex = start - 1 + size;
                // Check if the index is still in bounds
                if (toIndex > notifications.size()) {
                    toIndex = notifications.size();
                }
                // Decrease start by one since the API starts counting indexes from 1
                notifications = notifications.subList(start - 1, toIndex);
            }

            NotificationArrayWithLinks resultList = new NotificationArrayWithLinks();
            resultList.setNotifications(new NotificationArray());
            for (org.trade.core.model.notification.Notification notification : notifications) {

                NotificationWithLinks result = new NotificationWithLinks();

                result.setNotification(ResourceTransformationUtils.model2Resource(notification));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.path(result.getNotification().getId()).build();

                result.getNotification().setHref(valueUri.toASCIIString());

                // Set link to referenced notifier service
                result.setLinks(LinkUtils.createNotificationLinks(uriInfo, notification, result.getNotification().getHref()));

                resultList.getNotifications().add(result);
            }

            resultList.setLinks(LinkUtils.createPaginationLinks("notifications", uriInfo, start, size,
                    filteredListSize));

            response = Response.ok().entity(resultList).build();
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }

    @Override
    public Response getNotifierServiceOfNotification(String notificationId, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        boolean exists = NotificationManagerFactory.createNotificationManager().hasNotification(notificationId);

        if (exists) {
            org.trade.core.model.notification.Notification notification = NotificationManagerFactory
                    .createNotificationManager().getNotification(notificationId);

            try {
                String notifierServiceId = notification.getSelectedNotifierServiceId();

                if (notifierServiceId != null) {
                    exists = NotifierServiceFactory.isNotifierServiceAvailable(notifierServiceId);

                    if (exists) {
                        NotifierService result = ResourceTransformationUtils.model2Resource(NotifierServiceFactory.createNotifierService(notifierServiceId));

                        response = Response.ok().entity(result).build();
                    } else {
                        response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                                .singletonList(notificationId)).message("The notification with id = '" +
                                notificationId + "' specifies an id of a notifier service ('" + notifierServiceId +
                                "') which is not available/registered to the middleware.")).build();
                    }
                } else {
                    response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                            .singletonList(notificationId)).message("The notification with id = '" +
                            notificationId + "' is not associated to any notifier service (no value for " +
                            "'selectedNotifierServiceId' specified.")).build();
                }
            } catch (Exception e) {
                e.printStackTrace();

                response = Response.serverError().entity(e.getMessage()).build();
            }
        } else {
            response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                    .singletonList(notificationId)).message("A Notification with ID='" + notificationId + "' is " +
                    "not available."))
                    .build();
        }

        return response;
    }

    @Override
    public Response updateNotificationDirectly(String notificationId, Notification notification, SecurityContext securityContext, UriInfo uriInfo) throws NotFoundException {
        Response response = null;

        try {
            boolean exists = NotificationManagerFactory.createNotificationManager().hasNotification(notificationId);

            if (exists) {
                org.trade.core.model.notification.Notification model = NotificationManagerFactory.createNotificationManager()
                        .updateNotification(notificationId, notification.getName(), ResourceTransformationUtils
                                        .resolveModelResource(notification.getIdOfResourceToObserve(), notification
                                                .getTypeOfResourceToObserve()), notification
                                .getSelectedNotifierServiceId(), ResourceTransformationUtils.resource2Model
                                (notification.getNotifierParameterValues()), ResourceTransformationUtils
                                .resource2Model(notification.getResourceFilters()));

                NotificationWithLinks result = new NotificationWithLinks();

                result.setNotification(ResourceTransformationUtils.model2Resource(model));

                // Set HREF and links to related resources
                UriBuilder builder = uriInfo.getAbsolutePathBuilder();
                URI valueUri = builder.build();

                result.getNotification().setHref(valueUri.toASCIIString());

                // Set link to referenced notifier service
                result.setLinks(LinkUtils.createNotificationLinks(uriInfo, model, result.getNotification().getHref()));

                response = Response.ok().entity(result).build();
            } else {
                response = Response.status(Response.Status.NOT_FOUND).entity(new NotFound().properties(Collections
                        .singletonList(notificationId)).message("A Notification with ID='" + notificationId + "' is " +
                        "not available."))
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();

            response = Response.serverError().entity(e.getMessage()).build();
        }

        return response;
    }
}
