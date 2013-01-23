package zuo.processor.functionentry.dataset;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import zuo.processor.functionentry.profile.FunctionEntryItem;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;

public class FunctionEntryDataSet {
	private final Map<Integer, FunctionEntrySite> functionIdMap;
	private final Set<Integer>[] dataset;
	
	public FunctionEntryDataSet(FunctionEntryProfile[] profiles){
		Set[] setArrays = new LinkedHashSet[profiles.length];
		Map<Integer, FunctionEntrySite> map = new LinkedHashMap<Integer, FunctionEntrySite>();
		
		for (int i = 0; i < profiles.length; i++) {
			FunctionEntryProfile profile = profiles[i];
			Set<Integer> itemSet = new LinkedHashSet<Integer>();
			for(FunctionEntryItem item: profile.getFunctionEntryItems()){
				if(item.getCounter() > 0){
					FunctionEntrySite site = item.getSite();
					itemSet.add(site.getId());
					if(!map.containsKey(site.getId())){
						map.put(site.getId(), site);
					}
				}
			}
			setArrays[i] = itemSet;
		}
		
		this.functionIdMap = Collections.unmodifiableMap(map);
		this.dataset = setArrays;
	}

	public Map<Integer, FunctionEntrySite> getFunctionIdMap() {
		return functionIdMap;
	}

	public Set<Integer>[] getDataset() {
		return dataset;
	}
	
	

}
