package zuo.processor.functionentry.processor;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.cbi.processor.Processor;
import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;

/**
 * 
 * @author sunzzq
 *
 */
public class SelectingProcessor_pldi2016{
	private final FunctionEntryProfile[] profiles;
	private int totalNegative;
	private int totalPositive;
	private Map<FunctionEntrySite, FrequencyValue> frequencyMap;
	
	private final double[][] C_matrix;
	
	public SelectingProcessor_pldi2016(FunctionEntryProfile[] profiles){
		this.profiles = profiles;
		this.frequencyMap = new HashMap<FunctionEntrySite, FrequencyValue>();
		computeTotals();
		
		this.C_matrix = computeCMatrix(this.totalNegative, this.totalPositive);
	}
	
	public void process(){
		computeFrequencyPair();
	}
	
	private void computeTotals(){
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
	}
	
	/**compute the frequency pair <F(m), S(m)> of each function m
	 * 
	 */
	private void computeFrequencyPair(){
		//set C-score
		for(FunctionEntrySite site: frequencyMap.keySet()){
			FrequencyValue p = frequencyMap.get(site);
			frequencyMap.get(site).setC_score(this.C_matrix[p.getNegative()][p.getPositive()]);
		}
	}
	
	/**compute C-score matrix by dynamic programming
	 * @param totalNeg
	 * @param totalPos
	 * @return
	 */
	public static double[][] computeCMatrix(int totalNeg, int totalPos) {
		double[][] C = new double[totalNeg + 1][totalPos + 1];
		
		C[0][0] = getMaximum(0, 0, totalNeg, totalPos);
		for(int i = 1; i <= totalNeg; i++){
			double max = getMaximum(i, 0, totalNeg, totalPos);
			C[i][0] = max > C[i - 1][0] ? max : C[i - 1][0];
		}
		for(int j = 1; j <= totalPos; j++){
			double max = getMaximum(0, j, totalNeg, totalPos);
			C[0][j] = max > C[0][j - 1] ? max : C[0][j - 1];
		}
		for(int i = 1; i <= totalNeg; i++){
			for(int j = 1; j <= totalPos; j++){
				double max = getMaximum(i, j, totalNeg, totalPos);
				C[i][j] = max(max, C[i - 1][j], C[i][j - 1]);
			}
		}
		return C;
	}
	
	/**return the maximum among d1, d2 and d3
	 * @param d1
	 * @param d2
	 * @param d3
	 * @return
	 */
	public static double max(double d1, double d2, double d3){
		if(d1 > d2){
			if(d1 > d3){
				return d1;
			}
			else{
				return d3;
			}
		}
		else{
			if(d2 > d3){
				return d2;
			}
			else{
				return d3;
			}
		}
	}
	
	private static double getMaximum(int neg, int pos, int totalNeg, int totalPos){
		double max = 0;
		for(int i = 0; i <= neg; i++){
			for(int j = 0; j <= pos; j++){
				double im = Processor.importance(i, j, neg, pos, totalNeg, totalPos);
				if(im > max){
					max = im; 
				}
			}
		}
		return max;
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
		double c_score;
		
		public FrequencyValue(){
			this.negative = 0;
			this.positive = 0;
			this.c_score = 0;
		}
		
		public FrequencyValue(int n, int p){
			this.negative = n;
			this.positive = p;
			this.c_score = 0;
		}
		
		public String toString(){
			return String.format("%-10s", "F:" + negative) + String.format("%-10s", "S:" + positive) 
					+ String.format("%-15s", "C:" + new DecimalFormat("#.###").format(this.c_score));
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
		
		public int getPositive() {
			return positive;
		}

		public double getC_score() {
			return c_score;
		}

		public void setC_score(double c_score) {
			this.c_score = c_score;
		}
	}

}
