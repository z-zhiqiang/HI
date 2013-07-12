package sun.processor.profile.predicate;

import sun.processor.profile.InstrumentationSites.AbstractSite;

public class FloatKindPredicate extends AbstractPredicate {

	private final byte negativeInfinite;

	private final byte negativeNormalized;

	private final byte negativeDenormalized;

	private final byte negativeZero;

	private final byte nan;

	private final byte positiveZero;

	private final byte positiveDenormalized;

	private final byte positiveNormalized;

	private final byte positiveInfinite;

	public byte getNegativeInfinite() {
		return negativeInfinite;
	}

	public byte getNegativeNormalized() {
		return negativeNormalized;
	}

	public byte getNegativeDenormalized() {
		return negativeDenormalized;
	}

	public byte getNegativeZero() {
		return negativeZero;
	}

	public byte getNan() {
		return nan;
	}

	public byte getPositiveZero() {
		return positiveZero;
	}

	public byte getPositiveDenormalized() {
		return positiveDenormalized;
	}

	public byte getPositiveNormalized() {
		return positiveNormalized;
	}

	public byte getPositiveInfinite() {
		return positiveInfinite;
	}

	public int getTotalCount() {
		return this.negativeInfinite + this.negativeNormalized
				+ this.negativeDenormalized + this.negativeZero + this.nan
				+ this.positiveZero + this.positiveDenormalized
				+ this.positiveNormalized + this.positiveInfinite;
	}

	public FloatKindPredicate(int id, AbstractSite site, int negativeInfinite,
			int negativeNormalized, int negativeDenormalized, int negativeZero,
			int nan, int positiveZero, int positiveDenormalized,
			int positiveNormalized, int positiveInfinite) {
		super(id, site);
		this.negativeInfinite = normalize(negativeInfinite);
		this.negativeNormalized = normalize(negativeNormalized);
		this.negativeDenormalized = normalize(negativeDenormalized);
		this.negativeZero = normalize(negativeZero);

		this.nan = normalize(nan);

		this.positiveZero = normalize(positiveZero);
		this.positiveDenormalized = normalize(positiveDenormalized);
		this.positiveNormalized = normalize(positiveNormalized);
		this.positiveInfinite = normalize(positiveInfinite);
	}

	@Override
	protected String getScheme() {
		return "float-kinds";
	}

	@Override
	protected void toSpecificString(StringBuilder builder) {
		builder.append("-Inf = ").append(this.negativeInfinite)
				.append(", negative-normalized = ").append(this.negativeNormalized)
				.append(", negative-denormalized = ").append(this.negativeDenormalized)
				.append(", negative-zero = ").append(this.negativeZero)
				.append(", nan = ").append(this.nan).append(", positive-zero = ")
				.append(this.positiveZero).append(", positive-denormalized = ")
				.append(this.positiveDenormalized).append(", positive-normalized = ")
				.append(this.positiveNormalized).append(", +Inf = ")
				.append(this.positiveInfinite);
	}

}
