package de.unistuttgart.iaas.trade.model.ddg;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hahnml on 04.11.2016.
 */
public class DDGUtilsTest {

    @Test
    public void testUnmarshalGraphWithValidSource() throws Exception {
        DataDependenceGraphType graph = DDGUtils.unmarshalGraph(getClass().getResourceAsStream("/test-ddg.xml"));

        assertNotNull(graph);
    }

}