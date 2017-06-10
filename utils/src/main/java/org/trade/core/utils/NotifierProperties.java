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

package org.trade.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class defines and provides all registered notifier service implementations.
 * <p>
 * Created by hahnml on 11.05.2017.
 */
public class NotifierProperties extends Properties {

    private static final long serialVersionUID = -9153317318046996624L;

    private Logger logger = LoggerFactory.getLogger("org.trade.core.utils.NotifierProperties");

    private static final String PROPERTY_FILE_LOCATION = "/notifier.properties";

    private static final String NOTIFIERS_DELIMITER = ",";

    private static final String PROPERTY_NOTIFIERS = "notifiers";

    private static final String PROPERTY_NOTIFIER_CLASS = ".class";

    private static final String PROPERTY_TEMPLATE_DIRECTORY = "notificationMessageTemplateDirectory";

    private static final String PROPERTY_MESSAGE_TEMPLATE = "messageTemplate";

    /**
     * Instantiates a new Notifier properties.
     */
    public NotifierProperties() {
        this(null);
    }

    /**
     * Instantiates a new Notifier properties.
     *
     * @param defaults the defaults
     */
    public NotifierProperties(Properties defaults) {
        super(defaults);

        loadProperties();
    }

    /**
     * Gets the class of a notifier service by id.
     *
     * @param id the id of the notifier service
     * @return the class of the notifier service
     * @throws ClassNotFoundException if the class was not found on the classpath
     */
    public Class getNotifierServiceClass(String id) throws ClassNotFoundException {
        String className = getProperty(id + PROPERTY_NOTIFIER_CLASS);

        // Try to load the class from the classpath
        return Class.forName(className);
    }

    /**
     * Gets a list of ids of all registered notifier services and their classes.
     *
     * @return the list of all registered notifier service id's.
     */
    public List<String> getAllRegisteredNotifierServiceIds() {
        List<String> result = new ArrayList<>();

        String notifiersList = getProperty(PROPERTY_NOTIFIERS);

        // Split the list at each delimiter
        for (String id : notifiersList.split(NOTIFIERS_DELIMITER)) {
            // Add the id to the list
            result.add(id);
        }

        return result;
    }

    /**
     * Gets all registered notifier services and their classes.
     *
     * @return the map of all registered notifier service id's and the related classes.
     */
    public Map<String, Class> getAllRegisteredNotifierServices() {
        Map<String, Class> result = new HashMap<String, Class>();

        String notifiersList = getProperty(PROPERTY_NOTIFIERS);

        // Split the list at each delimiter
        for (String id : notifiersList.split(NOTIFIERS_DELIMITER)) {
            // Try to load the class from the classpath
            try {
                Class clazz = getNotifierServiceClass(id);

                // Add the id and the class to the map
                result.put(id, clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * Gets a custom notifier property by its name ('notifierId.propertyName' in notifier.properties file).
     *
     * @param notifierId   the notifier id
     * @param propertyName the property name
     * @return the value of the custom notifier property, if there is one or null if the property was not found
     */
    public String getCustomNotifierProperty(String notifierId, String propertyName) {
        return getProperty(notifierId + "." + propertyName);
    }

    /**
     * Gets notification message template directory.
     *
     * @return the notification message template directory
     */
    public String getNotificationMessageTemplateDirectory() {
        return getProperty(PROPERTY_TEMPLATE_DIRECTORY, "/notificationMsgTemplates");
    }


    /**
     * Gets notifier message template configured for a notifier. If not message template is registered, the 'plainMsg
     * .ftl' template is returned by default.
     *
     * @param notifierId the notifier id
     * @return the notifier message template
     */
    public String getNotifierMessageTemplate(String notifierId) {
        return getProperty(notifierId + "." + PROPERTY_MESSAGE_TEMPLATE, "plainMsg.ftl");
    }

    /**
     * Resolves a notifier message template based on the provided message format. If no message template can be found
     * for the specified format, the one registered for the notifier is returned or the 'plainMsg
     * .ftl' template if nothing can be resolved at all.
     *
     * @param notifierId    the notifier id
     * @param messageFormat the message format for which a template should be resolved
     * @return the notifier message template
     */
    public String getNotifierMessageTemplate(String notifierId, String messageFormat) {
        String template = null;

        if (messageFormat != null) {
            // Try to resolve a message template based on the provided message format
            Path templateDir = Paths.get(getNotificationMessageTemplateDirectory());

            // Check if the path is relative to make it absolute if required
            if (!templateDir.isAbsolute()) {
                templateDir = Paths.get(".", getNotificationMessageTemplateDirectory()).toAbsolutePath().normalize();
            }

            // We assume that the file name of a matching template file starts with the message format string, e.g.,
            // "xmlTemplate" or "jsonTemplate".
            String matchPattern = messageFormat.toLowerCase() + "*.ftl";
            PathMatcher matcher =
                    FileSystems.getDefault().getPathMatcher("glob:" + matchPattern);

            try {
                List<Path> paths = Files.list(templateDir).filter(f -> matcher.matches(f.getFileName())).collect
                        (Collectors.toList());

                // If there is more than one matching template, we use the first one from the list
                if (!paths.isEmpty()) {
                    template = paths.get(0).getFileName().toString();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check if we were able to resolve a template, if not used the registered one
        if (template == null) {
            template = getNotifierMessageTemplate(notifierId);
        }

        return template;
    }

    private void loadProperties() {
        try {
            InputStream in = NotifierProperties.class.getResourceAsStream(PROPERTY_FILE_LOCATION);

            if (in != null) {
                this.load(in);

                in.close();
            } else {
                logger.info("Loading notifier properties from file was not successful. Using default properties " +
                        "instead.");
            }
        } catch (IOException e) {
            logger.info("Loading notifier properties from file was not successful. Using default properties instead.");
        }
    }
}
