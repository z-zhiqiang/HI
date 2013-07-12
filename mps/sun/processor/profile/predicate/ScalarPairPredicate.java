package sun.processor.profile.predicate;

import java.util.HashMap;
import java.util.Map;

import sun.processor.predicate.PredicateItem;
import sun.processor.predicate.PredicateItemFactory;
import sun.processor.profile.InstrumentationSites.ScalarSite;

public class ScalarPairPredicate extends AbstractPredicate {

  private static class Key {

    private final int id;

    private final int counter;

    public Key(int id, byte lessThanCount, byte equalCount,
        byte greaterThanCount) {
      this.id = id;
      this.counter = buildCounter(lessThanCount, equalCount, greaterThanCount);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + counter;
      result = prime * result + id;
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
      Key other = (Key) obj;
      if (counter != other.counter)
        return false;
      if (id != other.id)
        return false;
      return true;
    }

    private int buildCounter(byte lessThanCount, byte equalCount,
        byte greaterThanCount) {
      int counter = (lessThanCount);
      counter = counter << 8;
      counter += (equalCount);
      counter = counter << 8;
      counter += (greaterThanCount);
      return counter;
    }

  }

  public static class Factory {

    private final Map<Key, ScalarPairPredicate> cache;

    public Factory() {
      this.cache = new HashMap<ScalarPairPredicate.Key, ScalarPairPredicate>();
    }

    public ScalarPairPredicate create(int id, ScalarSite site,
        int intLessThanCount, int intEqualCount, int intGreaterThanCount) {
      final byte lessThanCount = AbstractPredicate.normalize(intLessThanCount);
      final byte equalCount = AbstractPredicate.normalize(intEqualCount);
      final byte greaterThanCount = AbstractPredicate
          .normalize(intGreaterThanCount);

      final Key key = new Key(id, lessThanCount, equalCount, greaterThanCount);
      ScalarPairPredicate p = this.cache.get(key);
      if (p == null) {
        p = new ScalarPairPredicate(id, site, lessThanCount, equalCount,
            greaterThanCount);
        this.cache.put(key, p);
      }
      return p;
    }
  }

  private final byte lessThanCount;

  private final byte equalCount;

  private final byte greaterThanCount;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + equalCount;
    result = prime * result + greaterThanCount;
    result = prime * result + lessThanCount;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    ScalarPairPredicate other = (ScalarPairPredicate) obj;
    if (equalCount != other.equalCount)
      return false;
    if (greaterThanCount != other.greaterThanCount)
      return false;
    if (lessThanCount != other.lessThanCount)
      return false;
    return true;
  }

  private ScalarPairPredicate(int id, ScalarSite site, byte lessThanCount,
      byte equalCount, byte greaterThanCount) {
    super(id, site);
    // this.lessThanCount = (byte) (lessThanCount > 0 ? 1 : 0);
    this.lessThanCount = (lessThanCount);
    // this.equalCount = (byte) (equalCount > 0 ? 1 : 0);
    this.equalCount = (equalCount);
    // this.greaterThanCount = (byte) (greaterThanCount > 0 ? 1 : 0);
    this.greaterThanCount = (greaterThanCount);
  }

  public int getTotalCount() {
    return this.lessThanCount + this.equalCount + this.greaterThanCount;
  }

  public int getLessEqualCount() {
    return this.lessThanCount + this.equalCount;
  }

  public int getGreaterEqualCount() {
    return this.greaterThanCount + this.equalCount;
  }

  public int getNotEqualCount() {
    return this.lessThanCount + this.greaterThanCount;
  }

  public int getLessCount() {
    return lessThanCount;
  }

  public int getEqualCount() {
    return equalCount;
  }

  public int getGreaterCount() {
    return greaterThanCount;
  }

  @Override
  protected String getScheme() {
    return "scalar-pairs";
  }

  @Override
  protected void toSpecificString(StringBuilder builder) {
    builder.append("lessThanCount = ").append(this.lessThanCount)
        .append(", equalCount = ").append(this.equalCount)
        .append(", greaterThanCount = ").append(this.greaterThanCount);
  }

  private PredicateItem[] items;

  public PredicateItem[] getPredicateItems(PredicateItemFactory factory) {
    if (items == null) {
      items = new PredicateItem[PredicateItemFactory.NUM_PREDICATES_PER_SCALAR_PAIR];
      factory.createPredicateItems(this, items);
    }
    return items;
  }
  
  public void clearPredicateItems() {
    this.items = null;
  }

}
