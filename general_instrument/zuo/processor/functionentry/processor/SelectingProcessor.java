package zuo.processor.functionentry.processor;

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
	private Map<FunctionEntrySite, FrequencyPair> frequencyMap;
	private int totalNegative;
	private int totalPositive;
	
	public SelectingProcessor(FunctionEntryProfile[] profiles){
		this.profiles = profiles;
		this.frequencyMap = new HashMap<FunctionEntrySite, FrequencyPair>();
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
				if(item.getCounter() > 0){
					FunctionEntrySite function = item.getSite();
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
							frequencyMap.put(function, new FrequencyPair(0, 1));
						}
						else{
							frequencyMap.put(function, new FrequencyPair(1, 0));
						}
					}
				}
			}
		}
	}
	
	
	/**calculate the approximation of Importance: 
	 * 	Importance*(m)=2/(1/Increase*(m) + 1/(log(F(m)/log(TotalNegative))))
	 * 	Increase*(m)=F(m)/(F(m)+S(m))
	 * @param pair
	 * @return
	 */
	public double Importance_A(FrequencyPair pair){
		if(pair.getNegative() <= 1)
			return 0;
		return 2/(1 + ((double) pair.getPositive() / pair.getNegative()) + (Math.log(totalNegative) / Math.log(pair.getNegative())));
	}
	
	
//	public static void main(String[] args) {
//		String sitesFile = "/home/sunzzq/Research/grep/versions/v1/v1_c.sites";
//		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
//		
//		String profilesFile = "/home/sunzzq/Research/grep/traces/v1/coarse-grained";
//		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
//		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(685);
//		
//		SelectingProcessor processor = new SelectingProcessor(profiles);
//		processor.process();
//		System.out.println(processor.totalNegative);
//		System.out.println(processor.totalPositive);
//		for(FunctionEntrySite site: processor.frequencyMap.keySet()){
//			System.out.println(site.toString() + "\n\t" + processor.frequencyMap.get(site).toString() + "\t\t" + processor.Importance_A(processor.frequencyMap.get(site)));
//		}
//	}
	
	
	
	public Map<FunctionEntrySite, FrequencyPair> getFrequencyMap() {
		return frequencyMap;
	}

	public void setFrequencyMap(Map<FunctionEntrySite, FrequencyPair> frequencyMap) {
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



	public static class FrequencyPair{
		int negative;
		int positive;
		
		public FrequencyPair(){
			this.negative = 0;
			this.positive = 0;
		}
		
		public FrequencyPair(int n, int p){
			this.negative = n;
			this.positive = p;
		}
		
		public String toString(){
			return "F: " + negative + ", S: " + positive;
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
	}

}
