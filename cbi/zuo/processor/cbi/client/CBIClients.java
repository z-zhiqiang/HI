package zuo.processor.cbi.client;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.SitesInfo;

public class CBIClients {
	private final File consoleFolder;
	
	private final PredicateProfile[] profiles;
	private Set<Integer> failings;
	private Set<Integer> passings;
	private Set<String> functions;
	
	private String targetFunction;
	private final Map<String, CBIClient> clientsMap;
	

	public CBIClients(SitesInfo sitesInfo, File profilesFolder, File consoleFolder){
		this.consoleFolder = consoleFolder;
		clientsMap = new HashMap<String, CBIClient>();
		
		PredicateProfileReader reader = new PredicateProfileReader(profilesFolder, sitesInfo.getSites());
		profiles = reader.readProfiles();
		divideProfiles();
		functions = Collections.unmodifiableSet(sitesInfo.getMap().keySet());
		
		run();
	}


	private void divideProfiles() {
		// TODO Auto-generated method stub
		Set<Integer> passings = new HashSet<Integer>();
		Set<Integer> failings = new HashSet<Integer>();
		for(int i = 0; i < profiles.length; i++){
			if(profiles[i].isCorrect()){
				passings.add(i);
			}
			else{
				failings.add(i);
			}
		}
		assert(passings.size() + failings.size() == profiles.length);
		this.failings = Collections.unmodifiableSet(failings);
		this.passings = Collections.unmodifiableSet(passings);
	}


	private void run() {
		//full CBIClient
		int fk = 10;
		Set<Integer> fullSamples = buildFullSamples();
		File consoleFFile = new File(consoleFolder, "CBI.out");
		CBIClient fc = new CBIClient(fk, profiles, consoleFFile, functions, fullSamples);
		targetFunction = fc.getSortedPredictorsList().get(0).getPredicateItem().getPredicateSite().getSite().getFunctionName();
		
		//iterative CBIClient each for each function
		int pk = 3;
		Set<String> pFunctions;
		Set<Integer> pSamples;
		for(String function: functions){
			pFunctions = new HashSet<String>();
			pFunctions.add(function);
			
			pSamples = buildPartialSamples();
			
			File consolePFile = new File(consoleFolder, "CBI_" + function + ".out");
			CBIClient pc = new CBIClient(pk, profiles, consolePFile, pFunctions, pSamples);
			clientsMap.put(function, pc);
		}
		
	}


	/**get the full set of profiles
	 * @return
	 */
	private Set<Integer> buildFullSamples() {
		Set<Integer> fullSamples = new HashSet<Integer>();
		fullSamples.addAll(failings);
		fullSamples.addAll(passings);
		return fullSamples;
	}


	/**get the set of sampled profiles
	 * @return
	 */
	private Set<Integer> buildPartialSamples() {
		// TODO Auto-generated method stub
		Set<Integer> partialSamples = new HashSet<Integer>();
		
		return partialSamples;
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
