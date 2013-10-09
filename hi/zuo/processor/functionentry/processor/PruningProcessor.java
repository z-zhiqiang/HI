package zuo.processor.functionentry.processor;

import java.util.HashMap;
import java.util.Map;

import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;

/**
 * @author Zuo Zhiqiang
 *
 */
public class PruningProcessor{
	private final FunctionEntryProfile[] failingProfiles;
	private Map<FunctionEntrySite, Integer> negativeFrequencyMap;
	
	/**constructor based on profiles
	 * @param failingProfiles
	 */
	public PruningProcessor(FunctionEntryProfile[] failingProfiles){
		this.failingProfiles = failingProfiles;
		negativeFrequencyMap = new HashMap<FunctionEntrySite, Integer>();
	}
	
	public void process(){
		computeFrequency();
	}
	
	private void computeFrequency() {
		// TODO Auto-generated method stub
		for(int i = 0; i < failingProfiles.length; i++){
			FunctionEntryProfile profile = failingProfiles[i];
			assert(!profile.isCorrect());
//			if(!profile.isCorrect()) {
				for (FunctionEntryItem item : profile.getFunctionEntryItems()) {
					FunctionEntrySite functionSite = item.getSite();
					if (item.getCounter() > 0) {
						if (negativeFrequencyMap.containsKey(functionSite)) {
							negativeFrequencyMap.put(functionSite, negativeFrequencyMap.get(functionSite) + 1);
						} else {
							negativeFrequencyMap.put(functionSite, 1);
						}
					} else {
						if (!negativeFrequencyMap.containsKey(functionSite)) {
							negativeFrequencyMap.put(functionSite, 0);
						}
					}
				}
//			}
		}
	}

	public Map<FunctionEntrySite, Integer> getNegativeFrequencyMap() {
		return negativeFrequencyMap;
	}

//	public int getTotalNegative() {
//		return totalNegative;
//	}

}
