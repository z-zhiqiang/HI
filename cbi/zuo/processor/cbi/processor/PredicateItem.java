package zuo.processor.cbi.processor;

import zuo.processor.cbi.site.InstrumentationSites.AbstractSite;

public class PredicateItem {
	
	public static enum BranchPredicateType {
		// tow branches
		TRUE_BRANCH,

		FALSE_BRANCH,
	}
	
	public static enum FloatKindPredicateType {
		// float kinds
		NEGATIVE_INF,

		NEGATIVE_NORMALIZED,

		NEGATIVE_DENORMALIZED,

		NEGATIVE_ZERO,

		NAN,

		POSITIVE_ZERO,

		POSITIVE_DENORMALIZED,

		POSITIVE_NORMALIZED,

		POSITIVE_INF
	}
	
	public static enum ReturnScalarPairPredicateType {
		LESS,

		LESS_EQUAL,
		
		GREATER,

		GREATER_EQUAL,

		EQUAL,

		NOT_EQUAL
	}
	
	private final AbstractSite site;
	private final int id;
	private final int type;
	
	public PredicateItem(AbstractSite site, int id, int type){
		this.site = site;
		this.id = id;
		this.type = type;
	}
	
	public String toString(){
		String tp = null;
		
		switch(site.getCategory()) {
		case BRANCH: {
			tp = BranchPredicateType.values()[type].toString();
			break;
		}
		case FLOAT_KIND: {
			tp = FloatKindPredicateType.values()[type].toString();
			break;
		}
		case RETURN: {
			tp = ReturnScalarPairPredicateType.values()[type].toString();
			break;
		}
		case SCALAR_PAIR: {
			tp = ReturnScalarPairPredicateType.values()[type].toString();
			break;
		}
		default:
			throw new RuntimeException("Category Error");
		}
		
		StringBuilder string = new StringBuilder();
		string.append("(").append(id).append(", ").append(site.getCategory().toString()).append(", ").append(tp).append(")\n")
			.append(site.toStringWithoutFile()).append("\n")
			.append(site.getFileString()).append("\n");
		return string.toString();
	}
	
	public int hashCode(){
		int result = 1;
		result = 37 * result + id;
		result = 37 * result + type;
		return result;
	}
	
	public boolean equals(Object o){
		PredicateItem obj = (PredicateItem) o;
		return (o instanceof PredicateItem) && (id == obj.id) && (type == obj.type);
	}

}
