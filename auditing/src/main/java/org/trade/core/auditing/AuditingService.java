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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.auditing.events.TraDEEvent;
import org.trade.core.utils.TraDEProperties;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by hahnml on 21.04.2017.
 */
public enum AuditingService implements IAuditingService {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger("org.trade.core.auditing.AuditingService");

    /**
     * List of all registered event listeners. We use a {@link CopyOnWriteArrayList} to avoid synchronization
     * problems when the list is changed concurrently.
     */
    private final List<TraDEEventListener> eventListeners = new CopyOnWriteArrayList<TraDEEventListener>();

    private TraDEProperties properties = null;

    public boolean hasProperties() {
        return this.properties != null;
    }

    public void setProperties(TraDEProperties _properties) {
        this.properties = _properties;
    }

    @Override
    public void registerEventListener(TraDEEventListener listener) {
        listener.startup(properties);
        this.eventListeners.add(listener);
    }

    @Override
    public void unregisterEventListener(TraDEEventListener listener) {
        try {
            listener.shutdown();
        } catch (Exception e) {
            logger.warn("Shutting down TraDE event listener "
                    + listener.getClass().getName()
                    + " caused an exception. Nevertheless, the event listener is unregistered successfully.", e);
        } finally {
            eventListeners.remove(listener);
        }
    }

    @Override
    public void fireEvent(TraDEEvent event) {
        // Trigger the event on all registered event listeners
        for (TraDEEventListener l : eventListeners) {
            l.onEvent(event);
        }
    }
}
