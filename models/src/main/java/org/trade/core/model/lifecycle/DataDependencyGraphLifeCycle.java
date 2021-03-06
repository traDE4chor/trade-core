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
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.ModelStateChangeEvent;
import org.trade.core.model.data.DataDependencyGraph;
import org.trade.core.model.lifecycle.actions.DataDependencyGraphLogAction;
import org.trade.core.utils.events.ModelEvents;
import org.trade.core.utils.states.ModelStates;

/**
 * This class implements the lifecycle of a {@link DataDependencyGraph} using a finite state machine in order to reflect the
 * currents state of the object through its {@link DataDependencyGraph#state} attribute. The lifecycle can be managed by
 * triggering corresponding events which result in state changes of the managed object, if the underlying
 * state transition is allowed or lead to a corresponding {@link LifeCycleException}, if the transition is not
 * allowed.
 * <p>
 * Created by hahnml on 10.04.2017.
 */
public class DataDependencyGraphLifeCycle {

    Logger logger = LoggerFactory.getLogger("org.trade.core.model.lifecycle.DataDependencyGraphLifeCycle");

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

        fsmBuilder.
                buildState(ModelStates.INITIAL.name(), true)
                .addTransition(ModelEvents.initial.name(), ModelStates.INITIAL.name())
                .addTransition(ModelEvents.ready.name(), ModelStates.READY.name(), new DataDependencyGraphLogAction(ModelStates.INITIAL.name()))
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), new DataDependencyGraphLogAction(ModelStates.INITIAL.name()))
                .done()
                .buildState(ModelStates.READY.name())
                .addTransition(ModelEvents.initial.name(), ModelStates.INITIAL.name(), new DataDependencyGraphLogAction(ModelStates.READY.name()))
                .addTransition(ModelEvents.archive.name(), ModelStates.ARCHIVED.name(), new DataDependencyGraphLogAction(ModelStates.READY.name()))
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), new DataDependencyGraphLogAction(ModelStates.READY.name()))
                .done()
                .buildState(ModelStates.ARCHIVED.name())
                .addTransition(ModelEvents.unarchive.name(), ModelStates.READY.name(), new DataDependencyGraphLogAction(ModelStates.ARCHIVED.name()))
                .addTransition(ModelEvents.delete.name(), ModelStates.DELETED.name(), new DataDependencyGraphLogAction(ModelStates.ARCHIVED.name()))
                .done()
                .buildState(ModelStates.DELETED.name())
                .setEndState(true)
                .done();

        this.fsm = fsmBuilder.build();
    }

    private void init(DataDependencyGraph ddg) {
        if (ddg.getState() == null) {
            try {
                this.fsm.onEvent(ddg, ModelEvents.initial.name());

                Logger resourceLogger = LoggerFactory.getLogger(ddg.getClass().getCanonicalName());

                // Log the creation of the resource and trigger a corresponding event
                resourceLogger.info("State of data dependency graph ({}) changed from '{}' to '{}' on event '{}'.", ddg
                        .getIdentifier(), null, ddg.getState(), ModelEvents.initial.name());

                AuditingServiceFactory.createAuditingService().fireEvent(new ModelStateChangeEvent(ddg
                        .getIdentifier(), DataDependencyGraph.class, ddg, null, ddg.getState(), ModelEvents.initial
                        .name()));
            } catch (TooBusyException e) {
                e.printStackTrace();
            }
        }
    }

    public ModelStates triggerEvent(DataDependencyGraph ddg, ModelEvents event) throws TooBusyException {
        State state = this.fsm.onEvent(ddg, event.name());

        return ModelStates.valueOf(state.getName());
    }
}
