package zuo.processor.functionentry.client;

import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class Client {
	final int runs;
	final String sitesFile;
	final String profilesFile;
	
	
	public Client(int runs, String sitesFile, String profilesFile) {
		this.runs = runs;
		this.sitesFile = sitesFile;
		this.profilesFile = profilesFile;
	}


	public static void main(String[] args) {
		Client client = new Client(2717, "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v3/v3_c.sites", 
				"/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/v3/coarse-grained");
		client.printResults();
		
	}
	
	private void printResults(){
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(runs);
		SelectingProcessor processor = new SelectingProcessor(profiles);
		processor.process();
		
		System.out.println("\n");
		System.out.println("The general runs information are as follows:\n==============================================================");
		System.out.println("Total number of runs:\t\t\t\t" + runs);
		System.out.println("Total number of negative runs:\t\t\t" + processor.getTotalNegative());
		System.out.println("Total number of positive runs:\t\t\t" + processor.getTotalPositive());
		assert(processor.getTotalNegative() + processor.getTotalPositive() == runs);
		System.out.println("\n");
		
		System.out.println("Total number of methods instrumented:\t\t" + sites.getNumFunctionEntrySites());
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		System.out.println("\n");
		
		for(FunctionEntrySite site: processor.getFrequencyMap().keySet()){
			System.out.println(site.toString() + "\n\t" + processor.getFrequencyMap().get(site).toString());
		}
	}
	
	private void constructMaps(){
		
	}
	

}
