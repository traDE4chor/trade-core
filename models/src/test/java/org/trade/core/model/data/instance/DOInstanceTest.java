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
import org.trade.core.model.lifecycle.LifeCycleException;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link DataObjectInstance} model object.
 * <p>
 * Created by hahnml on 11.11.2016.
 */
public class DOInstanceTest {

    private String entity = "someEntity";

    private String doName = "dataObject1";

    @Test(expected = LifeCycleException.class)
    public void instantiationOfDataObjectShouldCauseException() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");

        HashMap<String, String> correlationProps = new HashMap<>();
        correlationProps.put("customerId", "1234");

        DataObjectInstance inst = obj.instantiate("owner", correlationProps);
    }

    @Test
    public void testDataObjectInstantiation() throws Exception {
        DataObject obj = new DataObject(entity, doName);
        DataElement elm = new DataElement(obj);
        elm.initialize();

        HashMap<String, String> correlationProps = new HashMap<>();
        correlationProps.put("customerId", "1234");

        DataObjectInstance inst = obj.instantiate("owner", correlationProps);

        assertNotNull(inst);
        assertTrue(inst.isCreated());
    }
}