package hotspotsensor;

import java.util.Set;

/**
 * @author lotus.jzx
 */
public interface L1LRU<E> {

    /**
     * put element into l1 LRU-cache.
     * <ol>
     * <li>If element already exists, remove from queue and return it</li>
     * <li>If element does not exists and cache is not full, add it to first and return null</li>
     * <li>If element does not exists and cache is full, add it to first, remove last and return null </li>
     * </ol>
     *
     * @param element
     * @return
     */
    E put(E element);

    void clear();

    /**
     * return this is full
     *
     * @return
     */
    boolean isFull();

    /**
     * return all alive elements
     *
     * @return
     */
    Set<E> getElements();

    interface Factory<T> {
        L1LRU<T> create();
    }

}
