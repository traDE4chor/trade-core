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
import de.slub.urn.URNSyntaxException;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.data.DataValue;
import org.trade.core.server.TraDEServer;
import org.trade.core.utils.TraDEProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by hahnml on 22.11.2016.
 */
public class TraDENode {

    public static void main(String[] args) {
        // Load custom properties such as MongoDB url and db name
        TraDEProperties properties = new TraDEProperties();

        TraDEServer server = new TraDEServer();

        try {
            server.startHTTPServer(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Register a shutdown hook to stop the embedded Jetty server
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                try {
                    server.stopHTTPServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));

        //testClusterAndPersistence(properties);
    }

    public static void testClusterAndPersistence(TraDEProperties properties) {
        // Apply the properties to the XML config
        XmlConfigBuilder builder = new XmlConfigBuilder();
        builder.setProperties(properties);
        Config config = builder.build();

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);

        Morphia morphia = new Morphia();

        // Map model classes to db collections
        morphia.mapPackage("org.trade.core.model.data");

        // create the datastore connecting to mongo
        Datastore store = morphia.createDatastore(new MongoClient(new MongoClientURI(properties
                .getCacheDbUrl())), properties.getCacheDbName());
        store.ensureIndexes();

        List<String> keys = new ArrayList<String>();
        keys.add("urn:chorModel1:lattice");
        keys.add("urn:userA:plot");
        keys.add("urn:random:input");

        try {
            IMap<String, DataObject> dataObjects = instance.getMap("dataObjects");
            System.out.println(dataObjects.getName() + " - " + dataObjects.size());

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

//            System.out.println(dataObjects.getName() + " - " + dataObjects.size());
//
//            dataObjects.evictAll();
//
//            System.out.println(dataObjects.getName() + " - " + dataObjects.size());
//
//            dataObjects.loadAll(true);
//
//            System.out.println(dataObjects.getName() + " - " + dataObjects.size());

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (String key : keys) {
                DataObject obj = (DataObject) instance.getMap("dataObjects").get(key);
                System.out.println("### DataObject ###");
                System.out.println("Name: " + obj.getName());
                System.out.println("Identifier: " + obj.getIdentifier());
                System.out.println("URN: " + obj.getUrn().toString());
                System.out.println("Entity: " + obj.getEntity());
                System.out.println("State: " + obj.getState());
                if (obj.getId() != null) {
                    System.out.println("ObjectID: " + obj.getId().toString());
                }
                System.out.println("##################");
            }

            IMap<String, DataValue> dataValues = instance.getMap("dataValues");
            System.out.println(dataValues.getName() + " - " + dataValues.size());

            List<String> dvKeys = new ArrayList<String>();
            for (DataValue value : store.createQuery(DataValue.class).retrievedFields(true, "identifier").asList()) {
                dvKeys.add(value.getIdentifier());
            }

            if (dvKeys.isEmpty()) {
                DataValue value = new DataValue("hahnml", "simA");

                try {
                    // byte[] data = Files.readAllBytes(Paths.get("C:\\test\\OpalMC\\scripts\\data\\000abcd0001.dat"));
                    byte[] data = Files.readAllBytes(Paths.get("C:\\test\\script.sh"));

                    value.setData(data, data.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dvKeys.add(value.getIdentifier());

                dataValues.set(value.getIdentifier(), value);

                value = new DataValue("hahnml", "simA");

                try {
                    // byte[] data = Files.readAllBytes(Paths.get("C:\\test\\OpalMC\\scripts\\data\\000abcd0001.dat"));
                    byte[] data = Files.readAllBytes(Paths.get("C:\\test\\opalClusterSnapshots.mp4"));
                    value.setContentType("video/mp4");
                    value.setData(data, data.length);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dvKeys.add(value.getIdentifier());

                dataValues.set(value.getIdentifier(), value);
                System.out.println(dataValues.getName() + " - " + dataValues.size());
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (String key : dvKeys) {
                DataValue value = (DataValue) instance.getMap("dataValues").get(key);
                System.out.println("### DataValue ###");
                System.out.println("Owner: " + value.getOwner());
                System.out.println("CreatedFor: " + value.getCreatedFor());
                System.out.println("Identifier: " + value.getIdentifier());
                System.out.println("URN: " + value.getUrn().toString());
                System.out.println("Timestamp: " + value.getCreationTimestamp());
                System.out.println("State: " + value.getState());
                if (value.getData() != null) {
                    System.out.println("Data: " + value.getData().toString() + ", size: " + value.getData().length);
                }
                System.out.println("#################");
            }

            instance.shutdown();
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
