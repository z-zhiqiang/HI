package zuo.processor.functionentry.processor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;

/**
 * @author Zuo Zhiqiang
 *
 */
public class PruningProcessor {
	private Map<FunctionEntrySite, Integer> negativeFrequencyMap;
	private final int totalNegative;
	
//	/**constructor based on data set
//	 * @param dataset
//	 */
//	public PruningProcessor(FunctionEntryDataSet dataset){
//		Set<Integer>[] itemSets = dataset.getDataset();
//		Map<Integer, FunctionEntrySite> map = dataset.getFunctionIdSiteMap();
//		
//		for(Set<Integer> itemset: itemSets){
//			for(int item: itemset){
//				FunctionEntrySite function = map.get(item);
//				if(negativeFrequencyMap.containsKey(function)){
//					negativeFrequencyMap.put(function, negativeFrequencyMap.get(function) + 1);
//				}
//				else{
//					negativeFrequencyMap.put(function, 1);
//				}
//			}
//		}
//	}
	
	/**constructor based on profiles
	 * @param profiles
	 */
	public PruningProcessor(FunctionEntryProfile[] profiles){
		this.totalNegative = profiles.length;
		
		negativeFrequencyMap = new HashMap<FunctionEntrySite, Integer>();
		for(int i = 0; i < profiles.length; i++){
			FunctionEntryProfile profile = profiles[i];
			for(FunctionEntryItem item: profile.getFunctionEntryItems()){
				FunctionEntrySite function = item.getSite();
				if(item.getCounter() > 0){
					if(negativeFrequencyMap.containsKey(function)){
						negativeFrequencyMap.put(function, negativeFrequencyMap.get(function) + 1);
					}
					else{
						negativeFrequencyMap.put(function, 1);
					}
				}
				else{
					if(!negativeFrequencyMap.containsKey(function)){
						negativeFrequencyMap.put(function, 0);
					}
				}
			}
		}
	}
	
	
//	/**get suspect calls whose frequency is greater than or equal to bound
//	 * @param bound
//	 * @return
//	 */
//	public List<String> getSuspectFunctions(int bound){
//		List<String> susList = new ArrayList<String>();
//		
//		for(FunctionEntrySite function: negativeFrequencyMap.keySet()){
//			if(negativeFrequencyMap.get(function) >= bound){
//				susList.add(function.getNameAndLineNumber());
//			}
//		}
//		
//		return susList;
//	}
	
	
	
	
	public Map<FunctionEntrySite, Integer> getNegativeFrequencyMap() {
		return negativeFrequencyMap;
	}

	public int getTotalNegative() {
		return totalNegative;
	}


//	public static void main(String[] args) {
//		String sitesFile = "/home/sunzzq/Research/space/versions/v7/csites.txt";
//		FunctionEntrySites sites = new FunctionEntrySites(new File(sitesFile));
//		for (String unit: sites.getSites().keySet()) {
//			List<FunctionEntrySite> list = sites.getSites().get(unit);
//			for (FunctionEntrySite functionEntrySite : list) {
//				System.out.println(functionEntrySite.toString());
//			}
//		}
//		System.out.println("\n\n");
//		
//		String profilesFile = "/home/sunzzq/Research/space/traces/v7/coarse-grained";
//		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
//		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(150);
//		for (int i = 0; i < profiles.length; i++) {
//			for(FunctionEntryItem item: profiles[i].getFunctionEntryItems()){
//				System.out.println(item.toString());
//			}
//			System.out.println();
//		}
//		System.out.println("\n\n");
//		
//		PruningProcessor pro = new PruningProcessor(profiles);
//		System.out.println(pro.negativeFrequencyMap);
//		List<String> calls = pro.getSuspectFunctions(153);
//		System.out.println(calls.size());
//		for (int i = 0; i < calls.size(); i++) {
//			System.out.print(" -finclude-function=" + calls.get(i));
//		}
//	}

}
