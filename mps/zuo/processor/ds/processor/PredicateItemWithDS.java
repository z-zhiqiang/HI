package zuo.processor.ds.processor;

import sun.processor.predicate.PredicateItem.PredicateKey;

public class PredicateItemWithDS {
	private final PredicateKey predicateKey;
	private final double ds;
	
	public PredicateItemWithDS(PredicateKey key, double ds){
		this.predicateKey = key;
		this.ds = ds;
	}

	public PredicateKey getPredicateKey() {
		return predicateKey;
	}

	public double getDs() {
		return ds;
	}
	
}
