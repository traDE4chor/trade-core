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

package org.trade.core.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concurrent hash map supporting the persistence of managed model objects. In order to allow the identification of
 * the model objects they have to extend the {@link PersistableObject} interface.
 * <p>
 * The implementation uses an {@link IPersistenceProvider} implementation which is identified through the
 * corresponding configuration of the middleware (i.e., {@link org.trade.core.utils.TraDEProperties}) to trigger the
 * storing and loading of managed model objects.
 * <p>
 * All available objects are loaded on instantiation of the map by default.
 * <p>
 * Created by hahnml on 24.04.2017.
 */
public class PersistableHashMap<V extends PersistableObject> extends ConcurrentHashMap<String, V> {

    private static final long serialVersionUID = -9007417974064859966L;

    private Logger logger = LoggerFactory.getLogger("org.trade.core.persistence.PersistableHashMap");

    /**
     * Instantiates a new Persistable hash map for the given type of model objects.
     *
     * @param objectType the object type that will be managed and persisted by this map.
     */
    public PersistableHashMap(Class<V> objectType) {
        super();

        // Create a new persistence provider for loading all objects
        IPersistenceProvider persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider
                (objectType);

        Map<String, V> map = null;
        try {
            map = persistProv.loadAllObjects(null);
        } catch (Exception e) {
            logger.error("Loading of all objects of type '" + objectType.getName() + "' caused an exception.", e);
        }

        // Use supertype methods without persistence support to add map elements during loading
        if (map != null) {
            super.putAll(map);
        }
    }

    @Override
    public V put(String key, V value) {
        V previous = super.put(key, value);

        // Write the new/changed object to data source
        try {
            value.storeToDS();
        } catch (Exception e) {
            logger.error("Storing object of type '" + value.getClass().getName() + "' with ID='" + key + "' caused an" +
                    " exception.", e);
        }

        return previous;
    }

    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        super.putAll(m);

        // Write all new/changed objects to data source
        try {
            for (Map.Entry<? extends String, ? extends V> e : m.entrySet()) {
                if (e.getValue() != null) {
                    e.getValue().storeToDS();
                }
            }
        } catch (Exception e) {
            logger.error("Storing of multiple objects at once caused an exception.", e);
        }
    }

    @Override
    public V remove(Object key) {
        V previous = super.remove(key);

        // Delete the object from data source
        if (previous != null) {
            try {
                previous.deleteFromDS();
            } catch (Exception e) {
                logger.error("Deleting object with ID='" + key + "' caused an exception.", e);
            }
        }

        return previous;
    }

    @Override
    public V putIfAbsent(String key, V value) {
        V previous = super.putIfAbsent(key, value);

        // Write the new object to data source
        try {
            // Check if there is no previous value for the key since putIfAbsent only adds a new entry if the key is
            // not already in use
            if (previous == null) {
                value.storeToDS();
            }
        } catch (Exception e) {
            logger.error("Storing object of type '" + value.getClass().getName() + "' with ID='" + key + "' caused an" +
                    " exception.", e);
        }

        return previous;
    }

    @Override
    public boolean remove(Object key, Object value) {
        boolean isRemoved = super.remove(key, value);

        // Delete the object from data source, if it was removed from the map
        if (isRemoved) {
            try {
                ((V) value).deleteFromDS();
            } catch (Exception e) {
                logger.error("Deleting object of type '" + value.getClass().getName() + "' with ID='" + key + "' " +
                        "caused an exception.", e);
            }
        }

        return isRemoved;
    }

    @Override
    public boolean replace(String key, V oldValue, V newValue) {
        boolean isReplaced = super.replace(key, oldValue, newValue);

        // Write the new/changed object to data source, if it was replaced in the map
        try {
            if (isReplaced) {
                newValue.storeToDS();
            }
        } catch (Exception e) {
            logger.error("Replacing object of type '" + oldValue.getClass().getName() + "' with ID='" + key + "' " +
                    " with a new object instance caused an exception.", e);
        }

        return isReplaced;
    }

    @Override
    public V replace(String key, V value) {
        V previous = super.replace(key, value);

        // Write the new/changed object to data source
        try {
            value.storeToDS();
        } catch (Exception e) {
            logger.error("Replacing object of type '" + value.getClass().getName() + "' with ID='" + key + "' " +
                    " with another object instance caused an exception.", e);
        }

        return previous;
    }
}
