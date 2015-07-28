package FuseJet.Utils;

import java.util.HashMap;

/**
 * User: yhe
 * Date: 11/13/12
 * Time: 2:16 PM
 */
public class CollectionUtils {

    public static void incrementHashMap (HashMap map, String key, int n) {
        int count;
        Integer countI = (Integer) map.get(key);
        if (countI == null)
            count = 0;
        else {
            count = countI;
        }
        map.put(key, count+n);
    }

    public static void incrementTwoLevelHashMap (HashMap map, String key1, String key2, int n) {
        HashMap map2 = (HashMap) map.get(key1);
        if (map2 == null) {
            map2 = new HashMap();
            map.put(key1, map2);
        }
        incrementHashMap (map2, key2, n);
    }

}
