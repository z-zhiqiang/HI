package zuo.processor.importance.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zuo.processor.cbi.processor.PredicateItemWithImportance;



public class ProcessorPreImportanceInfoWithinFun {
	private final List<PredicateItemWithImportance> predicatesList;
	private final Map<String, PredicateImportanceInfoWithinFunction> ImportanceInfoMap;
	
	public ProcessorPreImportanceInfoWithinFun(List<PredicateItemWithImportance> predicates){
		this.predicatesList = predicates;
		this.ImportanceInfoMap = new HashMap<String, PredicateImportanceInfoWithinFunction>();
	}

	public void process(){
		for(PredicateItemWithImportance item: this.predicatesList){
			String function = item.getPredicateItem().getPredicateSite().getSite().getFunctionName();
			if(this.ImportanceInfoMap.containsKey(function)){
				this.ImportanceInfoMap.get(function).addOnePredicateItemWithDS(item);
			}
			else{
				this.ImportanceInfoMap.put(function, new PredicateImportanceInfoWithinFunction(item));
			}
		}
		
		//set Importance values of predicates within each function
		for(String function: this.ImportanceInfoMap.keySet()){
			this.ImportanceInfoMap.get(function).setImportance_Max_Mean_Median();
		}
	}

	public Map<String, PredicateImportanceInfoWithinFunction> getImportanceInfoMap() {
		return ImportanceInfoMap;
	}

	
	
}
