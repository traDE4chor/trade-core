/* Copyright 2017 Michael Hahn
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

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class for model compilation functionality.
 * <p>
 * Created by hahnml on 10.04.2017.
 */
public class ACompiler {

    protected List<CompilationIssue> compilationIssues = new ArrayList<>();

    public List<CompilationIssue> getCompilationIssues() {
        return this.compilationIssues;
    }

    protected void writeCompilationIssuesToLog(Logger logger) {
        for (CompilationIssue issue : this.compilationIssues) {
            logger.warn("[{}]: " + issue.getMessage(), issue.getType().toString());
        }
    }
}
