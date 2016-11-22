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

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.model.ModelUtils;
import org.mongodb.morphia.annotations.Entity;

import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hahnml on 26.10.2016.
 */
@Entity("dataValues")
public class DataValue extends BaseResource implements Serializable {

    private static final long serialVersionUID = -1774719861199414867L;

    @Transient
    Logger logger = LoggerFactory.getLogger("de.unistuttgart.trade.model.data.DataValue");

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

    private UUID identifier = UUID.randomUUID();

    private String owner = "";

    private String state = "";

    private transient byte[] data = null;

    public DataValue(String owner, String createdFor) throws URNSyntaxException {
        this.owner = owner;

        if (owner != null) {
            this.urn = URN.newInstance(ModelUtils.DATA_URN_NAMESPACE_ID, owner + ModelUtils
                    .URN_NAMESPACE_STRING_DELIMITER + identifier.toString());
        } else {
            this.urn = URN.newInstance(ModelUtils.DATA_URN_NAMESPACE_ID, identifier.toString());
        }
    }

    public URN getUrn() {
        return urn;
    }

    public Date getCreationTimestamp() {
        return timestamp;
    }

    public UUID getIdentifier() {
        return identifier;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getState() {
        return state;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] value) {
        this.data = value;
    }
}
