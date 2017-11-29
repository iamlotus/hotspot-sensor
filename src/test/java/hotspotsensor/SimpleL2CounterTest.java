package hotspotsensor;

import org.junit.Assert;
import org.junit.Test;

import static hotspotsensor.TestUtils.set;


/**
 * @author iamlotus@gmail.com
 */
public class SimpleL2CounterTest {

    @Test
    public void testAdd() {


        L2Counter<String> c = new SimpleL2Counter<>(2);
        Assert.assertTrue(c.addIfAbsentAndNotFull("a"));

        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 1)), TestUtils.set(c.getElements()));

        //present
        Assert.assertFalse(c.addIfAbsentAndNotFull("a"));

        Assert.assertTrue(c.addIfAbsentAndNotFull("b"));
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 1), TestUtils.e("b", 1)), TestUtils.set(c.getElements()));

        // present
        Assert.assertFalse(c.addIfAbsentAndNotFull("b"));
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 1), TestUtils.e("b", 1)), TestUtils.set(c.getElements()));

        Assert.assertTrue(c.incIfPresent("b"));
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 1), TestUtils.e("b", 2)), TestUtils.set(c.getElements()));

        //full
        Assert.assertFalse(c.addIfAbsentAndNotFull("c"));

        Assert.assertTrue(c.incIfPresent("a"));
        Assert.assertTrue(c.incIfPresent("a"));
        Assert.assertEquals(TestUtils.set(TestUtils.e("a", 3), TestUtils.e("b", 2)), TestUtils.set(c.getElements()));

        Assert.assertFalse(c.incIfPresent("c"));

        c.clear();
        Assert.assertEquals(TestUtils.set(), TestUtils.set(c.getElements()));
        Assert.assertTrue(c.addIfAbsentAndNotFull("c"));
        Assert.assertEquals(TestUtils.set(TestUtils.e("c", 1)), TestUtils.set(c.getElements()));


    }



} 
