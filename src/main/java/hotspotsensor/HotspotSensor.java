package hotspotsensor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

/**
 * {@link HotspotSensor} is a high-performance component to detect hot spot increase in concurrent
 * circumstance.
 *
 * @author iamlotus@gmail.com
 */
public class HotspotSensor<T> {

    private static final Logger LOG = LoggerFactory.getLogger(HotspotSensor.class);
    private L1LRU.Factory<T> l1Factory;
    private L2Counter.Factory<T> l2Factory;
    private int hotThreshold;
    private ServerWindow[] windows;
    private int firstWindowNo;
    private long firstWindowTimeId;
    private List<NotificationHandler<T>> notificationHandlers;
    private ServerCounter<T> serverCounter;
    private Set<T> hotElementSet;
    private Set<? extends Entry<T>> hotEntrySet;
    private BlockingQueue<ClientMessage<T>> channel;
    private Thread consumer;
    private Watch watch;
    private ThreadLocal<Collector<T>> clients;

    HotspotSensor(int channelSize, int windowsNumber, L1LRU.Factory<T> l1Factory, L2Counter.Factory<T> l2Factory,
        int hotThreshold, List<NotificationHandler<T>> notificationHandlers, Watch watch) {
        if (channelSize <= 0) {
            throw new IllegalArgumentException("channelSize:" + channelSize);
        }

        if (l1Factory == null) {
            throw new NullPointerException("l1Factory");
        }

        if (l2Factory == null) {
            throw new NullPointerException("l2Factory");
        }

        if (windowsNumber <= 0) {
            throw new IllegalArgumentException("windowsNumber:" + windowsNumber);
        }

        if (hotThreshold <= 0) {
            throw new IllegalArgumentException("hotThreshold:" + hotThreshold);
        }


        if (watch == null) {
            throw new NullPointerException("watch");
        }



        this.channel = new ArrayBlockingQueue<>(channelSize);
        this.l1Factory = l1Factory;
        this.l2Factory = l2Factory;

        this.hotThreshold = hotThreshold;
        this.watch = watch;

        windows = new ServerWindow[windowsNumber];
        for (int i = 0; i < windowsNumber; i++) {
            windows[i] = new ServerWindow();
        }


        firstWindowNo = 0;
        firstWindowTimeId = watch.currentTimeId();
        serverCounter = new ServerCounter<>();

        this.notificationHandlers = notificationHandlers;

        clients = new ThreadLocal<>();

        consumer = new Thread(new ConsumeTask(), "hotspot-sensor-consumer-thread");
        consumer.setDaemon(true);
        consumer.start();
    }

    public static <T> HostspotSensorBuilder<T> builder() {
        return new HostspotSensorBuilder<>();
    }

    // for test purpose
    protected Watch getWatch() {
        return watch;
    }

    boolean submit(Collector.CollectorId collectorId, long timeId, List<Entry<T>> l2Counter, long totalCount) {
        ClientMessage<T> clientMessage = new ClientMessage(collectorId, timeId, l2Counter, totalCount);
        if (!channel.add(clientMessage)) {
            LOG.warn("hot detect, server channel overflow, discard client {}, timeId {}", collectorId, timeId);
            return false;
        }
        return true;
    }

    private ServerWindow<T> getWindow(int offset) {
        if (offset < 0 || offset > windows.length) {
            throw new IllegalArgumentException();
        }
        int i = getIndex(offset);
        return windows[i];
    }

    private int getIndex(int offset) {
        return (firstWindowNo + offset) % windows.length;
    }

    /**
     * Access element once, {@link HotspotSensor} increase count of element and if the total count exceeds
     * hotThresdhold, the element may be included in the following invocations of
     * {@link NotificationHandler#handleNotification(NotificationHandler.Notification)}
     *
     * @param element element to increase
     */
    public void increase(T element) {
        Collector collector = clients.get();
        if (collector == null) {
            collector = new Collector<>(this, this.watch, l1Factory.create(), l2Factory.create());
            clients.set(collector);
        }
        collector.access(element);
    }



