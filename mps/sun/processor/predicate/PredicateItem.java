package sun.processor.predicate;

import sun.processor.profile.InstrumentationSites.AbstractSite;

public class PredicateItem {

	public static enum PredicateCategory {
		RETURN, SCALAR_PAIR, BRANCH, FLOAT_KIND
	}

	public static enum PredicateType {

		GREATER,

		GREATER_EQUAL,

		EQUAL,

		NOT_EQUAL,

		LESS,

		LESS_EQUAL,

		// tow branches

		TRUE_BRANCH,

		FALSE_BRANCH,

		// float kinds

		NEGATIVE_INF,

		NEGATIVE_NORMALIZED,

		NEGATIVE_DENORMALIZED,

		NEGATIVE_ZERO,

		NAN,

		POSITIVE_ZERO,

		POSITIVE_DENORMALIZED,

		POSITIVE_NORMALIZED,

		POSITIVE_INF;

	}

	public static enum PredicateValue {

		NOT_OBSERVED,

		TRUE,

		FALSE;

	}

	public static class PredicateKey {

		private int id = Integer.MIN_VALUE;

		private final PredicateCategory category;

		private final PredicateType subcategory;

		// private final String methodName;
		private final AbstractSite site;

		private final int originalId;

		public void setId(int id) {
			if (this.id != Integer.MIN_VALUE) {
				throw new RuntimeException("id has been set.");
			}
			this.id = id;
		}

		public PredicateType getPredicateType() {
			return subcategory;
		}

		public AbstractSite getSite() {
			return site;
		}

		public PredicateKey(PredicateCategory category,
				PredicateType predicateType, int originalId, AbstractSite site) {
			super();
			this.category = category;
			this.subcategory = predicateType;
			this.originalId = originalId;
			// this.methodName = methodName;
			this.site = site;
		}

		public String getMethodName() {
			// return methodName;
			return this.site.getFunctionName();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("(").append(originalId).append(", ")
					.append(category.toString()).append(", ")
					.append(subcategory.toString()).append(')');
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((category == null) ? 0 : category.hashCode());
			result = prime * result + originalId;
			result = prime * result
					+ ((subcategory == null) ? 0 : subcategory.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PredicateKey other = (PredicateKey) obj;
			if (category != other.category)
				return false;
			if (originalId != other.originalId)
				return false;
			if (subcategory != other.subcategory)
				return false;
			return true;
		}

		public int getId() {
			if (this.id == Integer.MIN_VALUE) {
				throw new RuntimeException("the id has not been set.");
			}
			return this.id;
		}

	}

	// private final int id;

	// private final PredicateType predicateType;
	//
	// private final int originalId;
	private final PredicateKey key;

	// private final int visitedCount;
	private final PredicateValue predicateStatus;

	public PredicateItem(PredicateKey key, PredicateValue predicateStatus) {
		super();
		// this.id = id;
		// this.predicateType = predicateType;
		// this.originalId = originalId;
		this.key = key;
		this.predicateStatus = predicateStatus;
	}

	public boolean isObserved() {
		return !this.predicateStatus.equals(PredicateValue.NOT_OBSERVED);
	}

	public boolean isTrue() {
		return this.predicateStatus.equals(PredicateValue.TRUE);
	}

	public boolean isFalse() {
		return this.predicateStatus.equals(PredicateValue.FALSE);
	}

	public int getId() {
		return this.key.getId();
	}

	public PredicateKey getKey() {
		return key;
	}

	public PredicateValue getPredicateStatus() {
		return predicateStatus;
	}

	@Override
	public String toString() {
		return this.getId() + "";
	}

}
