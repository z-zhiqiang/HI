package zuo.processor.functionentry.processor;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;

/**
 * 
 * @author sunzzq
 *
 */
public class PruningDSProcessor{
	private final FunctionEntryProfile[] profiles;
	private final int totalNegative;
	private final int totalPositive;
	private Map<FunctionEntrySite, FrequencyValue> frequencyMap;
	
	public PruningDSProcessor(int totalNeg, int totalPos, FunctionEntryProfile[] profiles){
		this.totalNegative = totalNeg;
		this.totalPositive = totalPos;
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
			frequencyMap.get(site).setDS(DS(p.getNegative(), p.getPositive(), totalNegative, totalPositive));
		}
	}
	
	private double DS(int neg, int pos, int totalNeg, int totalPos){
		if((double)(neg / totalNeg) <= (double)(pos / totalPos)){
			return 0;
		}
		else{
			return IG(neg, pos, totalNeg, totalPos);
		}
	}
	
	private double IG(int neg, int pos, int totalNeg, int totalPos) {
		// TODO Auto-generated method stub
		int total = totalNeg + totalPos;
		return H(totalNeg, totalPos) - (neg + pos) / total * H(neg, pos) - (total - neg - pos) / total * H(totalNeg - neg, totalPos - pos);
		
	}

	private double H(int neg, int pos) {
		// TODO Auto-generated method stub
		if(neg * pos == 0){
			return 0;
		}
		else{
			return ((neg + pos) * Math.log(neg + pos) - neg * Math.log(neg) - pos * Math.log(pos)) / (Math.log(2) * (neg + pos));
		}
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
		double DS;
		
		public FrequencyValue(){
			this.negative = 0;
			this.positive = 0;
			this.DS = 0;
		}
		
		public FrequencyValue(int n, int p){
			this.negative = n;
			this.positive = p;
			this.DS = 0;
		}
		
		public String toString(){
			return String.format("%-10s", "F:" + negative) + String.format("%-10s", "S:" + positive) 
					+ String.format("%-15s", "DS:" + new DecimalFormat("#.###").format(this.DS))
					;
		}
		
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
		public double getDS() {
			return DS;
		}
		public void setDS(double dS) {
			DS = dS;
		}

		
	}

}
