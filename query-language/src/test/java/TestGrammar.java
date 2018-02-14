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

import org.junit.Test;
import org.trade.core.query.Query;

import static org.junit.Assert.*;

/**
 * This class provides some tests of the {@link Query} class and its underlying generated ANTLR parser and lexer
 * classes.
 * <p>
 * Created by hahnml on 02.02.2018.
 */
public class TestGrammar {

    @Test
    public void testDataObjectQuery() throws Exception {
        String queryString = "$dataObject-Name[]?size";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertTrue(query.getParserErrors().isEmpty());
        assertEquals("dataObject-Name[]", query.getDataObjectName());
        assertEquals(Query.PROPERTY.SIZE, query.getProperty());
        assertTrue(query.isValid());
    }

    @Test
    public void testWrongDataObjectQuery() throws Exception {
        String queryString = "$dataObject-Name[]?value";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertFalse(query.getParserErrors().isEmpty());
        assertTrue(query.getParserErrors().size() == 1);
        assertEquals("dataObject-Name[]", query.getDataObjectName());
        assertNull(query.getProperty());
        assertFalse(query.isValid());
    }

    @Test
    public void testDataElementQuery() throws Exception {
        String queryString = "$dataObjectName/data_ElementName[]?url";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertTrue(query.getParserErrors().isEmpty());
        assertEquals("dataObjectName", query.getDataObjectName());
        assertEquals("data_ElementName[]", query.getDataElementName());
        assertTrue(query.isValid());
    }

    @Test
    public void testWrongDataElementQuery() throws Exception {
        String queryString = "$dataObject Name/data_ElementName[]?prop";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertFalse(query.getParserErrors().isEmpty());
        assertTrue(query.getParserErrors().size() == 2);
        assertEquals("dataObject", query.getDataObjectName());
        assertEquals("data_ElementName[]", query.getDataElementName());
        assertNull(query.getProperty());
        assertFalse(query.isValid());
    }

    @Test
    public void testSimpleDataValueQuery() throws Exception {
        String queryString = "$dataObjectName/dataElementName/value";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertTrue(query.getParserErrors().isEmpty());
        assertEquals("dataObjectName", query.getDataObjectName());
        assertEquals("dataElementName", query.getDataElementName());
        assertTrue(query.isValid());
    }

    @Test
    public void testFullDataValueQuery() throws Exception {
        String queryString = "$dataObjectName/dataElementName/value[1]?url";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertTrue(query.getParserErrors().isEmpty());
        assertEquals("dataObjectName", query.getDataObjectName());
        assertEquals("dataElementName", query.getDataElementName());
        assertEquals("1", query.getIndexOfDataValue());
        assertEquals(Query.PROPERTY.URL, query.getProperty());
        assertTrue(query.isValid());

        queryString = "$dataObjectName/dataElementName/value[first]";
        query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertTrue(query.getParserErrors().isEmpty());
        assertEquals("first", query.getIndexOfDataValue());
        assertTrue(query.isValid());

        queryString = "$dataObjectName/dataElementName/value[last]";
        query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertTrue(query.getParserErrors().isEmpty());
        assertEquals("last", query.getIndexOfDataValue());
        assertTrue(query.isValid());
    }

    @Test
    public void testWrongDataValueQuery() throws Exception {
        String queryString = "$dataObjectName/dataElementName/valeu[3]";

        Query query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertFalse(query.getParserErrors().isEmpty());
        assertTrue(query.getParserErrors().size() == 1);
        assertEquals("dataObjectName", query.getDataObjectName());
        assertEquals("dataElementName", query.getDataElementName());
        assertEquals(null, query.getIndexOfDataValue());
        assertFalse(query.isValid());

        queryString = "$dataObjectName/dataElementName/value[3a]";

        query = Query.parseQuery(queryString);

        assertNotNull(query);
        assertFalse(query.getParserErrors().isEmpty());
        assertTrue(query.getParserErrors().size() == 1);
        assertEquals("dataObjectName", query.getDataObjectName());
        assertEquals("dataElementName", query.getDataElementName());
        assertEquals("3", query.getIndexOfDataValue());
        assertFalse(query.isValid());
    }
}
