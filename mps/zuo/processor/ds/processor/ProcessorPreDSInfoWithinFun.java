package zuo.processor.ds.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.processor.predicate.PredicateDataSet;
import sun.processor.predicate.PredicateDataSet.Run;
import sun.processor.predicate.PredicateItem;
import sun.processor.predicate.PredicateItem.PredicateKey;
import zuo.processor.functionentry.processor.PruningProcessor;


public class ProcessorPreDSInfoWithinFun {
	private final PredicateDataSet predicateDataSet;
	private final Map<String, PredicateDSInfoWithinFunction> DSInfoMap;
	
	public ProcessorPreDSInfoWithinFun(PredicateDataSet predicateDataSet){
		this.predicateDataSet = predicateDataSet;
		this.DSInfoMap = new HashMap<String, PredicateDSInfoWithinFunction>();
	}

	public void process(){
		Run[] runs = predicateDataSet.getRuns();
		List<PredicateItem> predicateItems = runs[0].getAllItems();
		
		for(int i = 0; i < predicateItems.size(); i++){
			processEachPredicateItem(i, runs);
		}
	}

	private void processEachPredicateItem(int i, Run[] runs) {
		// TODO Auto-generated method stub
		int neg = 0, pos = 0;
		PredicateKey key = runs[0].getAllItems().get(i).getKey();
		for(Run run: runs){
			PredicateItem item = run.getAllItems().get(i);
			assert(key.equals(item.getKey()));
			boolean correct = run.getLabel();
			if(item.isTrue()){
				if(correct){
					pos++;
				}
				else{
					neg++;
				}
			}
		}
		
		double ds = PruningProcessor.DS(neg, pos, this.predicateDataSet.getNegative(), this.predicateDataSet.getPositive());
		assert(this.predicateDataSet.getNegative() + this.predicateDataSet.getPositive() == runs.length);
		assert(ds > 0);
		PredicateItemWithDS predicateWithDS = new PredicateItemWithDS(key, ds);
		
		String function = key.getSite().getFunctionName();
		if(this.DSInfoMap.containsKey(function)){
			this.DSInfoMap.get(function).addOnePredicateItemWithDS(predicateWithDS);
		}
		else{
			this.DSInfoMap.put(function, new PredicateDSInfoWithinFunction(predicateWithDS));
		}
	}

	public Map<String, PredicateDSInfoWithinFunction> getDSInfoMap() {
		return DSInfoMap;
	}
	
	
}
