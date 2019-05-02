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

import io.swagger.trade.client.jersey.ApiException;
import io.swagger.trade.client.jersey.model.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 30.11.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalPersistenceProviderIT {

    private static IntegrationTestEnvironment env;

    @BeforeClass
    public static void setupEnvironment() {
        env = new IntegrationTestEnvironment();
        env.setupEnvironment(false);
    }

    @Test
    public void createAndLoadDataObjectAfterShutdownTest() {
        try {
            // Start the TraDE server
            env.startTraDEServer();

            // Create a new data object
            DataObjectData request = new DataObjectData();

            request.setEntity("hahnml");
            request.setName("inputData");

            DataObject result = env.getDataObjectApi().addDataObject(request);

            // Shutdown the server
            env.stopTraDEServer();

            // Start the environment again
            env.startTraDEServer();

            // Try to query the data object and check if it's loaded correctly
            DataObjectWithLinks reloaded = env.getDataObjectApi().getDataObjectById(result.getId());

            assertEquals(result, reloaded.getDataObject());

            // Delete the data object
            env.getDataObjectApi().deleteDataObject(reloaded.getDataObject().getId());

            DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
            assertEquals(0, objects.getDataObjects().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createAndLoadDataObjectHierarchyAfterShutdownTest() {
        try {
            // Start the TraDE server
            env.startTraDEServer();

            // Create a new data object
            DataObject dObject = env.getDataObjectApi().addDataObject(new DataObjectData().entity("hahnml").name
                    ("inputData"));
            assertNotNull(dObject);

            // Add a data element to it
            DataElement dElement = env.getDataElementApi().addDataElement(dObject.getId(), new DataElementData().name
                    ("testElement").entity("hahnml").type("string")).getDataElement();
            assertNotNull(dElement);

            // Create a new data object instance
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            DataObjectInstance doInst = env.getDataObjectInstanceApi().addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            // Create a data value and associate it to the data element instance
            DataValue dataValue = env.getDataValueApi().addDataValue(new DataValueData().name("inputData").createdBy("hahnml")
                    .type("string"));
            assertNotNull(dataValue);

            DataElementInstance deInst = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst.getId(),
                    dElement.getName()).getInstance();
            assertNotNull(deInst);

            // Associate the generated data value to the data element instance
            env.getDataValueApi().associateDataValueToDataElementInstance(deInst.getId(),
                    new DataValue().id(dataValue.getId()));

            // Upload some data for the data value
            env.getDataValueApi().pushDataValue(dataValue.getId(), "test".getBytes(), null, 4L);

            // Update the local values since the objects will have changed at the server (lifecycle states, etc.)
            dObject = env.getDataObjectApi().getDataObjectById(dObject.getId()).getDataObject();
            dElement = env.getDataElementApi().getDataElementDirectly(dElement.getId()).getDataElement();
            doInst = env.getDataObjectInstanceApi().getDataObjectInstance(doInst.getId()).getInstance();
            deInst = env.getDataElementInstanceApi().getDataElementInstance(deInst.getId()).getInstance();
            dataValue = env.getDataValueApi().getDataValueDirectly(dataValue.getId()).getDataValue();

            // Shutdown the server
            env.stopTraDEServer();

            // Start the server again
            env.startTraDEServer();

            // Try to query all created objects and check if they are loaded correctly
            DataObjectWithLinks doReloaded = env.getDataObjectApi().getDataObjectById(dObject.getId());
            assertEquals(dObject, doReloaded.getDataObject());

            DataElementWithLinks deReloaded = env.getDataElementApi().getDataElementDirectly(dElement.getId());
            assertEquals(dElement, deReloaded.getDataElement());

            DataObjectInstanceWithLinks doInstReloaded = env.getDataObjectInstanceApi().getDataObjectInstance(doInst.getId());
            assertEquals(doInst, doInstReloaded.getInstance());

            DataElementInstanceWithLinks deInstReloaded = env.getDataElementInstanceApi().getDataElementInstance(deInst.getId());
            assertEquals(deInst, deInstReloaded.getInstance());

            DataValueWithLinks dataValueReloaded = env.getDataValueApi().getDataValueDirectly(dataValue.getId());
            assertEquals(dataValue, dataValueReloaded.getDataValue());

            // Remove the association to the data element instance and delete data value
            env.getDataValueApi().removeDataValueFromDataElementInstance(deInst.getId(),
                    dataValueReloaded.getDataValue().getId());

            // Delete the data object
            env.getDataObjectApi().deleteDataObject(doReloaded.getDataObject().getId());

            // Delete the data value
            env.getDataValueApi().deleteDataValue(dataValueReloaded.getDataValue().getId());

            DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
            assertEquals(0, objects.getDataObjects().size());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        env.destroyEnvironment();
        env = null;
    }
}
