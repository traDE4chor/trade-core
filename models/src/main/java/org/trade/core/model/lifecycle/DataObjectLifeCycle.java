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

import org.trade.core.model.data.DataObject;
import org.trade.core.model.lifecycle.actions.DOLogAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataObjectLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataObjectLifeCycle");

    // defining states
    public enum States {
        INITIAL, READY, ARCHIVED, DELETED
    }

    // defining events
    public enum Events {
        initial, ready, archive, unarchive, delete
    }

    private FSM<DataObject> fsm = null;

    public DataObjectLifeCycle(DataObject dataObject) {
        this(dataObject, true);
    }

    public DataObjectLifeCycle(DataObject dataObject, boolean initialize) {
        buildFSM();

        if (initialize) {
            init(dataObject);
        }
    }

    private void buildFSM() {
        FSM.FSMBuilder<DataObject> fsmBuilder = FSM.FSMBuilder.newBuilder(DataObject.class);

        DOLogAction action = new DOLogAction();

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

    private void init(DataObject dataObject) {
        if (dataObject.getState() == null) {
            try {
                this.fsm.onEvent(dataObject, Events.initial.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public States triggerEvent(DataObject obj, Events event) throws TooBusyException {
        State state = this.fsm.onEvent(obj, event.name());

        return States.valueOf(state.getName());
    }
}
