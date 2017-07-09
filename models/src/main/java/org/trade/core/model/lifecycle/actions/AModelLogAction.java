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

package org.trade.core.model.lifecycle.actions;

import org.statefulj.fsm.model.Action;

/**
 * This class provides some generic functionality for log actions triggered on state changes of model objects.
 * <p>
 * Created by hahnml on 18.05.2017.
 */
public abstract class AModelLogAction<T> implements Action<T> {

    protected String oldState = null;

    public AModelLogAction(String oldState) {
        this.oldState = oldState;
    }

}
