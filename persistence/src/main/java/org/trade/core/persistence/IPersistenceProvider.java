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

package org.trade.core.persistence;

import org.trade.core.utils.TraDEProperties;

import java.util.Collection;
import java.util.Map;

/**
 * This interface defines basic methods for the persistence of binary data and objects in different data
 * sources. For each type of data source a corresponding implementation can be provided, e.g., through a corresponding
 * factory class.
 * <p>
 * Created by hahnml on 24.04.2017.
 *
 * @param <T> the object type that should be persisted by the corresponding implementation of this interface, needs
 *            to extend {@link PersistableObject}.
 */
public interface IPersistenceProvider<T extends PersistableObject> {

    /**
     * Initializes the provider.
     *
     * @param objectType the type of objects the provider should handle. This information is used within the
     *                   implementations to identify the collection to which an object belongs.
     * @param properties the properties to configure the provider.
     */
    void initProvider(Class<T> objectType, TraDEProperties properties);

    /**
     * Destroys the provider, e.g., closing open connections, cleanup, etc.
     */
    void destroyProvider();

    /**
     * Load all objects of the specified type (objectType) from a data source into a map where each map entry looks
     * like (object.identifier, object). Invoking this method with a {@code null} value loads all available objects, else
     * only a subset of objects according to the specified collection of identifiers will be loaded.
     *
     * @param identifiers a collection of identifiers for which objects should be loaded. Specifying a
     *                    {@code null} value for the collection will trigger the loading of the whole collection of objects
     *                    from the data source.
     * @return the map of loaded objects and their identifiers.
     * @throws Exception the exception
     */
    Map<String, T> loadAllObjects(Collection<String> identifiers) throws Exception;

    /**
     * Loads a single object with the specified identifier.
     *
     * @param identifier the identifier to load an object for
     * @return the resulting object
     * @throws Exception the exception
     */
    T loadObject(String identifier) throws Exception;

    /**
     * Stores a single object with the given identifier to a data source.
     *
     * @param object     the object to persist
     * @throws Exception the exception
     */
    void storeObject(T object) throws Exception;

    /**
     * Stores all objects provided through the collection to a data source.
     *
     * @param objects the collection of objects which should be stored
     * @throws Exception the exception
     */
    void storeAllObjects(Collection<T> objects) throws Exception;

    /**
     * Deletes a single object with the given identifier.
     *
     * @param identifier the identifier of the object to delete from the data source
     * @throws Exception the exception
     */
    void deleteObject(String identifier) throws Exception;

    /**
     * Deletes all objects of the specified type (objectType) from a data source.
     * Invoking this method with a {@code null} value deletes all available objects, else
     * only a subset of objects according to the specified collection of identifiers will be deleted.
     *
     * @param identifiers a collection of identifiers for which objects should be deleted. Specifying a
     *                    {@code null} value for the collection will trigger the deletion of the whole collection of
     *                    objects from the data source.
     * @throws Exception the exception
     */
    void deleteAllObjects(Collection<String> identifiers) throws Exception;

    /**
     * Loads binary data from a data source.
     *
     * @param collectionName the name of the collection the binary data belongs to
     * @param identifier     the identifier used to identify the binary data
     * @return the binary data as byte[]
     * @throws Exception the exception
     */
    byte[] loadBinaryData(String collectionName, String identifier) throws Exception;

    /**
     * Stores binary data to a data source.
     *
     * @param data           the data to store as byte[]
     * @param collectionName the name of the collection the binary data belongs to
     * @param identifier     the identifier used to identify the binary data
     * @throws Exception the exception
     */
    void storeBinaryData(byte[] data, String collectionName, String identifier) throws Exception;

    /**
     * Deletes binary data from a data source.
     *
     * @param collectionName the name of the collection the binary data belongs to
     * @param identifier     the identifier used to identify the binary data to delete
     * @throws Exception the exception
     */
    void deleteBinaryData(String collectionName, String identifier) throws Exception;
}
