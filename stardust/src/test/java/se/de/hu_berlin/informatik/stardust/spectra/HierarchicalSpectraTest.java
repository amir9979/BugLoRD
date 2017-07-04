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
import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer.ComputationStrategies;
import se.de.hu_berlin.informatik.stardust.spectra.HierarchicalSpectra;
import se.de.hu_berlin.informatik.stardust.spectra.IMutableTrace;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.Spectra;

public class HierarchicalSpectraTest {

	private double smallDelta = 0.00001;
	
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

        Assert.assertEquals(one.getOrCreateNode("P1").getNP(ComputationStrategies.STANDARD_SBFL), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getNF(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEP(ComputationStrategies.STANDARD_SBFL), 3, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEF(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P2").getNP(ComputationStrategies.STANDARD_SBFL), 2, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getNF(ComputationStrategies.STANDARD_SBFL), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEP(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEF(ComputationStrategies.STANDARD_SBFL), 2, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P3").getNP(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P3").getNF(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P3").getEP(ComputationStrategies.STANDARD_SBFL), 2, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P3").getEF(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
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

        Assert.assertEquals(one.getOrCreateNode("P1").getNP(ComputationStrategies.STANDARD_SBFL), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getNF(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEP(ComputationStrategies.STANDARD_SBFL), 3, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEF(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P2").getNP(ComputationStrategies.STANDARD_SBFL), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getNF(ComputationStrategies.STANDARD_SBFL), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEP(ComputationStrategies.STANDARD_SBFL), 2, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEF(ComputationStrategies.STANDARD_SBFL), 2, smallDelta);
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

        Assert.assertEquals(four.getOrCreateNode("P2.4").getNP(), 0, smallDelta);
        Assert.assertEquals(four.getOrCreateNode("P2.4").getNF(), 1, smallDelta);
        Assert.assertEquals(four.getOrCreateNode("P2.4").getEP(), 3, smallDelta);
        Assert.assertEquals(four.getOrCreateNode("P2.4").getEF(), 1, smallDelta);

        Assert.assertEquals(four.getOrCreateNode("P1.4").getNP(), 1, smallDelta);
        Assert.assertEquals(four.getOrCreateNode("P1.4").getNF(), 0, smallDelta);
        Assert.assertEquals(four.getOrCreateNode("P1.4").getEP(), 2, smallDelta);
        Assert.assertEquals(four.getOrCreateNode("P1.4").getEF(), 2, smallDelta);

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
        Assert.assertFalse(one.hasNode("S3"));

        Assert.assertEquals(one.getOrCreateNode("P1").getNP(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getNF(), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEP(), 3, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEF(), 1, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P2").getNP(), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getNF(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEP(), 2, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEF(), 2, smallDelta);

        // add new trace
        final IMutableTrace<String> newTrace = bottom.addTrace("trace1", true);
        
        Assert.assertEquals(one.getOrCreateNode("P1").getNP(), 1, smallDelta); // one more
        Assert.assertEquals(one.getOrCreateNode("P1").getNF(), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEP(), 3, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEF(), 1, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P2").getNP(), 2, smallDelta); // one more
        Assert.assertEquals(one.getOrCreateNode("P2").getNF(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEP(), 2, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEF(), 2, smallDelta);
        
        newTrace.setInvolvement("S1", true);

        Assert.assertEquals(one.getOrCreateNode("P1").getNP(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getNF(), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEP(), 4, smallDelta); // one more
        Assert.assertEquals(one.getOrCreateNode("P1").getEF(), 1, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P2").getNP(), 2, smallDelta); // one more
        Assert.assertEquals(one.getOrCreateNode("P2").getNF(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEP(), 2, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEF(), 2, smallDelta);

        // set new involvement of S3 in existing trace
        newTrace.setInvolvement("S3", true);

        Assert.assertEquals(one.getOrCreateNode("P1").getNP(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getNF(), 1, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEP(), 4, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P1").getEF(), 1, smallDelta);

        Assert.assertEquals(one.getOrCreateNode("P2").getNP(), 1, smallDelta); // one less
        Assert.assertEquals(one.getOrCreateNode("P2").getNF(), 0, smallDelta);
        Assert.assertEquals(one.getOrCreateNode("P2").getEP(), 3, smallDelta); // one more
        Assert.assertEquals(one.getOrCreateNode("P2").getEF(), 2, smallDelta);
    }
}
