package zuo.processor.cbi.profile.predicatesite;

import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;


public class ReturnPredicateSite extends AbstractPredicateSite {

	private final byte negativeCount;

	private final byte zeroCount;

	private final byte positveCount;

	public ReturnPredicateSite(int id, ReturnSite site, int negativeCount,
			int zeroCount, int positveCount) {
		super(id, site);
//		this.negativeCount = (byte) (negativeCount > 0 ? 1 : 0);
		this.negativeCount = this.normalize(negativeCount);
//		this.zeroCount = (byte) (zeroCount > 0 ? 1 : 0);
		this.zeroCount = this.normalize(zeroCount);
//		this.positveCount = (byte) (positveCount > 0 ? 1 : 0);
		this.positveCount = this.normalize(positveCount);
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


	@Override
	protected void toSpecificString(StringBuilder builder) {
		builder.append("negativeCount = ").append(negativeCount)
				.append(", zeroCount = ").append(zeroCount).append(", positveCount = ")
				.append(positveCount);
	}

}
