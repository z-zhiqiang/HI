package zuo.processor.cbi.processor;

import java.util.List;
import java.util.Map.Entry;

import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.FloatKindSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;

public class CBIClient {
	
	public static void main(String[] args) {
		int runs = 4528;
		int k = 10;
		
		PredicateProfileReader reader = new PredicateProfileReader("/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/v15/fine-grained", 
				"/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v15/v15_f.sites");
		PredicateProfile[] profiles = reader.readProfiles(runs);
		Processor p = new Processor(profiles);
		p.process();
		
		System.out.println("\n");
		System.out.println("Total number of runs:\t\t\t\t" + runs);
		System.out.println("Total number of negative runs:\t\t\t" + p.getTotalNegative());
		System.out.println("Total number of positive runs:\t\t\t" + p.getTotalPositive());
		assert(p.getTotalNegative() + p.getTotalPositive() == runs);
		System.out.println("Total number of predicates instrumented:\t" + p.getPredictors().size());
		System.out.println("\n");
		p.printTopKPredictors(k);
		
		
		
		
		
		//--------------------------------------------------------------------------------------------------------------
		int num = profiles[0].getBranchPredicateSites().size() * 2 + profiles[0].getFloatKindPredicateSites().size() * 9 
				+ profiles[0].getScalarPredicateSites().size() * 6 + profiles[0].getReturnPredicateSites().size() * 6;
//		System.out.println(num);
		assert(num == p.getPredictors().size());
		
		int num2 = 0;
		for(Entry<String, List<BranchSite>> entry: reader.getSites().getBranchSites().entrySet()){
			num2 += entry.getValue().size() * 2;
		}
		for(Entry<String, List<FloatKindSite>> entry: reader.getSites().getFloatSites().entrySet()){
			num2 += entry.getValue().size() * 9;
		}
		for(Entry<String, List<ReturnSite>> entry: reader.getSites().getReturnSites().entrySet()){
			num2 += entry.getValue().size() * 6;
		}
		for(Entry<String, List<ScalarSite>> entry: reader.getSites().getScalarSites().entrySet()){
			num2 += entry.getValue().size() * 6;
		}
//		System.out.println(num2);
		assert(num2 == p.getPredictors().size());
		//--------------------------------------------------------------------------------------------------------------
		
		
	}

}
