package zuo.processor.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FunctionEntrySplittingSiteProfile extends AbstractSplittingSiteProfile{

	public FunctionEntrySplittingSiteProfile(File sites, File profilesFolder, File targetSites, File targetProfiles) {
		super(sites, profilesFolder, targetSites, targetProfiles);
	}

	@Override
	public void splitSites() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.siteFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.matches("<sites\\sunit=\".*\"\\sscheme=\"function-entries\">")) {
					builder.append(line).append("\n");
					readFunctionEntrySites(reader, builder);
				}
			}
			reader.close();
		} 
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		writeToFile(this.targetSiteFile, builder.toString());
	}

	

	private void readFunctionEntrySites(BufferedReader reader, StringBuilder builder) throws IOException {
		// TODO Auto-generated method stub
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.matches("</sites>")) {
				builder.append(line);
				return;
			}
			else{
				builder.append(line).append("\n");
			}
		}
	}

	@Override
	public void splitProfiles() {
		// TODO Auto-generated method stub
		File[] files = this.profilesFolder.listFiles();
		for(File file: files){
			splitEachProfile(file);
		}
	}
	
	private void splitEachProfile(File profile){
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(profile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.matches("<report id=\"samples\">")) {
					// read the report.
					builder.append(line).append("\n");
					this.readReport(reader, builder);
					builder.append("</report>");
					break;
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		writeToFile(new File(this.targetProfilesFolder, profile.getName()), builder.toString());
	}
	
	private void readReport(BufferedReader reader, StringBuilder builder) throws IOException {
		for (String line = reader.readLine(); line != null; line = reader.readLine()) {
			if (line.matches("<samples\\sunit=\".*\"\\sscheme=\"function-entries\">")) {
				builder.append(line).append("\n");
				readFunctionEntries(reader, builder);
			}
		}
	}

	private void readFunctionEntries(BufferedReader reader, StringBuilder builder) {
		// TODO Auto-generated method stub
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.matches("</samples>")){
					builder.append(line).append("\n");
					return;
				}
				else{
					builder.append(line).append("\n");
				}
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

}
