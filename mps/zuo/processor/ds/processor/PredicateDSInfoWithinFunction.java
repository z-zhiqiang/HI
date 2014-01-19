package zuo.processor.ds.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PredicateDSInfoWithinFunction {
	private double Max_DS;
	private double Mean_DS;
	private double Median_DS;
	private List<PredicateItemWithDS> predicatesList;
	
	public PredicateDSInfoWithinFunction(PredicateItemWithDS predicate){
		this.predicatesList = new ArrayList<PredicateItemWithDS>();
		this.predicatesList.add(predicate);
		this.Max_DS = 0;
		this.Mean_DS = 0;
		this.Median_DS = 0;
	}
	
	public void addOnePredicateItemWithDS(PredicateItemWithDS predicate){
		this.predicatesList.add(predicate);
	}

	public double getMax_DS() {
		return Max_DS;
	}

	public double getMean_DS() {
		return Mean_DS;
	}

	public double getMedian_DS() {
		return Median_DS;
	}

	public void setDS_Max_Mean_Median() {
		Collections.sort(this.predicatesList, new Comparator<PredicateItemWithDS>(){

			@Override
			public int compare(PredicateItemWithDS o1, PredicateItemWithDS o2) {
				// TODO Auto-generated method stub
				return new Double(o2.getDs()).compareTo(new Double(o1.getDs()));
			}
			
		});
		this.Max_DS = this.predicatesList.get(0).getDs();
		this.Mean_DS = computeMean(this.predicatesList);
		this.Median_DS = computeMedian(this.predicatesList);
	}

	public static double computeMean(List<PredicateItemWithDS> sortedPredicatesList) {
		// TODO Auto-generated method stub
		double sum = 0;
		for(PredicateItemWithDS item: sortedPredicatesList){
			sum += item.getDs();
		}
		return sum / sortedPredicatesList.size();
	}

	public static double computeMedian(List<PredicateItemWithDS> sortedPredicatesList) {
		// TODO Auto-generated method stub
		int mid = sortedPredicatesList.size() / 2;
		if(sortedPredicatesList.size() % 2 == 1){
			return sortedPredicatesList.get(mid).getDs();
		}
		else{
			double left = sortedPredicatesList.get(mid - 1).getDs();
			double right = sortedPredicatesList.get(mid).getDs();
			return (left + right) / 2;
		}
	}

	public List<PredicateItemWithDS> getPredicatesList() {
		return predicatesList;
	}

	
}
