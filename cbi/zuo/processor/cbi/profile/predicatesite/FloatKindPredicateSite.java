package zuo.processor.cbi.profile.predicatesite;

import zuo.processor.cbi.site.InstrumentationSites.AbstractSite;


public class FloatKindPredicateSite extends AbstractPredicateSite {

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

	public FloatKindPredicateSite(int id, AbstractSite site, int negativeInfinite,
			int negativeNormalized, int negativeDenormalized, int negativeZero,
			int nan, int positiveZero, int positiveDenormalized,
			int positiveNormalized, int positiveInfinite) {
		super(id, site);
		this.negativeInfinite = this.normalize(negativeInfinite);
		this.negativeNormalized = this.normalize(negativeNormalized);
		this.negativeDenormalized = this.normalize(negativeDenormalized);
		this.negativeZero = this.normalize(negativeZero);

		this.nan = this.normalize(nan);

		this.positiveZero = this.normalize(positiveZero);
		this.positiveDenormalized = this.normalize(positiveDenormalized);
		this.positiveNormalized = this.normalize(positiveNormalized);
		this.positiveInfinite = this.normalize(positiveInfinite);
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
