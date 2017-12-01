/*
 * Copyright 2017 Michael Hahn
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

package org.trade.core.persistence.local.filesystem;

import org.junit.Test;
import org.trade.core.model.data.DataValue;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.PersistableObject;
import org.trade.core.utils.TraDEProperties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by hahnml on 30.11.2017.
 */
public class FileSystemPersistenceTest {

    @Test
    public void dataValueShouldBeStoredAndLoaded() throws Exception {
        IPersistenceProvider persistProv = new FileSystemPersistence();
        persistProv.initProvider(DataValue.class, new TraDEProperties());

        DataValue value = new DataValue("hahnml", "testValue");

        String type = "binary";
        String contentType = "text/plain";

        value.setType(type);
        value.setContentType(contentType);

        value.storeToDS();

        PersistableObject loadedValue = persistProv.loadObject(value.getIdentifier());

        assertEquals(value, loadedValue);

        persistProv.deleteObject(value.getIdentifier());

        loadedValue = persistProv.loadObject(value.getIdentifier());

        assertNull(loadedValue);
    }
}
