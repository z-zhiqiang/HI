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
public class PruningProcessor{
	private final FunctionEntryProfile[] profiles;
	private int totalNegative;
	private int totalPositive;
	private Map<FunctionEntrySite, FrequencyValue> frequencyMap;
	
	private int numberofTFFunctions;//the number of functions whose f(m)==F
	
	public PruningProcessor(FunctionEntryProfile[] profiles){
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
			frequencyMap.get(site).setDS(DS(p.getNegative(), p.getPositive(), totalNegative, totalPositive));
			if(p.getNegative() >= this.totalNegative){
				this.numberofTFFunctions++;
			}
		}
	}
	
	private static double DS(int neg, int pos, int totalNeg, int totalPos){
		if((double)(neg / totalNeg) <= (double)(pos / totalPos)){
			return 0;
		}
		else{
			return IG(neg, pos, totalNeg, totalPos);
		}
	}
	
	private static double IG(int neg, int pos, int totalNeg, int totalPos) {
		// TODO Auto-generated method stub
		int total = totalNeg + totalPos;
		return H(totalNeg, totalPos) - (neg + pos) * H(neg, pos) / total - (total - neg - pos) * H(totalNeg - neg, totalPos - pos) / total;
		
	}

	private static double H(int neg, int pos) {
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

	public int getNumberofTFFunctions() {
		return numberofTFFunctions;
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
