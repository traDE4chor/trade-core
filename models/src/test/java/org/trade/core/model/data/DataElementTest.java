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

import org.junit.Before;
import org.junit.Test;
import org.trade.core.utils.states.ModelStates;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link DataElement} model object.
 * <p>
 * Created by hahnml on 27.10.2016.
 */
public class DataElementTest {

    private DataObject obj = null;

    private String entity = "someEntity";

    private String doName = "dataObject1";

    @Before
    public void createDataObject() {
        this.obj = new DataObject(entity, doName);
    }

    @Test
    public void dataElementShouldBeReady() throws Exception {
        String name = "dataElement";
        DataElement elm = new DataElement(obj, entity, name);
        elm.initialize();

        assertEquals(ModelStates.READY.name(), elm.getState());
        assertEquals(entity, elm.getEntity());
    }

    // TODO: 28.10.2016 Add test cases for the different life cycle transitions
}