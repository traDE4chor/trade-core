/* Copyright 2017 Michael Hahn
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

package org.trade.core.auditing;

import org.trade.core.auditing.events.TraDEEvent;
import org.trade.core.utils.TraDEProperties;

/**
 * Created by hahnml on 21.04.2017.
 */
public interface TraDEEventListener {

    /**
     * Handle an event.
     *
     * @param event
     */
    void onEvent(TraDEEvent event);


    /**
     * Startup the event listener after its registration and pass available properties which can be used to access
     * configuration data such as database information.
     *
     * @param properties
     */
    void startup(TraDEProperties properties);

    /**
     * Shutdown the listener before it is unregistered.
     */
    void shutdown();
}
