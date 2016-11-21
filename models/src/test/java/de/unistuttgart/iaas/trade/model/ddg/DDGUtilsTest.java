package de.unistuttgart.iaas.trade.model.ddg;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 04.11.2016.
 */
public class DDGUtilsTest {

    @Test
    public void testUnmarshalGraphWithValidSource() throws Exception {
        DataDependenceGraph graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/test-ddg.xml"));

        assertNotNull(graph);
    }

    @Test
    public void testUnmarshalMarshalGraphRoundTrip() throws Exception {
        DataDependenceGraph graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/test-ddg.xml"));

        graph.getDependenceEdges().getDependenceEdge().get(0).setName("newName");

        byte[] result = DDGUtils.marshalGraph(graph);

        DataDependenceGraph graph1 = DDGUtils.unmarshalGraph(result);

        assertNotNull(result);
        assertEquals("newName", graph1.getDependenceEdges().getDependenceEdge().get(0).getName());
    }
}