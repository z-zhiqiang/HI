package zuo.processor.cbi.processor;

public class PredicateItemWithImportance {
	private final PredicateItem predicateItem;
	private final double importance;
	
	public PredicateItemWithImportance(PredicateItem item, double im){
		this.predicateItem = item;
		this.importance = im;
	}
	
	public String toString(){
		return importance + "\n" + predicateItem.toString();
	}

	public PredicateItem getPredicateItem() {
		return predicateItem;
	}

	public double getImportance() {
		return importance;
	}

	
}
