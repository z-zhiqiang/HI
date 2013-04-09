package zuo.processor.functionentry.processor;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

/**
 * 
 * @author sunzzq
 *
 */
public class SelectingProcessor {
	private final FunctionEntryProfile[] profiles;
	private Map<FunctionEntrySite, FrequencyValue> frequencyMap;
	private int totalNegative;
	private int totalPositive;
	
	public SelectingProcessor(FunctionEntryProfile[] profiles){
		this.profiles = profiles;
		this.frequencyMap = new HashMap<FunctionEntrySite, FrequencyValue>();
		this.totalNegative = 0;
		this.totalPositive = 0;
	}
	
	public void process(){
		computeTotalPositiveNegative();
		computeFrequencyPair();
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
	
	/**compute the frequency pair <F(m), S(m)> of each function m
	 * 
	 */
	private void computeFrequencyPair(){
		for(FunctionEntryProfile profile: profiles){
			for(FunctionEntryItem item: profile.getFunctionEntryItems()){
				FunctionEntrySite function = item.getSite();
				if(item.getCounter() > 0){
					if(frequencyMap.containsKey(function)){
						if(profile.isCorrect()){
							frequencyMap.get(function).increasePositive();
						}
						else{
							frequencyMap.get(function).increaseNegative();
						}
					}
					else{
						if(profile.isCorrect()){
							frequencyMap.put(function, new FrequencyValue(0, 1));
						}
						else{
							frequencyMap.put(function, new FrequencyValue(1, 0));
						}
					}
				}
				else{
					if(!frequencyMap.containsKey(function)){
						frequencyMap.put(function, new FrequencyValue(0, 0));
					}
				}
			}
		}
		
		//set the f-score and specificity
		for(FunctionEntrySite site: frequencyMap.keySet()){
			FrequencyValue p = frequencyMap.get(site);
			frequencyMap.get(site).setF_score(F_score(p.getNegative(), p.getPositive()));
			frequencyMap.get(site).setPrecision(Precision(p.getNegative(), p.getPositive()));
		}
	}
	
	
	/**calculate the F-score of method: 
	 * 	F_score(m)=2/(1/Increase*(m) + 1/(log(F(m)/log(TotalNegative))))
	 * 	Increase*(m)=F(m)/(F(m)+S(m))
	 * @param pair
	 * @return
	 */
	private double F_score(int neg, int pos){
		if(neg <= 1){
			return 0;
		}
		return (double) 2/(1 + ((double) pos / neg) + (Math.log(totalNegative) / Math.log(neg)));
	}
	
	/**calculate the Precision of method
	 * @param neg
	 * @param pos
	 * @return
	 */
	private double Precision(int neg, int pos){
		if(neg + pos == 0)
			return 0;
		return (double) neg / (neg + pos);
	}
	
	public Map<FunctionEntrySite, FrequencyValue> getFrequencyMap() {
		return frequencyMap;
	}

	public void setFrequencyMap(Map<FunctionEntrySite, FrequencyValue> frequencyMap) {
		this.frequencyMap = frequencyMap;
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



	public static class FrequencyValue{
		int negative;
		int positive;
		double precision;
		double f_score;
		
		public FrequencyValue(){
			this.negative = 0;
			this.positive = 0;
			this.precision = 0;
			this.f_score = 0;
		}
		
		public FrequencyValue(int n, int p){
			this.negative = n;
			this.positive = p;
			this.precision = 0;
			this.f_score = 0;
		}
		
//		public String toString(){
//			return "F:" + negative + "\t\tS:" + positive + "\t\tF_1:" + new DecimalFormat("#.#####").format(this.f_score);
//		}
		public String toString(){
			return String.format("%-20s", "F:" + negative) + String.format("%-20s", "S:" + positive) + String.format("%-20s", "Pre:" + new DecimalFormat("#.#####").format(this.precision)) + String.format("%-20s", "F_1:" + new DecimalFormat("#.#####").format(this.f_score));
		}
		
//		public String toStringByFScore(){
//			StringBuilder builder = new StringBuilder();
//			builder.append("F_score:").append(this.f_score).append("\tF:").append(this.negative).append("\tS:").append(this.positive);
//			return builder.toString();
//		}
//		public String toStringByNegative(){
//			StringBuilder builder = new StringBuilder();
//			builder.append("F:").append(this.negative).append("\tF_score:").append(this.f_score).append("\tS:").append(this.positive);
//			return builder.toString();
//		}
		
		public void increaseNegative(){
			this.negative++;
		}
		
		public void increasePositive(){
			this.positive++;
		}
		
		public int getNegative() {
			return negative;
		}
		public void setNegative(int negative) {
			this.negative = negative;
		}
		public int getPositive() {
			return positive;
		}
		public void setPositive(int positive) {
			this.positive = positive;
		}

		public double getPrecision() {
			return precision;
		}

		public void setPrecision(double precision) {
			this.precision = precision;
		}

		public double getF_score() {
			return f_score;
		}

		public void setF_score(double f_score) {
			this.f_score = f_score;
		}
		
	}

}
