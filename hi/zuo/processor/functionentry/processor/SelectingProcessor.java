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
		
		//get C-score matrix
		double C[][] = computeCMatrix(totalNegative, totalPositive);
		//just for debugging
		printout(C);
		//set the f-score and C-score
		for(FunctionEntrySite site: frequencyMap.keySet()){
			FrequencyValue p = frequencyMap.get(site);
			frequencyMap.get(site).setH_1(H_1(p.getNegative(), totalPositive, totalNegative));
			frequencyMap.get(site).setH_2(H_2(p.getNegative(), p.getPositive(), totalNegative));
			frequencyMap.get(site).setF_score(F_score(p.getNegative(), p.getPositive(), totalNegative));
			frequencyMap.get(site).setC_score(C[p.getNegative()][p.getPositive()]);
		}
	}
	
	private void printout(double[][] c) {
		// TODO Auto-generated method stub
		System.out.println(totalNegative);
		System.out.println(totalPositive);
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
	 * 	F_score(m)=2/(1/Precision(m) + 1/(log(F(m)/log(TotalNegative))))
	 * 	Precision(m)=F(m)/(F(m)+S(m))
	 * @param pair
	 * @return
	 */
	public static double F_score(int neg, int pos, int totalNegative){
		if(neg <= 1){
			return 0;
		}
		return (double) 2/(1 + ((double) pos / neg) + (Math.log(totalNegative) / Math.log(neg)));
	}
	

	/**compute C-score matrix by dynamic programming
	 * @param totalNeg
	 * @param totalPos
	 * @return
	 */
	private static double[][] computeCMatrix(int totalNeg, int totalPos) {
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
	private static double max(double d1, double d2, double d3){
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


	public int computeCBIBound(double threshold){
		if(DH(2, totalPositive) <= 0){
			throw new RuntimeException("abnormal case 1");
//			return 2;
		} 
		else {
			double h_1_2 = H_1(2, totalPositive, totalNegative);
			double h_1_F = H_1(totalNegative, totalPositive, totalNegative);
			if(DH(totalNegative, totalPositive) < 0){
				int f0 = compute_f0(totalPositive);
				
				double h_1_f0 = H_1(f0, totalPositive, totalNegative);
				double h_1_f1 = H_1(f0 + 1, totalPositive, totalNegative);
				double max = (h_1_f0 > h_1_f1 ? h_1_f0 : h_1_f1);
				double min = (h_1_f0 < h_1_f1 ? h_1_f0 : h_1_f1);
				if(threshold > max){
//					throw new RuntimeException("OutOfRange Error case 2: " + threshold + ">" + max);
					System.out.println("OutOfRange case 2: " + threshold + ">" + max);
					return totalNegative + 1;
				}
				int lb = 2, ub = totalNegative;
				if(threshold > h_1_f0){
					lb = f0 + 1;
				}
				else if(threshold >= h_1_2 && threshold <= h_1_f0){
					lb = calculateHBoundIn(2, f0, threshold);
				}
				if(threshold > h_1_f1){
					ub = f0;
				}
				else if(threshold >= h_1_F && threshold <= h_1_f1){
					ub = calculateHBoundDe(f0 + 1, totalNegative, threshold);
				}
//				System.out.println("[" + lb + "," + ub + "]");
				return lb;
			}
			else{
				int lb = 2;
				if(threshold > h_1_F){
//					throw new RuntimeException("OutOfRange Error case 3");
					System.out.println("OutOfRange case 3");
					return totalNegative + 1;
				}
				if(threshold > h_1_2){
					lb = calculateHBoundIn(2, totalNegative, threshold);
				}
//				System.out.println("[" + lb + ",F]");
				return lb;
			}
		}
	}
	
	/** derivative of H score
	 * @param f
	 * @param p: for H_1, p==P
	 * @return
	 */
	public double DH(int f, int p){
		if(f <= 1 || p == 0){
			return 0;
		}
		return Math.log(totalNegative) / (Math.log(f) * f * Math.log(f)) - (double) 1 / p;
	}
	
	public int compute_f0(int p) {
		int start, end, mid;
		start = 2;
		end = totalNegative;
		while(start < end){
			mid = (start + end) / 2;
			double dh = DH(mid, p);
//			System.out.println(mid + ": " + dh);
			if(dh > 0){
				start = mid;
			}
			else if(dh < 0){
				end = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return start;
			}
		}
		
		return -1;
	}
	
	private int calculateHBoundIn(int s, int e, double threshold){
		if(threshold < H_1(s, totalPositive, totalNegative) || threshold > H_1(e, totalPositive, totalNegative)){
			throw new RuntimeException("The threshold should be in the following range: [" + H_1(s, totalPositive, totalNegative) + ", " + H_1(e, totalPositive, totalNegative) + "]");
		}
		
		int start, end, mid;
		start = s;
		end = e;
		if(H_1(start, totalPositive, totalNegative) == threshold){
			return start;
		}
		if(H_1(end, totalPositive, totalNegative) == threshold){
			return end;
		}
		while(start < end){
			mid = (start + end) / 2;
			double g = H_1(mid, totalPositive, totalNegative);
//			System.out.println(mid + ": " + g);
			if(g > threshold){
				end = mid;
			}
			else if(g < threshold){
				start = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return end;
			}
		}
		return -1;
	}

	private int calculateHBoundDe(int s, int e, double threshold){
		if(threshold < H_1(e, totalPositive, totalNegative) || threshold > H_1(s, totalPositive, totalNegative)){
			throw new RuntimeException("The threshold should be in the following range: [" + H_1(e, totalPositive, totalNegative) + ", " + H_1(s, totalPositive, totalNegative) + "]");
		}
		
		int start, end, mid;
		start = s;
		end = e;
		if(H_1(start, totalPositive, totalNegative) == threshold){
			return start;
		}
		if(H_1(end, totalPositive, totalNegative) == threshold){
			return end;
		}
		while(start < end){
			mid = (start + end) / 2;
			double h = H_1(mid, totalPositive, totalNegative);
//			System.out.println(mid + ": " + h);
			if(h > threshold){
				start = mid;
			}
			else if(h < threshold){
				end = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return start;
			}
		}
		return -1;
	}
	

	public static class FrequencyValue{
		int negative;
		int positive;
		double h_1;
		double h_2;
		double f_score;
		double c_score;
		
		public FrequencyValue(){
			this.negative = 0;
			this.positive = 0;
			this.h_1 = 0;
			this.h_2 = 0;
			this.f_score = 0;
			this.c_score = 0;
		}
		
		public FrequencyValue(int n, int p){
			this.negative = n;
			this.positive = p;
			this.h_1 = 0;
			this.h_2 = 0;
			this.f_score = 0;
			this.c_score = 0;
		}
		
		public String toString(){
			return String.format("%-10s", "F:" + negative) + String.format("%-10s", "S:" + positive) 
					+ String.format("%-15s", "H_1:" + new DecimalFormat("#.###").format(this.h_1))
					+ String.format("%-15s", "H_2:" + new DecimalFormat("#.###").format(this.h_2))
					+ String.format("%-15s", "F_1:" + new DecimalFormat("#.###").format(this.f_score))
					+ String.format("%-15s", "C:" + new DecimalFormat("#.###").format(this.c_score)) 
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
		
		public int getPositive() {
			return positive;
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

		public double getC_score() {
			return c_score;
		}

		public void setC_score(double c_score) {
			this.c_score = c_score;
		}

		
	}

}
