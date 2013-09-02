package zuo.processor.cbi.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.site.SitesInfo;

public class CBIClients {
	public final static int fK = 5;
	private boolean zFlag;
	private final PredicateProfile[] profiles;
	private List<Integer> failings;
	private List<Integer> passings;
	private Set<String> functions;
	
	private CBIClient fullInstrumentedCBIClient;
	private Map<String, CBIClient> clientsMap;
	
	public CBIClients(SitesInfo sitesInfo, PredicateProfile[] profiles){
		this.zFlag = true;
		
		this.profiles = profiles;
		divideProfiles();
		this.functions = Collections.unmodifiableSet(sitesInfo.getMap().keySet());
		
		run(sitesInfo);
//		PrintWriter writer = null;
//		try {
//			if(!consoleFile.getParentFile().exists()){
//				consoleFile.getParentFile().mkdirs();
//			}
//			writer = new PrintWriter(new BufferedWriter(new FileWriter(consoleFile)));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		finally{
//			if(writer != null){
//				writer.close();
//			}
//		}
	}

	private void divideProfiles() {
		// TODO Auto-generated method stub
		List<Integer> passings = new ArrayList<Integer>();
		List<Integer> failings = new ArrayList<Integer>();
		for(int i = 0; i < profiles.length; i++){
			if(profiles[i].isCorrect()){
				passings.add(i);
			}
			else{
				failings.add(i);
			}
		}
		assert(passings.size() + failings.size() == profiles.length);
		this.failings = Collections.unmodifiableList(failings);
		this.passings = Collections.unmodifiableList(passings);
	}


	private void run(SitesInfo sitesInfo) {
		//print the information of instrumentation sites
//		printSitesInfo(sitesInfo, writer);
		
		//full CBIClient
		fullInstrumentedCBIClient = new CBIClient(profiles, functions, failings, passings);
//		fullInstrumentedCBIClient.runFull();
		
		//confirm that there exists predictor with non-zero importance value 
		checkNonZeroPredictor();
		if(!zFlag){
			return;
		}
		
		//iterative CBIClient each for each function
//		long time0 = System.currentTimeMillis();
		clientsMap = new HashMap<String, CBIClient>();
		for(String function: functions){
			Set<String> pFunctions = new HashSet<String>();
			pFunctions.add(function);
			
			CBIClient pc = new CBIClient(profiles, pFunctions, failings, passings);
			clientsMap.put(function, pc);
		}
//		long time1 = System.currentTimeMillis();
//		System.out.println("Iterative CBIClient:\t" + (time1 - time0));
	}

	private void checkNonZeroPredictor() {
		// TODO Auto-generated method stub
		if(fullInstrumentedCBIClient.getFullFixElement().getSortedPredictors().isEmpty()){
			zFlag = false;
		}
	}


	/**print the sites information
	 * @param sInfo
	 * @param writer
	 */
	public static void printSitesInfo(SitesInfo sitesInfo, PrintWriter writer) {
		writer.println("The general sites information are as follows:");
		writer.println("========================================================================");
		writer.println(String.format("%-60s", "Total number of sites instrumented:") + sitesInfo.getNumPredicateSites());
		writer.println(String.format("%-60s", "Total number of predicates instrumented:") + sitesInfo.getNumPredicateItems());
		writer.println(String.format("%-60s", "Total number of methods having sites instrumented:") + sitesInfo.getMap().size());
		writer.println();
		writer.println("The information of sites and predicates in each method:");
		writer.println("========================================================================");
		for(String method: sitesInfo.getMap().keySet()){
			writer.println(String.format("%-45s", method) + String.format("%-20s", ":" + sitesInfo.getMap().get(method).getNumSites()) + String.format("%-20s", ":" + sitesInfo.getMap().get(method).getNumPredicates()));
		}
		writer.println("\n\n");
	}
	
	
	public Map<String, CBIClient> getClientsMap() {
		return clientsMap;
	}


	public CBIClient getFullInstrumentedCBIClient() {
		return fullInstrumentedCBIClient;
	}

	public boolean iszFlag() {
		return zFlag;
	}

	
}
