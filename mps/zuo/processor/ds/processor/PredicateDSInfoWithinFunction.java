package zuo.processor.ds.processor;

import java.util.ArrayList;
import java.util.List;

public class PredicateDSInfoWithinFunction {
	private double Max_DS;
	private double Sum_DS;
	private double Mean_DS;
//	private double Median_DS;
	private List<PredicateItemWithDS> predicatesList;
	
//	public PredicateDSInfoWithinFunction(){
//		this.Max_DS = 0;
//		this.Sum_DS = 0;
//		this.Mean_DS = 0;
//		this.predicatesList = new ArrayList<PredicateItemWithDS>();
//	}
	
	public PredicateDSInfoWithinFunction(PredicateItemWithDS predicate){
		this.predicatesList = new ArrayList<PredicateItemWithDS>();
		this.predicatesList.add(predicate);
		this.Max_DS = predicate.getDs();
		this.Sum_DS = predicate.getDs();
		this.Mean_DS = predicate.getDs();
	}
	
	public void addOnePredicateItemWithDS(PredicateItemWithDS predicate){
		this.predicatesList.add(predicate);
		double ds = predicate.getDs();
		if(ds > this.Max_DS){
			this.Max_DS = ds;
		}
		this.Sum_DS += ds;
		this.Mean_DS = this.Sum_DS / predicatesList.size();
	}

	public double getMax_DS() {
		return Max_DS;
	}

	public double getSum_DS() {
		return Sum_DS;
	}

	public double getMean_DS() {
		return Mean_DS;
	}

	public List<PredicateItemWithDS> getPredicatesList() {
		return predicatesList;
	}

	
}
