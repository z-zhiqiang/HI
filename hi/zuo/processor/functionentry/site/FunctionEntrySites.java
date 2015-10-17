package zuo.processor.functionentry.site;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zuo.util.file.FileUtility;


public class FunctionEntrySites {
	private final Map<String, List<FunctionEntrySite>> sites;
	private final Set<String> functions;
	private final int numFunctionEntrySites;
	
	public FunctionEntrySites(File sitesFile){
		int index = 0;
		Map<String, List<FunctionEntrySite>> sites = new LinkedHashMap<String, List<FunctionEntrySite>>();
		Set<String> functions = new HashSet<String>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(sitesFile));
			String rLine;
			while ((rLine = in.readLine()) != null) {
				if(rLine.startsWith("<sites")){
					if(!rLine.contains("scheme=\"function-entries\"") && !rLine.contains("scheme=\"method-entries\"")){
						throw new RuntimeException();
					}
					
					String unit = FileUtility.getUnitID(rLine);
					List<FunctionEntrySite> sitesList = new ArrayList<FunctionEntrySite>();
					
					while((rLine = in.readLine()) != null && !rLine.contains("</sites>")){
						String[] s = rLine.split("\t");
						if(s.length != 4){
							throw new RuntimeException();
						}
						FunctionEntrySite site = new FunctionEntrySite(++index, s[0], Integer.parseInt(s[1]), s[2], Integer.parseInt(s[3]));
						sitesList.add(site);
						
						if(functions.contains(site.getFunctionName())){
							throw new RuntimeException("Function error");
						}
						functions.add(site.getFunctionName());
					}
					
					if(sites.containsKey(unit)){
						throw new RuntimeException();
					}
					sites.put(unit, sitesList);
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.sites = Collections.unmodifiableMap(sites);
		this.functions = Collections.unmodifiableSet(functions);
		this.numFunctionEntrySites = index;
		assert(index == this.functions.size());
	}
	
	

	public Map<String, List<FunctionEntrySite>> getSites() {
		return sites;
	}

	
	public Set<String> getFunctions() {
		return functions;
	}


	public int getNumFunctionEntrySites() {
		return numFunctionEntrySites;
	}



	public static void main(String[] args) {
		Set<String> set = new HashSet<String>();
		int i = 0;
		FunctionEntrySites fs = new FunctionEntrySites(new File("/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v3/v3_c.sites"));
		for (String unit: fs.sites.keySet()) {
			List<FunctionEntrySite> list = fs.sites.get(unit);
			i += list.size(); 
			for (FunctionEntrySite functionEntrySite : list) {
				set.add(functionEntrySite.getFunctionName());
			}
//			System.out.println(fs.sites.get(unit).toString());
			System.out.println();
		}
		System.out.println(i);
		System.out.println(set.size());
	}
}
