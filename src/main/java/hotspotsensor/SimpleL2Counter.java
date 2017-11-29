package hotspotsensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SimpleL2Counter<E> implements L2Counter<E> {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleL2Counter.class);

    private Map<E, MutableInt> map;

    private int capacity;

    public SimpleL2Counter(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity:" + capacity);
        }
        this.map = new HashMap<>(capacity);
        this.capacity = capacity;
    }

    @Override
    public boolean incIfPresent(E element) {

        MutableInt value = map.get(element);

        if (value == null) {
            return false;
        } else {
            value.inc();
            return true;
        }
    }

    @Override
    public boolean isFull() {
        return map.size() == this.capacity;
    }

    @Override
    public boolean addIfAbsentAndNotFull(E element) {
        if (!map.containsKey(element) && !isFull()) {
            map.put(element, new MutableInt(1));
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public List<Entry<E>> getElements() {
        return map.entrySet()
                  .stream()
                  .map(e -> new Entry<>(e.getKey(), e.getValue().value()))
                  .collect(Collectors.toList());
    }

    @Override
    public int hashCode() {
        int result = map != null ? map.hashCode() : 0;
        result = 31 * result + capacity;
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(map);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SimpleL2Counter)) {
            return false;
        }

        SimpleL2Counter<?> that = (SimpleL2Counter<?>) o;

        return map.equals(that.map);

    }
}
