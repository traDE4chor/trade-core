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

package org.trade.core.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MapLoaderLifecycleSupport;
import com.hazelcast.core.MapStore;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.trade.core.model.ABaseResource;

import java.util.*;

/**
 * Abstract class for the persistence of TraDE model objects ({@link ABaseResource}) for Hazelcast maps.
 * <p>
 * Created by hahnml on 29.11.2016.
 */
public abstract class AMongoStore<T extends ABaseResource> implements MapStore<String, T>, MapLoaderLifecycleSupport {

    private Datastore store;

    protected Class<T> resourceType;

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
    public T load(String key) {
        return this.store.createQuery(resourceType).field(IDENTIFIER_FIELD).equal(key).limit
                (1).get();
    }

    @Override
    public Map<String, T> loadAll(Collection keys) {
        HashMap<String, T> result = new HashMap<String, T>();

        List<T> objs = this.store.createQuery(resourceType).filter("identifier in", keys).asList();
        for (T obj : objs) {
            result.put(obj.getIdentifier(), obj);
        }

        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        List<String> keys = new LinkedList<String>();

        List<T> objs = this.store.createQuery(resourceType).asList();
        for (T obj : objs) {
            keys.add(obj.getIdentifier());
        }
        return keys;
    }

    @Override
    public void store(String key, T value) {
        this.store.save(value);
    }

    @Override
    public void storeAll(Map<String, T> map) {
        this.store.save(map.values());
    }

    @Override
    public void delete(String key) {
        Query<T> toDelete = this.store.createQuery(resourceType).field(IDENTIFIER_FIELD).equal(key);
        this.store.delete(toDelete);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        Query<T> toDelete = this.store.createQuery(resourceType).filter("identifier in", keys);
        this.store.delete(toDelete);
    }

}
