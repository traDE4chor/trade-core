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

import org.trade.core.model.data.DataElement;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.ddg.DataDependenceGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hahnml on 02.11.2016.
 */
public class DDGCompiler {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.compiler.DDGCompiler");

    private DataDependenceGraph ddgDef = null;

    private List<DataObject> dataObjects = new ArrayList<DataObject>();

    private HashMap<DataObject, List<DataElement>> dataElements = new HashMap<DataObject, List<DataElement>>();


}
