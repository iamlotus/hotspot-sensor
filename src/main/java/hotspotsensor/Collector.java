package hotspotsensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link Collector} works in fixed thread ({@code ThreadLocal}), which is responsible for collects hot(frequent)
 * requests and report them to {@link HotspotSensor} periodically.
 * Since there is no lock contention, {@code Collector} use lock-less data structures to get very high performance.
 * To make a trade-off between limited memory footprint and undeterminated element number ,{@code
 * Collector} combines a Level-1 LRU and a Level-2 Counter to record hotspot in current window.
 * <p>
 * An element is prompted to L2Counter if it is lucky enough to be accessed twice before
 * discarded in L1LRU, and it will never be discard after entering L2Counter. But if it is discarded from L1LRU,
 * it needs another two accesses to be prompted to L2Counter(e.g. the previous increase is forgotten at all because
 * of LRU).
 * After L2Counter is full, no new element will be recorded, so L2Counter should has an proper capacity.
 *
 * @author iamlotus@gmail.com
 */
class Collector<E> {

    private static final Logger LOG = LoggerFactory.getLogger(Collector.class);

    // report to hotspotSensor periodically.
    private HotspotSensor hotspotSensor;

    // ID
    private CollectorId id;

    private Watch.TimeId currentTime;
    private Watch watch;
    private L1LRU<E> l1LRU;
    private L2Counter<E> l2Counter;

    // increase count in the window(currentTime)
    private long totalCount;

    Collector(HotspotSensor hotspotSensor, Watch watch, L1LRU<E> l1LRU, L2Counter<E> l2Counter) {
        this.id = CollectorId.next();
        this.hotspotSensor = hotspotSensor;
        this.watch = watch;
        this.l1LRU = l1LRU;
        this.l2Counter = l2Counter;

        this.currentTime = watch.currentTimeId();
        this.totalCount = 0;
    }

    public void access(E element) {

        put(element);

        totalCount++;

        Watch.TimeId newTimeId = watch.currentTimeId();
        if (newTimeId.after(currentTime)) {
            // currentTime is elapsed, submit
            hotspotSensor.submit(id, currentTime, l2Counter.getElements(), totalCount);

            // reset
            currentTime = newTimeId;
            l1LRU.clear();
            totalCount = 0;
            l2Counter.clear();
        }

    }


    private void put(E element) {
        if (!l2Counter.incIfPresent(element)) {
            E twice = l1LRU.put(element);
            if (twice != null) {
                if (!l2Counter.addIfAbsentAndNotFull(twice)) {
                    LOG.debug("client {} discard element {} because of overflow", getId(), element);
                }
            }
        }
    }

    public CollectorId getId() {
        return this.id;
    }


    public static class CollectorId {
        private static AtomicLong sequence = new AtomicLong(0);

        private long value;

        public static CollectorId next() {
            return new CollectorId();
        }


        private CollectorId() {
            this.value = sequence.getAndIncrement();
        }

        public long value() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CollectorId)) {
                return false;
            }

            CollectorId that = (CollectorId) o;

            return value == that.value;

        }

        @Override
        public int hashCode() {
            return (int) (value ^ (value >>> 32));
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }



}
