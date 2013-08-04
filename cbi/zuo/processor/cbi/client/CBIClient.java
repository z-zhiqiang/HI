package zuo.processor.cbi.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.processor.PredicateItemWithImportance;
import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;

public class CBIClient {
	final int runs;
	final int k;
	final File sitesFile;
	final File profilesFolder;
	final File consoleFile;
	private List<PredicateItemWithImportance> sortedPredictorsList;
//	private Map<String, Double> methodsMap;
	
	private final Set<String> functions;
	private final Set<Integer> samples; 
	
	
	public CBIClient(int runs, int k, File sitesFile, File profilesFolder, File consoleF, Set<String> functions, Set<Integer> samples) {
		this.runs = runs;
		this.k = k;
		this.sitesFile = sitesFile;
		this.profilesFolder = profilesFolder;
		this.consoleFile = consoleF;
		
		this.functions = functions;
		this.samples = samples;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			run(writer);
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
	
	public static void main(String[] args) {
//		CBIClient client = new CBIClient(363, 10, new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/sed/versions/v2/subv1/v2_subv1_f.sites"), 
//				"/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/sed/traces/v2/subv1/fine-grained/", 
//				"/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/m3.out");
	}
	
	private void run(PrintWriter writer){
		InstrumentationSites sites = new InstrumentationSites(sitesFile);
		PredicateProfileReader reader = new PredicateProfileReader(profilesFolder, sites);
		PredicateProfile[] profiles = reader.readProfiles(runs);
		Processor p = new Processor(profiles);
		p.process();
		
		
		assert(p.getTotalNegative() + p.getTotalPositive() == runs);
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of runs:") + runs);
		writer.println(String.format("%-50s", "Total number of negative runs:") + p.getTotalNegative());
		writer.println(String.format("%-50s", "Total number of positive runs:") + p.getTotalPositive());
	
		//print out the static instrumentation sites information 
		writer.println("\n");
		printSitesInfo(new SitesInfo(sites), writer);
		
		//sort the list of predictors according to the importance value
		sortingPreditorsList(p.getPredictorsList());
		
		//print out top-k predictors information
		writer.println("\n");
		printTopKPredictors(writer);
	}

	private void sortingPreditorsList(List<PredicateItemWithImportance> predictorsList) {
		sortedPredictorsList = new ArrayList<PredicateItemWithImportance>(predictorsList);
		// TODO Auto-generated method stub
		Collections.sort(sortedPredictorsList, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Double(((PredicateItemWithImportance) arg1).getImportance())
				.compareTo(new Double(((PredicateItemWithImportance) arg0).getImportance()));
			}
			
		});
	}

	public void printTopKPredictors(PrintWriter writer){
		Set<String> topMethods = new LinkedHashSet<String>();
		Map<String, Double> methodsM = new HashMap<String, Double>();
		
		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
		for (int i = 0; i < sortedPredictorsList.size(); i++) {
			PredicateItemWithImportance pItemWI = sortedPredictorsList.get(i);
			String method = pItemWI.getPredicateItem().getPredicateSite().getSite().getFunctionName();
			double value = pItemWI.getImportance();
			
			if (i < k) {
				//collect the method
				if (!topMethods.contains(method)) {
					topMethods.add(method);
				}
				writer.println("(" + (i + 1) + "): " + pItemWI.toString());
				writer.println();
			}
			
			if(methodsM.containsKey(method)){
				if(value > methodsM.get(method)){
					throw new RuntimeException("Error");
//					methodsM.put(method, value);
				}
			}
			else{
				methodsM.put(method, value);
			}
		}
//		this.methodsMap = Collections.unmodifiableMap(methodsM);
	    assert(methodsM.size() == new SitesInfo(new InstrumentationSites(sitesFile)).getMap().size());
		
		writer.println();
		writer.println("The corresponding top " + topMethods.size() + " of " + methodsM.size() + " methods are as follows:\n--------------------------------------------------------------");
		writer.println(topMethods.toString());
	}

	/**print the sites information
	 * @param sInfo
	 * @param writer
	 */
	public static void printSitesInfo(SitesInfo sInfo, PrintWriter writer) {
		writer.println("The general sites information are as follows:\n==============================================================");
		writer.println(String.format("%-60s", "Total number of sites instrumented:") + sInfo.getNumPredicateSites());
		writer.println(String.format("%-60s", "Total number of predicates instrumented:") + sInfo.getNumPredicateItems());
		writer.println(String.format("%-60s", "Total number of methods having sites instrumented:") + sInfo.getMap().size());
		writer.println();
		writer.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		for(String method: sInfo.getMap().keySet()){
			writer.println(String.format("%-45s", method) + String.format("%-20s", ":" + sInfo.getMap().get(method).getNumSites()) + String.format("%-20s", ":" + sInfo.getMap().get(method).getNumPredicates()));
		}
	}
	

	
//	public Map<String, Double> getMethodsMap() {
//		return methodsMap;
//	}

	public int getRuns() {
		return runs;
	}

	public int getK() {
		return k;
	}

//	public File getSitesFile() {
//		return sitesFile;
//	}
//
//	public String getProfilesFile() {
//		return profilesFolder;
//	}
//
//	public String getConsoleFile() {
//		return consoleFile;
//	}

	public List<PredicateItemWithImportance> getSortedPredictorsList() {
		return sortedPredictorsList;
	}


}
