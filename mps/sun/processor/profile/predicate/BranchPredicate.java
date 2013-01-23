package sun.processor.profile.predicate;

import sun.processor.profile.InstrumentationSites.BranchSite;

public class BranchPredicate extends AbstractPredicate {

	private final byte trueCount;

	private final byte falseCount;

	public BranchPredicate(int id, BranchSite site, int trueCount, int falseCount) {
		super(id, site);
		this.trueCount = (byte) (trueCount > 0 ? 1 : 0);
		this.falseCount = (byte) (falseCount > 0 ? 1 : 0);
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
