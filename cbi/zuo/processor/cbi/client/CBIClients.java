package zuo.processor.cbi.client;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.cbi.site.SitesInfo;

public class CBIClients {
	private final File profilesFolder;
	private final File consoleFolder;
	private final SitesInfo sitesInfo;
	
	private String targetFunction;
	private final Map<String, CBIClient> clientsMap;
	

	public CBIClients(SitesInfo sInfo, File profiles, File consoleFolder){
		this.profilesFolder = profiles;
		this.consoleFolder = consoleFolder;
		this.sitesInfo = sInfo;
		
		clientsMap = new HashMap<String, CBIClient>();
		
		run();
	}


	private void run() {
		// TODO Auto-generated method stub
		
	}


	/**print the sites information
	 * @param sInfo
	 * @param writer
	 */
	public static void printSitesInfo(SitesInfo sitesInfo, PrintWriter writer) {
		writer.println("The general sites information are as follows:\n==============================================================");
		writer.println(String.format("%-60s", "Total number of sites instrumented:") + sitesInfo.getNumPredicateSites());
		writer.println(String.format("%-60s", "Total number of predicates instrumented:") + sitesInfo.getNumPredicateItems());
		writer.println(String.format("%-60s", "Total number of methods having sites instrumented:") + sitesInfo.getMap().size());
		writer.println();
		writer.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		for(String method: sitesInfo.getMap().keySet()){
			writer.println(String.format("%-45s", method) + String.format("%-20s", ":" + sitesInfo.getMap().get(method).getNumSites()) + String.format("%-20s", ":" + sitesInfo.getMap().get(method).getNumPredicates()));
		}
	}
	
	
	public String getTargetFunction() {
		return targetFunction;
	}

	public Map<String, CBIClient> getClientsMap() {
		return clientsMap;
	}
	
	
}
