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

package org.trade.core.auditing;

import org.trade.core.auditing.events.ATraDEEvent;

/**
 * This interface defines basic methods an auditing service implementation should provide.
 * <p>
 * Created by hahnml on 25.10.2016.
 */
public interface IAuditingService {

    void registerEventListener(TraDEEventListener listener);

    void unregisterEventListener(TraDEEventListener listener);

    void fireEvent(ATraDEEvent event);
}
