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

import java.util.*;

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


    public DataValue registerDataValue(DataValue body) {
        this.dataValues.put(body.getName(), body);

        return body;
    }

    public DataValue getDataValue(String dataValueId) {
        return this.dataValues.get(dataValueId);
    }

    public List<DataValue> getAllDataValues(Integer limit, String status) {
        List<DataValue> result = new ArrayList<>();

        String statusFilter = "";
        if (status != null && !status.isEmpty()) {
            statusFilter = status;
        }

        if (limit != null) {
            // If a limit is specified, add the first 'n' data values to the result list, where n <= limit.
            Iterator<DataValue> iter = dataValues.values().iterator();
            while (result.size() < limit && iter.hasNext()) {
                DataValue value = iter.next();
                if (statusFilter.isEmpty()) {
                    result.add(value);
                } else {
                    if (value.getState().equals(statusFilter)) {
                        result.add(value);
                    }
                }
            }
        } else {
            // If there is no limit, then add all available data values
            for (DataValue value : dataValues.values()) {
                if (statusFilter.isEmpty()) {
                    result.add(value);
                } else {
                    if (value.getState().equals(statusFilter)) {
                        result.add(value);
                    }
                }
            }
        }

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
