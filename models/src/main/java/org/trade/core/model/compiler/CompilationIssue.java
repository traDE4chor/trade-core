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

/**
 * This class represents an issue identified during compilation of model objects. For example, if an required
 * attribute value is missing.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class CompilationIssue {

    private CompilationIssueType type = CompilationIssueType.Unknown;

    private String message = "";

    public CompilationIssue(CompilationIssueType type, String message) {
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public CompilationIssueType getType() {
        return type;
    }

    public void setType(CompilationIssueType type) {
        this.type = type;
    }
}
