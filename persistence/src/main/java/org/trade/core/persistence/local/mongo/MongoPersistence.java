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

package org.trade.core.persistence.local.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.Binary;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.PersistableObject;
import org.trade.core.utils.TraDEProperties;

import java.util.*;

/**
 * Implementation of {@link IPersistenceProvider} using a MongoDB as data source.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class MongoPersistence<T extends PersistableObject> implements IPersistenceProvider<T> {

    private Logger logger = LoggerFactory.getLogger("org.trade.core.persistence.local.mongo.MongoPersistence");

    private Class<T> objectType;

    private Datastore store;

    private final String IDENTIFIER_FIELD = "identifier";

    private final String DATA_FIELD = "data";

    private String mongoUrl;

    private String dbName;

    private boolean isMorphiaMappingInitialized = false;

    @Override
    public void initProvider(Class<T> objectType, TraDEProperties properties) {
        this.objectType = objectType;

        mongoUrl = properties.getDataPersistenceDbUrl();
        dbName = properties.getDataPersistenceDbName();
    }

    @Override
    public byte[] loadBinaryData(String collectionName, String identifier) throws Exception {
        byte[] data = new byte[0];

        MongoClient client = new MongoClient(new MongoClientURI(this.mongoUrl));
        MongoDatabase db = client.getDatabase(this.dbName);

        Document doc = db.getCollection(collectionName).find(Filters.eq(IDENTIFIER_FIELD, identifier)).limit(1)
                .first();

        if (doc != null) {
            if (doc.containsKey(DATA_FIELD)) {
                data = ((Binary) doc.get(DATA_FIELD)).getData();
            } else {
                logger.info("Model object '{}' from model collection '{}' does not have any associated data at the " +
                                "moment.",
                        identifier, collectionName);
            }
        } else {
            logger.info("The database does not know the specified model object '{}' from model collection '{}'",
                    identifier, collectionName);
        }

        client.close();

        return data;
    }

    @Override
    public void storeBinaryData(byte[] data, String collectionName, String identifier) throws Exception {
        MongoClient client = new MongoClient(new MongoClientURI(this.mongoUrl));
        MongoDatabase db = client.getDatabase(this.dbName);

        MongoCollection<Document> collection = db.getCollection(collectionName);
        Document doc = collection.find(Filters.eq(IDENTIFIER_FIELD, identifier)).limit(1).first();

        if (data == null) {
            // We assume that if the value is set to null, we should delete also the corresponding database entry
            if (doc != null) {
                collection.deleteOne(Filters.eq(IDENTIFIER_FIELD, identifier));
            }
        } else {
            // Check if the document already exists and update it
            if (doc != null) {
                collection.updateOne(Filters.eq(IDENTIFIER_FIELD, identifier),
                        Updates.combine(Updates.set(DATA_FIELD, data), Updates.currentDate
                                ("lastModified")));
            } else {
                Document document = new Document(IDENTIFIER_FIELD, identifier)
                        .append(DATA_FIELD, data)
                        .append("lastModified", new Date());
                collection.insertOne(document);
            }
        }

        client.close();
    }

    @Override
    public void deleteBinaryData(String collectionName, String identifier) throws Exception {
        MongoClient client = new MongoClient(new MongoClientURI(this.mongoUrl));
        MongoDatabase db = client.getDatabase(this.dbName);

        Document doc = db.getCollection(collectionName).findOneAndDelete(Filters.eq(IDENTIFIER_FIELD, identifier));

        if (doc != null) {
            logger.info("Model object '{}' from model collection '{}' and its associated data successfully deleted " +
                    "from DB.", identifier, collectionName);
        } else {
            logger.info("The database does not know the specified model object '{}' from model collection '{}'",
                    identifier, collectionName);
        }

        client.close();
    }

    private void initializeMorphiaMapping() {
        // Check if the mapping is already initialized, if not trigger the mapping
        if (!isMorphiaMappingInitialized) {
            Morphia morphia = new Morphia();
            morphia.getMapper().getOptions().setMapSubPackages(true);

            // Map model classes to db collections
            morphia.mapPackage("org.trade.core.model.data");

            // create the datastore connecting to mongo
            this.store = morphia.createDatastore(new MongoClient(new MongoClientURI(mongoUrl)), dbName);
            this.store.ensureIndexes();
        }
    }

    @Override
    public Map<String, T> loadAllObjects(Collection<String> identifiers) throws Exception {
        HashMap<String, T> result = new HashMap<>();

        // Initialize the DB mapping if not done already
        initializeMorphiaMapping();

        if (identifiers == null) {
            List<T> objs = this.store.createQuery(objectType).asList();
            for (T obj : objs) {
                result.put(obj.getIdentifier(), obj);
            }
        } else {
            List<T> objs = this.store.createQuery(objectType).filter("identifier in", identifiers).asList();
            for (T obj : objs) {
                result.put(obj.getIdentifier(), obj);
            }
        }

        return result;
    }

    @Override
    public T loadObject(String identifier) throws Exception {
        // Initialize the DB mapping if not done already
        initializeMorphiaMapping();

        return this.store.createQuery(objectType).field(IDENTIFIER_FIELD).equal(identifier).limit
                (1).get();
    }

    @Override
    public void storeObject(T object) throws Exception {
        // Initialize the DB mapping if not done already
        initializeMorphiaMapping();

        this.store.save(object);
    }

    @Override
    public void storeAllObjects(Collection<T> objects) throws Exception {
        // Initialize the DB mapping if not done already
        initializeMorphiaMapping();

        this.store.save(objects);
    }

    @Override
    public void deleteObject(String identifier) throws Exception {
        // Initialize the DB mapping if not done already
        initializeMorphiaMapping();

        Query<T> toDelete = this.store.createQuery(objectType).field(IDENTIFIER_FIELD).equal(identifier);
        this.store.delete(toDelete);
    }

    @Override
    public void deleteAllObjects(Collection<String> identifiers) throws Exception {
        // Initialize the DB mapping if not done already
        initializeMorphiaMapping();

        if (identifiers != null) {
            Query<T> toDelete = this.store.createQuery(objectType).filter("identifier in", identifiers);
            this.store.delete(toDelete);
        } else {
            this.store.getCollection(objectType).drop();
        }
    }

    @Override
    public void destroyProvider() {
        if (isMorphiaMappingInitialized && this.store != null) {
            this.store.getMongo().close();
        }
    }
}
