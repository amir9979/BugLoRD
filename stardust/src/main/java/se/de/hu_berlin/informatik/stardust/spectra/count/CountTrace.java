/*
 * This file is part of the "STARDUST" project.
 *
 * (c) Fabian Keller <hello@fabian-keller.de>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package se.de.hu_berlin.informatik.stardust.spectra.count;

import java.util.HashMap;
import java.util.Map;

import se.de.hu_berlin.informatik.stardust.spectra.INode;
import se.de.hu_berlin.informatik.stardust.spectra.ISpectra;
import se.de.hu_berlin.informatik.stardust.spectra.hit.HitTrace;

/**
 * This class represents a single execution trace and its success state.
 * 
 * @param <T>
 *            type used to identify nodes in the system.
 */
public class CountTrace<T> extends HitTrace<T> {

	/**
	 * a map that contains the hit counts of the different nodes
	 */
	private Map<T,Integer> hitCountMap = new HashMap<>();
	
    /**
     * Create a trace for a spectra.
     * @param spectra
     * the spectra that the trace belongs to
     * @param identifier
     * the identifier of the trace (usually the test case name)
     * @param successful
     * true if the trace originates from a successful execution, false otherwise
     */
    protected CountTrace(final ISpectra<T,?> spectra, final String identifier, final boolean successful) {
        super(spectra, identifier, successful);
    }
	
    public void setHits(T identifier, long numberOfHits) {
    	if (numberOfHits > 0) {
    		super.setInvolvement(identifier, true);
    		hitCountMap.put(identifier, numberOfHits > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)numberOfHits);
    	} else {
    		super.setInvolvement(identifier, false);
    		hitCountMap.remove(identifier);
    	}
    	
    }
    
    public void setHits(INode<T> node, long numberOfHits) {
    	if (numberOfHits > 0) {
    		super.setInvolvement(node, true);
    		hitCountMap.put(node.getIdentifier(), numberOfHits > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)numberOfHits);
    	} else {
    		super.setInvolvement(node, false);
    		hitCountMap.remove(node.getIdentifier());
    	}
    	
    }
    
    @Override
	public void setInvolvement(T identifier, boolean involved) {
		if (involved) {
			setHits(identifier, 1);
		} else {
			setHits(identifier, 0);
		}
	}

	@Override
	public void setInvolvement(INode<T> node, boolean involved) {
		if (involved) {
			setHits(node, 1);
		} else {
			setHits(node, 0);
		}
	}

	public int getHits(T identifier) {
    	Integer hits = hitCountMap.get(identifier);
    	if (hits == null) {
    		return 0;
    	} else {
    		return hits.intValue();
    	}
    }
	
	public int getHits(INode<T> node) {
    	return getHits(node.getIdentifier());
    }

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CountTrace) {
			if (!super.equals(obj)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			CountTrace<T> oTrace = (CountTrace<T>) obj;
			if (this.getInvolvedNodes().size() != oTrace.getInvolvedNodes().size()) {
				return false;
			}
			for (T nodeIdentifier : this.getInvolvedNodes()) {
				if (this.getHits(nodeIdentifier) != oTrace.getHits(nodeIdentifier)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	
	
}
