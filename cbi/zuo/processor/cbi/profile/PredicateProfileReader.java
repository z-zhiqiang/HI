package zuo.processor.cbi.profile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;


import zuo.processor.cbi.site.InstrumentationSites;
import zuo.util.file.FileUtil;


public class PredicateProfileReader {
	private final InstrumentationSites sites;
	private final File profileFolder;

	public PredicateProfileReader(String profileFolder, String sitesPath) {
		this.profileFolder = new File(profileFolder);
		if (sitesPath == null) {
			this.sites = null;
		} else {
			this.sites = new InstrumentationSites(new File(sitesPath));
		}
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
//				System.out.println(profiles[i].getName());
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
		return new PredicateProfile(profileFile, sites, isCorrect);
	}
	

	public static void main(String[] args) {
		PredicateProfileReader reader = new PredicateProfileReader("/home/sunzzq/Research/tcas/traces/v1/fine-grained", "/home/sunzzq/Research/tcas/versions/v1/v1_f.sites");
		PredicateProfile[] profiles = reader.readProfiles(20);
		System.out.println(profiles[0].toString());
		System.out.println(profiles[1].toString());
		System.out.println(profiles[2].toString());
	}

	public InstrumentationSites getSites() {
		return sites;
	}

	public File getProfileFolder() {
		return profileFolder;
	}
	
}
