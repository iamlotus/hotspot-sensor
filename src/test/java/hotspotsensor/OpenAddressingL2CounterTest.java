package hotspotsensor;

import org.junit.Assert;
import org.junit.Test;

import static hotspotsensor.OpenAddressingL2Counter.nextPrimer;
import static hotspotsensor.TestUtils.e;
import static hotspotsensor.TestUtils.set;

/**
 * @author iamlotus@gmail.com
 */
public class OpenAddressingL2CounterTest {

    @Test
    public void testNextPrimer() {
        Assert.assertEquals(2, nextPrimer(1));
        Assert.assertEquals(2, nextPrimer(2));
        Assert.assertEquals(137, nextPrimer(136));
        Assert.assertEquals(137, nextPrimer(137));
        Assert.assertEquals(199, nextPrimer(199));
        Assert.assertEquals(211, nextPrimer(200));
    }

    @Test
    public void testAdd() {
        L2Counter<String> c = new OpenAddressingL2Counter<>(2);
        Assert.assertTrue(c.addIfAbsentAndNotFull("a"));

        Assert.assertEquals(set(e("a", 1)), set(c.getElements()));

        //present
        Assert.assertFalse(c.addIfAbsentAndNotFull("a"));
        Assert.assertTrue(c.addIfAbsentAndNotFull("b"));
        Assert.assertEquals(set(e("a", 1), e("b", 1)), set(c.getElements()));

        // present
        Assert.assertFalse(c.addIfAbsentAndNotFull("b"));
        Assert.assertEquals(set(e("a", 1), e("b", 1)), set(c.getElements()));

        Assert.assertTrue(c.incIfPresent("b"));
        Assert.assertEquals(set(e("a", 1), e("b", 2)), set(c.getElements()));

        //full
        Assert.assertFalse(c.addIfAbsentAndNotFull("c"));

        Assert.assertTrue(c.incIfPresent("a"));
        Assert.assertTrue(c.incIfPresent("a"));
        Assert.assertEquals(set(e("a", 3), e("b", 2)), set(c.getElements()));

        Assert.assertFalse(c.incIfPresent("c"));

        c.clear();
        Assert.assertEquals(set(), set(c.getElements()));
        Assert.assertTrue(c.addIfAbsentAndNotFull("c"));
        Assert.assertEquals(set(e("c", 1)), set(c.getElements()));
    }

} 
