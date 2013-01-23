package zuo.processor.functionentry.processor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zuo.processor.functionentry.dataset.FunctionEntryDataSet;
import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

/**
 * @author Zuo Zhiqiang
 *
 */
public class PruningProcessor {
	private Map<String, Integer> frequencyMap = new LinkedHashMap<String, Integer>();
	
	/**constructor based on data set
	 * @param dataset
	 */
	public PruningProcessor(FunctionEntryDataSet dataset){
		Set<Integer>[] itemSets = dataset.getDataset();
		Map<Integer, FunctionEntrySite> map = dataset.getFunctionIdMap();
		
		for(Set<Integer> itemset: itemSets){
			for(int item: itemset){
				String function = map.get(item).getFunctionName();
				if(frequencyMap.containsKey(function)){
					frequencyMap.put(function, frequencyMap.get(function) + 1);
				}
				else{
					frequencyMap.put(function, 1);
				}
			}
		}
	}
	
	/**constructor based on profiles
	 * @param profiles
	 */
	public PruningProcessor(FunctionEntryProfile[] profiles){
		for(int i = 0; i < profiles.length; i++){
			FunctionEntryProfile profile = profiles[i];
			for(FunctionEntryItem item: profile.getFunctionEntryItems()){
				if(item.getCounter() > 0){
					String function = item.getSite().getFunctionName();
					if(frequencyMap.containsKey(function)){
						frequencyMap.put(function, frequencyMap.get(function) + 1);
					}
					else{
						frequencyMap.put(function, 1);
					}
				}
			}
		}
	}
	
	
	/**get suspect calls whose frequency is greater than or equal to bound
	 * @param bound
	 * @return
	 */
	public List<String> getSuspectFunctions(int bound){
		List<String> susList = new ArrayList<String>();
		
		for(String function: frequencyMap.keySet()){
			if(frequencyMap.get(function) >= bound){
				susList.add(function);
			}
		}
		
		return susList;
	}
	
	public static void main(String[] args) {
		String sitesFile = "/home/sunzzq/Research/space/versions/v7/csites.txt";
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		for (String unit: sites.getSites().keySet()) {
			List<FunctionEntrySite> list = sites.getSites().get(unit);
			for (FunctionEntrySite functionEntrySite : list) {
				System.out.println(functionEntrySite.toString());
			}
		}
		System.out.println("\n\n");
		
		String profilesFile = "/home/sunzzq/Research/space/traces/v7/coarse-grained";
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles();
		for (int i = 0; i < profiles.length; i++) {
			for(FunctionEntryItem item: profiles[i].getFunctionEntryItems()){
				System.out.println(item.toString());
			}
			System.out.println();
		}
		System.out.println("\n\n");
		
		PruningProcessor pro = new PruningProcessor(profiles);
		System.out.println(pro.frequencyMap);
		System.out.println(pro.getSuspectFunctions(153));
		System.out.println("\n\n");
		
		FunctionEntryDataSet dataset = new FunctionEntryDataSet(profiles);
		for(Set set: dataset.getDataset()){
			System.out.println(set);
		}
		System.out.println();
		for(int key: dataset.getFunctionIdMap().keySet())
			System.out.println(dataset.getFunctionIdMap().get(key));
		
		System.out.println("\n\n");
		pro = new PruningProcessor(dataset);
		System.out.println(pro.frequencyMap);
		System.out.println();
		List<String> calls = pro.getSuspectFunctions(153);
		System.out.println(calls.size());
		for (int i = 0; i < calls.size(); i++) {
			System.out.print(" -finclude-function=" + calls.get(i));
		}
		
//		System.out.println("-finclude-function=" + );
		
	}

}
