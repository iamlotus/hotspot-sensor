package hotspotsensor;

import hotspotsensor.Collector.CollectorId;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author iamlotus@gmail.com
 */
public class CollectorTest {

    @Test
    public void testClientId() {
        CollectorId id1 = CollectorId.next();
        CollectorId id2 = CollectorId.next();
        Assert.assertTrue(id1.value() >= 0);
        Assert.assertTrue(id2.value() >= 0);
        Assert.assertTrue(id2.value() == id1.value() + 1);
    }

    @Test
    public void testAccess() throws InterruptedException {

        int windowSizeMills = 50;

        HotspotSensor<String> hotspotSensor = createMock(HotspotSensor.class);

        Watch watch = new Watch(windowSizeMills);
        int l1Capacity = 2;
        int l2Capacity = 2;
        Collector<String> collector =
            new Collector<>(hotspotSensor, watch, new SimpleL1LRU<>(l1Capacity), new SimpleL2Counter<>(l2Capacity));
        CollectorId collectorId = collector.getId();
        Watch.TimeId startTime = watch.currentTimeId();


        expect(hotspotSensor.submit(
            collectorId, startTime, TestUtils.list(TestUtils.e("a", 3), TestUtils.e("c", 1)), 10)).andReturn(true);
        replay(hotspotSensor);
        // a & c are promoted to L2, b is discard and d is overflow
        // getTotalCount=10(9+1)
        collector.access("a");
        collector.access("b");
        collector.access("a");
        collector.access("a");
        collector.access("c");
        collector.access("a");
        collector.access("d");
        collector.access("c");
        collector.access("d");

        TimeUnit.MILLISECONDS.sleep(windowSizeMills * 2);
        //trigger submit
        collector.access("x");
        verify(hotspotSensor);
    }

} 
