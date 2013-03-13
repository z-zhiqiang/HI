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
	final int numFunctionEntrySites;
	
	public FunctionEntrySites(File sitesFile){
		int index = 0;
		Map<String, List<FunctionEntrySite>> sites = new LinkedHashMap<String, List<FunctionEntrySite>>();
		
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(sitesFile));
			String rLine;
			while ((rLine = in.readLine()) != null) {
				if(rLine.startsWith("<sites")){
					if(!rLine.contains("scheme=\"function-entries\"")){
						throw new RuntimeException();
					}
					
					String unit = FileUtility.getUnitID(rLine);
					List<FunctionEntrySite> sitesList = new ArrayList<FunctionEntrySite>();
					
					while((rLine = in.readLine()) != null && !rLine.contains("</sites>")){
						String[] s = rLine.split("\t");
						if(s.length != 4){
							throw new RuntimeException();
						}
						sitesList.add(new FunctionEntrySite(++index, s[0], Integer.parseInt(s[1]), s[2], Integer.parseInt(s[3])));
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
		this.numFunctionEntrySites = index;
	}
	
	

	public Map<String, List<FunctionEntrySite>> getSites() {
		return sites;
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
