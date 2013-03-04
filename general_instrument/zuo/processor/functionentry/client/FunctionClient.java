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
		F_1, F, S
	}
	
	final int runs;
	
	final String sitesFile;
	final String profilesFolder;
	final String consoleFile;
	
	final SitesInfo sInfo;
	final List<Map.Entry<PredicateItem, Double>> predictors;
	
	final int[][] result;
	
	
	public FunctionClient(int runs, String sitesFile, String profilesFolder, String consoleFile, SitesInfo sInfo, List<Map.Entry<PredicateItem, Double>> predictors) {
		this.runs = runs;
		this.sitesFile = sitesFile;
		this.profilesFolder = profilesFolder;
		this.consoleFile = consoleFile;
		this.sInfo = sInfo;
		this.predictors = predictors;
		this.result = new int[3][6];
		
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
	
	public FunctionClient(int runs, String rootDir, String subject, String version, String consoleFolder, SitesInfo sInfo, List<Map.Entry<PredicateItem, Double>> predictors){
		this.runs = runs;
		this.sitesFile = rootDir + subject + "/versions/" + version + "/" + version + "_c.sites";
		this.profilesFolder = rootDir + subject + "/traces/" + version + "/coarse-grained";
		this.consoleFile = consoleFolder + subject + "_" + version + "_function.out"; 
		this.sInfo = sInfo;
		this.predictors = predictors;
		this.result = new int[3][6];
		
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
	
	public FunctionClient(int runs, String rootDir, String subject, String version, String consoleFolder){
		this.runs = runs;
		this.sitesFile = rootDir + subject + "/versions/" + version + "/" + version + "_c.sites";
		this.profilesFolder = rootDir + subject + "/traces/" + version + "/coarse-grained";
		this.consoleFile = consoleFolder + subject + "_" + version + "_function.out";
		
		this.sInfo = new SitesInfo(new InstrumentationSites(new File(rootDir + subject + "/versions/" + version + "/" + version + "_f.sites")));
		CBIClient c = new CBIClient(runs, TOP_K, rootDir + subject + "/versions/" + version + "/" + version + "_f.sites", 
				rootDir + subject + "/traces/" + version +"/fine-grained", consoleFolder + subject + "_" + version + "_cbi.out");
		this.predictors = c.getPredictorEntryList();
		
		this.result = new int[3][6];
		
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
		final String rootDir = "/home/sunzzq/Research/Automated_Debugging/Subjects/Siemens/";
		final String subject = "printtokens";
		final String version = "v1";
		final String consoleFolder = "/home/sunzzq/Console/";
		
		FunctionClient client = new FunctionClient(2717, rootDir, subject, version, consoleFolder);
		
		for (int i = 0; i < client.result.length; i++) {
			for (int j = 0; j < client.result[i].length; j++) {
				System.out.print(client.result[i][j] + "\t");
			}
			System.out.println();
		}
		
		
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
		
		writer.println("\n");
		printByFScoreOrder(processor.getFrequencyMap(), sInfo, writer);
		System.out.println();
		writer.println();
		printPercentageByFScoreOrder(processor.getFrequencyMap(), sInfo, writer);
		writer.println("\n");
		printByNegative(processor.getFrequencyMap(), sInfo, writer);
		writer.println();
		printPercentageByNegative(processor.getFrequencyMap(), sInfo, writer);
		writer.println("\n");
		printByPositive(processor.getFrequencyMap(), sInfo, writer);
		writer.println();
		printPercentageByPositive(processor.getFrequencyMap(), sInfo, writer);
		
	}


	/**print the methods in the increasing order of positive value
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printByPositive(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getPositive())
				.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getPositive()));
				if(r == 0){
					return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				}
				return r;
			}});
		writer.println("The methods ordered by positive are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
	}

	private void printPercentageByPositive(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getPositive())
				.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getPositive()));
				if(r == 0){
					return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				}
				return r;
			}});
		System.out.println("The information of sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.S);
	}


	/**print the methods in the descending order of negative value
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printByNegative(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getNegative())
				.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getNegative()));
				if(r == 0){
					return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				}
				return r;
			}});
		writer.println("The methods ordered by negative are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
	}

	private void printPercentageByNegative(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getNegative())
				.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getNegative()));
				if(r == 0){
					return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				}
				return r;
			}});
		System.out.println("The information of sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.F);
	}

	/**print the methods in the descending order of F-score value
	 * @param frequencyMap
	 * @param sInfo
	 * @param writer
	 */
	private void printByFScoreOrder(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
			}});
		writer.println("The methods ordered by F-score are as follows:\n--------------------------------------------------------------");
		printEntry(sInfo, writer, list);
	}
	
	private void printPercentageByFScoreOrder(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
			}});
		System.out.println("The information of sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		printPercentage(sInfo, writer, list, Score.F_1);
	}

	private void printEntry(SitesInfo sInfo, PrintWriter writer, List list) {
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				writer.println(String.format("%-25s", method) + entry.getValue().toString() + "\t\t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				writer.println(String.format("%-25s", method) + entry.getValue().toString());
			}
		}
	}

	private void printPercentage(SitesInfo sInfo, PrintWriter writer, List list, Score f) {
		int nSites = 0, nPredicates = 0;
		String m = this.predictors.get(0).getKey().getSite().getFunctionName();
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			//percentage information of instrumented sites and predicates
			if(m.equals(method)){
				System.out.println("Excluding " + m + "\t\ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "    \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				writer.println("Excluding " + m + "\t\ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "    \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				result[f.ordinal()][0] = nSites;
				result[f.ordinal()][1] = nPredicates;
				
				nSites += sInfo.getMap().get(method).getNumSites();
				nPredicates += sInfo.getMap().get(method).getNumPredicates();

				System.out.println("Including " + m + "\t\ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "    \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				System.out.println();
				writer.println("Including " + m + "\t\ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "    \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				writer.println();
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
			writer.println(String.format("%-25s", method) + ":" + sInfo.getMap().get(method).getNumSites() + "\t:" + sInfo.getMap().get(method).getNumPredicates());
		}
	}

	public int getRuns() {
		return runs;
	}

	public String getSitesFile() {
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
	

	
}
