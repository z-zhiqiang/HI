package zuo.processor.cbi.site;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import zuo.processor.cbi.site.InstrumentationSites.AbstractSite;
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.FloatKindSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;

public class SitesInfo {
	final InstrumentationSites sites;
	int numPredicateSites;
	int numPredicateItems;
	Map<String, HashSet<AbstractSite>> map;
	
	public SitesInfo(InstrumentationSites sites){
		this.sites = sites;
		this.numPredicateSites = 0;
		this.numPredicateItems = 0;
		map = new HashMap<String, HashSet<AbstractSite>>();
		
		collectInfo();
	}
	
	private void collectInfo(){
		//branchSites
		for(Entry<String, List<BranchSite>> entry: sites.getBranchSites().entrySet()){
			numPredicateItems += entry.getValue().size() * 2;
			numPredicateSites += entry.getValue().size();
			
			for(BranchSite site: entry.getValue()){
				String method = site.getFunctionName();
				
				if(map.containsKey(method)){
					assert(map.get(method).add(site));
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new HashSet<AbstractSite>());
					map.get(method).add(site);
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
			}
		}
		//floatkindSites
		for(Entry<String, List<FloatKindSite>> entry: sites.getFloatSites().entrySet()){
			numPredicateItems += entry.getValue().size() * 9;
			numPredicateSites += entry.getValue().size();
			
			for(FloatKindSite site: entry.getValue()){
				String method = site.getFunctionName();
				
				if(map.containsKey(method)){
					assert(map.get(method).add(site));
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new HashSet<AbstractSite>());
					map.get(method).add(site);
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
			}
		}
		//returnSites
		for(Entry<String, List<ReturnSite>> entry: sites.getReturnSites().entrySet()){
			numPredicateItems += entry.getValue().size() * 6;
			numPredicateSites += entry.getValue().size();
			
			for(ReturnSite site: entry.getValue()){
				String method = site.getFunctionName();
				
				if(map.containsKey(method)){
					assert(map.get(method).add(site));
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new HashSet<AbstractSite>());
					map.get(method).add(site);
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
			}
		}
		//scalarSites
		for(Entry<String, List<ScalarSite>> entry: sites.getScalarSites().entrySet()){
			numPredicateItems += entry.getValue().size() * 6;
			numPredicateSites += entry.getValue().size();
			
			for(ScalarSite site: entry.getValue()){
				String method = site.getFunctionName();
				
				if(map.containsKey(method)){
					assert(map.get(method).add(site));
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new HashSet<AbstractSite>());
					map.get(method).add(site);
					assert(map.get(method).iterator().next().getFileName().equals(site.getFileName()));
				}
			}
		}
	}

	public int getNumPredicateSites() {
		return numPredicateSites;
	}

	public void setNumPredicateSites(int numPredicateSites) {
		this.numPredicateSites = numPredicateSites;
	}

	public int getNumPredicateItems() {
		return numPredicateItems;
	}

	public void setNumPredicateItems(int numPredicateItems) {
		this.numPredicateItems = numPredicateItems;
	}

	public Map<String, HashSet<AbstractSite>> getMap() {
		return map;
	}

	public void setMap(Map<String, HashSet<AbstractSite>> map) {
		this.map = map;
	}

	public InstrumentationSites getSites() {
		return sites;
	}
	
	
	
}
