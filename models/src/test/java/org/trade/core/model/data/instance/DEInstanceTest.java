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

import org.junit.Before;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.lifecycle.LifeCycleException;

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

            elm = new DataElement(obj, entity, deName);
            elm.initialize();
        } catch (LifeCycleException e) {
            e.printStackTrace();
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: 07.04.2017
}