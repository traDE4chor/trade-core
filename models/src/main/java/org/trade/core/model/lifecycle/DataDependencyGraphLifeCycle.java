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

package org.trade.core.model.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;
import org.trade.core.model.data.DataDependencyGraph;
import org.trade.core.model.lifecycle.actions.DataDependencyGraphLogAction;

/**
 * Created by hahnml on 10.04.2017.
 */
public class DataDependencyGraphLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataDependencyGraphLifeCycle");

    // defining states
    public enum States {
        INITIAL, READY, ARCHIVED, DELETED
    }

    // defining events
    public enum Events {
        initial, ready, archive, unarchive, delete
    }

    private FSM<DataDependencyGraph> fsm = null;

    public DataDependencyGraphLifeCycle(DataDependencyGraph ddg) {
        this(ddg, true);
    }

    public DataDependencyGraphLifeCycle(DataDependencyGraph ddg, boolean initialize) {
        buildFSM();

        if (initialize) {
            init(ddg);
        }
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataDependencyGraph> fsmBuilder = FSM.FSMBuilder.newBuilder(DataDependencyGraph.class);

        DataDependencyGraphLogAction action = new DataDependencyGraphLogAction();

        fsmBuilder.
                buildState(States.INITIAL.name(), true)
                .addTransition(Events.initial.name(), States.INITIAL.name())
                .addTransition(Events.ready.name(), States.READY.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.READY.name())
                .addTransition(Events.initial.name(), States.INITIAL.name(), action)
                .addTransition(Events.archive.name(), States.ARCHIVED.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.ARCHIVED.name())
                .addTransition(Events.unarchive.name(), States.READY.name(), action)
                .addTransition(Events.delete.name(), States.DELETED.name(), action)
                .done()
                .buildState(States.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataDependencyGraph ddg) {
        if (ddg.getState() == null) {
            try {
                this.fsm.onEvent(ddg, Events.initial.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public States triggerEvent(DataDependencyGraph ddg, Events event) throws TooBusyException {
        State state = this.fsm.onEvent(ddg, event.name());

        return States.valueOf(state.getName());
    }
}
