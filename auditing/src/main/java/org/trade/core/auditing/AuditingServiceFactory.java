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

package org.trade.core.auditing;

import org.trade.core.utils.TraDEProperties;

/**
 * A service factory for {@link IAuditingService} implementations.
 * <p>
 * Created by hahnml on 21.04.2017.
 */
public class AuditingServiceFactory {

    /**
     * Provides an {@link IAuditingService} object which can be used to register event listeners and fire events.
     *
     * @return An {@link IAuditingService} object
     */
    public static IAuditingService createAuditingService() {
        AuditingService result = AuditingService.INSTANCE;

        if (!result.hasProperties()) {
            result.setProperties(new TraDEProperties());
        }

        return result;
    }

}
