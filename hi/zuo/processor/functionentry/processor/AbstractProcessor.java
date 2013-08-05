package zuo.processor.functionentry.processor;

import zuo.processor.functionentry.profile.FunctionEntryProfile;

public abstract class AbstractProcessor {
	protected final FunctionEntryProfile[] profiles;
	protected int totalNegative;
	protected int totalPositive;
	
	public AbstractProcessor(FunctionEntryProfile[] profiles){
		this.profiles = profiles;
		computeTotalPositiveNegative();
	}

	/**compute the number of passing runs and failing runs
	 * 
	 */
	private void computeTotalPositiveNegative(){
		for (int i = 0; i < profiles.length; i++) {
			if(profiles[i].isCorrect()){
				this.totalPositive++;
			}
			else{
				this.totalNegative++;
			}
		}
		assert(totalNegative + totalPositive == profiles.length);
	}

	public int getTotalNegative() {
		return totalNegative;
	}

	public void setTotalNegative(int totalNegative) {
		this.totalNegative = totalNegative;
	}

	public int getTotalPositive() {
		return totalPositive;
	}

	public void setTotalPositive(int totalPositive) {
		this.totalPositive = totalPositive;
	}

	public FunctionEntryProfile[] getProfiles() {
		return profiles;
	}
	
	
}