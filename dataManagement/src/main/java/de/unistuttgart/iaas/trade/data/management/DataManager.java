package de.unistuttgart.iaas.trade.data.management;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import de.slub.urn.URNSyntaxException;
import de.unistuttgart.iaas.trade.model.data.DataObject;

import java.util.stream.*;

/**
 * Created by hahnml on 25.10.2016.
 */
public class DataManager {

    public static void main(String[] args) {
        HazelcastInstance inst = Hazelcast.newHazelcastInstance();

        DataObject obj = null;
        try {
            obj = new DataObject();
        } catch (URNSyntaxException e) {
            e.printStackTrace();
        }

        if (obj != null)
        inst.getMap("dataObject").put(obj.getUrn().toString(), obj);

        IMap map = inst.getMap("dataObject");
        System.out.println(map.getName() + " - " + map.size());

        inst.shutdown();

        System.exit(1);
    }

}
