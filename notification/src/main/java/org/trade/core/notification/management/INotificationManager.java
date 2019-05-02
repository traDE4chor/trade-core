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

package org.trade.core.notification.management;

import org.trade.core.auditing.TraDEEventListener;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.notification.Notification;

import java.util.List;
import java.util.Map;

/**
 * This interface defines basic methods a notification manager implementation should provide.
 * <p>
 * Created by hahnml on 09.05.2017.
 */
public interface INotificationManager extends TraDEEventListener {

    /**
     * Register a new notification.
     *
     * @param notification the notification
     * @return the registered notification
     */
    Notification registerNotification(Notification notification);

    /**
     * Gets a notification.
     *
     * @param notificationId the ID of a notification
     * @return the notification
     */
    Notification getNotification(String notificationId);

    /**
     * Gets all notifications based on the provided criteria.
     *
     * @param name              the name to search for or null to not filter the result list
     *                          based on the name of notifications
     * @param notifierServiceId the id of a notifier service to search for or null to not filter the result list
     *                          based on notifier service ids specified by notifications.
     * @return a list of all notifications fulfilling the specified criteria
     */
    List<Notification> getAllNotifications(String name, String notifierServiceId);

    /**
     * Updates an existing notification with the provided values, if they differ from the current values.
     *
     * @param notificationId the id of the notification to update
     * @param name the name value of the notification
     * @param resourceToObserve the resource which is observed by this notification
     * @param notifierServiceId the id of the used notifier service
     * @param notifierParameterValues the parameter values for the notifier service
     * @param resourceFilters the resource filters of this notification
     *
     * @return the updated notification
     * @throws Exception the exception
     */
    Notification updateNotification(String notificationId, String name, ABaseResource resourceToObserve,
                                    String notifierServiceId, Map<String,
                String> notifierParameterValues, Map<String, String> resourceFilters) throws Exception;

    /**
     * Whether the {@link INotificationManager} implementation knows a notification with the provided ID or not.
     *
     * @param notificationId the ID of a notification
     * @return True, if the {@link INotificationManager} implementation knows the ID. False, otherwise.
     */
    boolean hasNotification(String notificationId);

    /**
     * Deletes the notification with the specified ID.
     *
     * @param notificationId the ID of a notification
     * @throws Exception the exception
     */
    void deleteNotification(String notificationId) throws Exception;

    /**
     * Clear all maps of objects cached by the IDataManager instance.
     */
    void clearCachedObjects();
}
