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

package org.trade.core.data.transformation;

import org.trade.core.data.transformation.camel.CamelDataTransformationManager;
import org.trade.core.model.data.DataDependencyGraph;

import java.util.HashMap;
import java.util.Map;

/**
 * This singleton class provides a factory to create or resolve {@link IDataTransformationManager} instances based on
 * the provided parameters. By default a new {@link IDataTransformationManager} instance is created and initialized
 * on a per data dependency graph level.
 * <p>
 * Created by hahnml on 25.01.2018.
 */
public enum DataTransformationManagerFactory {
    INSTANCE;

    DataTransformationManagerFactory() {
        transformationManagers = new HashMap<>();
    }

    private Map<String, IDataTransformationManager> transformationManagers;

    /**
     * Resolves a data transformation manager for the given data dependency graph. If no such manager is available, a
     * new one is created, initialized and returned.
     *
     * @return a data transformation manager for the provided data dependency graph.
     */
    public IDataTransformationManager createDataTransformationManager(DataDependencyGraph graph) {
        IDataTransformationManager result;

        if (transformationManagers.containsKey(graph.getIdentifier())) {
            result = transformationManagers.get(graph.getIdentifier());
        } else {
            result = new CamelDataTransformationManager();

            result.initializeFromGraph(graph);

            transformationManagers.put(graph.getIdentifier(), result);
        }

        return result;
    }

    /**
     * Clears and shuts down all registered data transformation managers.
     */
    public void shutdownDataTransformationManagers() {
        for (IDataTransformationManager manager : transformationManagers.values()) {
            manager.shutdown();
        }

        this.transformationManagers.clear();
    }
}
