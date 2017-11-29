package hotspotsensor.benchmark;

import hotspotsensor.HotspotSensor;
import hotspotsensor.MBeanNotificationHandler;
import hotspotsensor.NotificationHandler;
import hotspotsensor.TestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lotus.jzx
 */
public class BenchmarkMain {

    public static void main(String[] args) throws InterruptedException {
        // 50 threads do increase operation
        int threadNum = 50;

        //HotspotSensor detects hot element in past windowsNumber * windowSizeMills milliseconds, the default value
        // is 1 second (20 windows * 50 ms/window)
        int windowsNum = 20;
        int windowSizeMills = 50;

        //Set the threshold which is used to filter hot element. The threshold works on global scope, HotspotSensor
        // sum requests from all Collectors, then report elements who are accessed more then hotThreshold as HOT
        // elements, default value is 100.
        int hotThreshold = 3;

        //Set size of channel between Collectors and HotspotSensor, this must be bigger enough or some collector
        // thread will be blocked, the default value is 4096
        int channelSize = 512;

        //Set capacity of L1LRU of Collector, default value is 200
        int l1Capacity = 100;

        //Set capacity of L2Counter of Collector, default value is 200
        int l2Capacity = 3;

        AtomicLong sumQps = new AtomicLong(0);
        AtomicInteger times = new AtomicInteger(0);


        int durationMillis = windowSizeMills * windowsNum;

        // this handler print QPS and count of hot elements in the notification
        NotificationHandler logHandler = notification -> {
            long qps = notification.getTotalCount() / (durationMillis / 1000);
            sumQps.addAndGet(qps);
            times.incrementAndGet();
            System.out.println("QPS=" + qps + ", count(hot elements) = " + notification.getElementSet().size());
        };

        System.out.println(
            "Threads=" + threadNum + ", " + windowsNum + " windows, size=" + windowsNum + " ms , threads ="
                + hotThreshold + ", l1 capacity=" + l1Capacity + ", l2 capacity=" + l2Capacity);



        HotspotSensor<String> sensor = HotspotSensor.<String>builder().setWindowsNumber(windowsNum)
                                                                      .setWindowSizeMills(windowSizeMills)
                                                                      .setHotThreshold(hotThreshold)
                                                                      .setChannelSize(channelSize)
                                                                      .setL1Capacity(l1Capacity)
                                                                      .setL2Capacity(l2Capacity)
                                                                      // view notification detail by
                                                                      // jvisualvm or jconsole
                                                                      .addNotificationHandler(
                                                                          new MBeanNotificationHandler<>())
                                                                      // log QPS and hot
                                                                      .addNotificationHandler(logHandler)
                                                                      .build();



        List<EmitTask> tasks = new ArrayList<>();
        ExecutorService es = Executors.newFixedThreadPool(threadNum);

        for (int i = 0; i < threadNum / 2; i++) {
            EmitTask task1 =
                new EmitTask(sensor, new RandomHotEmitter(TestUtils.map("AAAA", 0.001d, "BBBB", 0.001d)), -1, -1);
            EmitTask task2 =
                new EmitTask(sensor, new RandomHotEmitter(TestUtils.map("AAAA", 0.002d, "CCCC", 0.0001d)), -1, -1);
            tasks.add(task1);
            tasks.add(task2);
            es.submit(task1);
            es.submit(task2);
        }


        TimeUnit.MINUTES.sleep(1);
        System.out.println("stopping ...");
        tasks.forEach(t -> t.stop());
        es.shutdownNow();
        es.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("stop, avg QPS=" + sumQps.get() / times.get());
    }
}
