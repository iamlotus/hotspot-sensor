package hotspotsensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Config params and build {@link HotspotSensor}
 *
 * @author iamlotus@gmail.com
 */
public class HostspotSensorBuilder<E> {

    private static final int DEFAULT_CHANNEL_SIZE = 4096;

    private static final int DEFAULT_WINDOWS_NUMBER = 20;

    private static final int DEFAULT_WINDOWS_SIZE_MILLS = 50;

    private static final int DEFAULT_L1_CAPACITY = 200;

    private static final int DEFAULT_L2_CAPACITY = 200;

    private static final int DEFAULT_HOT_THRESHOLD = 100;

    private int channelSize = DEFAULT_CHANNEL_SIZE;

    private int windowsNumber = DEFAULT_WINDOWS_NUMBER;

    private int windowSizeMills = DEFAULT_WINDOWS_SIZE_MILLS;

    private int l1Capacity = DEFAULT_L1_CAPACITY;

    private int l2Capacity = DEFAULT_L2_CAPACITY;

    private int hotThreshold = DEFAULT_HOT_THRESHOLD;

    private List<NotificationHandler<E>> notificationHandlers = new ArrayList<>();

    HostspotSensorBuilder() {

    }

    /**
     * Set size of channel between {@code Collector}s and {@code HotspotSensor}, this must be bigger enough or
     * some collector thread will be blocked, the default value is 4096
     *
     * @param channelSize
     * @return this
     */
    public HostspotSensorBuilder<E> setChannelSize(int channelSize) {
        if (channelSize <= 0) {
            throw new IllegalArgumentException("channelSize");
        }
        this.channelSize = channelSize;
        return this;
    }

    /**
     * Set number of windows for counting, {@code HotspotSensor} detects hot element
     * in past {@code windowsNumber} * {@code windowSizeMills} milliseconds, the default
     * value is 1 second (20 windows * 50 ms/window)
     *
     * @param windowsNumber
     * @return this
     */
    public HostspotSensorBuilder<E> setWindowsNumber(int windowsNumber) {
        if (windowsNumber <= 0) {
            throw new IllegalArgumentException("windowsNumber");
        }
        this.windowsNumber = windowsNumber;
        return this;
    }

    /**
     * Set window size (in milliseconds) for counting, {@code HotspotSensor} detects hot element
     * in past {@code windowsNumber} * {@code windowSizeMills} milliseconds, the default
     * value is 1 second (20 windows * 50 ms/window)
     *
     * @param windowSizeMills
     * @return this
     */
    public HostspotSensorBuilder<E> setWindowSizeMills(int windowSizeMills) {
        if (windowSizeMills <= 0) {
            throw new IllegalArgumentException("windowSizeMills");
        }
        this.windowSizeMills = windowSizeMills;
        return this;
    }

    /**
     * Set capacity of {@link L1LRU} of {@link Collector}, default value is 200.
     *
     * @param l1Capacity
     * @return this
     */
    public HostspotSensorBuilder<E> setL1Capacity(int l1Capacity) {
        if (l1Capacity <= 0) {
            throw new IllegalArgumentException("l1Capacity");
        }
        this.l1Capacity = l1Capacity;
        return this;
    }

    /**
     * Set capacity of {@link L2Counter} of {@link Collector}, default value is 200.
     *
     * @param l2Capacity
     * @return this
     */
    public HostspotSensorBuilder<E> setL2Capacity(int l2Capacity) {
        if (l2Capacity <= 0) {
            throw new IllegalArgumentException("l2Capacity");
        }
        this.l2Capacity = l2Capacity;
        return this;
    }

    /**
     * Set the threshold which is used to filter hot element. The threshold works on global scope,
     * {@link HotspotSensor} sum requests from all {@link Collector}s, then report elements who are
     * accessed more then {@code hotThreshold} as <b>HOT</b> elements, default value is 100.
     *
     * @param hotThreshold
     * @return this
     */
    public HostspotSensorBuilder<E> setHotThreshold(int hotThreshold) {
        if (hotThreshold <= 0) {
            throw new IllegalArgumentException("hotThreshold");
        }
        this.hotThreshold = hotThreshold;
        return this;
    }

    /**
     * Set the action which handle notification on hot element detected, default action do nothing.
     *
     * @param notificationHandler
     * @return this
     */
    public HostspotSensorBuilder<E> addNotificationHandler(NotificationHandler<E> notificationHandler) {
        if (notificationHandler == null) {
            throw new NullPointerException("notificationHandler");
        }
        this.notificationHandlers.add(notificationHandler);
        return this;
    }

    /**
     * Build a
     * {@link HotspotSensor} by given parameters, the follow modification on this {@code HotSpotSensorBuilder} will
     * not impact result anymore.
     *
     * @return HotspotSensor
     */
    public HotspotSensor<E> build() {
        Watch watch = new Watch(windowSizeMills);

        HotspotSensor<E> result =
            new HotspotSensor<>(channelSize, windowsNumber, new L1Factory<>(l1Capacity), new L2Factory<>(l2Capacity),
                hotThreshold, notificationHandlers, watch);

        return result;
    }


    private static class L1Factory<E> implements L1LRU.Factory<E> {

        private int capacity;

        public L1Factory(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public L1LRU<E> create() {
            return new SimpleL1LRU<>(capacity);
        }


    }


    private static class L2Factory<E> implements L2Counter.Factory<E> {

        private int capacity;

        public L2Factory(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public L2Counter<E> create() {
            //            return new OpenAddressingL2Counter<>(capacity);
            // SimpleL2Counter is always better than OpenAddressingL2Counter according to benchmark test （ ° △ °|||）
            return new SimpleL2Counter<>(capacity);

        }
    }
}
