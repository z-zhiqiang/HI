package zuo.processor.cbi.datastructure;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;

import zuo.processor.cbi.processor.PredicateItem;

public class FixPointStructure{
	private final TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors;
	private final Set<Integer> samples;
	private final double percent;
	
	public FixPointStructure(TreeMap<Double, SortedSet<PredicateItem>> predictors, Set<Integer> samples, double percent){
		this.sortedPredictors = predictors;
		this.samples = samples;
		this.percent = percent;
	}

	public TreeMap<Double, SortedSet<PredicateItem>> getSortedPredictors() {
		return sortedPredictors;
	}

	public Set<Integer> getSamples() {
		return samples;
	}

	public double getPercent() {
		return percent;
	}
	
}
