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

package org.trade.core.model.compiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.utils.DataModelUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hahnml on 07.04.2017.
 */
public class DataModelCompiler extends ACompiler {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.compiler.DataModelCompiler");

    private String modelId = "";

    private String targetNamespace = "";

    private List<DataObject> compiledDataObjects = new ArrayList<DataObject>();

    public void compile(String dataModelId, String entity, byte[] serializedDataModel) throws CompilationException {
        Object modelDef = null;
        try {
            modelDef = DataModelUtils.unmarshalGraph(serializedDataModel);

            compile(dataModelId, entity, modelDef);

            // Write all identified issues to the log
            writeCompilationIssuesToLog(logger);
        } catch (CompilationException e) {
            logger.warn("The compilation of data model '" + this.modelId + "' ended with errors.", e);

            // Output the compilation issues also to the log as warnings
            writeCompilationIssuesToLog(logger);

            // Simply forward the exception
            throw e;
        } catch (Exception e) {
            logger.error("The compilation of data model '" + this.modelId + "' caused an " +
                    "exception.", e);

            // Wrap any other kind of exception into a CompilationException
            throw new CompilationException("The compilation of data model '" + this.modelId + "' caused an " +
                    "exception.", e,
                    this.compilationIssues);

        }
    }

    public void compile(String dataModelId, String entity, Object modelDefinition) throws CompilationException {
        this.modelId = dataModelId;
        String targetNamespace = "";
        String name = "";

        // TODO: 10.04.2017
    }

    public List<DataObject> getCompiledDataObjects() {
        return this.compiledDataObjects;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }
}
