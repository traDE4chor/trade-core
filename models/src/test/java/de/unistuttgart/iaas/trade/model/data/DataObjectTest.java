package de.unistuttgart.iaas.trade.model.data;

import de.unistuttgart.iaas.trade.model.lifecycle.DataObjectLifeCycle;
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

    @Test
    public void testNotAllowedTransition() throws Exception {
        DataObject obj = new DataObject();
        obj.archive();
        assertEquals(DataObjectLifeCycle.States.INITIAL.name(), obj.getState());
    }

    @Test
    public void addDataElementShouldBeRejected() throws Exception {
        DataObject obj = new DataObject();

        DataElement elm = new DataElement(obj);
        obj.addDataElement(elm);

        assertTrue(obj.getDataElements().isEmpty());
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

    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}