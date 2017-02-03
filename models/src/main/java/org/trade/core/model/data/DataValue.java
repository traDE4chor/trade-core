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

package org.trade.core.model.data;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import org.bson.Document;
import org.bson.types.Binary;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.model.ModelUtils;
import org.trade.core.utils.TraDEProperties;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataValues")
public class DataValue extends BaseResource implements Serializable {

    private static final long serialVersionUID = -1774719861199414867L;

    @Transient
    Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.DataValue");

    private transient TraDEProperties props = null;

    /**
     * We use Uniform Resource Names (URN) to identify data values independent of their concrete location.
     * <p>
     * Therefore, we use the following scheme according to RFC 2141:
     * <URN> ::= "urn:" <Namespace Identifier (NID)> ":" <Namespace Specific String (NSS)>
     * <p>
     * Where the NID "data" is used to represent the data context. The NSS contains an optional name of the owner of
     * the data value and its auto-generated name separated by a ":" symbol.
     * For example, the URN "urn:data:hahnml:ca635810-8ec1-4e30-8bd7-b52469494fdd" refers to data value
     * "ca635810-8ec1-4e30-8bd7-b52469494fdd" which belongs to owner "hahnml". The "owner" reference does not
     * necessarily have to contain an identifier of a human being, also any type of resource or system can be
     * specified to be the owner of the data value.
     */
    private transient URN urn = null;

    private String name = null;

    private String readableName = "";

    private Date timestamp = new Date();

    private String owner = "";

    private String createdFor = "";

    private String state = "created";

    private String type = null;

    private String contentType = null;

    private Date lastModified = timestamp;

    private long size = 0L;

