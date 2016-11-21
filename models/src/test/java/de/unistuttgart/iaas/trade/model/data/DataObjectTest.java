package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URN;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.data.instance.DOInstance;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.DataObjectLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.LifeCycleException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 27.10.2016.
 */
public class DataObjectTest {

    @Test
    public void testStartState() throws Exception {
        DataObject obj = new DataObject();
        assertEquals(DataObjectLifeCycle.States.INITIAL.name(), obj.getState());
    }

    @Test(expected = LifeCycleException.class)
    public void testNotAllowedStateTransition() throws Exception {
        DataObject obj = new DataObject();
        obj.archive();
    }

    @Test(expected = LifeCycleException.class)
    public void addingDataElementShouldBeRejected() throws Exception {
        DataObject obj = new DataObject();

        DataElement elm = new DataElement(obj);
        obj.addDataElement(elm);
    }

    @Test
    public void dataElementShouldBeAdded() throws Exception {
        DataObject obj = new DataObject();

        DataElement elm = new DataElement(obj);
        elm.initialize();

        obj.addDataElement(elm);

        assertFalse(obj.getDataElements().isEmpty());
        assertEquals(DataObjectLifeCycle.States.READY.name(), obj.getState());
    }

    @Test
    public void dataElementShouldBeDeleted() throws Exception {
        DataObject obj = new DataObject();

        DataElement elm = new DataElement(obj);
        elm.initialize();

        obj.addDataElement(elm);
        obj.deleteDataElement(elm);

        assertTrue(obj.getDataElements().isEmpty());
        assertEquals(DataObjectLifeCycle.States.INITIAL.name(), obj.getState());
    }

    @Test
    public void dataObjectShouldBeDeleted() throws Exception {
        DataObject obj = new DataObject();

        DataElement elm = new DataElement(obj);
        elm.initialize();

        obj.addDataElement(elm);

        obj.delete();

        assertNull(obj.getDataElements());
        assertEquals(obj.getState(), DataObjectLifeCycle.States.DELETED.name());
        assertNull(elm.getUrn());
        assertEquals(elm.getState(), DataElementLifeCycle.States.DELETED.name());
    }

    @Test
    public void testDataObjectSerializationAndDeserialization() throws Exception {
        DataObject obj = new DataObject();
        DataElement elm = new DataElement(obj);
        elm.initialize();
        obj.addDataElement(elm);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();

        byte[] array = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(array);
        ObjectInputStream ois = new ObjectInputStream(bais);
        DataObject obj2 = (DataObject) ois.readObject();

        assertEquals(obj.getUrn(), obj2.getUrn());
        assertEquals(obj.getState(), obj2.getState());

        obj2.archive();

        assertEquals(obj2.getState(), DataObjectLifeCycle.States.ARCHIVED.name());
    }

    @Test(expected = LifeCycleException.class)
    public void instantiationOfDataObjectShouldCauseException() throws Exception {
        DataObject obj = new DataObject();

        DOInstance inst = obj.instantiate("owner", "createdFor");
    }

    @Test
    public void testDataObjectInstantiation() throws Exception {
        DataObject obj = new DataObject();

        DataElement elm = new DataElement(obj);
        elm.initialize();

        obj.addDataElement(elm);

        DOInstance inst = obj.instantiate("owner", "createdFor");

        assertNotNull(inst);
    }

    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}