package zuo.processor.cbi.profile;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import zuo.processor.cbi.site.InstrumentationSites;
import zuo.util.file.FileUtil;


public class PredicateProfileReader {
	private final InstrumentationSites sites;
	private final File profileFolder;
	
	private final Set<String> functions;
	private final Set<Integer> samples;

	public PredicateProfileReader(File profileFolder, InstrumentationSites sites, final Set<String> functions, final Set<Integer> samples) {
		this.profileFolder = profileFolder;
		this.sites = sites;
		this.functions = functions;
		this.samples = samples;
	}
	
	public final PredicateProfile[] readProfiles(int numRuns) {
		System.out.println("Reading profiles in folder: " + this.profileFolder);
		
		File[] profiles = this.profileFolder.listFiles(FileUtil.createProfileFilter());
		Arrays.sort(profiles, new FileUtil.FileComparator());
		if (profiles.length == 0)
			throw new RuntimeException("No profiles in folder " + this.profileFolder);
		
		
//		PredicateProfile[] profiles = new PredicateProfile[profileFiles.length];
		int m = profiles.length/numRuns;
		PredicateProfile[] PProfiles = new PredicateProfile[numRuns];
		for (int i = 0, j = 0; i < profiles.length && j < numRuns; i++) {
			if ((i + 1) % m == 0) {
				if ((j + 1) % (5) == 0)
					System.out.print(".");
				if ((j + 1) % (600) == 0)
					System.out.println();
				
				PProfiles[j++] = this.createProfile(profiles[i]);
			}
		}
		System.out.println();
		return PProfiles;
	}

	private PredicateProfile createProfile(File profileFile) {
		String filename = profileFile.getName();
		if(!filename.matches(FileUtil.profileFilterPattern())){
			throw new RuntimeException("wrong profile name");
		}
		boolean isCorrect = true;
		if(filename.endsWith(".fprofile")){
			isCorrect = false;
		}
		return new PredicateProfile(profileFile, sites, isCorrect, functions);
	}
	

	public InstrumentationSites getSites() {
		return sites;
	}

	public File getProfileFolder() {
		return profileFolder;
	}
	
}
