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
import org.trade.core.model.ModelUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hahnml on 26.10.2016.
 */
public class DOInstance implements Serializable {

    private static final long serialVersionUID = 4504379941592896623L;

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

    private UUID instanceID = UUID.randomUUID();

    private String owner = "";

    private String createdFor = "";

    private String state = "";

    public DOInstance(URN dataObjectURN, String owner, String createdFor) throws URNSyntaxException {
        this.owner = owner;
        this.createdFor = createdFor;

        this.urn = URN.newInstance(dataObjectURN.getNamespaceIdentifier(), dataObjectURN.getNamespaceSpecificString()
                + ModelUtils.URN_NAMESPACE_STRING_DELIMITER + instanceID.toString());
    }

    public URN getUrn() {
        return urn;
    }

    public Date getCreationTimestamp() {
        return timestamp;
    }

    public UUID getInstanceID() {
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

}