    static class ServerCounter<E> {

        private Map<E, MutableInt> map;

        public ServerCounter() {
            this.map = new HashMap<>();
        }

        public void merge(E element, int count) {
            MutableInt i = map.get(element);
            if (i == null) {
                i = new MutableInt(count);
                map.put(element, i);
            } else {
                i.add(count);
            }


        }

        public List<Entry<E>> getElements() {
            return map.entrySet()
                      .stream()
                      .map(entry -> new Entry<>(entry.getKey(), entry.getValue().value()))
                      .collect(Collectors.toList());
        }

        public Set<? extends Entry<E>> filterGreaterThanOrEqualsTo(int threshold) {
            Set<Entry<E>> result = new HashSet<>(map.size());
            map.forEach((e, i) -> {
                if (i.value() >= threshold) {
                    result.add(new Entry<E>(e, i.value()));
                }
            });
            return result;
        }

        public void clear() {
            map.clear();
        }
    }


    /**
     * HotspotSensor maintains N sequential StatWindows: w(0) to w(N-1). w(0) is closed
     * when the first ClientMessage of T(N) is met, and w(N) will be created as well.
     * <p>
     */
    static class ServerWindow<E> {

        private Set<Collector.CollectorId> collectorIds;

        private ServerCounter<E> serverCounter;

        private long totalCount;

        public ServerWindow() {
            collectorIds = new HashSet<>(256);
            serverCounter = new ServerCounter<>();
            totalCount = 0;
        }

        public boolean merge(Collector.CollectorId collectorId, long timeId, List<Entry<E>> clientCounter,
            long totalCount) {
            if (collectorIds.contains(collectorId)) {
                // one client should not submit twice;
                LOG.warn("client {} submits duplicated packet on timeId {}", collectorId, timeId);
                return false;
            } else {
                collectorIds.add(collectorId);

                for (Entry<E> entry : clientCounter) {
                    // plus the one from L1Counter
                    int count = entry.getCount() + 1;
                    serverCounter.merge(entry.getElement(), count);
                }

                this.totalCount += totalCount;
                return true;
            }
        }

        public ServerCounter<E> getServerCounter() {
            return serverCounter;
        }

        public long getTotalCount() {
            return totalCount;
        }

        /**
         * Clear for reuse;
         */
        public void clear() {
            totalCount = 0;
            collectorIds.clear();
            serverCounter.clear();
        }

    }


    private static class ClientMessage<T> {
        private Collector.CollectorId collectorId;

        private long timeId;

        private List<Entry<T>> l2Counter;

        private long totalCount;

        public ClientMessage(Collector.CollectorId collectorId, long timeId, List<Entry<T>> l2Counter,
            long totalCount) {
            this.collectorId = collectorId;
            this.timeId = timeId;
            this.l2Counter = l2Counter;
            this.totalCount = totalCount;
        }

        public Collector.CollectorId getCollectorId() {
            return collectorId;
        }

        public long getTimeId() {
            return timeId;
        }

        public List<Entry<T>> getL2Counter() {
            return l2Counter;
        }

        public long getTotalCount() {
            return totalCount;
        }
    }


    static class NotificationImpl<T> implements NotificationHandler.Notification<T> {

        private boolean elementSetChanged, entrySetChanged;

        private Set<? extends T> elementSet;

        private Set<Entry<T>> entrySet;

        private long totalCount;

        public NotificationImpl(boolean elementSetChanged, Set<? extends T> elementSet, boolean entrySetChanged,
            Set<Entry<T>> entrySet, long totalCount) {
            this.elementSetChanged = elementSetChanged;
            this.entrySetChanged = entrySetChanged;
            this.elementSet = elementSet;
            this.entrySet = entrySet;
            this.totalCount = totalCount;
        }

