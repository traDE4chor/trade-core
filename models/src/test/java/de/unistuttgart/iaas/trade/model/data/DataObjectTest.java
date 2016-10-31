package de.unistuttgart.iaas.trade.model.data;

import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.DataObjectLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.LifeCycleException;
import org.junit.Before;
import org.junit.Test;

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

    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}