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

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Version;
import org.trade.core.persistence.PersistableObject;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class specifies the basic attributes and methods of all model objects. Therefore, model objects have to
 * extend this class.
 * <p>
 * Created by hahnml on 22.11.2016.
 */
public abstract class BaseResource implements Serializable, PersistableObject {

    private static final long serialVersionUID = 1666273823086587345L;

    // We directly initialize the id to avoid problems with non-initialized @Id fields during runtime
    @Id
    protected ObjectId id = new ObjectId(); ;

    @Version
    private Long version;

    protected String identifier = UUID.randomUUID().toString();

    /**
     * Provides the identifier of the model object.
     *
     * @return A UUID which identifies the model object.
     */
    @Override
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Provides access to the data source id under which the object is persisted at a data source.
     *
     * @return the data source id of the object
     */
    public ObjectId getId() {
        return this.id;
    }

    /**
     * Sets the data source id under which the object is persisted at a data source.
     *
     * @param id the data source id of the object
     */
    public void setId(ObjectId id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
