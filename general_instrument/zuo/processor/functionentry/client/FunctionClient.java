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
	static enum Score{
		F_1, F, S
	}
	
	final int runs;
	final String version;
	
	final String sitesFile;
	final String profilesFile;
	final String consoleFile;
	
	final SitesInfo sInfo;
	final Set<String> methods;
	final List<Map.Entry<PredicateItem, Double>> predictors;
	
	static Map<String, int[][]> results = new HashMap<String, int[][]>();
	
	
	public FunctionClient(int runs, String sitesFile, String profilesFile, String consoleFile, SitesInfo sInfo, Set<String> methods, List predictors, String ver) {
		this.runs = runs;
		this.version = ver;
		
		this.sitesFile = sitesFile;
		this.profilesFile = profilesFile;
		this.consoleFile = consoleFile;
		
		this.sInfo = sInfo;
		this.methods = methods;
		this.predictors = predictors;
		
		int[][] array = new int[3][6];
		results.put(this.version, array);
		
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
		File[] versions = new File("/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name);
			}});
		Arrays.sort(versions, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(((File) arg0).getName().substring(1))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(1))));
			}});
		
		for(File version: versions){
			if(version.isDirectory() && version.listFiles().length == 10){
				String vi = version.getName();
				System.out.println();
				System.out.println(vi);
				SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File("/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/" + vi + "/" + vi + "_f.sites")));
				CBIClient c = new CBIClient(2717, 10, "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/" + vi + "/" + vi + "_f.sites", 
						"/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/" + vi +"/fine-grained", "/home/sunzzq/Console/space_" + vi + "_cbi.out");
				FunctionClient client = new FunctionClient(2717, "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/" + vi + "/" + vi + "_c.sites", 
						"/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/" + vi + "/coarse-grained", "/home/sunzzq/Console/space_" + vi + "_function.out", sInfo, c.getMethods(), c.getPredictorEntryList(), vi);
				
				for (int i = 0; i < results.get(vi).length; i++) {
					for (int j = 0; j < results.get(vi)[i].length; j++) {
						System.out.print(results.get(vi)[i][j] + "\t");
					}
					System.out.println();
				}
			}
		}
		
		double[][] sum = new double[3][4];
		for(String version: results.keySet()){
			int[][] array = results.get(version);
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length - 2; j++) {
					sum[i][j] += (double) array[i][j]/array[i][j%2 + 4];
				}
			}
		}
		for (int i = 0; i < sum.length; i++) {
			for (int j = 0; j < sum[i].length; j++) {
				System.out.print(sum[i][j]/(results.size()) + "\t");
			}
			System.out.println();
		}
		
	}
	
	private void printResults(PrintWriter writer){
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(runs);
		SelectingProcessor processor = new SelectingProcessor(profiles);
		processor.process();
		
//		System.out.println("\n");
//		System.out.println("The general runs information are as follows:\n==============================================================");
//		System.out.println("Total number of runs:\t\t\t\t" + runs);
//		System.out.println("Total number of negative runs:\t\t\t" + processor.getTotalNegative());
//		System.out.println("Total number of positive runs:\t\t\t" + processor.getTotalPositive());
		assert(processor.getTotalNegative() + processor.getTotalPositive() == runs);
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println("Total number of runs:\t\t\t\t" + runs);
		writer.println("Total number of negative runs:\t\t\t" + processor.getTotalNegative());
		writer.println("Total number of positive runs:\t\t\t" + processor.getTotalPositive());
		
//		System.out.println("\n");
		writer.println("\n");
		printSitesInfo(sInfo, writer);
		
//		System.out.println("\n");
//		System.out.println("The general methods information are as follows:\n==============================================================");
//		System.out.println("Total number of methods instrumented:\t\t" + sites.getNumFunctionEntrySites());
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		writer.println("\n");
		writer.println("The general methods information are as follows:\n==============================================================");
		writer.println("Total number of methods instrumented:\t\t" + sites.getNumFunctionEntrySites());
		
//		System.out.println("\n");
		writer.println("\n");
		printByFScoreOrder(processor.getFrequencyMap(), sInfo, writer);
		System.out.println();
		writer.println();
		printPercentageByFScoreOrder(processor.getFrequencyMap(), sInfo, writer);
//		System.out.println("\n");
		writer.println("\n");
		printByNegative(processor.getFrequencyMap(), sInfo, writer);
//		System.out.println();
		writer.println();
		printPercentageByNegative(processor.getFrequencyMap(), sInfo, writer);
//		System.out.println("\n");
		writer.println("\n");
		printByPositive(processor.getFrequencyMap(), sInfo, writer);
//		System.out.println();
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
//		System.out.println("The methods ordered by positive are as follows:\n--------------------------------------------------------------");
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
//		System.out.println("The methods ordered by negative are as follows:\n--------------------------------------------------------------");
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
//		System.out.println("The methods ordered by F-score are as follows:\n--------------------------------------------------------------");
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
//				System.out.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
				writer.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
//				System.out.println(method + "    \t" + entry.getValue().toString());
				writer.println(method + "    \t" + entry.getValue().toString());
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
				System.out.println("Excluding " + m + "   \ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "     \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				writer.println("Excluding " + m + "   \ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "     \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				results.get(version)[f.ordinal()][0] = nSites;
				results.get(version)[f.ordinal()][1] = nPredicates;
				
				nSites += sInfo.getMap().get(method).getNumSites();
				nPredicates += sInfo.getMap().get(method).getNumPredicates();

				System.out.println("Including " + m + "   \ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "     \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				System.out.println();
				writer.println("Including " + m + "   \ts:" + nSites + "\ts%:" + new DecimalFormat("##.##").format((double) 100 * nSites/sInfo.getNumPredicateSites())
						+ "     \tp:" + nPredicates + "\tp%:" + new DecimalFormat("##.##").format((double)100 * nPredicates/sInfo.getNumPredicateItems()));
				writer.println();
				results.get(version)[f.ordinal()][2] = nSites;
				results.get(version)[f.ordinal()][3] = nPredicates;
				
				results.get(version)[f.ordinal()][4] = sInfo.getNumPredicateSites();
				results.get(version)[f.ordinal()][5] = sInfo.getNumPredicateItems();
				
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
		// TODO Auto-generated method stub
//		System.out.println("The general sites information are as follows:\n==============================================================");
//		System.out.println("Total number of sites instrumented:\t\t\t" + sInfo.getNumPredicateSites());
//		System.out.println("Total number of predicates instrumented:\t\t" + sInfo.getNumPredicateItems());
//		System.out.println("Total number of methods having sites instrumented:\t" + sInfo.getMap().size());
//		System.out.println();
//		System.out.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		
		writer.println("The general sites information are as follows:\n==============================================================");
		writer.println("Total number of sites instrumented:\t\t\t" + sInfo.getNumPredicateSites());
		writer.println("Total number of predicates instrumented:\t\t" + sInfo.getNumPredicateItems());
		writer.println("Total number of methods having sites instrumented:\t" + sInfo.getMap().size());
		writer.println();
		writer.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		for(String method: sInfo.getMap().keySet()){
//			System.out.println(method + "     \t:" + sInfo.getMap().get(method).getNumSites() + "\t:" + sInfo.getMap().get(method).getNumPredicates());
			writer.println(method + "     \t:" + sInfo.getMap().get(method).getNumSites() + "\t:" + sInfo.getMap().get(method).getNumPredicates());
		}
		
	}
	

}
