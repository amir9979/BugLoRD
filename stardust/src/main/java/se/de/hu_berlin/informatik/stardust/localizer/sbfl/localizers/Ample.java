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
 * Ample fault localizer $\left|\frac{\EF}{\EF+\NF}-\frac{\EP}{\EP+\NP}\right|$
 *
 * @param <T>
 *            type used to identify nodes in the system
 */
public class Ample<T> extends AbstractSpectrumBasedFaultLocalizer<T> {

    /**
     * Create fault localizer
     */
    public Ample() {
        super();
    }

    @Override
    public double suspiciousness(final INode<T> node) {
        double left = (double)node.getEF() / (double)(node.getEF() + node.getNF());
        if (node.getEF() == 0) {
        	left = 0;
        }
        double right = (double)node.getEP() / (double)(node.getEP() + node.getNP());
        if (node.getEP() == 0) {
        	right = 0;
        }
        return Math.abs(left - right);
    }

    @Override
    public String getName() {
        return "Ample";
    }

}