package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.data.instance.DEInstance;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import de.unistuttgart.iaas.trade.model.lifecycle.LifeCycleException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 27.10.2016.
 */
public class DataElementTest {

    private DataObject obj = null;

    private String entity = "someEntity";

    private String doName = "dataObject1";

    @Before
    public void createDataObject() {
        try {
            this.obj = new DataObject(entity, doName);
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void dataElementShouldBeReady() throws Exception {
        String name = "dataElement";
        DataElement elm = new DataElement(obj, name);
        elm.initialize();

        assertEquals(DataElementLifeCycle.States.READY.name(), elm.getState());
        assertEquals(entity, elm.getUrn().getNamespaceIdentifier
                ());
        assertEquals(doName + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + name, elm.getUrn()
                .getNamespaceSpecificString());
    }

    @Test(expected = LifeCycleException.class)
    public void instantiationOfDataElementShouldCauseException() throws Exception {
        DataElement elm = new DataElement(obj);

        elm.instantiate("owner", "createdFor");
    }

    @Test
    public void testDataElementInstantiation() throws Exception {
        DataElement elm = new DataElement(obj);
        elm.initialize();

        DEInstance inst = elm.instantiate("owner", "createdFor");

        assertNotNull(inst);
    }
    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}