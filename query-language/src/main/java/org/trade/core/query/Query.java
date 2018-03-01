/* Copyright 2018 Michael Hahn
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

package org.trade.core.query;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.trade.core.query.antlr.QueryLexer;
import org.trade.core.query.antlr.QueryParser;
import org.trade.core.query.listener.QueryErrorListener;
import org.trade.core.query.listener.QueryParseError;
import org.trade.core.query.listener.QueryTranslator;

import java.util.List;

/**
 * This class provides the model of a TraDE query.
 * <p>
 * Created by hahnml on 05.02.2018.
 */
public class Query {

    public static final String QUERY_PREFIX = "$";

    private String queryString;

    private String dataObjectName;

    private String dataElementName;

    private String indexOfDataValue;

    private PROPERTY property;

    private boolean specifiesPropertySelection;

    private boolean specifiesDataValue;

    private boolean specifiesDataValueIndex;

    private boolean isValid;

    private List<QueryParseError> parserErrors;

    private Query(String queryString) {
        this.queryString = queryString;
    }

    /**
     * Parses a query string into a respective Query model object.
     *
     * @param queryString the query string
     * @return the query object
     */
    public static Query parseQuery(String queryString) {
        // Create a new Query object that can be used by the QueryTranslator and QueryErrorListener
        Query query = new Query(queryString);

        // Read the query string as a char stream
        CharStream input = CharStreams.fromString(queryString);

        // Forward the char stream to the lexer
        QueryLexer lexer = new QueryLexer(input);

        // Create a stream for the tokens
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);

        // Forward the token stream to the parser
        QueryParser parser = new QueryParser(tokenStream);

        // Add a special error listener that collects all occurring errors in this class
        QueryErrorListener errorListener = new QueryErrorListener();
        parser.addErrorListener(errorListener);

        // Start parsing the query string
        ParseTree tree = parser.query();

        // Translate the query string into a Query object for further processing by walking through the parse tree
        // using our QueryTranslator
        ParseTreeWalker walker = new ParseTreeWalker();
        QueryTranslator translator = new QueryTranslator(query);
        walker.walk(translator, tree);

        query.setParserErrors(errorListener.getErrors());

        if (query.parserErrors.isEmpty()) {
            // Set to "false" as default
            query.setValid(false);

            // Apply some "semantic" checks, i.e., if the query will point to a usable value or not
            if (query.getDataObjectName() != null) {
                if (query.getDataElementName() != null) {
                    if (query.specifiesDataValue()) {
                        // Queries specifying a data object, data element and a data value might be valid.
                        // Further checks require run time information, e.g., a data value query
                        // ($dataObject/dataElement/value) without an index specification is valid if the data
                        // element is not a collection, else the query is invalid in that context since it requires
                        // an index to point to a single value.
                        query.setValid(true);
                    } else {
                        // Queries specifying a data object and data element without a data value are
                        // only valid, if they have a property selection
                        if (query.specifiesPropertySelection()) {
                            query.setValid(true);
                        }
                    }
                } else {
                    // Queries specifying only a data object without a data element are only valid, if they have a
                    // property selection
                    if (query.specifiesPropertySelection()) {
                        query.setValid(true);
                    }
                }
            }
        } else {
            query.setValid(false);
        }

        return query;
    }

    /**
     * Gets the data object name referenced in the query.
     *
     * @return the data object name
     */
    public String getDataObjectName() {
        return dataObjectName;
    }

    /**
     * Sets the data object name referenced in the query.
     *
     * @param dataObjectName the data object name
     */
    public void setDataObjectName(String dataObjectName) {
        this.dataObjectName = dataObjectName;
    }

    /**
     * Gets the data element name referenced in the query.
     *
     * @return the data element name
     */
    public String getDataElementName() {
        return dataElementName;
    }

    /**
     * Sets the data element name referenced in the query.
     *
     * @param dataElementName the data element name
     */
    public void setDataElementName(String dataElementName) {
        this.dataElementName = dataElementName;
    }

    /**
     * Gets the index of data value referenced in the query.
     *
     * @return the index of data value
     */
    public String getIndexOfDataValue() {
        return indexOfDataValue;
    }

    /**
     * Sets the index of data value referenced in the query.
     *
     * @param indexOfDataValue the index of data value
     */
    public void setIndexOfDataValue(String indexOfDataValue) {
        this.indexOfDataValue = indexOfDataValue;
    }

    /**
     * Gets the property referenced in the property selection of the query.
     *
     * @return the property
     */
    public PROPERTY getProperty() {
        return property;
    }

    /**
     * Sets the property referenced in the property selection of the query.
     *
     * @param property the property
     */
    public void setProperty(PROPERTY property) {
        this.property = property;
    }

    /**
     * Whether the query specifies a property selection (e.g., "..?size") or not.
     *
     * @return True, if the query has a property selection. False, otherwise.
     */
    public boolean specifiesPropertySelection() {
        return specifiesPropertySelection;
    }

    /**
     * Sets whether the query specifies a property selection or not.
     *
     * @param specifiesPropertySelection True, if the query has a property selection. False, otherwise.
     */
    public void setSpecifiesPropertySelection(boolean specifiesPropertySelection) {
        this.specifiesPropertySelection = specifiesPropertySelection;
    }

    /**
     * Whether the query refers to a data value ("../value") or not.
     *
     * @return True, if the query refers to a data value. False, otherwise.
     */
    public boolean specifiesDataValue() {
        return specifiesDataValue;
    }

    /**
     * Sets whether the query refers to a data value or not.
     *
     * @param specifiesDataValue True, if the query refers to a data value. False, otherwise.
     */
    public void setSpecifiesDataValue(boolean specifiesDataValue) {
        this.specifiesDataValue = specifiesDataValue;
    }

    /**
     * Whether the query refers to a collection of data values and specifies an index selection ("../value[3]") or not.
     *
     * @return True, if the query specifies an index selection. False, otherwise.
     */
    public boolean specifiesDataValueIndex() {
        return specifiesDataValueIndex;
    }

    /**
     * Sets whether the query refers to a collection of data values and specifies an index selection or not.
     *
     * @param specifiesDataValueIndex True, if the query specifies an index selection. False, otherwise.
     */
    public void setSpecifiesDataValueIndex(boolean specifiesDataValueIndex) {
        this.specifiesDataValueIndex = specifiesDataValueIndex;
    }

    /**
     * Whether the query is valid, i.e., it's syntax is correct. If the query is invalid, all detected errors can be
     * retrieved by calling {@link Query#getParserErrors()}.
     *
     * @return True, if the provided query string represents a valid query. False, otherwise.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Sets whether the query is valid or not.
     *
     * @param valid True, if the provided query string represents a valid query. False, otherwise.
     */
    public void setValid(boolean valid) {
        isValid = valid;
    }

    /**
     * Provides a list of detected parser errors.
     *
     * @return the parser errors
     */
    public List<QueryParseError> getParserErrors() {
        return parserErrors;
    }

    /**
     * Sets the list of detected parser errors.
     *
     * @param parserErrors the parser errors
     */
    public void setParserErrors(List<QueryParseError> parserErrors) {
        this.parserErrors = parserErrors;
    }

    /**
     * Gets the raw query string.
     *
     * @return the query string
     */
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String toString() {
        return this.queryString;
    }

    /**
     * This enum represents possible property selection values.
     */
    public enum PROPERTY {
        SIZE, URL
    }

    /**
     * This enum represents possible static index values.
     */
    public enum INDEX {
        FIRST, LAST
    }
}
