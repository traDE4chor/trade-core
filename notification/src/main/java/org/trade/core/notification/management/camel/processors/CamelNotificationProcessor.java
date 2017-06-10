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

package org.trade.core.notification.management.camel.processors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.support.ServiceSupport;
import org.trade.core.model.notification.Notification;
import org.trade.core.notifiers.INotifierService;
import org.trade.core.notifiers.NotifierServiceFactory;

import java.util.HashMap;

/**
 * This class provides a custom processor for notifications which triggers the selected notifier with the
 * properties and data specified in the underlying notification.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class CamelNotificationProcessor extends ServiceSupport implements Processor {

    private Notification notification = null;

    private HashMap<String, INotifierService> serviceCache = new HashMap<>();

    public CamelNotificationProcessor(Notification notification) {
        this.notification = notification;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        // Try to resolve a service
        INotifierService service = resolveService(this.notification.getSelectedNotifierServiceId());

        if (service != null) {
            service.executeNotification(this.notification, exchange.getIn().getBody());
        } else {
            exchange.setException(new IllegalArgumentException("No notifier service is registered for the given id = '" +
                    this.notification.getSelectedNotifierServiceId() + "'."));
        }
    }

    private INotifierService resolveService(String serviceId) throws Exception {
        INotifierService service;

        if (this.serviceCache.containsKey(serviceId)) {
            // Use the cached service
            service = this.serviceCache.get(serviceId);
        } else {
            // Create a new instance of the service
            service = NotifierServiceFactory.createNotifierService(serviceId);

            // Startup the notifier service
            service.startup();

            // Add the instance to the cache
            this.serviceCache.put(serviceId, service);
        }

        return service;
    }

    @Override
    protected void doStart() throws Exception {
        // noop
    }

    @Override
    protected void doStop() throws Exception {
        // Shutdown all notifier services
        for (INotifierService service : serviceCache.values()) {
            service.shutdown();
        }
    }
}
