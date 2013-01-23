package zuo.processor.functionentry.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.processor.utility.FileUtility;

public class FunctionEntryProfile {
	final FunctionEntrySites sites;
	final File profile;
	
	final List<FunctionEntryItem> functionEntryItems;
	
	public FunctionEntryProfile(File profile, FunctionEntrySites sites){
		this.profile = profile;
		this.sites = sites;
		
		ArrayList<FunctionEntryItem> items = new ArrayList<FunctionEntryItem>();
		
		BufferedReader in = null;  
		try {  
			in = new BufferedReader(new FileReader(profile));  
			String line;  
			while ((line = in.readLine()) != null) {  
				if(line.contains("<report id=\"samples\">")){
					while ((line = in.readLine()) != null && !line.contains("</report>")) {
						if(line.startsWith("<samples")){
							if(!line.contains("scheme=\"function-entries\"")){
								throw new RuntimeException();
							}
							
							String unit = FileUtility.getUnitID(line);
							List<FunctionEntrySite> siteList = sites.getSites().get(unit);
							int index = -1;
							while((line = in.readLine()) != null && !line.contains("</samples>")){
								int count = Integer.parseInt(line.trim());
								items.add(new FunctionEntryItem(count, siteList.get(++index)));
							}
							assert(siteList.size() == ++index);
						}
					}
				}
			}  
		}  
		catch (FileNotFoundException e) {  
			e.printStackTrace();
		}  
		catch (IOException ee) {  
			ee.printStackTrace();
		} 
		finally {  
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
		}
		
		this.functionEntryItems = Collections.unmodifiableList(items);
	}
	
	public String getProfilePath(){
		return this.profile.getAbsolutePath();
	}
	
	
	public FunctionEntrySites getSites() {
		return sites;
	}

	public List<FunctionEntryItem> getFunctionEntryItems() {
		return functionEntryItems;
	}

	public static void main(String[] args) {
		FunctionEntrySites sites = new FunctionEntrySites("/home/sunzzq/Research/test/main.sites");
		FunctionEntryProfile p = new FunctionEntryProfile(new File("/home/sunzzq/Research/test", "/main.profile"), sites);
		for (FunctionEntryItem item : p.functionEntryItems) {
			System.out.println(item.toString());
		}
	}

}
