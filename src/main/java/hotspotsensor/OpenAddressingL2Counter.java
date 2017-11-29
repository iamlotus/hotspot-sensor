package hotspotsensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 *
 * @author iamlotus@gmail.com
 */
public class OpenAddressingL2Counter<E> implements L2Counter<E> {

    static final int[] PRIMERS =
        new int[] {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101,
            103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199, 211,
            223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317, 331, 337,
            347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449, 457, 461,
            463, 467, 479, 487, 49, 1, 499, 503, 509, 521, 523, 541, 547, 557, 563, 5, 69, 571, 577, 587, 593, 599, 601,
            607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727, 733, 739,
            743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877, 881,
            883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997, 1009};
    static final int MAX = PRIMERS[PRIMERS.length - 1];
    private static final double LOAD_FACTOR = 0.5;
    private int[] hashCodes;
    private Object[] elements;
    private int[] counters;
    private int tableSize;
    private int size;
    private int capacity;


    public OpenAddressingL2Counter(int capacity) {

        this.tableSize = nextPrimer((int) (capacity / LOAD_FACTOR));
        this.hashCodes = new int[tableSize];
        this.elements = new Object[tableSize];
        this.counters = new int[tableSize];
        this.capacity = capacity;
        this.size = 0;
    }

    static int nextPrimer(int i) {
        if (i < 1 || i > MAX) {
            throw new IllegalArgumentException("" + i);
        }

        int pos = Arrays.binarySearch(PRIMERS, i);
        if (pos < 0) {
            pos = -pos - 1;
        }

        return PRIMERS[pos];
    }

    private int indexOf(E element, int hashCode) {

        //Quadratic Probing

        // the first hash function
        int currentPosition = hashCode % tableSize;

        // the second hash function
        int step = 1 + (hashCode % (tableSize - 2));

        while (elements[currentPosition] != null && (hashCodes[currentPosition] != hashCode || !element.equals(
            elements[currentPosition]))) {
            currentPosition -= step;
            if (currentPosition < 0) {
                currentPosition += tableSize;
            }
        }

        if (elements[currentPosition] == null) {
            return -currentPosition - 1;
        }

        return currentPosition;
    }

    @Override
    public boolean incIfPresent(E element) {
        int hashCode = element.hashCode();
        int pos = indexOf(element, hashCode);

        if (pos < 0) {
            return false;
        } else {
            counters[pos]++;
            return true;
        }


    }

    @Override
    public boolean isFull() {
        return size == capacity;
    }

    @Override
    public boolean addIfAbsentAndNotFull(E element) {
        if (isFull()) {
            return false;
        } else {
            int hashCode = element.hashCode();
            int pos = indexOf(element, hashCode);
            if (pos >= 0) {
                return false;
            } else {
                pos = -pos - 1;
                hashCodes[pos] = hashCode;
                elements[pos] = element;
                counters[pos]++;
                size++;
                return true;
            }
        }
    }

    @Override
    public void clear() {

        Arrays.fill(hashCodes, 0);

        Arrays.fill(elements, null);

        Arrays.fill(counters, 0);

        size = 0;
    }

    @Override
    public List<Entry<E>> getElements() {
        List<Entry<E>> result = new ArrayList<>(size);

        int sum = 0;

        for (int i = 0; i < tableSize; i++) {
            if (elements[i] != null) {
                result.add(new Entry<E>((E) elements[i], counters[i]));
                if (++sum == size) {
                    break;
                }
            }
        }

        return result;
    }


}
