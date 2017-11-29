package hotspotsensor;

import org.junit.Test;
import org.testng.Assert;

import static hotspotsensor.TestUtils.set;

/**
 * @author iamlotus@gmail.com
 */
public class SimpleL1LRUTest {


    @Test
    public void testReplace() {
        //capacity =1
        L1LRU<String> l1 = new SimpleL1LRU<>(1);
        Assert.assertEquals(TestUtils.set(), l1.getElements());
        Assert.assertNull(l1.put("a"));
        Assert.assertEquals(TestUtils.set("a"), l1.getElements());
        Assert.assertEquals("a", l1.put("a"));
        Assert.assertEquals(TestUtils.set(), l1.getElements());
        Assert.assertNull(l1.put("a"));
        Assert.assertNull(l1.put("b"));
        Assert.assertEquals(TestUtils.set("b"), l1.getElements());

        // capacity = 2
        L1LRU<String> l2 = new SimpleL1LRU<>(2);
        Assert.assertEquals(TestUtils.set(), l2.getElements());
        Assert.assertNull(l2.put("a"));
        Assert.assertEquals(TestUtils.set("a"), l2.getElements());
        Assert.assertEquals("a", l2.put("a"));
        Assert.assertEquals(TestUtils.set(), l2.getElements());
        Assert.assertNull(l2.put("b"));
        Assert.assertEquals(TestUtils.set("b"), l2.getElements());
        Assert.assertEquals("b", l2.put("b"));
        Assert.assertEquals(TestUtils.set(), l2.getElements());

        Assert.assertNull(l2.put("a"));
        Assert.assertNull(l2.put("b"));
        Assert.assertEquals(TestUtils.set("a", "b"), l2.getElements());
        Assert.assertNull(l2.put("c"));
        Assert.assertEquals(TestUtils.set("b", "c"), l2.getElements());

        //capacity =3

        L1LRU<String> l3 = new SimpleL1LRU<>(3);
        Assert.assertEquals(TestUtils.set(), l3.getElements());
        Assert.assertNull(l3.put("a"));
        Assert.assertNull(l3.put("b"));
        Assert.assertEquals(TestUtils.set("a", "b"), l3.getElements());
        Assert.assertNull(l3.put("c"));
        Assert.assertEquals(TestUtils.set("a", "b", "c"), l3.getElements());
        Assert.assertEquals("a", l3.put("a"));
        Assert.assertEquals(TestUtils.set("b", "c"), l3.getElements());
    }


} 
