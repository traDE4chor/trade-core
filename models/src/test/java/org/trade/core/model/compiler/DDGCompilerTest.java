/* Copyright 2017 Michael Hahn
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

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.model.data.DataModel;
import org.trade.core.model.dataTransformation.DataTransformation;
import org.trade.core.model.ddg.DataDependenceGraph;
import org.trade.core.model.utils.DDGUtils;
import org.trade.core.utils.states.ModelStates;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by hahnml on 10.04.2017.
 */
public class DDGCompilerTest {

    @Test
    public void testDDGCompilationWithGraph() throws Exception {
        DataDependenceGraph graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/opalData." +
                ModelConstants.DDG_FILE_EXTENSION));

        assertNotNull(graph);

        // Deserialize and compile the provided data dependency graph, i.e., generate the specified data objects and data
        // elements
        DDGCompiler comp = new DDGCompiler();
        comp.compile(UUID.randomUUID().toString(), "someTestEntity", graph);

        // Get the resulting data model of this DDG
        DataModel dataModel = comp.getCompiledDataModel();
        assertNotNull(dataModel);
        assertEquals(ModelStates.READY.toString(), dataModel.getState());
        assertNotNull(dataModel.getDataObjects());
        assertTrue(dataModel.getDataObjects().size() > 0);

        // Get the list of compilation issues
        List<CompilationIssue> issues = comp.getCompilationIssues();
        assertNotNull(issues);
        assertEquals(0, issues.size());
    }

    @Test
    public void testDDGCompilationWithByteArray() throws Exception {
        byte[] graphData = IOUtils.toByteArray(getClass().getResourceAsStream("/opalData." + ModelConstants
                .DDG_FILE_EXTENSION));
        assertNotNull(graphData);

        // Deserialize and compile the provided data dependency graph, i.e., generate the specified data objects and data
        // elements
        DDGCompiler comp = new DDGCompiler();
        comp.compile(UUID.randomUUID().toString(), "someTestEntity", graphData);

        // Get the resulting data model of this DDG
        DataModel dataModel = comp.getCompiledDataModel();
        assertNotNull(dataModel);
        assertEquals(ModelStates.READY.toString(), dataModel.getState());
        assertNotNull(dataModel.getDataObjects());
        assertTrue(dataModel.getDataObjects().size() > 0);

        // Get the list of compilation issues
        List<CompilationIssue> issues = comp.getCompilationIssues();
        assertNotNull(issues);
        assertEquals(0, issues.size());
    }

    @Test
    public void testDDGCompilationWithDataTransformations() throws Exception {
        DataDependenceGraph graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/opalDataTransformation." +
                ModelConstants.DDG_FILE_EXTENSION));

        assertNotNull(graph);

        // Deserialize and compile the provided data dependency graph, i.e., generate the specified data objects and data
        // elements
        DDGCompiler comp = new DDGCompiler();
        comp.compile(UUID.randomUUID().toString(), "someTestEntity", graph);

        // Get the resulting data model of this DDG
        DataModel dataModel = comp.getCompiledDataModel();
        assertNotNull(dataModel);
        assertEquals(ModelStates.READY.toString(), dataModel.getState());
        assertNotNull(dataModel.getDataObjects());
        assertTrue(dataModel.getDataObjects().size() > 0);

        List<DataTransformation> transformations = comp.getCompiledDataTransformations();
        assertNotNull(transformations);
        assertTrue(transformations.size() == 2);

        // Get the list of compilation issues
        List<CompilationIssue> issues = comp.getCompilationIssues();
        assertNotNull(issues);
        assertEquals(0, issues.size());
    }
}
