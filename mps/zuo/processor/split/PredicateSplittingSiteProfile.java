package zuo.processor.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import zuo.processor.functionentry.site.FunctionEntrySite;

public class PredicateSplittingSiteProfile extends AbstractSplittingSiteProfile {
	private final Set<String> functions;
	private final Map<String, Set<Integer>> sitesMap;

	public PredicateSplittingSiteProfile(File siteFile, File profilesFolder, File targetSiteFile, File targetProfilesFolder, Set<String> functions) {
		super(siteFile, profilesFolder, targetSiteFile, targetProfilesFolder);
		this.functions = functions;
		this.sitesMap = new HashMap<String, Set<Integer>>();
	}

	public void split(){
		splitSites();
		splitProfiles();
	}
	
	@Override
	public void splitSites() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this.siteFile));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.matches("<sites\\sunit=\".*\"\\sscheme=\"function-entries\">")) {
					skip(reader, "</sites>");
				}
				else if (line.matches("<sites\\sunit=\".*\"\\sscheme=\".*\">")) {
					Set<Integer> set = new HashSet<Integer>();
					builder.append(line).append("\n");
					readPredicateSites(reader, builder, set);
					
					assert(!this.sitesMap.containsKey(extractUnitScheme(line)));
					this.sitesMap.put(extractUnitScheme(line), Collections.unmodifiableSet(set));
				}
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		writeToFile(this.targetSiteFile, builder.toString());
	}

	private void readPredicateSites(BufferedReader reader, StringBuilder builder, Set<Integer> set) throws IOException {
		// TODO Auto-generated method stub
		int index = 1;
		for (String line = reader.readLine(); line != null; line = reader.readLine(), index++) {
			if (line.contains("</sites>")) {
				builder.append(line).append("\n");
				return;
			} 
			else{
				String[] s = line.split("\t");
				String functionName = FunctionEntrySite.getUniqueFunctionName(s[2], s[0]);
				if(this.functions.contains(functionName)){
					builder.append(line).append("\n");
					set.add(index);
				}
			}
		}
	}


	private void skip(BufferedReader reader, String endString) throws IOException {
		// TODO Auto-generated method stub
		for (String line = reader.readLine(); line != null && !line.matches(endString); line = reader.readLine()) {
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
				skip(reader, "</samples>");
			}
			else if (line.matches("<samples\\sunit=\".*\"\\sscheme=\".*\">")) {
				builder.append(line).append("\n");
				readPredicates(reader, builder, this.sitesMap.get(extractUnitScheme(line)));
			}
		}
	}

	private void readPredicates(BufferedReader reader, StringBuilder builder, Set<Integer> set) {
		// TODO Auto-generated method stub
		int index = 1;
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine(), index++) {
				if (line.matches("</samples>")){
					builder.append(line).append("\n");
					return;
				}
				else{
					if(set.contains(index)){
						builder.append(line).append("\n");
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static String extractUnitScheme(String line){
		if (line.matches("<sites\\sunit=\".*\"\\sscheme=\".*\">")) {
			return line.substring(7, line.length() - 1);
		}
		else if (line.matches("<samples\\sunit=\".*\"\\sscheme=\".*\">")) {
			return line.substring(9, line.length() - 1);
		}
		else{
			throw new RuntimeException("wrong string");
		}
	}

}
