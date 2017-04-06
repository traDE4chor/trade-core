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

import de.slub.urn.URNSyntaxException;
import org.trade.core.model.ModelUtils;
import org.trade.core.model.data.DataObject;
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
        DOInstance inst = new DOInstance(obj.getUrn(), "owner");

        assertEquals(entity, inst.getUrn().getNamespaceIdentifier
                ());
        assertEquals(doName + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + inst.getInstanceID(), inst.getUrn()
                .getNamespaceSpecificString());
    }
}