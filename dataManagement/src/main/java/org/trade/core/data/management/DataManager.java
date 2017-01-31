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

package org.trade.core.data.management;

import org.trade.core.model.data.DataValue;

import java.util.HashMap;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataManager {

    private static DataManager instance = new DataManager();

    // TODO: Use Hazelcast, etc. instead of local maps
    private HashMap<String, DataValue> dataValues = new HashMap<>();

    private DataManager() {
        // Block instantiation
    }

    public static DataManager getInstance() {
        return instance;
    }


    public DataValue registerDataValue(DataValue body)
    {
        this.dataValues.put(body.getName(), body);

        return body;
    }

    public DataValue getDataValue(String dataValueId) {
        return this.dataValues.get(dataValueId);
    }
}
