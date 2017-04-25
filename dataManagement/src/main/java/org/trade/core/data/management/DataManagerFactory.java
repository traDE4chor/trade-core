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

package org.trade.core.data.management;

import org.trade.core.data.management.hazelcast.HazelcastDataManager;
import org.trade.core.data.management.simple.SimpleDataManager;
import org.trade.core.utils.TraDEProperties;

/**
 * This class provides a factory to create a {@link IDataManager} object based on the configuration of the middleware
 * (i.e., {@link org.trade.core.utils.TraDEProperties}).
 * <p>
 * Created by hahnml on 24.04.2017.
 */
public class DataManagerFactory {
    /**
     * Creates a data manager based on the specified properties.
     *
     * @return the data manager according to the defined property in 'config.properties'.
     */
    public static IDataManager createDataManager() {
        IDataManager result;

        TraDEProperties props = new TraDEProperties();

        switch (props.getDeploymentMode()) {
            case SINGLE_NODE:
                result = SimpleDataManager.INSTANCE;
                break;
            case MULTI_NODE:
                result = HazelcastDataManager.INSTANCE;
                break;
            default:
                result = SimpleDataManager.INSTANCE;
        }

        return result;
    }
}
