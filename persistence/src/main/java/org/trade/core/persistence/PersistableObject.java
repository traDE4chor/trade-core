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

package org.trade.core.persistence;

import java.io.Serializable;

/**
 * This interface defines basic methods for persistable objects so that they can be stored and loaded by an
 * {@link IPersistenceProvider}.
 * <p>
 * Created by hahnml on 25.04.2017.
 */
public interface PersistableObject extends Serializable {

    /**
     * Provides the identifier of the persistable object.
     *
     * @return An ID which uniquely identifies the object.
     */
    String getIdentifier();
}
