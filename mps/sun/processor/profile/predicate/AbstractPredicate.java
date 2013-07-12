package sun.processor.profile.predicate;

import sun.processor.profile.InstrumentationSites.AbstractSite;

public abstract class AbstractPredicate {

  /**
   * id is a unique identify for a predicate. two predicates are the same if
   * they have the same id.
   */
  protected final int id;

  protected final AbstractSite site;

  @Override
  public int hashCode() {
    return this.id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractPredicate other = (AbstractPredicate) obj;
    if (id != other.id)
      return false;
    return true;
  }

  public AbstractPredicate(int id, AbstractSite site) {
    this.id = id;
    this.site = site;
  }

  public int getId() {
    return id;
  }

  public AbstractSite getSite() {
    return site;
  }

  public String getMethodName() {
    return this.site.getFunctionName();
  }

  public static byte normalize(int counter) {
    return (byte) (counter > 0 ? 1 : 0);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Preciate(scheme = ").append(this.getScheme())
        .append(", id=").append(this.id).append(", method=")
        .append(this.site.getFunctionName()).append(", ");
    this.toSpecificString(builder);
    builder.append(")");
    return builder.toString();
  }

  protected abstract String getScheme();

  protected abstract void toSpecificString(StringBuilder builder);

}
