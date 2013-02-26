package zuo.processor.functionentry.processor;

import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class Client {
	public static void main(String[] args) {
		int runs = 4528;
		
		String sitesFile = "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v15/v15_c.sites";
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		String profilesFile = "/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/v15/coarse-grained";
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(runs);
		SelectingProcessor processor = new SelectingProcessor(profiles);
		processor.process();
		
		System.out.println("\n");
		System.out.println("Total number of runs:\t\t\t\t" + runs);
		System.out.println("Total number of negative runs:\t\t\t" + processor.getTotalNegative());
		System.out.println("Total number of positive runs:\t\t\t" + processor.getTotalPositive());
		assert(processor.getTotalNegative() + processor.getTotalPositive() == runs);
		System.out.println("Total number of methods instrumented:\t" + processor.getFrequencyMap().size());
		System.out.println("\n");
		
		for(FunctionEntrySite site: processor.getFrequencyMap().keySet()){
			System.out.println(site.toString() + "\n\t" + processor.getFrequencyMap().get(site).toString() + "\t\t" + processor.Importance_A(processor.getFrequencyMap().get(site)));
		}
	}
	

}
