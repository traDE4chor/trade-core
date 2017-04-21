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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statefulj.fsm.RetryException;
import org.statefulj.fsm.model.Action;
import org.trade.core.auditing.AuditingServiceFactory;
import org.trade.core.auditing.events.InstanceStateChangeEvent;
import org.trade.core.model.data.DataValue;

/**
 * Created by hahnml on 07.04.2017.
 */
public class DataValueLogAction implements Action<DataValue> {

    @Override
    public void execute(DataValue stateful, String event, Object... args) throws RetryException {
        Logger logger = LoggerFactory.getLogger(stateful.getClass().getCanonicalName());

        logger.info("State of data value ({}) changed to '{}' on event '{}'.", stateful.getIdentifier(),
                stateful.getState()
                , event);

        AuditingServiceFactory.createAuditingService().fireEvent(new InstanceStateChangeEvent(stateful.getIdentifier(),
                DataValue.class, stateful.getState(), event));
    }

}
