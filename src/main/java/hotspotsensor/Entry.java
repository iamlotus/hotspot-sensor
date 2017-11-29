package hotspotsensor;

/**
 * Element and Count, immutable.
 *
 * @param <E> Element type
 * @author iamlotus@gmail.com
 */
public class Entry<E> {
    private E element;

    private int count;

    public Entry(E element, int count) {
        if (element == null) {
            throw new NullPointerException();
        }

        if (count < 1) {
            throw new IllegalArgumentException("count");
        }

        this.element = element;
        this.count = count;
    }


    public E getElement() {
        return element;
    }


    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Entry)) {
            return false;
        }

        Entry<?> that = (Entry<?>) o;

        if (count != that.getCount()) {
            return false;
        }
        return element.equals(that.getElement());

    }

    @Override
    public int hashCode() {
        int result = element.hashCode();
        result = 31 * result + count;
        return result;
    }

    public String toString() {
        return element + "=" + count;
    }
}
