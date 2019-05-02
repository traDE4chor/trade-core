/*
 * Copyright 2019 Michael Hahn
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

package org.trade.server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import io.swagger.trade.client.jersey.ApiClient;
import io.swagger.trade.client.jersey.api.*;
import org.apache.camel.test.AvailablePortFinder;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

public class IntegrationTestEnvironment {

    private DataDependencyGraphApi ddgApi;

    private DataModelApi dataModelApi;

    private DataObjectApi dataObjectApi;

    private DataObjectInstanceApi dataObjectInstanceApi;

    private DataElementApi dataElementApi;

    private DataElementInstanceApi dataElementInstanceApi;

    private NotificationApi notificationApi;

    private DataValueApi dataValueApi;

    private TraDEProperties properties;

    private TraDEServer server;

    public DataDependencyGraphApi getDdgApi() {
        return ddgApi;
    }

    public DataModelApi getDataModelApi() {
        return dataModelApi;
    }

    public DataObjectApi getDataObjectApi() {
        return dataObjectApi;
    }

    public DataObjectInstanceApi getDataObjectInstanceApi() {
        return dataObjectInstanceApi;
    }

    public DataElementApi getDataElementApi() {
        return dataElementApi;
    }

    public DataElementInstanceApi getDataElementInstanceApi() {
        return dataElementInstanceApi;
    }

    public NotificationApi getNotificationApi() {
        return notificationApi;
    }

    public DataValueApi getDataValueApi() {
        return dataValueApi;
    }

    public TraDEProperties getProperties() {
        return properties;
    }

    public void setupEnvironment(boolean startServer) {
        this.setupEnvironment(startServer, -1);
    }

    public void setupEnvironment(boolean startServer, int fixedPort) {
        // Load custom properties such as MongoDB url and db name
        properties = new TraDEProperties();

        // Find an unused available port
        int port = properties.getHttpServerPort();

        if (fixedPort > 0) {
            port = fixedPort;
        } else {
            port = AvailablePortFinder.getNextAvailable(port);
        }

        // Set the port
        properties.setProperty(TraDEProperties.PROPERTY_HTTP_SERVER_PORT, String.valueOf(port));

        // Create a new server
        server = new TraDEServer();

        // Start the server
        if (startServer) {
            try {
                server.startHTTPServer(properties);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ApiClient client = new ApiClient();

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client.setBasePath("http://127.0.0.1:" + port + "/api");

        ddgApi = new DataDependencyGraphApi(client);
        dataModelApi = new DataModelApi(client);
        dataObjectApi = new DataObjectApi(client);
        dataObjectInstanceApi = new DataObjectInstanceApi(client);
        dataElementApi = new DataElementApi(client);
        dataElementInstanceApi = new DataElementInstanceApi(client);
        notificationApi = new NotificationApi(client);
        dataValueApi = new DataValueApi(client);
    }

    public void startTraDEServer() {
        // Start the server
        try {
            server.startHTTPServer(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopTraDEServer() {
        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroyEnvironment() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
        dataStore.getCollection(ModelConstants.DATA_MODEL__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_DEPENDENCY_GRAPH__DATA_COLLECTION).drop();
        dataStore.getCollection(ModelConstants.DATA_VALUE__DATA_COLLECTION).drop();

        dataStore.getCollection("dataElements").drop();
        dataStore.getCollection("dataObjects").drop();
        dataStore.getCollection("dataModels").drop();
        dataStore.getCollection("dataDependencyGraphs").drop();
        dataStore.getCollection("notifications").drop();
        dataStore.getCollection("dataValues").drop();
        dataStore.getCollection("dataElementInstances").drop();
        dataStore.getCollection("dataObjectInstances").drop();

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
