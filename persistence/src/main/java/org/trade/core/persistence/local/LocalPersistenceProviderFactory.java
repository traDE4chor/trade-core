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

package org.trade.core.persistence.local;

import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.PersistableObject;
import org.trade.core.persistence.local.filesystem.FileSystemPersistence;
import org.trade.core.persistence.local.mongo.MongoPersistence;
import org.trade.core.utils.TraDEProperties;

/**
 * This factory creates a new {@link IPersistenceProvider} object based on the specified objectType and
 * configuration of the middleware (i.e., {@link org.trade.core.utils.TraDEProperties}).
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class LocalPersistenceProviderFactory {

    /**
     * Creates a local persistence provider based on the specified properties.
     *
     * @return the local persistence provider according to the defined property in 'config.properties'.
     */
    public static <T extends PersistableObject> IPersistenceProvider<T> createLocalPersistenceProvider(Class<T> objectType) {
        IPersistenceProvider<T> result = null;

        TraDEProperties props = new TraDEProperties();

        switch (props.getDataPersistenceMode()) {
            case DB:
                result = new MongoPersistence();
                result.initProvider(objectType, props);
                break;
            case FILE:
                result = new FileSystemPersistence();
                result.initProvider(objectType, props);
                break;
            default:
                result = new FileSystemPersistence();
                result.initProvider(objectType, props);
        }

        return result;
    }
}
