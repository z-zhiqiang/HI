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
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.SitesInfo;

public class CBIClients {
	public static final double percent = 0.8;
	private final PredicateProfile[] profiles;
	private List<Integer> failings;
	private List<Integer> passings;
	private Set<String> functions;
	
	private CBIClient fullInstrumentedCBIClient;
	private final Map<String, CBIClient> clientsMap;
	

	public CBIClients(SitesInfo sitesInfo, File profilesFolder, File consoleFile){
		clientsMap = new HashMap<String, CBIClient>();
		
		profiles = new PredicateProfileReader(profilesFolder, sitesInfo.getSites()).readProfiles();
		divideProfiles();
		functions = Collections.unmodifiableSet(sitesInfo.getMap().keySet());

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(consoleFile)));
			run(sitesInfo, writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}
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


	private void run(SitesInfo sitesInfo, PrintWriter writer) {
		//print the information of instrumentation sites
		printSitesInfo(sitesInfo, writer);
		
		//full CBIClient
		int fk = 30;
//		Set<Integer> fullSamples = buildFullSamples();
		Set<Integer> fullSamples = buildPartialSamples();
		fullInstrumentedCBIClient = new CBIClient(fk, profiles, writer, functions, fullSamples);
		
		//iterative CBIClient each for each function
		int pk = 3;
		Set<String> pFunctions;
		Set<Integer> pSamples;
		for(String function: functions){
			pFunctions = new HashSet<String>();
			pFunctions.add(function);
			
			pSamples = buildPartialSamples();
			
			CBIClient pc = new CBIClient(pk, profiles, writer, pFunctions, pSamples);
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
		
		Random randomFGenerator = new Random();
		int fs = (int) (failings.size() * percent);
		for(; partialSamples.size() < (fs > 2 ? fs : 2);){
			int fSample = randomFGenerator.nextInt(failings.size());
			partialSamples.add(failings.get(fSample));
		}
		
		Random randomPGenerator = new Random();
		int ps = (int) (passings.size() * percent);
		for(; partialSamples.size() < ps + (fs > 2 ? fs : 2);){
			int pSample = randomPGenerator.nextInt(passings.size());
			partialSamples.add(passings.get(pSample));
		}
		
		return partialSamples;
	}


	/**print the sites information
	 * @param sInfo
	 * @param writer
	 */
	public static void printSitesInfo(SitesInfo sitesInfo, PrintWriter writer) {
		writer.println("The general sites information are as follows:\n========================================================================");
		writer.println(String.format("%-60s", "Total number of sites instrumented:") + sitesInfo.getNumPredicateSites());
		writer.println(String.format("%-60s", "Total number of predicates instrumented:") + sitesInfo.getNumPredicateItems());
		writer.println(String.format("%-60s", "Total number of methods having sites instrumented:") + sitesInfo.getMap().size());
		writer.println();
		writer.println("The information of sites and predicates in each method:\n------------------------------------------------------------------------");
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
	
	
}
