package zuo.processor.functionentry.profile;

import java.io.File;
import java.io.FilenameFilter;

import zuo.processor.functionentry.site.FunctionEntrySites;

public class FunctionEntryProfileReader {
	private final String profileFolder;
	private final FunctionEntrySites sites;
	
	public FunctionEntryProfileReader(String proFolder, FunctionEntrySites sites){
		this.profileFolder = proFolder;
		this.sites = sites;
	}
	
	public FunctionEntryProfile[] readFunctionEntryProfiles(){
		File[] profiles = new File(profileFolder).listFiles(new FilenameFilter(){

			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				return arg1.matches(".+\\.fprofile");
			}
			
		});
		
		FunctionEntryProfile[] FEProfiles = new FunctionEntryProfile[profiles.length];
		for (int i = 0; i < profiles.length; i++) {
			FEProfiles[i] = new FunctionEntryProfile(profiles[i], sites);
		}
		
		
		return FEProfiles;
	}
	

}
