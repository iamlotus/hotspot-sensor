package hotspotsensor;

import hotspotsensor.Watch.TimeId;
import junit.framework.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author iamlotus@gmail.com
 */
public class WatchTest {

    @Test
    public void testWindowId() {

        int windowSizeMillis = 50;

        Watch watch = new Watch(windowSizeMillis);

        TimeId window1 = watch.currentTimeId();

        Assert.assertTrue(window1.value() == 0);

        try {
            TimeUnit.MILLISECONDS.sleep(windowSizeMillis * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        TimeId window2 = watch.currentTimeId();
        long delta = window2.minus(window1);
        Assert.assertTrue(delta == 2);
        Assert.assertTrue(window1.before(window2));
        Assert.assertTrue(window2.after(window1));

    }



}
