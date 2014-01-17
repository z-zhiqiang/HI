package zuo.processor.split;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public abstract class AbstractSplittingSiteProfile {
	protected final File siteFile;
	protected final File profilesFolder;
	
	protected final File targetSiteFile;
	protected final File targetProfilesFolder;
	
	AbstractSplittingSiteProfile(File siteFile, File profilesFolder, File targetSiteFile, File targetProfilesFolder) {
		this.siteFile = siteFile;
		this.profilesFolder = profilesFolder;
		
		this.targetSiteFile = targetSiteFile;
		this.targetProfilesFolder = targetProfilesFolder;
	}
	

	public abstract void splitSites(); 
	public abstract void splitProfiles();

	protected void writeToFile(File targetFile, String string) {
		// TODO Auto-generated method stub
		PrintWriter writer = null;
		try {
			if(!targetFile.getParentFile().exists()){
				targetFile.getParentFile().mkdirs();
			}
			writer = new PrintWriter(new BufferedWriter(new FileWriter(targetFile)));
			writer.print(string);
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
