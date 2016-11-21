package de.unistuttgart.iaas.trade.model.data;

import de.slub.urn.URN;
import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.ModelUtils;

import java.io.*;
import java.util.Date;
import java.util.UUID;

/**
 * Created by hahnml on 26.10.2016.
 */
public class DataValue implements Serializable {

    private static final long serialVersionUID = -1774719861199414867L;

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
