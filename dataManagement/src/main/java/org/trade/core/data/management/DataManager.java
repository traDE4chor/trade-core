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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataManager {

    private static DataManager instance = new DataManager();

    // TODO: Use Hazelcast, etc. instead of local maps
    private HashMap<String, DataValue> dataValues = new LinkedHashMap<>();

    private DataManager() {
        // Block instantiation
    }

    public static DataManager getInstance() {
        return instance;
    }


    public DataValue registerDataValue(DataValue body) {
        this.dataValues.put(body.getName(), body);

        return body;
    }

    public DataValue getDataValue(String dataValueId) {
        return this.dataValues.get(dataValueId);
    }

    public List<DataValue> getAllDataValues(String status, String createdBy) {
        Stream<DataValue> stream = dataValues.values().stream();

        if (status != null && !status.isEmpty()) {
            stream = stream.filter(d -> (d.getState() != null && d.getState().equals(status)));
        }

        if (createdBy != null && !createdBy.isEmpty()) {
            stream = stream.filter(d -> (d.getOwner() != null && d.getOwner().equals(createdBy)));
        }

        List<DataValue> result = stream.collect(Collectors.toList());

        // Return an unmodifiable copy of the list
        return Collections.unmodifiableList(result);
    }

    public boolean hasDataValue(String dataValueId) {
        return this.dataValues.containsKey(dataValueId);
    }

    public DataValue updateDataValue(String dataValueId, String name, String contentType, String type) {
        DataValue result = null;

        if (this.dataValues.containsKey(dataValueId)) {
            DataValue value = this.dataValues.get(dataValueId);

            if (name != null && !name.isEmpty() && !name.equals(value.getHumanReadableName())) {
                value.setHumanReadableName(name);
            }
            if (type != null && !type.isEmpty() && !type.equals(value.getType())) {
                value.setType(type);
            }
            if (contentType != null && !contentType.isEmpty() && !contentType.equals(value.getContentType())) {
                value.setContentType(contentType);
            }

            result = value;
        }

        return result;
    }

    public DataValue deleteDataValue(String dataValueId) {
        DataValue result = null;

        if (this.dataValues.containsKey(dataValueId)) {
            result = this.dataValues.remove(dataValueId);

            result.destroy();
        }

        return result;
    }
}
