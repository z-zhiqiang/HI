package sun.processor.profile.predicate;

import sun.processor.profile.InstrumentationSites.ReturnSite;

public class ReturnPredicate extends AbstractPredicate {

	private final byte negativeCount;

	private final byte zeroCount;

	private final byte positveCount;

	public ReturnPredicate(int id, ReturnSite site, int negativeCount,
			int zeroCount, int positveCount) {
		super(id, site);
		this.negativeCount = (byte) (negativeCount > 0 ? 1 : 0);
		this.zeroCount = (byte) (zeroCount > 0 ? 1 : 0);
		this.positveCount = (byte) (positveCount > 0 ? 1 : 0);
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
