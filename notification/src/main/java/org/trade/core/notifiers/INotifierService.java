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

package org.trade.core.notifiers;

import org.trade.core.model.notification.Notification;

import java.util.Map;

/**
 * This interface defines basic methods a notifier service implementation should provide. All implementations should
 * be stateless in order to execute different notifications using the same notification logic (notifier service
 * implementation). The provided implementations can be used as templates for new INotifierService implementations.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public interface INotifierService {

    /**
     * Provides the unique id of the underlying notifier service implementation as registered in the notifier
     * .properties file of the middleware.
     *
     * @return the unique id of the notifier service
     */
    String getNotifierServiceId();

    /**
     * Gets available notifier service parameters and descriptions.
     *
     * @return the available notifier service parameters and descriptions
     */
    Map<String, String> getAvailableNotifierServiceParametersAndDescriptions();


    /**
     * Startup the notifier service to prepare it for executing notifications.
     *
     * @throws Exception the exception
     */
    void startup() throws Exception;

    /**
     * Shutdown the notifier service to cleanup its internal state and close open connections, etc.
     *
     * @throws Exception the exception
     */
    void shutdown() throws Exception;

    /**
     * Executes the notification with the help of the corresponding notifier service implementation.
     *
     * @param notification       the notification which provides required data (e.g., endpoint information,
     *                           notification message to sent) in order to handle the notification with the
     *                           help of the notifier service.
     * @param notificationSource the notification source which triggers the notification. This source is used to
     *                           generate a message if none is specified through the
     *                           {@link Notification#getNotifierParameters()} map.
     * @throws Exception the exception
     */
    void executeNotification(Notification notification, Object notificationSource) throws Exception;

}
