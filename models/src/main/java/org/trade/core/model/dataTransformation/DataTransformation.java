/*
 * Copyright 2018 Michael Hahn
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

package org.trade.core.model.dataTransformation;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.model.ABaseResource;
import org.trade.core.model.data.DataModel;
import org.trade.core.model.data.instance.DataElementInstance;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.local.LocalPersistenceProviderFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class holds information about a data transformation within the middleware.
 * <p>
 * Created by hahnml on 25.01.2018.
 */
@Entity("dataTransformations")
public class DataTransformation extends ABaseResource {

    @Transient
    private Logger logger = LoggerFactory.getLogger("org.trade.core.model.dataTransformation.DataTransformation");

    private String name = null;

    private String transformerQName;

    @Reference
    private DataModel dataModel;

    @Transient
    private Object hdtTransformer;

    // The source model object (data object or data element) of the data transformation.
    private ABaseResource source = null;

    // The target model object (data object or data element) of the data transformation.
    private ABaseResource target = null;

    // A map of key-value pairs representing the transformer parameters and their values.
    private Map<String, Object> transformerParameters;

    @Transient
    private Map<String, Set<DataElementInstance>> relatedDataElementInstances = new HashMap<>();

    private transient IPersistenceProvider<DataTransformation> persistProv;

    /**
     * This constructor is only used by Morphia to load objects from the database.
     */
    private DataTransformation() {
        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataTransformation.class);
    }

    /**
     * Instantiates a new data transformation with the given name.
     *
     * @param name  the name
     */
    public DataTransformation(String name) {
        this(name, null);
    }

    /**
     * Instantiates a new data transformation with the given name and transformationApp identifier.
     *
     * @param name             the name
     * @param transformerQName the full-qualified name of the transformation application that provides the
     *                         logic for this data transformation.
     */
    public DataTransformation(String name, String transformerQName) {
        this.name = name;
        this.transformerQName = transformerQName;

        this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataTransformation.class);
    }

    /**
     * Gets the name of the data transformation.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the data transformation.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the data model.
     *
     * @return the data model
     */
    public DataModel getDataModel() {
        return dataModel;
    }

    /**
     * Sets the data model.
     *
     * @param dataModel the data model
     */
    public void setDataModel(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    /* Gets the transformer QName specified in the data dependency graph.
     *
     * @return the full-qualified name of the transformer
     */
    public String getTransformerQName() {
        return transformerQName;
    }

    /**
     * Sets the transformers QName which is used for the resolution of a matching transformation app at the HDT
     * framework.
     *
     * @param transformerQName the full-qualified name of the transformer
     */
    public void setTransformerQName(String transformerQName) {
        this.transformerQName = transformerQName;
    }

    /**
     * Gets the resolved HDT framework transformer object.
     *
     * @return the HDT app framework transformer object.
     */
    public Object getHdtTransformer() {
        return hdtTransformer;
    }

    /**
     * Sets the resolved HDT framework transformer object.
     *
     * @param hdtTransformer the HDT framework transformer object.
     */
    public void setHdtTransformer(Object hdtTransformer) {
        this.hdtTransformer = hdtTransformer;
    }

    /**
     * Gets the source data object or data element of this data transformation.
     *
     * @return the source resource
     */
    public ABaseResource getSource() {
        return source;
    }

    /**
     * Sets the source data object or data element for this data transformation.
     *
     * @param source the source resource
     */
    public void setSource(ABaseResource source) {
        this.source = source;
    }

    /**
     * Gets the target data object or data element of this data transformation.
     *
     * @return the target resource
     */
    public ABaseResource getTarget() {
        return target;
    }

    /**
     * Sets the target data object or data element for this data transformation.
     *
     * @param target the target resource
     */
    public void setTarget(ABaseResource target) {
        this.target = target;
    }

    /**
     * Gets transformer parameters. The key is always a {@link String} while the value can be a {@link String} representing a
     * fixed value or {@link org.trade.core.query.Query} representing a reference to the value to be used during run
     * time.
     *
     * @return the transformer parameters
     */
    public Map<String, Object> getTransformerParameters() {
        return transformerParameters;
    }

    /**
     * Sets transformer parameters.
     *
     * @param transformerParameters the transformer parameters
     */
    public void setTransformerParameters(Map<String, Object> transformerParameters) {
        this.transformerParameters = transformerParameters;
    }

    /**
     * Gets the related data element instances for which the data transformation might be triggered.
     *
     * @return the related data element instances ["data value ID", "list of data element instances"]
     */
    public Map<String, Set<DataElementInstance>> getRelatedDataElementInstances() {
        return relatedDataElementInstances;
    }

    @Override
    public void storeToDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.storeObject(this);
            } catch (Exception e) {
                logger.error("Storing data transformation '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    @Override
    public void deleteFromDS() {
        if (this.persistProv != null) {
            try {
                this.persistProv.deleteObject(this.getIdentifier());
            } catch (Exception e) {
                logger.error("Deleting data transformation '" + this.getIdentifier() + "' caused an exception.", e);
            }
        }
    }

    private void readObject(ObjectInputStream ois) throws IOException {
        try {
            ois.defaultReadObject();

            this.persistProv = LocalPersistenceProviderFactory.createLocalPersistenceProvider(DataTransformation.class);
        } catch (ClassNotFoundException e) {
            logger.error("Class not found during deserialization of data transformation '{}'", getIdentifier());
            throw new IOException("Class not found during deserialization of data transformation.");
        }
    }
}
