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

import org.antlr.v4.runtime.RecognitionException;

import java.util.List;

/**
 * This class represents an error detected during parsing a query string.
 * <p>
 * Created by hahnml on 05.02.2018.
 */
public class QueryParseError {

    private List<String> ruleInvocationStack;

    private Object offendingSymbol;

    private int line;

    private int charPositionInLine;

    private String errorMessage;

    private RecognitionException exception;

    /**
     * Instantiates a new Query parse error.
     *
     * @param ruleInvocationStack the rule invocation stack
     * @param offendingSymbol     the offending symbol
     * @param line                the line
     * @param charPositionInLine  the char position in line
     * @param errorMessage        the error message
     * @param exception           the exception
     */
    public QueryParseError(List<String> ruleInvocationStack, Object offendingSymbol, int line, int charPositionInLine,
                           String errorMessage, RecognitionException exception) {
        this.ruleInvocationStack = ruleInvocationStack;
        this.offendingSymbol = offendingSymbol;
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public List<String> getRuleInvocationStack() {
        return ruleInvocationStack;
    }

    public Object getOffendingSymbol() {
        return offendingSymbol;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public RecognitionException getException() {
        return exception;
    }
}
