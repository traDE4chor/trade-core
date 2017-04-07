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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.persistence.local.LocalPersistenceProvider;
import org.trade.core.utils.TraDEProperties;

import java.util.Date;

/**
 * Created by hahnml on 07.04.2017.
 */
public class MongoPersistence implements LocalPersistenceProvider {

    Logger logger = LoggerFactory.getLogger("org.trade.core.persistence.local.mongo.MongoPersistence");

    private TraDEProperties props = null;

    public MongoPersistence() {
        props = new TraDEProperties();
    }

    @Override
    public byte[] loadData(String collectionName, String identifier) throws Exception {
        byte[] data = new byte[0];

        MongoClient client = new MongoClient(new MongoClientURI(this.props.getDataPersistenceDbUrl()));
        MongoDatabase db = client.getDatabase(this.props.getDataPersistenceDbName());

        Document doc = db.getCollection(collectionName).find(Filters.eq("identifier", identifier)).limit(1)
                .first();

        if (doc != null) {
            if (doc.containsKey("data")) {
                data = ((Binary) doc.get("data")).getData();
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
    public void storeData(byte[] data, String collectionName, String identifier) throws Exception {
        MongoClient client = new MongoClient(new MongoClientURI(this.props.getDataPersistenceDbUrl()));
        MongoDatabase db = client.getDatabase(this.props.getDataPersistenceDbName());

        MongoCollection<Document> collection = db.getCollection(collectionName);
        Document doc = collection.find(Filters.eq("identifier", identifier)).limit(1).first();

        if (data == null) {
            // We assume that if the value is set to null, we should delete also the corresponding database entry
            if (doc != null) {
                collection.deleteOne(Filters.eq("identifier", identifier));
            }
        } else {
            // Check if the document already exists and update it
            if (doc != null) {
                collection.updateOne(Filters.eq("identifier", identifier),
                        Updates.combine(Updates.set("data", data), Updates.currentDate
                                ("lastModified")));
            } else {
                Document document = new Document("identifier", identifier)
                        .append("data", data)
                        .append("lastModified", new Date());
                collection.insertOne(document);
            }
        }

        client.close();
    }

    @Override
    public void removeData(String collectionName, String identifier) throws Exception {
        MongoClient client = new MongoClient(new MongoClientURI(this.props.getDataPersistenceDbUrl()));
        MongoDatabase db = client.getDatabase(this.props.getDataPersistenceDbName());

        Document doc = db.getCollection(collectionName).findOneAndDelete(Filters.eq("identifier", identifier));

        if (doc != null) {
            logger.info("Model object '{}' from model collection '{}' and its associated data successfully deleted " +
                    "from DB.", identifier, collectionName);
        } else {
            logger.info("The database does not know the specified model object '{}' from model collection '{}'",
                    identifier, collectionName);
        }

        client.close();
    }
}
