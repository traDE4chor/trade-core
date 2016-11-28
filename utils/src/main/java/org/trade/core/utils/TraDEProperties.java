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

package org.trade.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by hahnml on 24.11.2016.
 */
public class TraDEProperties extends Properties {

    private static final long serialVersionUID = -7722413438150969901L;

    private static final String PROPERTY_FILE_LOCATION = "/config.properties";

    public static final String PROPERTY_CACHE_DB_URL = "cache.db.url";
    public static final String PROPERTY_CACHE_DB_NAME = "cache.db.name";

    public enum DataPersistenceMode {
        FILE, DB
    }

    public static final String PROPERTY_DATA_PERSIST_MODE = "data.persistence.mode";
    public static final String PROPERTY_DATA_PERSIST_DB_URL = "data.persistence.db.url";
    public static final String PROPERTY_DATA_PERSIST_DB_NAME = "data.persistence.db.name";
    public static final String PROPERTY_DATA_PERSIST_FILE_DIRECTORY = "data.persistence.file.directory";

    public TraDEProperties() {
        this(null);
    }

    public TraDEProperties(Properties defaults) {
        super(defaults);

        loadProperties();
    }

    public String getCacheDbUrl() {
        return getProperty(PROPERTY_CACHE_DB_URL, "mongodb://localhost:27017");
    }

    public String getCacheDbName() {
        return getProperty(PROPERTY_CACHE_DB_NAME, "tradeCacheDB");
    }

    public DataPersistenceMode getDataPersistenceMode() {
        return DataPersistenceMode.valueOf(getProperty(PROPERTY_DATA_PERSIST_MODE, "FILE"));
    }

    public String getDataPersistenceDbUrl() {
        return getProperty(PROPERTY_DATA_PERSIST_DB_URL, "mongodb://localhost:27017");
    }

    public String getDataPersistenceDbName() {
        return getProperty(PROPERTY_DATA_PERSIST_DB_NAME, "tradeDataDB");
    }

    public String getDataPersistenceFileDirectory() {
        return getProperty(PROPERTY_DATA_PERSIST_FILE_DIRECTORY, System.getProperty("java.io.tmpdir"));
    }

    private void loadProperties() {
        try {
            InputStream in = TraDEProperties.class.getResourceAsStream(PROPERTY_FILE_LOCATION);

            if (in != null) {
                this.load(in);

                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
