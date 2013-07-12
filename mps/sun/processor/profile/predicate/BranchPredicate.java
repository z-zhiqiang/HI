package sun.processor.profile.predicate;

import java.util.HashMap;
import java.util.Map;

import sun.processor.profile.InstrumentationSites.BranchSite;

public class BranchPredicate extends AbstractPredicate {

  private static class Key {

    private final int id;

    private final int counter;

    public Key(int id, byte trueCount, byte falseCount) {
      this.id = id;
      this.counter = buildCounter(trueCount, falseCount);
    }

    private int buildCounter(byte trueCount, byte falseCount) {
      int counter = trueCount;
      counter = counter << 8;
      counter += falseCount;
      return counter;
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

  }

  public final static class Factory {

    private final Map<Key, BranchPredicate> cache;

    public Factory() {
      this.cache = new HashMap<BranchPredicate.Key, BranchPredicate>();
    }

    public BranchPredicate create(int id, BranchSite site, int intTrueCount,
        int intFalseCount) {
      final byte trueCount = AbstractPredicate.normalize(intTrueCount);
      final byte falseCount = AbstractPredicate.normalize(intFalseCount);
      final Key key = new Key(id, trueCount, falseCount);
      BranchPredicate value = this.cache.get(key);
      if (value == null) {
        value = new BranchPredicate(id, site, trueCount, falseCount);
        this.cache.put(key, value);
      }
      return value;
    }
  }

  private final byte trueCount;

  private final byte falseCount;

  private BranchPredicate(int id, BranchSite site, byte trueCount,
      byte falseCount) {
    super(id, site);
    this.trueCount = trueCount;
    this.falseCount = falseCount;
  }

  public int getTrueCount() {
    return trueCount;
  }

  public int getFalseCount() {
    return falseCount;
  }

  public int getTotalCount() {
    return trueCount + falseCount;
  }

  @Override
  protected String getScheme() {
    return "branches";
  }

  @Override
  protected void toSpecificString(StringBuilder builder) {
    builder.append("true = ").append(trueCount).append(", false = ")
        .append(falseCount);
  }
}
