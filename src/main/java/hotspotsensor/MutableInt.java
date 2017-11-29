package hotspotsensor;

/**
 * @author lotus.jzx
 */
class MutableInt {
    public int value;

    public MutableInt(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public void inc() {
        value++;
    }

    public int add(int delta) {
        value += delta;
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MutableInt)) {
            return false;
        }

        MutableInt that = (MutableInt) o;

        return value == that.value;

    }

    @Override
    public int hashCode() {
        return value;
    }
}
