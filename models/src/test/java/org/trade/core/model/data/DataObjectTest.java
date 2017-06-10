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

package org.trade.core.model.data;

import org.junit.Test;
import org.trade.core.model.lifecycle.LifeCycleException;
import org.trade.core.utils.states.ModelStates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/**
 * Test class for {@link DataObject} model object.
 * <p>
 * Created by hahnml on 27.10.2016.
 */
public class DataObjectTest {

    @Test
    public void testStartState() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");
        assertEquals(ModelStates.INITIAL.name(), obj.getState());
    }

    @Test(expected = LifeCycleException.class)
    public void testNotAllowedStateTransition() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");
        obj.archive();
    }

    @Test(expected = LifeCycleException.class)
    public void addingDataElementShouldBeRejected() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");

        DataElement elm = new DataElement(obj);
        obj.addDataElement(elm);
    }

    @Test
    public void dataElementShouldBeAdded() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");

        DataElement elm = new DataElement(obj);
        elm.initialize();

        assertFalse(obj.getDataElements().isEmpty());
        assertEquals(ModelStates.READY.name(), obj.getState());
    }

    @Test
    public void dataElementShouldBeDeleted() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");

        DataElement elm = new DataElement(obj);
        elm.initialize();

        obj.deleteDataElement(elm);

        assertTrue(obj.getDataElements().isEmpty());
        assertEquals(ModelStates.INITIAL.name(), obj.getState());
    }

    @Test
    public void dataObjectShouldBeDeleted() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");

        DataElement elm = new DataElement(obj);
        elm.initialize();

        obj.delete();

        assertNull(obj.getDataElements());
        assertEquals(obj.getState(), ModelStates.DELETED.name());
        assertEquals(elm.getState(), ModelStates.DELETED.name());
    }

    @Test
    public void testDataObjectSerializationAndDeserialization() throws Exception {
        DataObject obj = new DataObject("modelA", "inputData");
        DataElement elm = new DataElement(obj);
        elm.initialize();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        byte[] array = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DataObject obj2 = (DataObject) ois.readObject();

        assertEquals(obj.getIdentifier(), obj2.getIdentifier());
        assertEquals(obj.getState(), obj2.getState());

        obj2.archive();

        assertEquals(obj2.getState(), ModelStates.ARCHIVED.name());
    }

    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}