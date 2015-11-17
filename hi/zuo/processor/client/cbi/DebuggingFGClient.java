package zuo.processor.client.cbi;

import java.io.File;

import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;

public class DebuggingFGClient {

	final File fgProfileDir;
	final File fgSitesFile;
	
	public DebuggingFGClient(File fgProfileDir, File fgSites) {
		this.fgProfileDir = fgProfileDir;
		this.fgSitesFile = fgSites;
	}

	public void run(){
		InstrumentationSites fSites = new InstrumentationSites(this.fgSitesFile);
		PredicateProfileReader predicateProfileReader = new PredicateProfileReader(this.fgProfileDir, fSites);
		PredicateProfile[] fProfiles = predicateProfileReader.readProfiles();
		
		Processor p = new Processor(fProfiles);
		p.process();
//		System.out.println(p.getPredictorsList().toString());
	}

	public static void main(String[] args) {
		if(args.length != 2){
			System.err.println("\nUsage: fgProfilesDir fgSitesFile\n");
			return;
		}
		
		long time0 = System.currentTimeMillis();

		DebuggingFGClient c = new DebuggingFGClient(new File(args[0]), new File(args[1]));
		c.run();
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");

	}
	
}
