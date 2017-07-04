/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.localizer.sbfl.localizers;

import se.de.hu_berlin.informatik.stardust.localizer.sbfl.AbstractSpectrumBasedFaultLocalizer;
import se.de.hu_berlin.informatik.stardust.spectra.INode;

/**
 * ArithmeticMean fault localizer $\frac{2\EF\NP - 2\NF\EP}{(\EF+\EP)\cdot(\NP+\NF)+(\EF+\NF)\cdot(\EP+\NP)}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class ArithmeticMean<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public ArithmeticMean() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
        final double enu1 = 2 * node.getEF(strategy) * node.getNP(strategy);
        final double enu2 = 2 * node.getNF(strategy) * node.getEP(strategy);
        final double enu = enu1 - enu2;

        final double denom1 = (node.getEF(strategy) + node.getEP(strategy)) * (node.getNP(strategy) + node.getNF(strategy));
        final double denom2 = (node.getEF(strategy) + node.getNF(strategy)) * (node.getEP(strategy) + node.getNP(strategy));
        final double denom = denom1 + denom2;

        if (enu == 0) {
    		return 0;
    	}
        return enu / denom;
    }

    @Override
    public String getName() {
        return "ArithmeticMean";
    }

}