        @Override
        public boolean isElementSetChanged() {
            return elementSetChanged;
        }

        @Override
        public boolean isEntrySetChanged() {
            return entrySetChanged;
        }

        @Override
        public Set<? extends T> getElementSet() {
            return elementSet;
        }

        @Override
        public Set<Entry<T>> getEntrySet() {
            return entrySet;
        }

        @Override
        public long getTotalCount() {
            return totalCount;
        }


        @Override
        public String toString() {
            return "[" +
                "elementSetChanged=" + elementSetChanged +
                ", entrySetChanged=" + entrySetChanged +
                ", elementSet=" + elementSet +
                ", entrySet=" + entrySet +
                ", getTotalCount=" + totalCount +
                ']';
        }
    }


    // the only consumer thread pull form channel and do hot detection
    private class ConsumeTask implements Runnable {

        @Override
        public void run() {
            while (true) {

                try {
                    ClientMessage<T> packet = channel.take();
                    long timeId = packet.getTimeId();
                    Collector.CollectorId collectorId = packet.getCollectorId();

                    int offset = (int) (timeId - firstWindowTimeId);

                    if (offset < 0) {
                        // out-of-date package, discard
                        LOG.debug("discard out-of-date package of timeId {}, oldest timeId is {} ", timeId,
                            firstWindowTimeId);
                    } else if (offset < windows.length) {
                        // package of existed window, merge
                        ServerWindow<T> currentWindow = getWindow(offset);
                        currentWindow.merge(collectorId, timeId, packet.getL2Counter(), packet.getTotalCount());
                    } else {
                        // package of new timeId
                        calculateHotElementsAndNotify(offset);

                        //evict obsolete windows, initialize new windows
                        int interval = Math.min(windows.length, offset);

                        for (int i = 0; i < interval; i++) {
                            ServerWindow window = getWindow(i);
                            window.clear();
                        }

                        firstWindowNo = getIndex(interval);
                        firstWindowTimeId = timeId;


                    }


                } catch (Throwable t) {
                    LOG.error("meet error in server consumer task", t);

                }
            }

        }

        private void calculateHotElementsAndNotify(int offset) {

            Set<T> newHotElementSet;
            Set<Entry<T>> newHotEntrySet;
            long totalCount = 0;

            if (offset >= windows.length * 2) {
                // It has been too long since last packet arrived, all windows are invalid now

                newHotEntrySet = Collections.emptySet();
            } else {
                serverCounter.clear();
                //TODO: calculate hot element by weight of interval
                int length = 2 * windows.length - offset;


                // from old to young
                for (int i = 0; i < length; i++) {
                    int index = getIndex(i);
                    ServerWindow<T> window = windows[index];
                    totalCount += window.getTotalCount();

                    for (Entry<T> entry : window.getServerCounter().getElements()) {
                        serverCounter.merge(entry.getElement(), entry.getCount());
                    }
                }

                newHotEntrySet = Collections.unmodifiableSet(serverCounter.filterGreaterThanOrEqualsTo(hotThreshold));


            }


            newHotElementSet = newHotEntrySet.stream().map(Entry::getElement).collect(Collectors.toSet());
            boolean isHotElementSetChanged = !newHotElementSet.equals(hotElementSet);
            boolean isHotEntrySetChanged = !newHotEntrySet.equals(hotEntrySet);
            NotificationHandler.Notification<T> notification =
                new NotificationImpl<>(isHotElementSetChanged, newHotElementSet, isHotEntrySetChanged, newHotEntrySet,
                    totalCount);

            if (isHotElementSetChanged) {
                LOG.debug("hot elements set changed at time {}: {} ", firstWindowTimeId, hotEntrySet);
            }

            if (notificationHandlers != null) {
                notificationHandlers.forEach(handler -> handler.handleNotification(notification));
            }

            hotEntrySet = newHotEntrySet;
            hotElementSet = newHotElementSet;

        }
    }


}
