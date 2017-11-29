package hotspotsensor;

import java.util.HashMap;
import java.util.Set;

/**
 * @author lotus.jzx
 */
public class SimpleL1LRU<E> implements L1LRU<E> {

    private final int capacity;
    private Entry<E> first;
    private Entry<E> last;
    private HashMap<E, Entry<E>> map;

    public SimpleL1LRU(int capacity) {
        this.capacity = capacity;
        map = new HashMap<>(capacity);
    }

    @Override
    public E put(E element) {
        Entry<E> entry = getEntry(element);

        if (entry != null) {
            // If element already exists, remove from queue and return it
            remove(entry);
            map.remove(entry.element);
            return element;
        } else if (!isFull()) {
            // If element does not exists and queue is not full, add it to first and return null
            map.put(element, addFirst(element));
            return null;
        } else {
            // If element does not exists and queue is full, remove last , add it to first and return null
            map.remove(removeLast().element);
            map.put(element, addFirst(element));
            return null;
        }

    }

    @Override
    public void clear() {
        map.clear();
        first = last = null;
    }

    @Override
    public boolean isFull() {
        return map.size() == capacity;
    }

    @Override
    public Set<E> getElements() {
        return map.keySet();
    }

    private void remove(Entry<E> entry) {

        if (first == entry) {
            first = entry.next;
        }

        if (last == entry) {
            last = entry.pre;
        }

        if (entry.pre != null) {
            entry.pre.next = entry.next;
        }

        if (entry.next != null) {
            entry.next.pre = entry.pre;
        }

    }

    private Entry<E> addFirst(E element) {
        Entry<E> entry = new Entry<>();
        entry.element = element;

        entry.next = first;
        if (first != null) {
            first.pre = entry;
        }

        first = entry;

        if (last == null) {
            last = entry;
        }

        return entry;



    }

    private Entry<E> removeLast() {
        Entry<E> result = last;
        last = last.pre;
        if (last == null) {
            first = null;
        } else {
            last.next = null;
        }

        return result;

    }

    private Entry<E> getEntry(E element) {
        return map.get(element);
    }


    static class Entry<K> {
        public Entry<K> pre;
        public Entry<K> next;
        public K element;
    }



}
