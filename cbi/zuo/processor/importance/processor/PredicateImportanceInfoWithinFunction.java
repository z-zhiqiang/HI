package zuo.processor.importance.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import zuo.processor.cbi.processor.PredicateItemWithImportance;

public class PredicateImportanceInfoWithinFunction {
	private double Max_Importance;
	private double Mean_Importance;
	private double Median_Importance;
	private List<PredicateItemWithImportance> predicatesList;
	
	public PredicateImportanceInfoWithinFunction(PredicateItemWithImportance predicate){
		this.predicatesList = new ArrayList<PredicateItemWithImportance>();
		this.predicatesList.add(predicate);
		this.Max_Importance = 0;
		this.Mean_Importance = 0;
		this.Median_Importance = 0;
	}
	
	public void addOnePredicateItemWithDS(PredicateItemWithImportance predicate){
		this.predicatesList.add(predicate);
	}


	public void setImportance_Max_Mean_Median() {
		Collections.sort(this.predicatesList, new Comparator<PredicateItemWithImportance>(){

			@Override
			public int compare(PredicateItemWithImportance o1, PredicateItemWithImportance o2) {
				// TODO Auto-generated method stub
				return new Double(o2.getImportance()).compareTo(new Double(o1.getImportance()));
			}
			
		});
		this.Max_Importance = this.predicatesList.get(0).getImportance();
		this.Mean_Importance = computeMean(this.predicatesList);
		this.Median_Importance = computeMedian(this.predicatesList);
	}

	public static double computeMean(List<PredicateItemWithImportance> sortedPredicatesList) {
		// TODO Auto-generated method stub
		double sum = 0;
		for(PredicateItemWithImportance item: sortedPredicatesList){
			sum += item.getImportance();
		}
		return sum / sortedPredicatesList.size();
	}

	public static double computeMedian(List<PredicateItemWithImportance> sortedPredicatesList) {
		// TODO Auto-generated method stub
		int mid = sortedPredicatesList.size() / 2;
		if(sortedPredicatesList.size() % 2 == 1){
			return sortedPredicatesList.get(mid).getImportance();
		}
		else{
			double left = sortedPredicatesList.get(mid - 1).getImportance();
			double right = sortedPredicatesList.get(mid).getImportance();
			return (left + right) / 2;
		}
	}

	public List<PredicateItemWithImportance> getPredicatesList() {
		return predicatesList;
	}

	public double getMax_Importance() {
		return Max_Importance;
	}

	public double getMean_Importance() {
		return Mean_Importance;
	}

	public double getMedian_Importance() {
		return Median_Importance;
	}

	
}
