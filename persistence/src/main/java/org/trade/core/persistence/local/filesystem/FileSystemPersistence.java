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
import org.trade.core.persistence.local.LocalPersistenceProvider;
import org.trade.core.utils.TraDEProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by hahnml on 07.04.2017.
 */
public class FileSystemPersistence implements LocalPersistenceProvider {

    Logger logger = LoggerFactory.getLogger("org.trade.core.persistence.local.filesystem.FileSystemPersistence");

    private TraDEProperties props = null;

    public FileSystemPersistence() {
        props = new TraDEProperties();
    }

    @Override
    public byte[] loadData(String collectionName, String identifier) throws Exception {
        byte[] result = new byte[0];

        // We use the UUID identifier as file name and the collectionName to distinguish data of different model
        // objects, e.g., data models (serialized model files) and data values (serialized application data)
        Path file = Paths.get(this.props.getDataPersistenceFileDirectory(), collectionName, identifier);

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
    public void storeData(byte[] data, String collectionName, String identifier) throws Exception {
        Path path = Paths.get(this.props.getDataPersistenceFileDirectory(), collectionName);
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
    public void removeData(String collectionName, String identifier) throws Exception {
        Path rootFolder = Paths.get(this.props.getDataPersistenceFileDirectory(), collectionName);

        // We use the UUID identifier as file name and the collectionName to distinguish data of different model
        // objects, e.g., data models (serialized model files) and data values (serialized application data)
        Path file = Paths.get(this.props.getDataPersistenceFileDirectory(), collectionName, identifier);

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
}
