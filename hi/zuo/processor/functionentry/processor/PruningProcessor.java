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
	private final int actualTotalPositive; // the actually total number of positive runs
	private final FunctionEntryProfile[] profiles;
	private int totalNegative;
	private int totalPositive;
	private Map<FunctionEntrySite, FrequencyValue> frequencyMap;
	
	private int numberofTFFunctions;//the number of functions whose f(m)==F
	
	public PruningProcessor(FunctionEntryProfile[] profiles, int actualTotalPos){
		this.actualTotalPositive = actualTotalPos;
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
		
		//set the DS
		for(FunctionEntrySite site: frequencyMap.keySet()){
			FrequencyValue p = frequencyMap.get(site);
			frequencyMap.get(site).setC_r(DS(p.getNegative(), p.getPositive(), totalNegative, totalPositive));
			frequencyMap.get(site).setC_p(IG(p.getNegative(), 0, totalNegative, this.actualTotalPositive));
			if(p.getNegative() >= this.totalNegative){
				this.numberofTFFunctions++;
			}
		}
	}
	
	public static double DS(int neg, int pos, int totalNeg, int totalPos){
		if((double) neg / totalNeg <= (double) pos / totalPos){
			return 0;
		}
		else{
			return IG(neg, pos, totalNeg, totalPos);
		}
	}
	
	public static double IG(int neg, int pos, int totalNeg, int totalPos) {
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
		double C_r;
		double C_p;
		
		public FrequencyValue(){
			this.negative = 0;
			this.positive = 0;
			this.C_r = 0;
			this.C_p = 0;
		}
		
		public FrequencyValue(int n, int p){
			this.negative = n;
			this.positive = p;
			this.C_r = 0;
			this.C_p = 0;
		}
		
		public String toString(){
			return String.format("%-10s", "neg:" + negative) + String.format("%-10s", "pos:" + positive) 
					+ String.format("%-15s", "C_r:" + new DecimalFormat("#.###").format(this.C_r))
					+ String.format("%-15s", "C_p:" + new DecimalFormat("#.###").format(this.C_p))
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
		public double getC_r() {
			return C_r;
		}
		public void setC_r(double dS) {
			C_r = dS;
		}

		public double getC_p() {
			return C_p;
		}

		public void setC_p(double c_p) {
			C_p = c_p;
		}

		
	}

}
