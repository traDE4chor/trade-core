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

import org.trade.core.utils.NotifierProperties;

import java.util.List;

/**
 * This class provides a factory to create a {@link INotifierService} object based on the provided ID and the list of
 * registered notifier implementations in '/config/notifier.properties'
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class NotifierServiceFactory {
    /**
     * Creates a notifier service based on the specified id and registered notifier service classes.
     *
     * @param id the id to create a notifier service for
     * @return the notifier service for the given id or null if no class for the given ID can be found in '/config/notifier.properties'.
     * @throws Exception the exception
     */
    public static INotifierService createNotifierService(String id) throws Exception {
        NotifierProperties props = new NotifierProperties();

        Class clazz = props.getNotifierServiceClass(id);

        // Try to create a new instance of the notifier service
        return (INotifierService) clazz.newInstance();
    }

    /**
     * Provides a list of ids of available notifier services.
     *
     * @return the list of ids of available notifier services or an empty list if no notifier services are registered
     * in '/config/notifier.properties'.
     * @throws Exception the exception
     */
    public static List<String> getAvailableNotifierServiceIds() throws Exception {
        NotifierProperties props = new NotifierProperties();

        // Reply the list of ids of all registered notifier services
        return props.getAllRegisteredNotifierServiceIds();
    }

    /**
     * Checks if a notifier service with the given id is available.
     *
     * @param notifierServiceId the id of a notifier service to check availability of
     * @return True, if a notifier service with the provided id is registered. False, otherwise.
     */
    public static boolean isNotifierServiceAvailable(String notifierServiceId) {
        NotifierProperties props = new NotifierProperties();

        // Reply the list of ids of all registered notifier services
        return props.getAllRegisteredNotifierServiceIds().contains(notifierServiceId);
    }
}
