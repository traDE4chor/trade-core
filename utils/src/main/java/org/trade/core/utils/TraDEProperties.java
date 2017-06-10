/*
 * Copyright 2016 Michael Hahn
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
import java.util.Properties;

/**
 * This class defines and provides all TraDE-related properties, e.g., deployment mode or configuration of the
 * embedded Jetty HTTP server.
 * <p>
 * Created by hahnml on 24.11.2016.
 */
public class TraDEProperties extends Properties {

    private Logger logger = LoggerFactory.getLogger("org.trade.core.utils.TraDEProperties");

    private static final long serialVersionUID = -7722413438150969901L;

    private static final String PROPERTY_FILE_LOCATION = "/config.properties";

    public static final String PROPERTY_CACHE_DB_URL = "cache.db.url";
    public static final String PROPERTY_CACHE_DB_NAME = "cache.db.name";

    public enum DataPersistenceMode {
        FILE, DB, CUSTOM
    }

    public enum DeploymentMode {
        SINGLE_NODE, MULTI_NODE, CUSTOM
    }

    public enum NotificationMode {
        CAMEL, CUSTOM
    }

    public static final String PROPERTY_DEPLOYMENT_MODE = "deployment.mode";
    public static final String PROPERTY_DATA_MANAGER_CLASS = "data.manager.class";

    public static final String PROPERTY_NOTIFICATION_MODE = "notification.mode";
    public static final String PROPERTY_NOTIFICATION_MANAGER_CLASS = "notification.manager.class";

    public static final String PROPERTY_DATA_PERSIST_MODE = "data.persistence.mode";
    public static final String PROPERTY_DATA_PERSIST_DB_URL = "data.persistence.db.url";
    public static final String PROPERTY_DATA_PERSIST_DB_NAME = "data.persistence.db.name";
    public static final String PROPERTY_DATA_PERSIST_FILE_DIRECTORY = "data.persistence.file.directory";
    public static final String PROPERTY_DATA_PERSIST_PROVIDER_CLASS = "data.persistence.provider.class";

    public static final String PROPERTY_HTTP_SERVER_PORT = "server.port.http";
    public static final String PROPERTY_HTTPS_SERVER_PORT = "server.port.https";
    public static final String PROPERTY_SERVER_MAX_NUMBER_OF_THREADS = "server.threads.max";

    public static final String PROPERTY_SERVER_SSL_KEYSTORE = "server.ssl.keystore.path";
    public static final String PROPERTY_SERVER_SSL_KEYSTORE_PASSWORD = "server.ssl.keystore.password";

    public TraDEProperties() {
        this(null);
    }

    public TraDEProperties(Properties defaults) {
        super(defaults);

        loadProperties();
    }

    public String getCacheDbUrl() {
        return getProperty(PROPERTY_CACHE_DB_URL, "mongodb://localhost:27017");
    }

    public String getCacheDbName() {
        return getProperty(PROPERTY_CACHE_DB_NAME, "tradeCacheDB");
    }

    public DeploymentMode getDeploymentMode() {
        return DeploymentMode.valueOf(getProperty(PROPERTY_DEPLOYMENT_MODE, "SINGLE_NODE"));
    }

    public String getDataManagerClass() {
        return getProperty(PROPERTY_DATA_MANAGER_CLASS, "org.trade.core.data.management.simple.SimpleDataManager");
    }

    public NotificationMode getNotificationMode() {
        return NotificationMode.valueOf(getProperty(PROPERTY_NOTIFICATION_MODE, "CAMEL"));
    }

    public String getNotificationManagerClass() {
        return getProperty(PROPERTY_NOTIFICATION_MANAGER_CLASS, "org.trade.core.notification.management.camel.CamelNotificationManager");
    }

    public DataPersistenceMode getDataPersistenceMode() {
        return DataPersistenceMode.valueOf(getProperty(PROPERTY_DATA_PERSIST_MODE, "FILE"));
    }

    public String getDataPersistProviderClass() {
        return getProperty(PROPERTY_DATA_PERSIST_PROVIDER_CLASS, "org.trade.core.persistence.local.filesystem.FileSystemPersistence");
    }

    public String getDataPersistenceDbUrl() {
        return getProperty(PROPERTY_DATA_PERSIST_DB_URL, "mongodb://localhost:27017");
    }

    public String getDataPersistenceDbName() {
        return getProperty(PROPERTY_DATA_PERSIST_DB_NAME, "tradeDataDB");
    }

    public String getDataPersistenceFileDirectory() {
        return getProperty(PROPERTY_DATA_PERSIST_FILE_DIRECTORY, System.getProperty("java.io.tmpdir"));
    }

    public int getHttpServerPort() {
        int port = 8080;

        String prop = getProperty(PROPERTY_HTTP_SERVER_PORT, "8080");
        try {
            port = Integer.valueOf(prop);
            if (port < 1 || port > 65535) {
                logger.warn("The HTTP server port ({}) specified in the properties file is not a valid port number. " +
                        "Therefore, the default value '{}' is used. " +
                        "Please change the value of the port in the properties.", prop, port);
            }
        } catch (NumberFormatException e) {
            logger.warn("The HTTP server port ({}) specified in the properties file is not a valid port number. " +
                    "Therefore, the default value '{}' is used. " +
                    "Please change the value of the port in the properties.", prop, port);
        }

        return port;
    }

    public int getHttpsServerPort() {
        int port = 8443;

        String prop = getProperty(PROPERTY_HTTPS_SERVER_PORT, "8443");
        try {
            port = Integer.valueOf(prop);
            if (port < 1 || port > 65535) {
                logger.warn("The HTTPS server port ({}) specified in the properties file is not a valid port number. " +
                        "Therefore, the default value '{}' is used. " +
                        "Please change the value of the port in the properties.", prop, port);
            }
        } catch (NumberFormatException e) {
            logger.warn("The HTTPS server port ({}) specified in the properties file is not a valid port number. " +
                    "Therefore, the default value '{}' is used. " +
                    "Please change the value of the port in the properties.", prop, port);
        }

        return port;
    }

    public String getServerKeystore() {
        return getProperty(PROPERTY_SERVER_SSL_KEYSTORE, "/ssl/keystore.jks");
    }

    public String getKeyStorePassword() {
        return getProperty(PROPERTY_SERVER_SSL_KEYSTORE_PASSWORD, "someKeyStorePassword");
    }

    public int getServerMaxNumberOfThreads() {
        int maxThreads = 1000;

        String prop = getProperty(PROPERTY_SERVER_MAX_NUMBER_OF_THREADS, "1000");
        try {
            maxThreads = Integer.valueOf(prop);
        } catch (NumberFormatException e) {
            logger.warn("The maximum thread number ({}) specified in the properties file is not a valid number. " +
                    "Therefore, the default value '{}' is used. " +
                    "Please specify a valid number in the properties file as soon as possible.", prop, maxThreads);
        }

        return maxThreads;
    }

    private void loadProperties() {
        try {
            InputStream in = TraDEProperties.class.getResourceAsStream(PROPERTY_FILE_LOCATION);

            if (in != null) {
                this.load(in);

                in.close();
            } else {
                logger.info("Loading properties from file was not successful. Using default properties instead.");
            }
        } catch (IOException e) {
            logger.info("Loading properties from file was not successful. Using default properties instead.");
        }
    }
}
