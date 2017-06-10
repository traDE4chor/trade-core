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

/**
 * This class provides required constants and utility methods for the realization of notifications based on Apache
 * Camel.
 * <p>
 * Created by hahnml on 09.05.2017.
 */
public class TraDECamelUtils {

    public static final String ERROR_LOG_ENDPOINT = "log:org.trade.core.notification.management.camel.route";

    public static final String DEFAULT_ROUTING_COMPONENT = "vm:";

    public static final String ENDPOINT_STATE_CHANGE_EVENTS = "vm:stateChanges";

    public static final String ENDPOINT_LOG = "log:org.trade.core.camel";

    public static final String HEADER_ROUTE_TO = "routeTo";

    public static final String ENDPOINT_DELIMITER = ",";

    public static String getDefaultEndpointString(String endpoint) {
        return DEFAULT_ROUTING_COMPONENT + endpoint;
    }
}
