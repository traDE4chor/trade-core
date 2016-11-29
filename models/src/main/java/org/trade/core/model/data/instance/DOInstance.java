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

package org.trade.core.model.data.instance;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.model.ModelUtils;
import org.trade.core.model.data.BaseResource;
import org.trade.core.utils.TraDEProperties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hahnml on 26.10.2016.
 */
public class DOInstance extends BaseResource implements Serializable {

    private static final long serialVersionUID = 4504379941592896623L;

    @Transient
    Logger logger = LoggerFactory.getLogger("org.trade.core.model.data.instance.DOInstance");

    /**
     * We use Uniform Resource Names (URN) to identify data object instances and the data object to which they belong,
     * independent of their concrete location.
     * <p>
     * Therefore, we use the following scheme according to RFC 2141:
     * <URN> ::= "urn:" <Namespace Identifier (NID)> ":" <Namespace Specific String (NSS)>
     * <p>
     * Where the NID is used to represent the context to which the data object belongs. The NSS contains the name of
     * the data object and the instance identifier separated by a ":" symbol.
     * For example, the URN "urn:cm23:resultData:ca635810-8ec1-4e30-8bd7-b52469494fdd" refers to instance
     * "ca635810-8ec1-4e30-8bd7-b52469494fdd" of data object "resultData" which belongs to entity "cm23".
     */
    private transient URN urn = null;

    private Date timestamp = new Date();

    private String identifier = null;

    private String instanceID = null;

    private String owner = "";

    private String createdFor = "";

    private String state = "";

    public DOInstance(URN dataObjectURN, String owner, String createdFor) throws URNSyntaxException {
        this.owner = owner;
        this.createdFor = createdFor;

        this.instanceID = UUID.randomUUID().toString();

        this.urn = URN.newInstance(dataObjectURN.getNamespaceIdentifier(), dataObjectURN.getNamespaceSpecificString()
                + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + instanceID.toString());

        // Set the identifier to the stringified value of the URN
        this.identifier = urn.toString();
    }

    /**
     * This constructor is only used by Morphia to load data value from the database.
     */
    private DOInstance() {

    }

    public URN getUrn() {
        return urn;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Date getCreationTimestamp() {
        return timestamp;
    }

    public String getInstanceID() {
        return instanceID;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getCreatedFor() {
        return createdFor;
    }

    public void setCreatedFor(String createdFor) {
        this.createdFor = createdFor;
    }

    public String getState() {
        return state;
    }

    @PostLoad
    private void postLoad() {
        try {
            this.urn = URN.fromString(this.identifier);
        } catch (URNSyntaxException e) {
            logger.error("Reloading the persisted data object instance '{}' caused an URNSyntaxException", this
                    .getIdentifier());
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();
            this.urn = URN.fromString(this.identifier);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data object instance '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data object instance.");
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }
    }
}
