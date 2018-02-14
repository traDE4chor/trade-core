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
grammar Query;

@header {
    package org.trade.core.query.antlr;
}

/*
Grammar of a simple query language for the TraDE Middleware which allows users to reference resources and/or their
properties through a corresponding query string.

Author: Michael Hahn
Version: 1.0
*/

/*
 * Parser Rules
 */
query : '$' data_object_reference ('/' data_element_reference ('/' data_value_selection)?)? ('?' property_selection)? EOF ;

data_object_reference : REFERENCE_NAME ;

data_element_reference : REFERENCE_NAME ;

data_value_selection : 'value' ('[' index ']')? ;

property_selection : 'size' | 'url' ;

index : INDEX | 'first' | 'last';

/*
 * Lexer Rules
 */

/*
* Introduce character fragments to allow lower and uppercase (or even mixed) writing of keywords
*/
fragment DIGIT : [0-9] ;

fragment REFERENCE_CHAR : REFERENCE_START_CHAR | DIGIT | '_' | '-' | '[]' | '#' ;

fragment REFERENCE_START_CHAR : [a-zA-Z] ;

REFERENCE_NAME : REFERENCE_START_CHAR REFERENCE_CHAR* ;

INDEX : [1-9] DIGIT* ;