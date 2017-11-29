package hotspotsensor;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Update time every milli second.
 *
 * @author lotus.jzx
 */
public class Watch {
    private static final AtomicInteger INSTANCE_ID = new AtomicInteger();
    private final long startTimeMillis;
    private int instanceId;

    private int windowSizeMills;

    private volatile long currentTimeMillis;

    public Watch(int windowSizeMills) {
        if (windowSizeMills <= 0) {
            throw new IllegalArgumentException("windowsSizeMills: " + windowSizeMills);
        }

        this.instanceId = INSTANCE_ID.getAndIncrement();
        this.windowSizeMills = windowSizeMills;
        this.startTimeMillis = System.currentTimeMillis();

        Thread setTickThread = new Thread() {
            public void run() {
                while (true) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        //unreachable
                        e.printStackTrace();
                    }
                    currentTimeMillis = System.currentTimeMillis();
                }
            }
        };

        setTickThread.setDaemon(true);
        setTickThread.setName("hot-detect-time-ticket-thread-" + instanceId);
        setTickThread.start();

        while (setTickThread.getState() != Thread.State.RUNNABLE) {
            //wait until thread has been ready, so it update currentTimeMillis ASAP
        }

        currentTimeMillis = System.currentTimeMillis();

    }

    /**
     * get Current Time ID
     *
     * @return TimeId, 0-based
     */
    public TimeId currentTimeId() {
        return new TimeId((currentTimeMillis - startTimeMillis) / windowSizeMills);
    }


    public static class TimeId {

        private long value;

        TimeId(long value) {
            if (value < 0) {
                throw new IllegalArgumentException("value must be non-positive");
            }
            this.value = value;
        }

        public TimeId next() {
            return new TimeId(value + 1);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TimeId)) {
                return false;
            }

            TimeId that = (TimeId) o;

            return value == that.value;

        }


        public long value() {
            return this.value;
        }

        public long minus(TimeId another) {
            return value - another.value;
        }


        public boolean after(TimeId another) {
            return value > another.value;
        }

        public boolean before(TimeId another) {
            return value < another.value;
        }


        @Override
        public int hashCode() {
            int result = (int) (value ^ (value >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "T[" + value +
                "]";
        }

    }


}
