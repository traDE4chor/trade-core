/*
 * Copyright 2018 Michael Hahn
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

import static org.junit.Assert.*;

/**
 * Created by hahnml on 10.05.2017.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiValueDataElementIT {

    private static IntegrationTestEnvironment env;

    @BeforeClass
    public static void setupEnvironment() {
        env = new IntegrationTestEnvironment();

        env.setupEnvironment(true);
    }

    @Test
    public void createMultiValueDataElementTest() throws Exception {
        try {
            // Create a data object with a multi-value data element
            // object
            DataObject dObject = env.getDataObjectApi().addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = env.getDataElementApi().addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain").isCollectionElement
                    (true)).getDataElement();
            assertNotNull(dElement);
            assertTrue(dElement.isIsCollectionElement());

            DataElementWithLinks dElm = env.getDataElementApi().getDataElementDirectly(dElement.getId());
            assertNotNull(dElm.getDataElement());
            assertTrue(dElm.getDataElement().isIsCollectionElement());

            env.getDataObjectApi().deleteDataObject(dObject.getId());

            DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
            assertEquals(0, objects.getDataObjects().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void instantiateMultiValueDataElementTest() throws Exception {
        try {
            // Create a data object with a multi-value data element and then instantiating the data
            // object
            DataObject dObject = env.getDataObjectApi().addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = env.getDataElementApi().addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain").isCollectionElement
                    (true)).getDataElement();
            assertNotNull(dElement);
            assertTrue(dElement.isIsCollectionElement());

            // Set correlation property
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            // Create a new data object instance
            DataObjectInstance doInst = env.getDataObjectInstanceApi().addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            DataElementInstanceWithLinks elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                            .getId(), dElement.getName());
            assertNotNull(elmInstance);
            assertNotNull(elmInstance.getInstance());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Create and associated two data values to the data element instance
            DataValueWithLinks firstDataValue = env.getDataValueApi().associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data1").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(firstDataValue);
            assertNotNull(firstDataValue.getDataValue());

            DataValueWithLinks secondDataValue = env.getDataValueApi().associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data2").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(secondDataValue);
            assertNotNull(secondDataValue.getDataValue());

            // Data element instance should still be in state CREATED
            elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());
            assertEquals(2, (int) elmInstance.getInstance().getNumberOfDataValues());

            // Assign data to the first data value
            env.getDataValueApi().pushDataValue(firstDataValue.getDataValue().getId(), "test".getBytes(), null, 4L);

            // Data element instance should still be in state CREATED
            elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Assign data to the second data value
            env.getDataValueApi().pushDataValue(secondDataValue.getDataValue().getId(), "abcd".getBytes(), null, 4L);

            // Data element instance should be be in state INITIALIZED now
            elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.INITIALIZED, elmInstance.getInstance().getStatus());

            // Unset data from the second data value
            env.getDataValueApi().pushDataValue(secondDataValue.getDataValue().getId(), new byte[0], null, 0L);

            // Data element instance should be be in state CREATED again
            elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Remove the second data value from the element instance
            env.getDataValueApi().removeDataValueFromDataElementInstance(elmInstance.getInstance().getId(), secondDataValue
                    .getDataValue().getId());

            // Data element instance should be be in state INITIALIZED again and only have one data value
            elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());

            assertEquals(1, (int) elmInstance.getInstance().getNumberOfDataValues());
            assertEquals(InstanceStatusEnum.INITIALIZED, elmInstance.getInstance().getStatus());

            // Delete data object and all related data elements, instances, etc. to remove data value associations
            env.getDataObjectApi().deleteDataObject(dObject.getId());

            DataObjectInstanceArrayWithLinks instances = env.getDataObjectInstanceApi().getAllDataObjectInstances(null, null, null);
            assertEquals(0, instances.getInstances().size());

            DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
            assertEquals(0, objects.getDataObjects().size());

            // Delete data values
            env.getDataValueApi().deleteDataValue(firstDataValue.getDataValue().getId());
            env.getDataValueApi().deleteDataValue(secondDataValue.getDataValue().getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void querySpecificDataValueFromMultiValueDataElementTest() throws Exception {
        try {
            // Create a data object with a multi-value data element and then instantiating the data
            // object
            DataObject dObject = env.getDataObjectApi().addDataObject(new DataObjectData().name("testDataObject").entity
                    ("hahnml"));
            assertNotNull(dObject);

            DataElement dElement = env.getDataElementApi().addDataElement(dObject.getId(), new DataElementData().name
                    ("testDataElement").entity("hahnml").type("binary").contentType("text/plain").isCollectionElement
                    (true)).getDataElement();
            assertNotNull(dElement);
            assertTrue(dElement.isIsCollectionElement());

            // Set correlation property
            CorrelationPropertyArray corPropArray = new CorrelationPropertyArray();
            corPropArray.add(new CorrelationProperty().key("chorID").value("1234567"));

            // Create a new data object instance
            DataObjectInstance doInst = env.getDataObjectInstanceApi().addDataObjectInstance(dObject.getId(), new
                    DataObjectInstanceData().createdBy("hahnml").correlationProperties(corPropArray)).getInstance();
            assertNotNull(doInst);

            DataElementInstanceWithLinks elmInstance = env.getDataElementInstanceApi().getDataElementInstanceByDataElementName(doInst
                    .getId(), dElement.getName());
            assertNotNull(elmInstance);
            assertNotNull(elmInstance.getInstance());
            assertEquals(InstanceStatusEnum.CREATED, elmInstance.getInstance().getStatus());

            // Create and associated two data values to the data element instance
            DataValueWithLinks firstDataValue = env.getDataValueApi().associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data1").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(firstDataValue);
            assertNotNull(firstDataValue.getDataValue());

            DataValueWithLinks secondDataValue = env.getDataValueApi().associateDataValueToDataElementInstance(elmInstance
                    .getInstance().getId(), new DataValue()
                    .name("data2").createdBy("hahnml").type("binary").contentType("text/plain"));

            assertNotNull(secondDataValue);
            assertNotNull(secondDataValue.getDataValue());

            DataValueArrayWithLinks dvArray1 = env.getDataValueApi().getDataValues(elmInstance.getInstance().getId(), 1);
            assertNotNull(dvArray1.getDataValues());
            assertFalse(dvArray1.getDataValues().isEmpty());
            assertEquals(firstDataValue.getDataValue().getId(), dvArray1.getDataValues().get(0).getDataValue().getId());

            DataValueArrayWithLinks dvArray2 = env.getDataValueApi().getDataValues(elmInstance.getInstance().getId(), 2);
            assertNotNull(dvArray2.getDataValues());
            assertFalse(dvArray2.getDataValues().isEmpty());
            assertEquals(secondDataValue.getDataValue().getId(), dvArray2.getDataValues().get(0).getDataValue().getId());

            // Delete data object and all related data elements, instances, etc. to remove data value associations
            env.getDataObjectApi().deleteDataObject(dObject.getId());

            DataObjectArrayWithLinks objects = env.getDataObjectApi().getAllDataObjects(null, null, null, null, null);
            assertEquals(0, objects.getDataObjects().size());

            DataObjectInstanceArrayWithLinks instances = env.getDataObjectInstanceApi().getAllDataObjectInstances(null, null, null);
            assertEquals(0, instances.getInstances().size());

            // Delete data values
            env.getDataValueApi().deleteDataValue(firstDataValue.getDataValue().getId());
            env.getDataValueApi().deleteDataValue(secondDataValue.getDataValue().getId());

            DataValueArrayWithLinks values = env.getDataValueApi().getDataValuesDirectly(null, null, null, null);
            assertEquals(0, values.getDataValues().size());
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() throws Exception {
        env.destroyEnvironment();
        env = null;
    }
}
