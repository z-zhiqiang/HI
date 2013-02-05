package zuo.processor.cbi.profile.predicatesite;

import zuo.processor.cbi.site.InstrumentationSites.AbstractSite;


public abstract class AbstractPredicateSite {

	protected final int id;

	protected final AbstractSite site;

	public AbstractPredicateSite(int id, AbstractSite site) {
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

	protected byte normalize(int counter) {
		return (byte) (counter > 0 ? 1 : 0);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Preciate(scheme = ").append(this.getScheme())
				.append(", id=").append(this.id).append(", site=")
				.append(this.site.toStringWithoutFile()).append(", ");
		this.toSpecificString(builder);
		builder.append(")");
		return builder.toString();
	}

	protected abstract String getScheme();

	protected abstract void toSpecificString(StringBuilder builder);

}