    public DataValue(String owner, String createdFor) throws URNSyntaxException {
        props = new TraDEProperties();

        this.name = UUID.randomUUID().toString();

        this.owner = owner;
        this.createdFor = createdFor;

        if (owner != null) {
            this.urn = URN.newInstance(ModelUtils.DATA_URN_NAMESPACE_ID, owner + ModelUtils
                    .URN_NAMESPACE_STRING_DELIMITER + name);
        } else {
            this.urn = URN.newInstance(ModelUtils.DATA_URN_NAMESPACE_ID, name);
        }

        // Set the identifier to the stringified value of the URN
        this.identifier = urn.toString();
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DataValue() {
        props = new TraDEProperties();
    }

    public URN getUrn() {
        return urn;
    }

    /**
     * Provides the name of the data element.
     *
     * @return The name of the data element.
     */
    public String getName() {
        return this.name;
    }

    public Date getCreationTimestamp() {
        return timestamp;
    }

    public String getOwner() {
        return owner;
    }

    public String getCreatedFor() {
        return createdFor;
    }

    public String getState() {
        return state;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getHumanReadableName() {
        return readableName;
    }

    public void setHumanReadableName(String readableName) {
        this.readableName = readableName;
        this.lastModified = new Date();
    }

    /**
     * Gets the type of the data element. The following basic types are supported by default: "string", "number",
     * "boolean", "xml_element", "xml_element_list", or "binary". This list should be extensible in
     * the future by adding corresponding type (system) plugins.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
        this.lastModified = new Date();
    }

    /**
     * Gets content type of the data element in form of a MIME type. For example, "text/plain; charset=utf-8",
     * "image/jpeg" or "video/mpeg".
     *
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets content type.
     *
     * @param contentType the content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.lastModified = new Date();
    }

    /**
     * Return the size of the attached data.
     *
     * @return The size of the data attached to the data value object.
     */
    public long getSize() {
        return this.size;
    }

    public byte[] getData() {
        byte[] result = null;

        switch (this.props.getDataPersistenceMode()) {
            case DB:
                result = loadDataFromDB();
                break;
            case FILE:
                result = loadDataFromFile();
                break;
            default:
                result = loadDataFromFile();
        }

        return result;
    }

    public void setData(byte[] data, long size) throws Exception {
        this.size = size;

        // TODO: Where and how do we handle/track state changes?
        this.state = "ready";

        switch (this.props.getDataPersistenceMode()) {
            case DB:
                storeDataToDB(data);
                break;
            case FILE:
                storeDataToFile(data);
                break;
            default:
                storeDataToFile(data);
        }

        this.lastModified = new Date();
    }

    public void destroy() {
        switch (this.props.getDataPersistenceMode()) {
            case DB:
                removeDataFromDB();
                break;
            case FILE:
                removeDataFromFile();
                break;
            default:
                removeDataFromFile();
        }
    }

    private byte[] loadDataFromFile() {
        byte[] result = new byte[0];

        // We use the identifier as file name
        Path file = Paths.get(this.props.getDataPersistenceFileDirectory(), ModelUtils.translateURNtoFolderPath
                (getUrn()), ModelUtils.DATA_FILE_NAME);

        try {
            if (Files.exists(file)) {
                result = Files.readAllBytes(file);
            }
        } catch (IOException e) {
            logger.error("Loading data from file '{}' for data value '{}' caused an exception", file.toString(),
                    getIdentifier());
        }

        return result;
    }

    private byte[] loadDataFromDB() {
        byte[] data = new byte[0];

        MongoClient client = new MongoClient(new MongoClientURI(this.props.getDataPersistenceDbUrl()));
        MongoDatabase db = client.getDatabase(this.props.getDataPersistenceDbName());

        Document doc = db.getCollection("dataCollection").find(Filters.eq("urn", getIdentifier())).limit(1).first();

        if (doc != null) {
            if (doc.containsKey("data")) {
                data = ((Binary) doc.get("data")).getData();
                this.lastModified = doc.getDate("lastModified");
            } else {
                logger.info("Data value '{}' does not have any associated data at the moment.", getIdentifier());
            }
        } else {
            logger.info("The database does not contain the specified data value '{}'",
                    getIdentifier());
        }

        client.close();

        return data;
    }

    private void storeDataToFile(byte[] data) throws IOException {
        Path path = Paths.get(this.props.getDataPersistenceFileDirectory(), ModelUtils.translateURNtoFolderPath(this
                .urn));
        Path file = Paths.get(path.toString(), ModelUtils.DATA_FILE_NAME);

        if (data == null) {
            // We assume that if the value is set to null, we should delete also the corresponding file
            Files.deleteIfExists(file);
        } else {

            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }

            Files.write(file, data, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        }
    }

    private void storeDataToDB(byte[] data) {
        MongoClient client = new MongoClient(new MongoClientURI(this.props.getDataPersistenceDbUrl()));
        MongoDatabase db = client.getDatabase(this.props.getDataPersistenceDbName());

        MongoCollection<Document> collection = db.getCollection("dataCollection");
        Document doc = collection.find(Filters.eq("urn", getIdentifier())).limit(1).first();

        if (data == null) {
            // We assume that if the value is set to null, we should delete also the corresponding database entry
            if (doc != null) {
                collection.deleteOne(Filters.eq("urn", getIdentifier()));
            }
        } else {
            // Check if the document already exists and update it
            if (doc != null) {
                collection.updateOne(Filters.eq("urn", getIdentifier()),
                        Updates.combine(Updates.set("data", data), Updates.currentDate
                                ("lastModified")));
            } else {
                Document document = new Document("urn", getIdentifier())
                        .append("data", data)
                        .append("lastModified", new Date());
                collection.insertOne(document);
            }
        }

        client.close();
    }

    private void removeDataFromFile() {
        Path rootFolder = Paths.get(this.props.getDataPersistenceFileDirectory());

        // We use the identifier as file name
        Path file = Paths.get(this.props.getDataPersistenceFileDirectory(), ModelUtils.translateURNtoFolderPath
                (getUrn()), ModelUtils.DATA_FILE_NAME);

        try {
            // Delete the associated file, if it exists
            if (Files.exists(file)) {
                Files.delete(file);
            }

            // Check if parent folders are empty, if so delete also these folders
            Path current = file.getParent();
            while (!current.equals(rootFolder)) {
                File cFile = current.toFile();
                if (cFile.isDirectory() && cFile.listFiles().length == 0) {
                    Files.delete(current);
                }

                current = current.getParent();
            }
        } catch (IOException e) {
            logger.error("Deleting data from file '{}' for data value '{}' caused an exception", file.toString(),
                    getIdentifier());
        }
    }

    private void removeDataFromDB() {
        MongoClient client = new MongoClient(new MongoClientURI(this.props.getDataPersistenceDbUrl()));
        MongoDatabase db = client.getDatabase(this.props.getDataPersistenceDbName());

        Document doc = db.getCollection("dataCollection").findOneAndDelete(Filters.eq("urn", getIdentifier()));

        if (doc != null) {
            logger.info("Data value '{}' and its associated data successfully deleted from DB.", getIdentifier());
        } else {
            logger.info("The database does not contain the specified data value '{}'",
                    getIdentifier());
        }

        client.close();
    }

    @PostLoad
    private void postLoad() {
        try {
            this.urn = URN.fromString(this.identifier);
        } catch (URNSyntaxException e) {
            logger.error("Reloading the persisted data value '{}' caused an URNSyntaxException", this.getIdentifier());
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            this.urn = URN.fromString(this.identifier);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data value '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data value.");
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }

        // We need to recreate the properties object
        if (this.props == null) {
            props = new TraDEProperties();
        }
    }
}
