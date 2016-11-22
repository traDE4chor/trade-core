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
import org.trade.core.model.data.DataObject;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;

import java.util.*;

import static com.mongodb.client.model.Filters.in;

public class DataObjectStore implements MapStore<String, DataObject>, MapLoaderLifecycleSupport {

    private Datastore store;

    public DataObjectStore() {
    }

    private final String COLLECTION_NAME = "dataObjects";

    private final String URN_FIELD = "urn";

    private final String ENTITY_FIELD = "entity";

    private final String NAME_FIELD = "name";

    @Override
    public void init(HazelcastInstance hazelcastInstance, Properties properties, String mapName) {
        String mongoUrl = (String) properties.get("mongo.url");
        String dbName = (String) properties.get("mongo.db");

        Morphia morphia = new Morphia();

        // tell Morphia where to find your classes
        // can be called multiple times with different packages or classes
        morphia.mapPackage("de.unistuttgart.iaas.trade.model.data");

        // create the Datastore connecting to mongo
        this.store = morphia.createDatastore(new MongoClient(new MongoClientURI(mongoUrl)), dbName);
        this.store.ensureIndexes();
    }

    @Override
    public void destroy() {
        this.store.getMongo().close();
    }

    @Override
    public DataObject load(String key) {
        System.out.println("Load " + key);
        return this.store.createQuery(DataObject.class).field(URN_FIELD).equal(key).asList().get(0);
    }

    @Override
    public Map<String, DataObject> loadAll(Collection keys) {
        System.out.println("LoadAll " + keys);
        HashMap<String, DataObject> result = new HashMap<String, DataObject>();

        List<DataObject> objs = this.store.createQuery(DataObject.class).filter("urn in", keys).asList();
        for (DataObject obj : objs) {
            result.put(obj.getUrn().toString(), obj);
        }

        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        System.out.println("LoadAllKeys");
        List<String> keys = new LinkedList<String>();

        List<DataObject> objs = this.store.createQuery(DataObject.class).asList();
        for (DataObject obj : objs) {
            keys.add(obj.getUrn().toString());
        }
        return keys;
    }

    @Override
    public void store(String key, DataObject value) {
        this.store.save(value);
    }

    @Override
    public void storeAll(Map<String, DataObject> map) {
        this.store.save(map.values());
    }

    @Override
    public void delete(String key) {
        Query<DataObject> toDelete = this.store.createQuery(DataObject.class).field(URN_FIELD).equal(key);
        this.store.delete(toDelete);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        Query<DataObject> toDelete = this.store.createQuery(DataObject.class).filter("urn in", keys);
        this.store.delete(toDelete);
    }
}