package sun.processor.predicate;

import sun.processor.profile.InstrumentationSites.AbstractSite;

public final class PredicateItem {

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

  public final static class PredicateKey {

    private int id = Integer.MIN_VALUE;

    private final PredicateCategory category;

    private final PredicateType subcategory;

    private final AbstractSite site;

    private final int originalId;

    private final int hashCode;

    private final PredicateItem instanceNotObserved;

    private final PredicateItem instanceTrue;

    private final PredicateItem instanceFalse;

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
      this.site = site;

      this.hashCode = this.computeHashCode();

      this.instanceNotObserved = new PredicateItem(this,
          PredicateValue.NOT_OBSERVED);
      this.instanceFalse = new PredicateItem(this, PredicateValue.FALSE);
      this.instanceTrue = new PredicateItem(this, PredicateValue.TRUE);
    }

    public PredicateItem getInstance(PredicateValue value) {
      switch (value) {
      case FALSE:
        return this.instanceFalse;
      case TRUE:
        return this.instanceTrue;
      case NOT_OBSERVED:
        return this.instanceNotObserved;
      default:
        throw new RuntimeException("Cannot reach here.");
      }
    }

    public String getMethodName() {
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
      return this.hashCode;
    }

    public int computeHashCode() {
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

  private final PredicateKey key;

  private final PredicateValue predicateStatus;

  private PredicateItem(PredicateKey key, PredicateValue predicateStatus) {
    super();
    this.key = key;
    this.predicateStatus = predicateStatus;
  }

  public boolean isObserved() {
    return this.predicateStatus != (PredicateValue.NOT_OBSERVED);
  }

  public boolean isTrue() {
    return this.predicateStatus == (PredicateValue.TRUE);
  }

  public boolean isFalse() {
    return this.predicateStatus == (PredicateValue.FALSE);
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
