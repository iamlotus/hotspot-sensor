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
    public long currentTimeId() {
        return (currentTimeMillis - startTimeMillis) / windowSizeMills;
    }


}
