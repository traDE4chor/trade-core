package de.unistuttgart.iaas.trade.model.data.instance;

import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.data.DataElement;
import de.unistuttgart.iaas.trade.model.data.DataObject;
import de.unistuttgart.iaas.trade.model.lifecycle.LifeCycleException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 11.11.2016.
 */
public class DEInstanceTest {

    private DataObject obj = null;

    private DataElement elm = null;

    private String entity = "someEntity";

    private String doName = "dataObject1";

    private String deName = "dataElement1";

    @Before
    public void createDataObject() {
        try {
            this.obj = new DataObject(entity, doName);

            elm = new DataElement(obj, deName);
            elm.initialize();
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        } catch (LifeCycleException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testURNGeneration() throws Exception {
        DEInstance inst = new DEInstance(elm.getUrn(), "owner", "createdFor");

        assertEquals(entity, inst.getUrn().getNamespaceIdentifier
                ());
        assertEquals(doName + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + deName + ModelUtils
                .URN_NAMESPACE_STRING_DELIMITER + inst.getInstanceID(), inst.getUrn()
                .getNamespaceSpecificString());
    }
}