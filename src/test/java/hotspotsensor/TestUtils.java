package hotspotsensor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author iamlotus@gmail.com
 */
public class TestUtils {

    public static <T> Set<T> set(T... elements) {
        Set<T> result = new HashSet<>();
        for (T element : elements) {
            result.add(element);
        }
        return result;
    }

    public static <T> List<T> list(T... elements) {
        return Arrays.asList(elements);

    }

    public static <T> Set<T> set(Iterable<T> elements) {
        Set<T> result = new HashSet<>();
        for (T element : elements) {
            result.add(element);
        }
        return result;
    }

    public static <T> Entry<T> e(T element, int count) {
        return new Entry<>(element, count);
    }

    public static Map<String, Double> map(String key, double d) {
        Map result = new HashMap<>();
        result.put(key, d);
        return result;
    }

    public static Map<String, Double> map(String key1, double d1, String key2, double d2) {
        Map result = new HashMap<>();
        result.put(key1, d1);
        result.put(key2, d2);
        return result;
    }

    public static Map<String, Double> map(String key1, double d1, String key2, double d2, String key3, double d3) {
        Map result = new HashMap<>();
        result.put(key1, d1);
        result.put(key2, d2);
        result.put(key3, d3);
        return result;
    }


}
