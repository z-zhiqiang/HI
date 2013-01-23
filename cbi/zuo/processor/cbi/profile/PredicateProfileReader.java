package zuo.processor.cbi.profile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;


import zuo.processor.cbi.site.InstrumentationSites;


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
	
	public final PredicateProfile[] readProfiles() {
		System.out.println("reading profiles in folder " + this.profileFolder);
		
		File[] profileFiles = this.collectProfileFiles();
		
		PredicateProfile[] profiles = new PredicateProfile[profileFiles.length];
		System.out.println("reading profiles...");
		for (int i = 0; i < profiles.length; ++i) {
			if ((i + 1) % 5 == 0)
				System.out.print(".");
			if ((i + 1) % 600 == 0)
				System.out.println();
			profiles[i] = this.createProfile(profileFiles[i]);
		}
		System.out.println();
		return profiles;
	}

	private File[] collectProfileFiles() {
		File[] profileFiles = this.profileFolder.listFiles(this.createProfileFilter());
		Arrays.sort(profileFiles, new FileComparator());
		if (profileFiles.length == 0)
			throw new RuntimeException("No profiles in folder " + this.profileFolder);
		return profileFiles;
	}

	private PredicateProfile createProfile(File profileFile) {
		String filename = profileFile.getName();
		if(!filename.matches(profileFilterPattern())){
			throw new RuntimeException("wrong profile name");
		}
		boolean isCorrect = true;
		if(filename.endsWith(".fprofile")){
			isCorrect = false;
		}
		return new PredicateProfile(profileFile, sites, isCorrect);
	}
	

	private class FileComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
			// return mapTestToInteger(o1.getName()) - mapTestToInteger(o2.getName());
//			return canonicalizeTestName(o1.getName()).compareTo(canonicalizeTestName(o2.getName()));
			int i1 = Integer.parseInt(canonicalizeProfileName(o1.getName()).substring(1));
			int i2 = Integer.parseInt(canonicalizeProfileName(o2.getName()).substring(1));
			if(i1 < i2)
				return -1;
			else if(i1 > i2)
				return 1;
			else
				return 0;
			
		}
	}
	private String canonicalizeProfileName(String profileName) {
		return profileName.substring(0, profileName.lastIndexOf('.'));
	}

	
	private FilenameFilter createProfileFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String name) {
				return Pattern.matches(profileFilterPattern(), name);
			}
		};
	}

	private String profileFilterPattern() {
		return "o[0-9]+\\.[fp]profile";
	}
	
}
