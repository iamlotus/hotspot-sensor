package hotspotsensor;

import org.junit.Test;
import org.testng.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static hotspotsensor.TestUtils.list;
import static hotspotsensor.TestUtils.set;

/**
 * @author iamlotus@gmail.com
 */
public class HotspotSensorTest {



    @Test
    public void testConcurrentSubmit() throws InterruptedException {


        int windowsNum = 3;
        int hotThreshold = 5;
        int windowSizeMills = 10;

        final AtomicReference<NotificationHandler.Notification<String>> notificationAtomicReference =
            new AtomicReference<>();
        HotspotSensor<String> d = HotspotSensor.<String>builder().addNotificationHandler(notification -> {
            notificationAtomicReference.set(notification);

        }).setWindowsNumber(windowsNum).setWindowSizeMills(windowSizeMills).setHotThreshold(hotThreshold).build();

        ExecutorService es = Executors.newFixedThreadPool(2);

        long startTime = d.getWatch().currentTimeId();
        es.submit(() -> {
            // a=2(L2)+1(L1) b=1(L2)+1(L1)
            d.submit(Collector.CollectorId.next(), startTime, TestUtils.list(TestUtils.e("a", 2), TestUtils.e("b", 1)),
                3);

            try {
                TimeUnit.MILLISECONDS.sleep(windowSizeMills);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // a=1(L2)+1(L1) c=2(L2)+1(L1)
            d.submit(Collector.CollectorId.next(), startTime + 1,
                TestUtils.list(TestUtils.e("a", 1), TestUtils.e("c", 2)), 3);
        });

        es.submit(() -> {
            // a=1(L2)+1(L1)
            d.submit(Collector.CollectorId.next(), startTime, list(TestUtils.e("a", 1)), 1);

            try {
                TimeUnit.MILLISECONDS.sleep(windowSizeMills);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // b=1(L2)+1(L1) c=1(L2)+1(L1)
            d.submit(Collector.CollectorId.next(), startTime + 1,
                TestUtils.list(TestUtils.e("b", 1), TestUtils.e("c", 1)), 1);

        });

        // trigger calculate
        // sum(a)= 5, sum(b)=2
        long newEpisode = startTime + windowsNum;

        try {
            TimeUnit.MILLISECONDS.sleep(windowSizeMills * windowsNum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //trigger calculate
        // sum(a)=7,sum(b)=4 sum(c)=5
        d.submit(Collector.CollectorId.next(), newEpisode, TestUtils.list(), 1);
        //make sure consume is done
        TimeUnit.MILLISECONDS.sleep(50);


        NotificationHandler.Notification<String> result = notificationAtomicReference.get();
        Assert.assertEquals(true, result.isElementSetChanged());
        Assert.assertEquals(TestUtils.set("a", "c"), result.getElementSet());
        Assert.assertEquals(true, result.isEntrySetChanged());
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 7), TestUtils.e("c", 5)), result.getEntrySet());
        Assert.assertEquals(8, result.getTotalCount());



    }

    @Test
    public void testSubmit() throws InterruptedException {
        int windowsNum = 3;
        int hotThreshold = 5;

        final AtomicReference<NotificationHandler.Notification<String>> notificationAtomicReference =
            new AtomicReference<>();
        HotspotSensor<String> d = HotspotSensor.<String>builder().addNotificationHandler(notification -> {
            notificationAtomicReference.set(notification);

        }).setWindowsNumber(3).setHotThreshold(hotThreshold).build();

        Collector.CollectorId id = Collector.CollectorId.next();
        Watch watch = d.getWatch();
        long now = watch.currentTimeId();
        // a=2(L2)+1(L1) b=1(L2)+1(L1)
        d.submit(id, now, TestUtils.list(TestUtils.e("a", 2), TestUtils.e("b", 1)), 10);

        // a=1(L2)+1(L1)
        long nextTime = now + 1;
        d.submit(id, nextTime, list(TestUtils.e("a", 1)), 20);

        // trigger calculate
        // sum(a)= 5, sum(b)=2
        long newEpisode = now + windowsNum;

        d.submit(id, newEpisode, TestUtils.list(), 10);

        //make sure consume is done
        TimeUnit.MILLISECONDS.sleep(50);


        NotificationHandler.Notification<String> result = notificationAtomicReference.get();
        Assert.assertEquals(true, result.isElementSetChanged());
        Assert.assertEquals(TestUtils.set("a"), result.getElementSet());
        Assert.assertEquals(true, result.isEntrySetChanged());
        Assert.assertEquals(set(TestUtils.e("a", 5)), result.getEntrySet());
        Assert.assertEquals(30, result.getTotalCount());



    }

    @Test
    public void testServerCounter() {
        HotspotSensor.ServerCounter<String> serverCounter = new HotspotSensor.ServerCounter<>();
        serverCounter.merge("a", 2);
        serverCounter.merge("b", 3);
        serverCounter.merge("a", 2);
        Assert.assertEquals(TestUtils.set(), serverCounter.filterGreaterThanOrEqualsTo(5));
        Assert.assertEquals(set(TestUtils.e("a", 4)), serverCounter.filterGreaterThanOrEqualsTo(4));
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 4), TestUtils.e("b", 3)),
            serverCounter.filterGreaterThanOrEqualsTo(3));
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 4), TestUtils.e("b", 3)),
            serverCounter.filterGreaterThanOrEqualsTo(0));
    }

    @Test
    public void testServerWindow() {

        HotspotSensor.ServerWindow serverWindow = new HotspotSensor.ServerWindow();
        Collector.CollectorId collectorId1 = Collector.CollectorId.next();
        Collector.CollectorId collectorId2 = Collector.CollectorId.next();
        Watch w = new Watch(10);
        long timeId1 = w.currentTimeId();


        Assert.assertTrue(serverWindow.merge(collectorId1, timeId1, list(TestUtils.e("a", 1)), 10));

        Assert.assertFalse(serverWindow.merge(collectorId1, timeId1, list(TestUtils.e("b", 1)), 20));

        Assert.assertTrue(serverWindow.merge(collectorId2, timeId1, list(TestUtils.e("c", 1)), 30));

        Assert.assertEquals(40, serverWindow.getTotalCount());
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 2), TestUtils.e("c", 2)),
            serverWindow.getServerCounter().filterGreaterThanOrEqualsTo(1));


    }



}
