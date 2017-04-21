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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.FSM;
import org.statefulj.fsm.TooBusyException;
import org.statefulj.fsm.model.State;
import org.trade.core.model.data.DataObject;
import org.trade.core.model.lifecycle.actions.DataObjectLogAction;
import org.trade.core.utils.ModelEvents;
import org.trade.core.utils.ModelStates;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataObjectLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataObjectLifeCycle");

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

        DataObjectLogAction action = new DataObjectLogAction();

        fsmBuilder.
                buildState(ModelStates.INITIAL.name(), true)
                .addTransition(ModelEvents.initial.name(), ModelStates.INITIAL.name())
                .addTransition(ModelEvents.ready.name(), ModelStates.READY.name(), action)
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), action)
                .done()
                .buildState(ModelStates.READY.name())
                .addTransition(ModelEvents.initial.name(), ModelStates.INITIAL.name(), action)
                .addTransition(ModelEvents.archive.name(), ModelStates.ARCHIVED.name(), action)
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), action)
                .done()
                .buildState(ModelStates.ARCHIVED.name())
                .addTransition(ModelEvents.unarchive.name(), ModelStates.READY.name(), action)
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), action)
                .done()
                .buildState(ModelStates.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataObject dataObject) {
        if (dataObject.getState() == null) {
            try {
                this.fsm.onEvent(dataObject, ModelEvents.initial.name());
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public ModelStates triggerEvent(DataObject obj, ModelEvents event) throws TooBusyException {
        State state = this.fsm.onEvent(obj, event.name());

        return ModelStates.valueOf(state.getName());
    }
}
