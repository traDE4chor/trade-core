package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by hahnml on 27.10.2016.
 */
public class DataElementTest {

    private DataObject obj = null;

    @Before
    public void createDataObject() {
        try {
            this.obj = new DataObject();
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void dataElementShouldBeReady() throws Exception {
        DataElement elm = new DataElement(obj);
        elm.initialize();
        assertEquals(DataElementLifeCycle.States.READY.name(), elm.getState());
    }

    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}