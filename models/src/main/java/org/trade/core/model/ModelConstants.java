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

package org.trade.core.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines a set of basic model constants used throughout the whole middleware.
 * <p>
 * Created by hahnml on 26.10.2016.
 */
public class ModelConstants {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.ModelConstants");

    public static final String DDG_SCHEMA_LOCATION = "/schema/ddgModel.xsd";

    public static final String DDG_FILE_EXTENSION = "trade";

    public static final String DATA_VALUE__DATA_COLLECTION = "dataValueData";

    public static final String DATA_MODEL__DATA_COLLECTION = "dataModelData";

    public static final String DATA_DEPENDENCY_GRAPH__DATA_COLLECTION = "dataDependencyGraphData";

    public static final String BINARY_TYPE = "binary";
}
