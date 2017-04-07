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

package org.trade.core.notification;

import org.trade.core.model.notification.NotificationRegistration;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by hahnml on 07.04.2017.
 */
public class NotificationManager {

    private static NotificationManager instance = new NotificationManager();

    // TODO: Use Hazelcast, etc. instead of local maps
    private HashMap<String, NotificationRegistration> notifications = new LinkedHashMap<>();

    private NotificationManager() {
        // Block instantiation
    }

    public static NotificationManager getInstance() {
        return instance;
    }

}
