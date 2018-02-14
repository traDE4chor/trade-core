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

package org.trade.core.query.listener;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides a special {@link BaseErrorListener} implementation that collects all syntax errors detected
 * during the parsing of a query string.
 * <p>
 * Created by hahnml on 05.02.2018.
 */
public class QueryErrorListener extends BaseErrorListener {

    private List<QueryParseError> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
        Collections.reverse(stack);

        // Translate the syntax error into our object representation
        QueryParseError error = new QueryParseError(stack, offendingSymbol, line, charPositionInLine, msg, e);

        errors.add(error);
    }

    /**
     * Gets the syntax errors detected during the parsing of a query string.
     *
     * @return the errors
     */
    public List<QueryParseError> getErrors() {
        return errors;
    }
}
