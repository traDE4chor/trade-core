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

package org.trade.core;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import de.slub.urn.URNSyntaxException;
import org.trade.core.model.data.DataObject;

/**
 * Created by hahnml on 22.11.2016.
 */
public class TraDENode {

    public static void main(String[] args) {
        HazelcastInstance instance = Hazelcast.newHazelcastInstance();

        try {
            IMap<String, DataObject> dataObjects = instance.getMap("dataObjects");
            System.out.println(dataObjects.size());

            DataObject obj = new DataObject("chorModel1", "lattice");
            dataObjects.set(obj.getUrn().toString(), obj);

            obj = new DataObject("userA", "plot");
            dataObjects.set(obj.getUrn().toString(), obj);

            obj = new DataObject("random", "input");
            dataObjects.set(obj.getUrn().toString(), obj);

            System.out.println(dataObjects.getName() + " - " + dataObjects.size());

            dataObjects.evictAll();

            System.out.println(dataObjects.getName() + " - " + dataObjects.size());

            dataObjects.loadAll(true);

            System.out.println(dataObjects.getName() + " - " + dataObjects.size());

            instance.shutdown();
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }

        System.exit(1);
    }
}
