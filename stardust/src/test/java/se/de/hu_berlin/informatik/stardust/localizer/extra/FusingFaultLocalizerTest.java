/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.extra;

import org.junit.Test;

import fk.stardust.test.data.SimpleSpectraProvider;
import se.de.hu_berlin.informatik.stardust.localizer.extra.FusingFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.localizer.extra.FusingFaultLocalizer.DataFusionTechnique;
import se.de.hu_berlin.informatik.stardust.localizer.extra.FusingFaultLocalizer.SelectionTechnique;
import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitSpectra;
import se.de.hu_berlin.informatik.utils.experiments.ranking.SimpleRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking.NormalizationStrategy;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;

public class FusingFaultLocalizerTest {

    @Test
    public void selectOverlapBased() throws Exception {
        final SimpleSpectraProvider t = new SimpleSpectraProvider();
        final HitSpectra<String> s = t.loadHitSpectra();
        final FusingFaultLocalizer<String> f = new FusingFaultLocalizer<>(NormalizationStrategy.ZeroToOneRankingValue,
                SelectionTechnique.OVERLAP_RATE, DataFusionTechnique.COMB_ANZ);
        final SimpleRanking<INode<String>> r = (SimpleRanking<INode<String>>) f.localize(s);
        for (final INode<String> n : r) {
        	Log.out(this, String.format("Node %s: %f", n.getIdentifier(), r.getRankingValue(n)));
        }
    }
}
