package hotspotsensor;

import java.util.List;

/**
 * L2 Counter
 *
 * @author lotus.jzx
 */
public interface L2Counter<E> {

    interface Factory<T> {
        L2Counter<T> create();
    }

    /**
     * increase counter of element if present
     *
     * @param element
     * @return true if present, false else
     */
    boolean incIfPresent(E element);


    /**
     * counter is full (a full counter can not accept new element)
     *
     * @return true is full ,false else
     */
    boolean isFull();

    /**
     * add element and set counter to 1 if element is absent and counter is not full.
     *
     * @param element
     * @return true if element is absent  and counter is not full, false else
     */
    boolean addIfAbsentAndNotFull(E element);

    /**
     * clear all elements
     */
    void clear();

    /**
     * get all element and count number, with random order.
     *
     * @return
     */
    List<Entry<E>> getElements();



}
