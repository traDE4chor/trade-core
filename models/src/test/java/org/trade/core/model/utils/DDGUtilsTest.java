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

package org.trade.core.model.utils;

import org.junit.Test;
import org.trade.core.model.ModelConstants;
import org.trade.core.model.ddg.DataDependenceGraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hahnml on 04.11.2016.
 */
public class DDGUtilsTest {

    @Test
    public void testUnmarshalGraphWithValidSource() throws Exception {
        DataDependenceGraph graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/test-ddg." +
                ModelConstants.DDG_FILE_EXTENSION));

        assertNotNull(graph);
    }

    @Test
    public void testUnmarshalMarshalGraphRoundTrip() throws Exception {
        DataDependenceGraph graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/test-ddg." +
                ModelConstants.DDG_FILE_EXTENSION));

        graph.getDependenceEdges().getDependenceEdge().get(0).setName("newName");

        byte[] result = DDGUtils.marshalGraph(graph);

        DataDependenceGraph graph1 = DDGUtils.unmarshalGraph(result);

        assertNotNull(result);
        assertEquals("newName", graph1.getDependenceEdges().getDependenceEdge().get(0).getName());
    }
}