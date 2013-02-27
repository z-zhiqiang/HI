package zuo.processor.cbi.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.InstrumentationSites.AbstractSite;
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.FloatKindSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;
import zuo.processor.cbi.site.SitesInfo;

public class CBIClient {
	final int runs;
	final int k;
	final String sitesFile;
	final String profilesFile;
	
	public CBIClient(int runs, int k, String sitesFile, String profilesFile) {
		this.runs = runs;
		this.k = k;
		this.sitesFile = sitesFile;
		this.profilesFile = profilesFile;
	}
	
	public static void main(String[] args) {
		CBIClient client = new CBIClient(2717, 10, "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v3/v3_f.sites", 
				"/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/v3/fine-grained");
		client.printResults();
	}
	
	private void printResults(){
		InstrumentationSites sites = new InstrumentationSites(new File(sitesFile));
		SitesInfo sInfo = new SitesInfo(sites);
		PredicateProfileReader reader = new PredicateProfileReader(profilesFile, sites);
		PredicateProfile[] profiles = reader.readProfiles(runs);
		Processor p = new Processor(profiles);
		p.process();
		
		
		System.out.println("\n");
		System.out.println("The general runs information are as follows:\n==============================================================");
		System.out.println("Total number of runs:\t\t\t\t" + runs);
		System.out.println("Total number of negative runs:\t\t\t" + p.getTotalNegative());
		System.out.println("Total number of positive runs:\t\t\t" + p.getTotalPositive());
		assert(p.getTotalNegative() + p.getTotalPositive() == runs);
		System.out.println("\n");
		printSitesInfo(sInfo);
		assert(sInfo.getNumPredicateItems() == p.getPredictors().size());
		System.out.println("\n");
		printTopKPredictors(p.getPredictors(), k);
		
		
		//--------------------------------------------------------------------------------------------------------------
		int num = profiles[0].getBranchPredicateSites().size() * 2 + profiles[0].getFloatKindPredicateSites().size() * 9 
				+ profiles[0].getScalarPredicateSites().size() * 6 + profiles[0].getReturnPredicateSites().size() * 6;
		assert(num == p.getPredictors().size());
		
		int num1 = profiles[0].getBranchPredicateSites().size() + profiles[0].getFloatKindPredicateSites().size() 
				+ profiles[0].getScalarPredicateSites().size() + profiles[0].getReturnPredicateSites().size();
		assert(num1 == sInfo.getNumPredicateSites());
		//--------------------------------------------------------------------------------------------------------------
	}

	
	public static void printSitesInfo(SitesInfo sInfo) {
		// TODO Auto-generated method stub
		System.out.println("The general sites information are as follows:\n==============================================================");
		System.out.println("Total number of sites instrumented:\t\t\t" + sInfo.getNumPredicateSites());
		System.out.println("Total number of predicates instrumented:\t\t" + sInfo.getNumPredicateItems());
		System.out.println("Total number of methods having sites instrumented:\t" + sInfo.getMap().size());
		System.out.println();
		System.out.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		for(String method: sInfo.getMap().keySet()){
			int nSites = sInfo.getMap().get(method).size();
			int nPredicates = 0;
			for(AbstractSite site: sInfo.getMap().get(method)){
				if(site instanceof BranchSite){
					nPredicates += 2;
				}
				else if(site instanceof FloatKindSite){
					nPredicates += 9;
				}
				else{
					nPredicates += 6;
				}
			}
			System.out.println(method + "     \t:" + nSites + "\t:" + nPredicates);
		}
		
	}

	public void printTopKPredictors(Map<PredicateItem, Double> predictors, int k){
		Set<String> methods = new LinkedHashSet<String>();
		List list = new ArrayList(predictors.entrySet());
		Collections.sort(list, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return ((Map.Entry<PredicateItem, Double>) arg1).getValue()
						.compareTo(((Entry<PredicateItem, Double>) arg0).getValue());
			}
			});
		
		System.out.println("The top " + k + " predicates are as follows:\n==============================================================");
		for (int i = 0; i < k && i < list.size(); i++) {
			Entry<PredicateItem, Double> entry = (Entry<PredicateItem, Double>) list.get(i);
			System.out.println("(" + (i + 1) + "): " + entry.getValue() + "\n" + entry.getKey().toString());
			System.out.println();
			
			//collect the method
			String method = entry.getKey().getSite().getFunctionName();
			if(!methods.contains(method)){
				methods.add(method);
			}
		}
		
		System.out.println();
		System.out.println("The corresponding top " + methods.size() + " methods are as follows:\n--------------------------------------------------------------");
		System.out.println(methods.toString());
	}

}
