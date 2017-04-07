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

import org.trade.core.model.data.DataObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by hahnml on 07.04.2017.
 */
public class DataModelCompiler {

    private byte[] modelData = null;

    private List<DataObject> compiledDataObjects = new ArrayList<DataObject>();

    private List<CompilationError> compilerErrors = new ArrayList<CompilationError>();

    public DataModelCompiler(byte[] data) {
        this.modelData = data;
    }

    public List<DataObject> compileDataModel(byte[] data) {
        // TODO: 07.04.2017

        return Collections.emptyList();
    }

    public List<DataObject> getCompiledDataObjects() {
        return compiledDataObjects;
    }

    public List<CompilationError> getCompilerErrors() {
        return compilerErrors;
    }
}
