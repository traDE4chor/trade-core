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

import de.slub.urn.URN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by hahnml on 26.10.2016.
 */
public class ModelUtils {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.ModelUtils");

    public static final String RANDOM_URN_NAMESPACE_ID = "uuid";

    public static final String DATA_URN_NAMESPACE_ID = "data";

    public static final String URN_NAMESPACE_STRING_DELIMITER = ":";

    public static final String DDG_SCHEMA_LOCATION = "/schema/ddgModel.xsd";

    public static final String DATA_FILE_NAME = "data";

    public static String translateURNtoFolderPath(URN urn){
        StringBuilder builder = new StringBuilder();

        if (urn != null) {
            builder.append(File.separatorChar);
            builder.append(urn.getNamespaceIdentifier());
            builder.append(File.separatorChar);

            for (String s : urn.getNamespaceSpecificString().split(":")) {
                builder.append(s);
                builder.append(File.separatorChar);
            }
        }

        return builder.toString();
    }
}
