package hotspotsensor;

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

        long window1 = watch.currentTimeId();

        Assert.assertTrue(window1 == 0);

        try {
            TimeUnit.MILLISECONDS.sleep(windowSizeMillis * 2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long window2 = watch.currentTimeId();
        long delta = window2 - window1;
        Assert.assertTrue(delta == 2);
        Assert.assertTrue(window1 < window2);


    }



}
