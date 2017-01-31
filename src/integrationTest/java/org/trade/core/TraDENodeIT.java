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

package org.trade.core;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import de.slub.urn.URNSyntaxException;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.Binary;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.DataValue;
import org.trade.core.utils.TraDEProperties;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 29.11.2016.
 */
public class TraDENodeIT {

    private static HazelcastInstance instance;

    private static Datastore cacheStore;

    private static MongoDatabase dataStore;
    private static MongoClient dataStoreClient;

    private static TraDEProperties properties;

    @BeforeClass
    public static void setupEnvironment() {
        // Load custom properties such as MongoDB url and db name
        properties = new TraDEProperties();

        // Apply the properties to the XML config
        XmlConfigBuilder builder = new XmlConfigBuilder();
        builder.setProperties(properties);
        Config config = builder.build();

        instance = Hazelcast.newHazelcastInstance(config);

        Morphia morphia = new Morphia();

        // Map model classes to db collections
        morphia.mapPackage("org.trade.core.model.data");

        // create the datastore connecting to mongo holding the cache data
        cacheStore = morphia.createDatastore(new MongoClient(new MongoClientURI(properties
                .getCacheDbUrl())), properties.getCacheDbName());
        cacheStore.ensureIndexes();

        dataStoreClient = new MongoClient(new MongoClientURI(properties.getDataPersistenceDbUrl()));
        dataStore = dataStoreClient.getDatabase(properties.getDataPersistenceDbName());
    }

    @Test
    public void createDataObjectsTest() {
        List<String> keys = new ArrayList<String>();
        keys.add("urn:chorModel1:lattice");
        keys.add("urn:userA:plot");
        keys.add("urn:random:input");

        try {
            IMap<String, DataObject> dataObjects = instance.getMap("dataObjects");

            assertEquals(0, cacheStore.createQuery(DataObject.class).asList().size());

            if (!dataObjects.containsKey(keys.get(0))) {
                DataObject obj = new DataObject("chorModel1", "lattice");
                dataObjects.set(obj.getIdentifier(), obj);
            }

            if (!dataObjects.containsKey(keys.get(1))) {
                DataObject obj = new DataObject("userA", "plot");
                dataObjects.set(obj.getIdentifier(), obj);
            }

            if (!dataObjects.containsKey(keys.get(2))) {
                DataObject obj = new DataObject("random", "input");
                dataObjects.set(obj.getIdentifier(), obj);
            }

            for (String key : keys) {
                DataObject obj = (DataObject) instance.getMap("dataObjects").get(key);
                assertNotNull(obj);
            }

            assertEquals(3, instance.getMap("dataObjects").size());
            assertEquals(3, cacheStore.createQuery(DataObject.class).asList().size());
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createDataValuesTest() {
        try {
            IMap<String, DataValue> dataValues = instance.getMap("dataValues");

            assertEquals(0, cacheStore.createQuery(DataValue.class).asList().size());

            List<String> dvKeys = new ArrayList<String>();
            for (DataValue value : cacheStore.createQuery(DataValue.class).retrievedFields(true, "identifier").asList()) {
                dvKeys.add(value.getIdentifier());
            }

            if (dvKeys.isEmpty()) {
                DataValue value = new DataValue("hahnml", "simA");

                try {
                    InputStream in = getClass().getResourceAsStream("/data.dat");

                    byte[] data = IOUtils.toByteArray(in);
                    assertNotNull(data);

                    value.setData(data, data.length);

                    MongoCollection<Document> collection = dataStore.getCollection("dataCollection");
                    Document doc = collection.find(Filters.eq("urn", value.getIdentifier())).limit(1).first();
                    assertNotNull(((Binary) doc.get("data")).getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                dvKeys.add(value.getIdentifier());

                dataValues.set(value.getIdentifier(), value);

                assertEquals(1, instance.getMap("dataValues").size());
                assertEquals(1, cacheStore.createQuery(DataValue.class).asList().size());
            }
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void destroy() {
        cacheStore.createQuery(DataObject.class).getCollection().drop();
        cacheStore.createQuery(DataValue.class).getCollection().drop();

        dataStore.getCollection("dataCollection").drop();
        dataStoreClient.close();

        instance.shutdown();
        cacheStore.getMongo().close();
    }
}