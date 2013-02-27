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
	public static class InfoValue{ 
		int numSites;
		int numPredicates;
		HashSet<AbstractSite> sitesSet;
		
		public InfoValue(int numSites, int numPredicates, HashSet<AbstractSite> sSet) {
			super();
			this.numSites = numSites;
			this.numPredicates = numPredicates;
			this.sitesSet = sSet;
		}

		public int getNumSites() {
			return numSites;
		}

		public void setNumSites(int numSites) {
			this.numSites = numSites;
		}

		public int getNumPredicates() {
			return numPredicates;
		}

		public void setNumPredicates(int numPredicates) {
			this.numPredicates = numPredicates;
		}

		public HashSet<AbstractSite> getSitesSet() {
			return sitesSet;
		}

		public void setSitesSet(HashSet<AbstractSite> sitesSet) {
			this.sitesSet = sitesSet;
		}

		public void increaseNumSites(int i){
			this.numSites += i;
		}
		public void increaseNumPredicates(int i){
			this.numPredicates += i;
		}
		
	}

	
	final InstrumentationSites sites;
	int numPredicateSites;
	int numPredicateItems;
	Map<String, InfoValue> map;
	
	public SitesInfo(InstrumentationSites sites){
		this.sites = sites;
		this.numPredicateSites = 0;
		this.numPredicateItems = 0;
		map = new HashMap<String, InfoValue>();
		
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
					assert(map.get(method).getSitesSet().add(site));
					map.get(method).increaseNumSites(1);
					map.get(method).increaseNumPredicates(2);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new InfoValue(1, 2, new HashSet<AbstractSite>()));
					map.get(method).getSitesSet().add(site);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
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
					assert(map.get(method).getSitesSet().add(site));
					map.get(method).increaseNumSites(1);
					map.get(method).increaseNumPredicates(9);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new InfoValue(1, 9, new HashSet<AbstractSite>()));
					map.get(method).getSitesSet().add(site);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
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
					assert(map.get(method).getSitesSet().add(site));
					map.get(method).increaseNumSites(1);
					map.get(method).increaseNumPredicates(6);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new InfoValue(1, 6, new HashSet<AbstractSite>()));
					map.get(method).getSitesSet().add(site);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
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
					assert(map.get(method).getSitesSet().add(site));
					map.get(method).increaseNumSites(1);
					map.get(method).increaseNumPredicates(6);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
				}
				else{
					map.put(method, new InfoValue(1, 6, new HashSet<AbstractSite>()));
					map.get(method).getSitesSet().add(site);
					assert(map.get(method).getSitesSet().iterator().next().getFileName().equals(site.getFileName()));
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

	public Map<String, InfoValue> getMap() {
		return map;
	}

	public void setMap(Map<String, InfoValue> map) {
		this.map = map;
	}

	public InstrumentationSites getSites() {
		return sites;
	}
	
	
	
}
