package sun.processor.profile.predicate;

import sun.processor.profile.InstrumentationSites.AbstractSite;

public abstract class AbstractPredicate {

	protected final int id;

	// protected String containingMethodName;
	protected final AbstractSite site;

	public AbstractPredicate(int id, AbstractSite site) {
		this.id = id;
		// this.containingMethodName = containingMethodName;
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

	protected byte normalize(int counter) {
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
