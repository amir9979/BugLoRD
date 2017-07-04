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
 * Goodman fault localizer $\frac{2\EF-\NF-\EP}{2\EF+\NF+\EP}$
 * 
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Goodman<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Goodman() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node, ComputationStrategies strategy) {
    	double numerator = 2.0d * node.getEF(strategy) - node.getNF(strategy) - node.getEP(strategy);
    	if (numerator == 0) {
    		return 0;
    	}
        return numerator / (double)(2.0d * node.getEF(strategy) + node.getNF(strategy) + node.getEP(strategy));
    }

    @Override
    public String getName() {
        return "Goodman";
    }

}
