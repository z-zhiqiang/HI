package zuo.processor.cbi.processor;

import zuo.processor.cbi.profile.predicatesite.AbstractPredicateSite;

public class PredicateItem {
	public static enum SiteCategory {
		 BRANCH, FLOAT_KIND, RETURN, SCALAR_PAIR
	}
	
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
	
	private final AbstractPredicateSite predicateSite;
	private final int type;
	
	
	public PredicateItem(AbstractPredicateSite pSite, int type){
		this.predicateSite = pSite;
		this.type = type;
	}
	
	public String toString(){
		String tp = null;
		
		switch(predicateSite.getSite().getCategory()) {
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
		string.append("(").append(predicateSite.getId()).append(", ").append(predicateSite.getSite().getCategory().toString()).append(", ").append(tp).append(", ").append(type).append(")\n")
			.append(predicateSite.getSite().toStringWithoutFile()).append("\n")
			.append(predicateSite.getSite().getFileString());
		return string.toString();
	}
	
//	public int hashCode(){
//		int result = 1;
//		result = 31 * result + predicateSite.getId();
//		result = 31 * result + type;
//		return result;
//	}
//	
//	public boolean equals(Object o){
//		PredicateItem obj = (PredicateItem) o;
//		return (o instanceof PredicateItem) && (predicateSite.getId() == obj.predicateSite.getId()) && (type == obj.type);
//	}

	public int getType() {
		return type;
	}

	public AbstractPredicateSite getPredicateSite() {
		return predicateSite;
	}
	
	

}
