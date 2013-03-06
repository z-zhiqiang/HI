package zuo.processor.functionentry.profile;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.util.file.FileUtil;

public class FunctionEntryProfileReader {
	private final String profileFolder;
	private final FunctionEntrySites sites;
	
	public FunctionEntryProfileReader(String proFolder, FunctionEntrySites sites){
		this.profileFolder = proFolder;
		this.sites = sites;
	}
	
	public FunctionEntryProfile[] readFunctionEntryProfiles(int numRuns){
		System.out.println("Reading profiles in folder: " + this.profileFolder);
		
		File[] profiles = new File(profileFolder).listFiles(FileUtil.createProfileFilter());
		Arrays.sort(profiles, new FileUtil.FileComparator());
		if (profiles.length == 0)
			throw new RuntimeException("No profiles in folder " + this.profileFolder);
		
//		for (int i = 0; i < numRuns; i++) {
//			String name = profiles[i].getName();
//			if(name.contains(".fprofile"))
//			System.out.println(profiles[i].getName());
//		}
		
//		FunctionEntryProfile[] FEProfiles = new FunctionEntryProfile[profiles.length];
		int m = profiles.length/numRuns;
		FunctionEntryProfile[] FEProfiles = new FunctionEntryProfile[numRuns];
		for (int i = 0, j = 0; i < profiles.length && j < numRuns; i++) {
			if ((i + 1) % m == 0) {
				if ((j + 1) % (5) == 0)
					System.out.print(".");
				if ((j + 1) % (600) == 0)
					System.out.println();
				
				boolean isCorrect = true;
				if (profiles[i].getName().endsWith(".fprofile")) {
					isCorrect = false;
				}
				FEProfiles[j++] = new FunctionEntryProfile(profiles[i], sites, isCorrect);
//				System.out.println(profiles[i].getName());
			}
		}
		
		return FEProfiles;
	}
	

}
