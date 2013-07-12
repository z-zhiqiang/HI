package sun.processor.profile.predicate;

import java.util.HashMap;
import java.util.Map;

import sun.processor.profile.InstrumentationSites.ReturnSite;

public class ReturnPredicate extends AbstractPredicate {

  private final static class Key {

    private final int id;

    private final int counter;

    public Key(int id, byte negativeCount, byte zeroCount, byte positiveCount) {
      this.id = id;
      this.counter = buildCounter(negativeCount, zeroCount, positiveCount);
    }

    private int buildCounter(byte negativeCount, byte zeroCount,
        byte positiveCount) {
      int counter = negativeCount;
      counter = counter << 8;
      counter += zeroCount;
      counter = counter << 8;
      counter += positiveCount;
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

    private final Map<Key, ReturnPredicate> cache;

    public Factory() {
      this.cache = new HashMap<ReturnPredicate.Key, ReturnPredicate>();
    }

    public ReturnPredicate create(int id, ReturnSite site,
        int intNegativeCount, int intZeroCount, int intPositveCount) {
      final byte negativeCount = AbstractPredicate.normalize(intNegativeCount);
      final byte zeroCount = AbstractPredicate.normalize(intZeroCount);
      final byte positiveCount = AbstractPredicate.normalize(intPositveCount);

      final Key key = new Key(id, negativeCount, zeroCount, positiveCount);
      ReturnPredicate value = this.cache.get(key);
      if (value == null) {
        value = new ReturnPredicate(id, site, negativeCount, zeroCount,
            positiveCount);
        this.cache.put(key, value);
      }
      return value;
    }

  }

  private final byte negativeCount;

  private final byte zeroCount;

  private final byte positveCount;

  private ReturnPredicate(int id, ReturnSite site, byte negativeCount,
      byte zeroCount, byte positveCount) {
    super(id, site);
    this.negativeCount = negativeCount;
    this.zeroCount = zeroCount;
    this.positveCount = positveCount;
  }

  public int getTotalCount() {
    return this.negativeCount + this.positveCount + this.zeroCount;
  }

  public int getGreaterCount() {
    return this.positveCount;
  }

  public int getGreaterEqualCount() {
    return this.positveCount + this.zeroCount;
  }

  public int getEqualCount() {
    return this.zeroCount;
  }

  public int getNotEqualCount() {
    return this.positveCount + this.negativeCount;
  }

  public int getLessCount() {
    return this.negativeCount;
  }

  public int getLessEqualCount() {
    return this.negativeCount + this.zeroCount;
  }

  // public int getNegativeCount() {
  // return negativeCount;
  // }
  //
  // public int getZeroCount() {
  // return zeroCount;
  // }
  //
  // public int getPositveCount() {
  // return positveCount;
  // }

  @Override
  protected String getScheme() {
    return "returns";
  }

  @Override
  protected void toSpecificString(StringBuilder builder) {
    builder.append("negativeCount = ").append(negativeCount)
        .append(", zeroCount = ").append(zeroCount).append(", positveCount = ")
        .append(positveCount);
  }

}
