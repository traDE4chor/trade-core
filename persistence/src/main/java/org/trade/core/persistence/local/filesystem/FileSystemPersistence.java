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

package org.trade.core.persistence.local.filesystem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.persistence.IPersistenceProvider;
import org.trade.core.persistence.PersistableObject;
import org.trade.core.utils.TraDEProperties;

import java.io.*;
import java.nio.file.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of {@link IPersistenceProvider} using the local file system as data source.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class FileSystemPersistence<T extends PersistableObject> implements IPersistenceProvider<T> {

    private Logger logger = LoggerFactory.getLogger("org.trade.core.persistence.local.filesystem.FileSystemPersistence");

    private Class<T> objectType;

    private String persistenceFileDirectory;

    @Override
    public void initProvider(Class<T> objectType, TraDEProperties properties) {
        this.objectType = objectType;

        persistenceFileDirectory = properties.getDataPersistenceFileDirectory();

        Path path = Paths.get(persistenceFileDirectory);
        if (!path.isAbsolute()) {
            String test = path.toAbsolutePath().toString();
            persistenceFileDirectory = Paths.get(".", persistenceFileDirectory).toString();
        }
    }

    @Override
    public byte[] loadBinaryData(String collectionName, String identifier) throws Exception {
        byte[] result = new byte[0];

        // We use the UUID identifier as file name and the collectionName to distinguish data of different model
        // objects, e.g., data models (serialized model files) and data values (serialized application data)
        Path file = Paths.get(this.persistenceFileDirectory, collectionName, identifier);

        try {
            if (Files.exists(file)) {
                result = Files.readAllBytes(file);
            }
        } catch (IOException e) {
            logger.error("Loading data from file '{}' for model object '{}' of model collection '{}' caused an " +
                    "exception", file.toString(), identifier, collectionName);

            throw e;
        }

        return result;
    }

    @Override
    public void storeBinaryData(byte[] data, String collectionName, String identifier) throws Exception {
        Path path = Paths.get(this.persistenceFileDirectory, collectionName);
        Path file = Paths.get(path.toString(), identifier);

        if (data == null) {
            // We assume that if the value is set to null, we should delete also the corresponding file
            Files.deleteIfExists(file);
        } else {

            if (Files.notExists(path)) {
                Files.createDirectories(path);
            }

            Files.write(file, data, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    @Override
    public void deleteBinaryData(String collectionName, String identifier) throws Exception {
        Path rootFolder = Paths.get(this.persistenceFileDirectory, collectionName);

        // We use the UUID identifier as file name and the collectionName to distinguish data of different model
        // objects, e.g., data models (serialized model files) and data values (serialized application data)
        Path file = Paths.get(this.persistenceFileDirectory, collectionName, identifier);

        try {
            // Delete the associated file, if it exists
            if (Files.exists(file)) {
                Files.delete(file);
            }

            // Check if root folder is empty, if so delete also this folder
            File cFile = rootFolder.toFile();
            if (cFile.isDirectory() && cFile.listFiles().length == 0) {
                Files.delete(rootFolder);
            }
        } catch (IOException e) {
            logger.error("Deleting data from file '{}' for model object '{}' of model collection '{}' caused an " +
                    "exception", file.toString(), identifier, collectionName);

            throw e;
        }
    }

    @Override
    public Map<String, T> loadAllObjects(Collection<String> identifiers) throws Exception {
        Map<String, T> result = new HashMap<>();

        if (identifiers == null) {
            // Load all available objects

            // We use the simple name of the class as collection name (i.e. folder name)
            Path folder = Paths.get(this.persistenceFileDirectory, objectType.getSimpleName());

            if (Files.exists(folder)) {
                // Load each of the files contained in the folder,
                // where the filename is the identifier of the object to load
                List<Path> paths = Files.list(folder).collect(Collectors.toList());

                for (Path path : paths) {
                    result.put(path.getFileName().toString(), loadObject(path.getFileName().toString()));
                }
            }
        } else {
            for (String id : identifiers) {
                result.put(id, loadObject(id));
            }
        }

        return result;
    }

    @Override
    public T loadObject(String identifier) throws Exception {
        T result = null;

        // We use the simple name of the class as collection name (i.e. folder name)
        Path file = Paths.get(this.persistenceFileDirectory, objectType.getSimpleName(), identifier);

        try {
            if (Files.exists(file)) {
                InputStream fio = Files.newInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fio);
                result = (T) ois.readObject();

                fio.close();
                ois.close();
            }
        } catch (IOException e) {
            logger.error("Loading an object of type '{}' for the given identifier '{}' from file '{}' caused an " +
                    "exception", objectType.getSimpleName(), identifier, file.toString());

            throw e;
        }

        return result;
    }

    @Override
    public void storeObject(T object) throws Exception {
        // We use the simple name of the class as collection name (i.e. folder name)
        Path path = Paths.get(this.persistenceFileDirectory, objectType.getSimpleName());
        Path file = Paths.get(path.toString(), object.getIdentifier());

        // Check if the required folders exist
        if (Files.notExists(path)) {
            // Create them, if they do not exist
            Files.createDirectories(path);
        }

        try {
            // Write the serialized object to the file
            OutputStream fio = Files.newOutputStream(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            ObjectOutputStream oos = new ObjectOutputStream(fio);
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            logger.error("Storing an object of type '{}' with the given identifier '{}' in file '{}' caused an " +
                    "exception", objectType.getSimpleName(), object.getIdentifier(), file.toString());

            throw e;
        }
    }

    @Override
    public void storeAllObjects(Collection<T> objects) throws Exception {
        // Store all objects contained in the collection
        for (T obj : objects) {
            storeObject(obj);
        }
    }

    @Override
    public void deleteObject(String identifier) throws Exception {
        // We use the simple name of the class as collection name (i.e. folder name)
        Path file = Paths.get(this.persistenceFileDirectory, objectType.getSimpleName(), identifier);

        try {
            // Delete the file representing the serialized object, if it exists
            if (Files.exists(file)) {
                Files.delete(file);
            }
        } catch (IOException e) {
            logger.error("Deleting an object of type '{}' with the given identifier '{}' in file '{}' caused an " +
                    "exception", objectType.getSimpleName(), identifier, file.toString());

            throw e;
        }
    }

    @Override
    public void deleteAllObjects(Collection<String> identifiers) throws Exception {
        if (identifiers == null) {
            // Delete all available objects

            // We use the simple name of the class as collection name (i.e. folder name)
            Path folder = Paths.get(this.persistenceFileDirectory, objectType.getSimpleName());

            if (Files.exists(folder)) {
                // Delete all files in the folder
                List<Path> paths = Files.list(folder).collect(Collectors.toList());

                for (Path path : paths) {
                    Files.deleteIfExists(path);
                }
            }
        } else {
            for (String id : identifiers) {
                deleteObject(id);
            }
        }
    }

    @Override
    public void destroyProvider() {
        // Nothing to do
    }
}
