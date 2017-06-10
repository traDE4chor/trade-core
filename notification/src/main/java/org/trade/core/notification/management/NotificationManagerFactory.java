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

import org.trade.core.notification.management.camel.CamelNotificationManager;
import org.trade.core.utils.TraDEProperties;

/**
 * This class provides a factory to create a {@link INotificationManager} object based on the configuration of the middleware
 * (i.e., {@link org.trade.core.utils.TraDEProperties}).
 * <p>
 * Created by hahnml on 09.05.2017.
 */
public class NotificationManagerFactory {
    /**
     * Creates a notification manager based on the specified properties.
     *
     * @return the notification manager according to the defined property in 'config.properties'.
     */
    public static INotificationManager createNotificationManager() {
        INotificationManager result = null;

        TraDEProperties props = new TraDEProperties();

        switch (props.getNotificationMode()) {
            case CAMEL:
                result = CamelNotificationManager.INSTANCE;
                break;
            case CUSTOM:
                try {
                    // Try to load the class from the classpath
                    Class clazz = Class.forName(props.getNotificationManagerClass());
                    // Try to get the first and only ("INSTANCE") enum constant from the class implementing the
                    // singleton design pattern
                    result = (INotificationManager) clazz.getEnumConstants()[0];
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                result = CamelNotificationManager.INSTANCE;
        }

        return result;
    }
}
