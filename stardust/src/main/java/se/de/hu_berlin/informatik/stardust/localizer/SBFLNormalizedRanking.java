/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.utils.experiments.ranking.NormalizedRanking;
import se.de.hu_berlin.informatik.utils.experiments.ranking.Ranking;

public class SBFLNormalizedRanking<T> extends NormalizedRanking<INode<T>> {

    public SBFLNormalizedRanking(final Ranking<INode<T>> toNormalize, final NormalizationStrategy strategy) {
        super(toNormalize, strategy);
    }
    
}
