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

package org.trade.core.model.data.instance;

import org.junit.Test;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.DataValue;
import org.trade.core.model.lifecycle.LifeCycleException;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link DataElementInstance} model object.
 * <p>
 * Created by hahnml on 11.11.2016.
 */
public class DEInstanceTest {

    private String entity = "someEntity";

    private String doName = "dataObject1";

    private String deName = "dataElement1";

    @Test(expected = LifeCycleException.class)
    public void instantiationOfDataElementShouldCauseException() throws Exception {
        DataObject obj = new DataObject(entity, doName);
        DataElement elm = new DataElement(obj, entity, deName);

        HashMap<String, String> correlationProps = new HashMap<>();

        elm.instantiate(new DataObjectInstance(obj, "someone", correlationProps), "owner", correlationProps);
    }

    @Test
    public void testDataElementInstantiation() throws Exception {
        DataObject obj = new DataObject(entity, doName);

        DataElement elm = new DataElement(obj, entity, deName);
        elm.initialize();

        HashMap<String, String> correlationProps = new HashMap<>();
        correlationProps.put("customerId", "1234");

        DataObjectInstance objInstance = obj.instantiate("someone", correlationProps);

        DataElementInstance elmInstance = elm.instantiate(objInstance, "owner",
                correlationProps);

        assertNotNull(elmInstance);
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());

        // Try to initialize the data element instance should not trigger any state change, since no data value is
        // associated to the data element instance
        elmInstance.initialize();
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());

        // Create and add a new data value
        DataValue value = new DataValue(entity, "dataValue");
        elmInstance.setDataValue(value);

        // Should not change anything since the data value is not initialized
        assertTrue(value.isCreated());
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());

        // Now add some data to the data value, this should trigger state changes
        value.setData("test".getBytes(), 4);
        assertTrue(value.isInitialized());
        assertTrue(objInstance.isInitialized());
        assertTrue(elmInstance.isInitialized());

        // Remove the data again to test if the state changes are propagated correctly
        value.setData(null, 0);
        assertTrue(value.isCreated());
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());
    }

    @Test
    public void testDataElementInstantiationWithExistingDataValue() throws Exception {
        DataObject obj = new DataObject(entity, doName);

        DataElement elm = new DataElement(obj, entity, deName);
        elm.initialize();

        HashMap<String, String> correlationProps = new HashMap<>();
        correlationProps.put("customerId", "1234");

        DataObjectInstance objInstance = obj.instantiate("someone", correlationProps);

        DataElementInstance elmInstance = elm.instantiate(objInstance, "owner",
                correlationProps);

        assertNotNull(elmInstance);
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());

        // Try to initialize the data element instance should not trigger any state change, since no data value is
        // associated to the data element instance
        elmInstance.initialize();
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());

        // Create and initialize new data value
        DataValue value = new DataValue(entity, "dataValue");
        value.setData("test".getBytes(), 4);
        assertTrue(value.isInitialized());

        // Setting the initialized data value should directly trigger corresponding state changes in all model objects
        elmInstance.setDataValue(value);
        assertTrue(elmInstance.isInitialized());
        assertTrue(objInstance.isInitialized());

        // Remove the data again to test if the state changes are propagated correctly
        value.setData(null, 0);
        assertTrue(value.isCreated());
        assertTrue(objInstance.isCreated());
        assertTrue(elmInstance.isCreated());
    }
}