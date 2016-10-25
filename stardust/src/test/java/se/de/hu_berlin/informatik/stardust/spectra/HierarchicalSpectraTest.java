/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra;

import org.junit.Assert;
import org.junit.Test;

import fk.stardust.test.data.SimpleSpectraProvider;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

public class HierarchicalSpectraTest {

    /**
     * Provide test data
     */
    private Spectra<String> getTestData() {
        try {
            return new SimpleSpectraProvider().loadSpectra();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void hierarchyWithParentsHavingSingleChild() {
        final ISpectra<String> bottom = this.getTestData();

        final HierarchicalSpectra<String, String> one = new HierarchicalSpectra<>(bottom);
        one.setParent("P1", "S1");
        one.setParent("P2", "S2");
        one.setParent("P3", "S3");

        Assert.assertTrue(one.hasNode("P1"));
        Assert.assertTrue(one.hasNode("P2"));
        Assert.assertTrue(one.hasNode("P3"));

        Assert.assertEquals(one.getNode("P1").getNP(), 0);
        Assert.assertEquals(one.getNode("P1").getNF(), 1);
        Assert.assertEquals(one.getNode("P1").getEP(), 3);
        Assert.assertEquals(one.getNode("P1").getEF(), 1);

        Assert.assertEquals(one.getNode("P2").getNP(), 2);
        Assert.assertEquals(one.getNode("P2").getNF(), 0);
        Assert.assertEquals(one.getNode("P2").getEP(), 1);
        Assert.assertEquals(one.getNode("P2").getEF(), 2);

        Assert.assertEquals(one.getNode("P3").getNP(), 1);
        Assert.assertEquals(one.getNode("P3").getNF(), 1);
        Assert.assertEquals(one.getNode("P3").getEP(), 2);
        Assert.assertEquals(one.getNode("P3").getEF(), 1);
    }

    @Test
    public void hierarchyWithMergedChildren() {
        final ISpectra<String> bottom = this.getTestData();

        final HierarchicalSpectra<String, String> one = new HierarchicalSpectra<>(bottom);
        one.setParent("P1", "S1");
        one.setParent("P2", "S2");
        one.setParent("P2", "S3");

        Assert.assertTrue(one.hasNode("P1"));
        Assert.assertTrue(one.hasNode("P2"));
        Assert.assertFalse(one.hasNode("P3"));

        Assert.assertEquals(one.getNode("P1").getNP(), 0);
        Assert.assertEquals(one.getNode("P1").getNF(), 1);
        Assert.assertEquals(one.getNode("P1").getEP(), 3);
        Assert.assertEquals(one.getNode("P1").getEF(), 1);

        Assert.assertEquals(one.getNode("P2").getNP(), 1);
        Assert.assertEquals(one.getNode("P2").getNF(), 0);
        Assert.assertEquals(one.getNode("P2").getEP(), 2);
        Assert.assertEquals(one.getNode("P2").getEF(), 2);
    }

    @Test
    public void hierarchyWithMergedChildrenOverMultipleLevels() {
        final ISpectra<String> bottom = this.getTestData();

        // go up one level
        final HierarchicalSpectra<String, String> one = new HierarchicalSpectra<>(bottom);
        one.setParent("P1.1", "S1");
        one.setParent("P2.1", "S2");
        one.setParent("P3.1", "S3");

        // go up one level
        final HierarchicalSpectra<String, String> two = new HierarchicalSpectra<>(one);
        two.setParent("P1.2", "P1.1");
        two.setParent("P2.2", "P2.1");
        two.setParent("P3.2", "P3.1");

        // merge P2.2 and P3.2 to P2.3
        final HierarchicalSpectra<String, String> three = new HierarchicalSpectra<>(two);
        three.setParent("P1.3", "P1.2");
        three.setParent("P2.3", "P2.2");
        three.setParent("P2.3", "P3.2");

        // swap and go up
        final HierarchicalSpectra<String, String> four = new HierarchicalSpectra<>(three);
        four.setParent("P1.4", "P2.3");
        four.setParent("P2.4", "P1.3");

        Assert.assertTrue(four.hasNode("P1.4"));
        Assert.assertTrue(four.hasNode("P2.4"));
        Assert.assertFalse(four.hasNode("P3.4"));

        Assert.assertEquals(four.getNode("P2.4").getNP(), 0);
        Assert.assertEquals(four.getNode("P2.4").getNF(), 1);
        Assert.assertEquals(four.getNode("P2.4").getEP(), 3);
        Assert.assertEquals(four.getNode("P2.4").getEF(), 1);

        Assert.assertEquals(four.getNode("P1.4").getNP(), 1);
        Assert.assertEquals(four.getNode("P1.4").getNF(), 0);
        Assert.assertEquals(four.getNode("P1.4").getEP(), 2);
        Assert.assertEquals(four.getNode("P1.4").getEF(), 2);

        // check child references
        Assert.assertSame(bottom, one.getChildSpectra());
        Assert.assertSame(one, two.getChildSpectra());
        Assert.assertSame(two, three.getChildSpectra());
        Assert.assertSame(three, four.getChildSpectra());
    }

    @Test
    public void hierarchyWithMergedChildrenAddTrace() {
        final Spectra<String> bottom = this.getTestData();

        final HierarchicalSpectra<String, String> one = new HierarchicalSpectra<>(bottom);
        one.setParent("P1", "S1");
        one.setParent("P2", "S2");
        one.setParent("P2", "S3");

        Assert.assertTrue(one.hasNode("P1"));
        Assert.assertTrue(one.hasNode("P2"));
        Assert.assertFalse(one.hasNode("P3"));

        Assert.assertEquals(one.getNode("P1").getNP(), 0);
        Assert.assertEquals(one.getNode("P1").getNF(), 1);
        Assert.assertEquals(one.getNode("P1").getEP(), 3);
        Assert.assertEquals(one.getNode("P1").getEF(), 1);

        Assert.assertEquals(one.getNode("P2").getNP(), 1);
        Assert.assertEquals(one.getNode("P2").getNF(), 0);
        Assert.assertEquals(one.getNode("P2").getEP(), 2);
        Assert.assertEquals(one.getNode("P2").getEF(), 2);

        // add new trace
        final IMutableTrace<String> newTrace = bottom.addTrace(true);
        newTrace.setInvolvement("S1", true);

        Assert.assertEquals(one.getNode("P1").getNP(), 0);
        Assert.assertEquals(one.getNode("P1").getNF(), 1);
        Assert.assertEquals(one.getNode("P1").getEP(), 4); // one more
        Assert.assertEquals(one.getNode("P1").getEF(), 1);

        Assert.assertEquals(one.getNode("P2").getNP(), 2); // one more
        Assert.assertEquals(one.getNode("P2").getNF(), 0);
        Assert.assertEquals(one.getNode("P2").getEP(), 2);
        Assert.assertEquals(one.getNode("P2").getEF(), 2);

        // set new involvement of S3 in existing trace
        newTrace.setInvolvement("S3", true);

        Assert.assertEquals(one.getNode("P1").getNP(), 0);
        Assert.assertEquals(one.getNode("P1").getNF(), 1);
        Assert.assertEquals(one.getNode("P1").getEP(), 4);
        Assert.assertEquals(one.getNode("P1").getEF(), 1);

        Assert.assertEquals(one.getNode("P2").getNP(), 1); // one less
        Assert.assertEquals(one.getNode("P2").getNF(), 0);
        Assert.assertEquals(one.getNode("P2").getEP(), 3); // one more
        Assert.assertEquals(one.getNode("P2").getEF(), 2);
    }
}
