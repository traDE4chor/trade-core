package de.unistuttgart.iaas.trade.model.data.instance;

import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;
import de.unistuttgart.iaas.trade.model.data.DataElement;
import de.unistuttgart.iaas.trade.model.data.DataObject;
import de.unistuttgart.iaas.trade.model.lifecycle.DataElementLifeCycle;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 11.11.2016.
 */
public class DOInstanceTest {

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
    public void testURNGeneration() throws Exception {
        DOInstance inst = new DOInstance(obj.getUrn(), "owner", "createdFor");

        assertEquals(entity, inst.getUrn().getNamespaceIdentifier
                ());
        assertEquals(doName + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + inst.getInstanceID(), inst.getUrn()
                .getNamespaceSpecificString());
    }
}