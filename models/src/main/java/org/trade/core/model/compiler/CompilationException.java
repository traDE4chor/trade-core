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

import java.util.ArrayList;
import java.util.List;

/**
 * An exeption occured during the compilation of model objects.
 * <p>
 * Created by hahnml on 07.04.2017.
 */
public class CompilationException extends Exception {

    private static final long serialVersionUID = 4415585724489667357L;

    private List<CompilationIssue> compilationErrors = new ArrayList<CompilationIssue>();

    public CompilationException(String message, List<CompilationIssue> compilationErrors) {
        super(message);
        this.compilationErrors = compilationErrors;
    }

    public CompilationException(String message, Throwable t, List<CompilationIssue> compilationErrors) {
        super(message, t);
        this.compilationErrors = compilationErrors;
    }
}
