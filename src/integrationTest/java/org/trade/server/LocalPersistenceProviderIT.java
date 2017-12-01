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

package org.trade.server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import io.swagger.trade.client.jersey.ApiClient;
import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.api.*;
import io.swagger.trade.client.jersey.model.*;
import org.junit.AfterClass;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 30.11.2017.
 */
public class LocalPersistenceProviderIT {

    private static TraDEServer server;

    private static TraDEProperties properties;

    private static DataObjectApi dataObjectApiInstance;

    private static DataElementApi dataElementApiInstance;

    private static DataValueApi dvApiInstance;

    private static DataObjectInstanceApi doInstApiInstance;

    private static DataElementInstanceApi deInstApiInstance;

    public static void setupEnvironment() {
        // Load custom properties such as MongoDB url and db name
        properties = new TraDEProperties();

        // Create a new server
        server = new TraDEServer();

        // Start the server
        try {
            server.startHTTPServer(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ApiClient client = new ApiClient();

        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        client.setBasePath("http://localhost:8080/api");

        dataObjectApiInstance = new DataObjectApi(client);
        dataElementApiInstance = new DataElementApi(client);
        dvApiInstance = new DataValueApi(client);
        doInstApiInstance = new DataObjectInstanceApi(client);
        deInstApiInstance = new DataElementInstanceApi(client);
    }

    @Test
    public void createAndLoadDataObjectAfterShutdownTest() {
        try {
            // Start the environment
            setupEnvironment();

            DataObjectData request = new DataObjectData();

            request.setEntity("hahnml");
            request.setName("inputData");

            DataObject result = dataObjectApiInstance.addDataObject(request);

            // Shutdown the server
            shutdownEnvironment();

            // Start the environment again
            setupEnvironment();

            // Try to query the data object and check if it's loaded correctly
            DataObjectWithLinks reloaded = dataObjectApiInstance.getDataObjectById(result.getId());

            assertEquals(result, reloaded.getDataObject());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createAndLoadDataObjectHierarchyAfterShutdownTest() {
        try {
            // Start the environment
            setupEnvironment();

            // Create a new data object
            DataObject dObject = dataObjectApiInstance.addDataObject(new DataObjectData().entity("hahnml").name
                    ("inputData"));
            assertNotNull(dObject);

            // Add a data element to it
            DataElement dElement = dataElementApiInstance.addDataElement(dObject.getId(), new DataElementData().name
                    ("testElement").entity("hahnml").type("string")).getDataElement();
            assertNotNull(dElement);

            // Create a new data object instance
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            DataObjectInstance doInst = doInstApiInstance.addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            // Create a data value and associate it to the data element instance
            DataValue dataValue = dvApiInstance.addDataValue(new DataValueData().name("inputData").createdBy("hahnml")
                    .type("string"));
            assertNotNull(dataValue);

            DataElementInstance deInst = deInstApiInstance.getDataElementInstanceByDataElementName(doInst.getId(),
                    dElement.getName()).getInstance();
            assertNotNull(deInst);

            // Associate the generated data value to the data element instance
            dvApiInstance.setDataValue(deInst.getId(), new DataValue().id(dataValue.getId())).getDataValue();

            // Upload some data for the data value
            dvApiInstance.pushDataValue(dataValue.getId(), 4L, "test".getBytes());

            // Update the local values since the objects will have changed at the server (lifecycle states, etc.)
            dObject = dataObjectApiInstance.getDataObjectById(dObject.getId()).getDataObject();
            dElement = dataElementApiInstance.getDataElementDirectly(dElement.getId()).getDataElement();
            doInst = doInstApiInstance.getDataObjectInstance(doInst.getId()).getInstance();
            deInst = deInstApiInstance.getDataElementInstance(deInst.getId()).getInstance();
            dataValue = dvApiInstance.getDataValueDirectly(dataValue.getId()).getDataValue();

            // Shutdown the server
            shutdownEnvironment();

            // Start the environment again
            setupEnvironment();

            // Try to query all created objects and check if they are loaded correctly
            DataObjectWithLinks doReloaded = dataObjectApiInstance.getDataObjectById(dObject.getId());
            assertEquals(dObject, doReloaded.getDataObject());

            DataElementWithLinks deReloaded = dataElementApiInstance.getDataElementDirectly(dElement.getId());
            assertEquals(dElement, deReloaded.getDataElement());

            DataObjectInstanceWithLinks doInstReloaded = doInstApiInstance.getDataObjectInstance(doInst.getId());
            assertEquals(doInst, doInstReloaded.getInstance());

            DataElementInstanceWithLinks deInstReloaded = deInstApiInstance.getDataElementInstance(deInst.getId());
            assertEquals(deInst, deInstReloaded.getInstance());

            DataValueWithLinks dataValueReloaded = dvApiInstance.getDataValueDirectly(dataValue.getId());
            assertEquals(dataValue, dataValueReloaded.getDataValue());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    public static void shutdownEnvironment() {
        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        // Cleanup the database
        MongoClient dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        MongoDatabase dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());

        dataStore.getCollection(ModelConstants.DATA_VALUE__DATA_COLLECTION).drop();
        dataStore.getCollection("dataElementInstances").drop();
        dataStore.getCollection("dataObjectInstances").drop();
        dataStore.getCollection("dataValues").drop();
        dataStore.getCollection("dataElements").drop();
        dataStore.getCollection("dataObjects").drop();

        dataStoreClient.close();

        // Stop the server
        try {
            server.stopHTTPServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
