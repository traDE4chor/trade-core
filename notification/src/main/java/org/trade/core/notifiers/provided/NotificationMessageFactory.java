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

package org.trade.core.notifiers.provided;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.trade.core.auditing.events.InstanceStateChangeEvent;
import org.trade.core.auditing.events.ModelStateChangeEvent;
import org.trade.core.model.notification.Notification;
import org.trade.core.notifiers.INotifierService;
import org.trade.core.utils.NotifierProperties;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hahnml on 12.05.2017.
 */
public enum NotificationMessageFactory {
    INSTANCE;

    private Configuration configuration;

    private NotifierProperties properties;

    NotificationMessageFactory() {
        properties = new NotifierProperties();

        Path dir = Paths.get(properties.getNotificationMessageTemplateDirectory());

        // Check if the path is relative to make it absolute if required
        if (!dir.isAbsolute()) {
            dir = Paths.get(".", properties.getNotificationMessageTemplateDirectory()).toAbsolutePath().normalize();
        }

        configuration = new Configuration(Configuration.VERSION_2_3_26);
        try {
            configuration.setDirectoryForTemplateLoading(dir.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        DefaultObjectWrapperBuilder owb = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_26);

        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setObjectWrapper(owb.build());
    }

    public Template getMessageTemplate(String templateName) throws Exception {
        // Resolve and return the template
        return this.configuration.getTemplate(templateName);
    }

    public Template getMessageTemplate(INotifierService service, String messageFormat) throws Exception {
        // Resolve and return the template
        return this.configuration.getTemplate(properties.getNotifierMessageTemplate(service.getNotifierServiceId(), messageFormat));
    }

    public String createMessage(INotifierService service, String message, Notification notification, Object
            notificationSource, String messageFormat) throws Exception {
        String result;

        String resourceURL = notification.getResourceURL();

        if (message == null || message.isEmpty()) {
            // Generate a message
            if (notificationSource instanceof ModelStateChangeEvent || notificationSource instanceof
                    InstanceStateChangeEvent) {
                // Resolve the correct template
                Template temp = getMessageTemplate(service, messageFormat);

                Map<String, Object> data = new HashMap<>();
                data.put("notification", notification);
                data.put("notificationSource", notificationSource);

                Writer out = new StringWriter();
                temp.process(data, out);
                result = out.toString();
            } else {
                result = notificationSource.toString() + "\n" + "TraDE URL: " + resourceURL;
            }
        } else {
            result = message;
        }

        return result;
    }
}
