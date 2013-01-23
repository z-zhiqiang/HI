package zuo.processor.cbi.profile.predicatesite;

import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;


public class ScalarPairPredicateSite extends AbstractPredicateSite {

	private final byte lessThanCount;

	private final byte equalCount;

	private final byte greaterThanCount;

	public ScalarPairPredicateSite(int id, ScalarSite site, int lessThanCount,
			int equalCount, int greaterThanCount) {
		super(id, site);
		// this.lessThanCount = (byte) (lessThanCount > 0 ? 1 : 0);
		this.lessThanCount = this.normalize(lessThanCount);
		// this.equalCount = (byte) (equalCount > 0 ? 1 : 0);
		this.equalCount = this.normalize(equalCount);
		// this.greaterThanCount = (byte) (greaterThanCount > 0 ? 1 : 0);
		this.greaterThanCount = this.normalize(greaterThanCount);
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

}
