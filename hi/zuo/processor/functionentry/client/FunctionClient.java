package zuo.processor.functionentry.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.processor.SelectingProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class FunctionClient {
	public static final int TOP_K = 10;

	static enum Score{
		F_1, Specificity, F, S
	}
	
	final int runs;
	
	final File sitesFile;
	final String profilesFolder;
	final String consoleFile;
	
	final SitesInfo sInfo;
	final List<Map.Entry<PredicateItem, Double>> predictors;
	final String method;
	
	final int[][] result;
	final PrintWriter clientWriter;
	
	
	public FunctionClient(int runs, File sitesFile, String profilesFolder, String consoleFile, SitesInfo sInfo, List<Map.Entry<PredicateItem, Double>> predictors, PrintWriter cWriter) {
		this.runs = runs;
		this.sitesFile = sitesFile;
		this.profilesFolder = profilesFolder;
		this.consoleFile = consoleFile;
		this.sInfo = sInfo;
		this.predictors = predictors;
		this.method = this.predictors.get(0).getKey().getSite().getFunctionName();
		this.result = new int[4][6];
		this.clientWriter = cWriter;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults(writer);
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
	
	public FunctionClient(int runs, String rootDir, String subject, String version, String consoleFolder, SitesInfo sInfo, List<Map.Entry<PredicateItem, Double>> predictors, PrintWriter cWriter){
		this.runs = runs;
		this.sitesFile = new File(rootDir + subject + "/versions/" + version + "/" + version + "_c.sites");
		this.profilesFolder = rootDir + subject + "/traces/" + version + "/coarse-grained";
		this.consoleFile = consoleFolder + subject + "_" + version + "_function.out"; 
		this.sInfo = sInfo;
		this.predictors = predictors;
		this.method = this.predictors.get(0).getKey().getSite().getFunctionName();
		this.result = new int[4][6];
		this.clientWriter = cWriter;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults(writer);
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
	
	public FunctionClient(int runs, String rootDir, String subject, String version, String consoleFolder, PrintWriter cWriter){
		this.runs = runs;
		this.sitesFile = new File(rootDir + subject + "/versions/" + version + "/" + version + "_c.sites");
		this.profilesFolder = rootDir + subject + "/traces/" + version + "/coarse-grained";
		this.consoleFile = consoleFolder + subject + "_" + version + "_function.out";
		
		this.sInfo = new SitesInfo(new InstrumentationSites(new File(rootDir + subject + "/versions/" + version + "/" + version + "_f.sites")));
		CBIClient c = new CBIClient(runs, TOP_K, this.sInfo.getSites().getSitesFile(), 
				rootDir + subject + "/traces/" + version +"/fine-grained", consoleFolder + subject + "_" + version + "_cbi.out");
		this.predictors = c.getPredictorEntryList();
		this.method = this.predictors.get(0).getKey().getSite().getFunctionName();
		
		this.result = new int[4][6];
		this.clientWriter = cWriter;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults(writer);
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
//		final String rootDir = "/home/sunzzq/Research/Automated_Debugging/Subjects/";
//		final String subject = "space";
//		final String version = "v3";
//		final String consoleFolder = "/home/sunzzq/Console/";
//		
//		FunctionClient client = new FunctionClient(4000, rootDir, subject, version, consoleFolder, null);
//		
//		for (int i = 0; i < client.result.length; i++) {
//			for (int j = 0; j < client.result[i].length; j++) {
//				System.out.print(client.result[i][j] + "\t");
//			}
//			System.out.println();
//		}
	}

	
	private void printResults(PrintWriter writer){
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFolder, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(runs);
		SelectingProcessor processor = new SelectingProcessor(profiles);
		processor.process();
		
		assert(processor.getTotalNegative() + processor.getTotalPositive() == runs);
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of runs:") + runs);
		writer.println(String.format("%-50s", "Total number of negative runs:") + processor.getTotalNegative());
		writer.println(String.format("%-50s", "Total number of positive runs:") + processor.getTotalPositive());
		
		writer.println("\n");
		printSitesInfo(sInfo, writer);
		
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		writer.println("\n");
		writer.println("The general methods information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of methods instrumented:") + sites.getNumFunctionEntrySites());
		
		System.out.println();
		writer.println("\n");
		printEntryAndPercentageByFScore(processor.getFrequencyMap(), sInfo, writer);
		writer.println("\n");
		printEntryAndPercentageBySpecificity(processor.getFrequencyMap(), sInfo, writer);
		writer.println("\n");
		printEntryAndPercentageByNegative(processor.getFrequencyMap(), sInfo, writer);
		writer.println("\n");
		printEntryAndPercentageByPositive(processor.getFrequencyMap(), sInfo, writer);
		
	}
	
	/**print the methods in the descending order of F-score value and the corresponding percentage to be instrumented
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printEntryAndPercentageByFScore(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				if (r == 0) {
					int rr = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getNegative())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getNegative()));
					rr = order(arg0, arg1, rr);
					return rr;
				}
				return r;
			}});
		writer.println("The methods ordered by F-score are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
		writer.println();
		System.out.println("The information of sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		this.clientWriter.println("The information of sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.F_1);
	}

	/**print the methods in the descending order of Specificity value and the corresponding percentage to be instrumented
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printEntryAndPercentageBySpecificity(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getSpecificity())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getSpecificity()));
				if (r == 0) {
					int rr = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
					rr = order(arg0, arg1, rr);
					return rr;
				}
				return r;
			}});
		writer.println("The methods ordered by Specificity are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
		writer.println();
		System.out.println("The information of sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		this.clientWriter.println("The information of sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.Specificity);
	}
	
	/**print the method entry in the descending order of negative value and the corresponding percentage to be instrumented
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printEntryAndPercentageByNegative(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getNegative())
					.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getNegative()));
				if(r == 0){
					int rr = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
					rr = order(arg0, arg1, rr);
					return rr;
				}
				return r;
			}});
		writer.println("The methods ordered by Negative are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
		writer.println();
		System.out.println("The information of sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		this.clientWriter.println("The information of sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.F);
	}

	/**print the method entry in the increasing order of positive value and the corresponding percentage to be instrumented
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printEntryAndPercentageByPositive(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer){
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getPositive())
					.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getPositive()));
				if(r == 0){
					int rr = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
					rr = order(arg0, arg1, rr);
					return rr;
				}
				return r;
			}});
		writer.println("The methods ordered by Positive are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
		writer.println();
		System.out.println("The information of sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		this.clientWriter.println("The information of sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.S);
	}


	private void printEntry(SitesInfo sInfo, PrintWriter writer, List list) {
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				writer.println(String.format("%-45s", method) + entry.getValue().toString() + "\t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				writer.println(String.format("%-45s", method) + entry.getValue().toString());
			}
		}
	}

	private void printPercentage(SitesInfo sInfo, PrintWriter writer, List list, Score f) {
		int nSites = 0, nPredicates = 0;
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			//percentage information of instrumented sites and predicates
			if(this.method.equals(method)){
				System.out.println(String.format("%-45s", "Excluding " + method) + String.format("%-20s", "\t\ts:" + nSites) + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites()))
						+ String.format("%-20s", "p:" + nPredicates) + String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems())));
				writer.println(String.format("%-45s", "Excluding " + method) + String.format("%-20s", "\t\ts:" + nSites) + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites()))
						+ String.format("%-20s", "p:" + nPredicates) + String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems())));
				this.clientWriter.println(String.format("%-45s", "Excluding " + method) + String.format("%-20s", "\t\ts:" + nSites) + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites()))
						+ String.format("%-20s", "p:" + nPredicates) + String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems())));
				result[f.ordinal()][0] = nSites;
				result[f.ordinal()][1] = nPredicates;
				
				nSites += sInfo.getMap().get(method).getNumSites();
				nPredicates += sInfo.getMap().get(method).getNumPredicates();

				System.out.println(String.format("%-45s", "Including " + method) + String.format("%-20s", "\t\ts:" + nSites) + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites()))
						+ String.format("%-20s", "p:" + nPredicates) + String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems())));
				System.out.println();
				writer.println(String.format("%-45s", "Including " + method) + String.format("%-20s", "\t\ts:" + nSites) + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites()))
						+ String.format("%-20s", "p:" + nPredicates) + String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems())));
				writer.println();
				this.clientWriter.println(String.format("%-45s", "Including " + method) + String.format("%-20s", "\t\ts:" + nSites) + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites()))
						+ String.format("%-20s", "p:" + nPredicates) + String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems())));
				this.clientWriter.println();
				
				result[f.ordinal()][2] = nSites;
				result[f.ordinal()][3] = nPredicates;
				
				result[f.ordinal()][4] = sInfo.getNumPredicateSites();
				result[f.ordinal()][5] = sInfo.getNumPredicateItems();
				
				break;
			}
			else{
				if (sInfo.getMap().containsKey(method)) {
					nSites += sInfo.getMap().get(method).getNumSites();
					nPredicates += sInfo.getMap().get(method).getNumPredicates();
				}
			}
		}
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

	public int getRuns() {
		return runs;
	}

	public File getSitesFile() {
		return sitesFile;
	}

	public String getProfilesFolder() {
		return profilesFolder;
	}

	public String getConsoleFile() {
		return consoleFile;
	}

	public SitesInfo getsInfo() {
		return sInfo;
	}

	public List<Map.Entry<PredicateItem, Double>> getPredictors() {
		return predictors;
	}

	public int[][] getResult() {
		return result;
	}

	private int order(Object arg0, Object arg1, int rr) {
		if(rr == 0){
			if(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getKey().getFunctionName().equals(method)){
				rr = 1;
			}
			if(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getKey().getFunctionName().equals(method)){
				rr = -1;
			}
		}
		return rr;
	}
	

	
}