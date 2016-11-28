/*
 * Copyright 2016 Michael Hahn
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

import org.trade.core.model.data.DataElement;
import org.trade.core.model.lifecycle.actions.DELogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataElementLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataElementLifeCycle");

    // defining states
    public enum States {
        INITIAL, READY, ARCHIVED, DELETED
    }

    // defining events
    public enum Events {
        initial, ready, archive, unarchive, delete
    }

    private FSM<DataElement> fsm = null;

    public DataElementLifeCycle(DataElement dataElement) {
        this(dataElement, true);
    }

    public DataElementLifeCycle(DataElement dataElement, boolean initialize) {
        buildFSM();

        if (initialize) {
            init(dataElement);
        }
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataElement> fsmBuilder = FSM.FSMBuilder.newBuilder(DataElement.class);

        DELogAction action = new DELogAction();

        fsmBuilder.
                buildState(States.INITIAL.name(), true)
                .addTransition(Events.initial.name(), States.INITIAL.name())
                .addTransition(Events.ready.name(), States.READY.name(), action)
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

    private void init(DataElement dataElement) {
        if (this.fsm.getCurrentState(dataElement) == null) {
            try {
                this.fsm.onEvent(dataElement, Events.initial.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public States triggerEvent(DataElement obj, Events event) throws TooBusyException {
        State state = this.fsm.onEvent(obj, event.name());

        return States.valueOf(state.getName());
    }
}
