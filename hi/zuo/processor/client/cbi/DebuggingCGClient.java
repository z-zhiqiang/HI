package zuo.processor.client.cbi;

import java.io.File;

import zuo.processor.functionentry.processor.SelectingProcessor_pldi2016;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class DebuggingCGClient {

	final File cgProfileDir;
	final File cgSitesFile;
	
	public DebuggingCGClient(File cgProfileDir, File cgSites) {
		this.cgProfileDir = cgProfileDir;
		this.cgSitesFile = cgSites;
	}

	public void run(){
		FunctionEntrySites cSites = new FunctionEntrySites(this.cgSitesFile);
		FunctionEntryProfileReader functionEntryProfileReader = new FunctionEntryProfileReader(this.cgProfileDir, cSites);
		FunctionEntryProfile[] cProfiles = functionEntryProfileReader.readFunctionEntryProfiles();
		
		SelectingProcessor_pldi2016 processor = new SelectingProcessor_pldi2016(cProfiles);
		processor.process();
//		System.out.println(processor.getFrequencyMap().toString());
	}

	public static void main(String[] args) {
		if(args.length != 2){
			System.err.println("\nUsage: cgProfilesDir cgSitesFile\n");
			return;
		}
		
		long time0 = System.currentTimeMillis();

		DebuggingCGClient c = new DebuggingCGClient(new File(args[0]), new File(args[1]));
		c.run();
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");

	}
	
}
