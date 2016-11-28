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

package org.trade.core.persistence;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataValue;
import org.trade.core.utils.TraDEProperties;

import java.util.*;

/**
 * Created by hahnml on 23.11.2016.
 */
public class DataValueStore implements MapStore<String, DataValue>, MapLoaderLifecycleSupport {

    private Datastore store;

    public DataValueStore() {
    }

    private final String IDENTIFIER_FIELD = "identifier";

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        String mongoUrl = (String) properties.get("mongo.url");
        String dbName = (String) properties.get("mongo.db");

        Morphia morphia = new Morphia();

        // Map model classes to db collections
        morphia.mapPackage("org.trade.core.model.data");

        // create the datastore connecting to mongo
        this.store = morphia.createDatastore(new MongoClient(new MongoClientURI(mongoUrl)), dbName);
        this.store.ensureIndexes();
    }

    @Override
    public void destroy() {
        this.store.getMongo().close();
    }

    @Override
    public DataValue load(String key) {
        return this.store.createQuery(DataValue.class).field(IDENTIFIER_FIELD).equal(key).limit(1).get();
    }

    @Override
    public Map<String, DataValue> loadAll(Collection keys) {
        HashMap<String, DataValue> result = new HashMap<String, DataValue>();

        List<DataValue> objs = this.store.createQuery(DataValue.class).filter("identifier in", keys).asList();
        for (DataValue obj : objs) {
            result.put(obj.getIdentifier(), obj);
        }

        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        List<String> keys = new LinkedList<String>();

        List<DataValue> objs = this.store.createQuery(DataValue.class).asList();
        for (DataValue obj : objs) {
            keys.add(obj.getIdentifier());
        }
        return keys;
    }

    @Override
    public void store(String key, DataValue value) {
        this.store.save(value);
    }

    @Override
    public void storeAll(Map<String, DataValue> map) {
        this.store.save(map.values());
    }

    @Override
    public void delete(String key) {
        Query<DataValue> toDelete = this.store.createQuery(DataValue.class).field(IDENTIFIER_FIELD).equal(key);
        this.store.delete(toDelete);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        Query<DataValue> toDelete = this.store.createQuery(DataValue.class).filter("identifier in", keys);
        this.store.delete(toDelete);
    }
}