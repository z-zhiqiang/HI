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

	protected byte normalize(int counter) {
		return (byte) (counter > 0 ? 1 : 0);
	}

	protected abstract void toSpecificString(StringBuilder builder);

}
