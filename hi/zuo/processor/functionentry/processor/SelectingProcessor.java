package zuo.processor.functionentry.processor;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.functionentry.processor.PruningProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;

/**
 * 
 * @author sunzzq
 *
 */
public class SelectingProcessor{
	private final FunctionEntryProfile[] profiles;
	private int totalNegative;
	private int totalPositive;
	private Map<FunctionEntrySite, FrequencyValue> frequencyMap;
	
	public SelectingProcessor(FunctionEntryProfile[] profiles){
		this.profiles = profiles;
		this.frequencyMap = new HashMap<FunctionEntrySite, FrequencyValue>();
	}
	
	public void process(){
		computeFrequencyPair();
	}
	
	/**compute the frequency pair <F(m), S(m)> of each function m
	 * 
	 */
	private void computeFrequencyPair(){
		for(FunctionEntryProfile profile: profiles){
			if(profile.isCorrect()){
				this.totalPositive++;
				for(FunctionEntryItem item: profile.getFunctionEntryItems()){
					FunctionEntrySite function = item.getSite();
					if(item.getCounter() > 0){
						if(frequencyMap.containsKey(function)){
							frequencyMap.get(function).increasePositive();
						}
						else{
							frequencyMap.put(function, new FrequencyValue(0, 1));
						}
					}
					else{
						if(!frequencyMap.containsKey(function)){
							frequencyMap.put(function, new FrequencyValue(0, 0));
						}
					}
				}
			}
			else{
				this.totalNegative++;
				for(FunctionEntryItem item: profile.getFunctionEntryItems()){
					FunctionEntrySite function = item.getSite();
					if(item.getCounter() > 0){
						if(frequencyMap.containsKey(function)){
							frequencyMap.get(function).increaseNegative();
						}
						else{
							frequencyMap.put(function, new FrequencyValue(1, 0));
						}
					}
					else{
						if(!frequencyMap.containsKey(function)){
							frequencyMap.put(function, new FrequencyValue(0, 0));
						}
					}
				}
			}
		}
		
		//set the f-score and specificity
		for(FunctionEntrySite site: frequencyMap.keySet()){
			FrequencyValue p = frequencyMap.get(site);
			frequencyMap.get(site).setF_score(F_score(p.getNegative(), p.getPositive(), totalNegative));
			frequencyMap.get(site).setPrecision(Precision(p.getNegative(), p.getPositive()));
			frequencyMap.get(site).setH_1(H_1(p.getNegative(), totalPositive, totalNegative));
			frequencyMap.get(site).setH_2(H_2(p.getNegative(), p.getPositive(), totalNegative));
		}
	}
	
	public static double H_1(int neg, int totalPositive, int totalNegative){
		if(neg <= 1){
			return 0;
		}
		return (double) 2/(1 + ((double) neg / totalPositive) + (Math.log(totalNegative) / Math.log(neg)));
	}
	
	private double H_2(int neg, int pos, int totalNegative){
		if(neg <= 1 || pos == 0){
			return 0;
		}
		return (double) 2/(1 + ((double) neg / pos) + (Math.log(totalNegative) / Math.log(neg)));
	}
	
	/**calculate the F-score of method: 
	 * 	F_score(m)=2/(1/Increase*(m) + 1/(log(F(m)/log(TotalNegative))))
	 * 	Increase*(m)=F(m)/(F(m)+S(m))
	 * @param pair
	 * @return
	 */
	public static double F_score(int neg, int pos, int totalNegative){
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
	public static double Precision(int neg, int pos){
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

	public int getTotalPositive() {
		return totalPositive;
	}



	public static class FrequencyValue{
		int negative;
		int positive;
		double precision;
		double f_score;
		double h_1;
		double h_2;
		
		public FrequencyValue(){
			this.negative = 0;
			this.positive = 0;
			this.precision = 0;
			this.f_score = 0;
			this.h_1 = 0;
			this.h_2 = 0;
		}
		
		public FrequencyValue(int n, int p){
			this.negative = n;
			this.positive = p;
			this.precision = 0;
			this.f_score = 0;
			this.h_1 = 0;
			this.h_2 = 0;
		}
		
		public String toString(){
			return String.format("%-10s", "F:" + negative) + String.format("%-10s", "S:" + positive) 
					+ String.format("%-15s", "Pre:" + new DecimalFormat("#.###").format(this.precision)) 
					+ String.format("%-15s", "F_1:" + new DecimalFormat("#.###").format(this.f_score))
					+ String.format("%-15s", "H_1:" + new DecimalFormat("#.###").format(this.h_1))
					+ String.format("%-15s", "H_2:" + new DecimalFormat("#.###").format(this.h_2))
					;
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

		public double getH_1() {
			return h_1;
		}

		public void setH_1(double h_1) {
			this.h_1 = h_1;
		}

		public double getH_2() {
			return h_2;
		}

		public void setH_2(double h_2) {
			this.h_2 = h_2;
		}
		
	}

}
