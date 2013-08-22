package zuo.processor.functionentry.client.iterative;

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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.client.CBIClients;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Order;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class Client {
	public final static int fK = 5;
	public final static int iK = 3;
	public final static int fKM = 5;
	public final static int k = 1;
	
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final int round;
	final double percent;
	
	final Map<String, double[][][][]> results;
	final Map<String, double[][]> pResults;
	final Map<String, int[]> cResults;
	final Map<String, Set<Integer>> statistics;
	
	final Map<String, double[][][][]> resultsX;
	final Map<String, double[][]> pResultsX;
	final Map<String, int[]> cResultsX;
	final Map<String, Set<Integer>> statisticsX;
	
	public Client(File rootDir, String subject, File consoleFolder, int round, double percent) {
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		
		this.round = round;
		this.percent = percent;
		
		this.results = new HashMap<String, double[][][][]>();
		this.pResults = new HashMap<String, double[][]>();
		this.cResults = new HashMap<String, int[]>();
		this.statistics = new LinkedHashMap<String, Set<Integer>>();
		
		this.resultsX = new HashMap<String, double[][][][]>();
		this.pResultsX = new HashMap<String, double[][]>();
		this.cResultsX = new HashMap<String, int[]>();
		this.statisticsX = new LinkedHashMap<String, Set<Integer>>();
		
	}

	/**
	 * compute and print out the results for Sir subject excluding Siemens and space
	 */
	private void computeSirResults() {
		PrintWriter clientWriter = null;
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			clientWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.consoleFolder, this.subject + ".out"))));
			printSirResults(clientWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(clientWriter != null){
				clientWriter.close();
			}
		}
	}
	
	/**
	 * compute and print out the results of Siemens' subject including space
	 */
	private void computeSiemensResults() {
		PrintWriter clientWriter = null;
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			clientWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.consoleFolder, this.subject + ".out"))));
			printSiemensResults(clientWriter);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(clientWriter != null){
				clientWriter.close();
			}
		}
	}
	
	private void printSiemensResults(PrintWriter cWriter){
		File[] versions = new File(rootDir, subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length >= 10);
			}});
		Arrays.sort(versions, new Comparator<File>(){

			@Override
			public int compare(File arg0, File arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(arg0.getName().substring(1))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(1))));
			}});
		List<String> versionsList = new ArrayList<String>();
		for(File version: versions){
			String vi = version.getName();
			versionsList.add(vi);
			System.out.println(vi);
			cWriter.println(vi);
			
			SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File(version, vi + "_f.sites")));
			PredicateProfile[] fProfiles = new PredicateProfileReader(new File(rootDir, subject + "/traces/" + vi +"/fine-grained"), sInfo.getSites()).readProfiles();
//			getFullMethodsList(sInfo, new File(version, "adaptive"));
			
			FunctionEntrySites cSites = new FunctionEntrySites(new File(version, vi + "_c.sites"));
			FunctionEntryProfile[] cProfiles = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + vi + "/coarse-grained"), cSites).readFunctionEntryProfiles();
			
			CBIClients cs = null;
			IterativeFunctionClient client = null;
			Set<Integer> versionsSet = new LinkedHashSet<Integer>(); 
			Set<Integer> versionsSetX = new LinkedHashSet<Integer>(); 
			for(int i = 0; i < round; i++){
				System.out.println(i);
				while(true){
					cs = new CBIClients(sInfo, fProfiles, new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_cbi.out"), percent);
					if(cs.iszFlag()){
						break;
					}
				}
				client = new IterativeFunctionClient(cSites, 
						cProfiles, 
						new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_function.out"), 
						sInfo, 
						cs.getFullInstrumentedCBIClient(), 
						cs.getClientsMap(), 
						cWriter, 
						new File(version, "adaptive"));
				
				if(cs.iscFlag() && client.ispFlag()){
					versionsSet.add(i);
					if(!pResults.containsKey(vi) || client.getpResult()[Score.H_2.ordinal()][0] > pResults.get(vi)[Score.H_2.ordinal()][0]){
						results.put(vi, client.getResult());
						pResults.put(vi, client.getpResult());
						cResults.put(vi, client.getcResult());
					}
				}
				if(client.iscPFlag()){
					assert(cs.iscFlag() && client.ispFlag());
					versionsSetX.add(i);
					if(!pResultsX.containsKey(vi) || client.getpResult()[Score.H_2.ordinal()][0] > pResultsX.get(vi)[Score.H_2.ordinal()][0]){
						resultsX.put(vi, client.getResult());
						pResultsX.put(vi, client.getpResult());
						cResultsX.put(vi, client.getcResult());
					}
				}
			}
			assert(versionsSet.containsAll(versionsSetX));
			statistics.put(vi, versionsSet);
			statisticsX.put(vi, versionsSetX);
			
			System.out.println();
			cWriter.println();	
		}
		
		printFinalResults(cWriter, results, pResults, cResults, statistics);
		printRoundsInfo(cWriter, statistics);
		System.out.println("\n");
		cWriter.println("\n");
		printFinalResults(cWriter, resultsX, pResultsX, cResultsX, statisticsX);
		printRoundsInfo(cWriter, statisticsX);
	}

	private void getFullMethodsList(SitesInfo sInfo, File folder) {
		// TODO Auto-generated method stub
		PrintWriter out = null;
		try{
			if (!folder.exists()) {
				folder.mkdirs();
			}
			//write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(new File(folder, "full"))));
			for(String method: sInfo.getMap().keySet()){
				out.println(method);
			}
			out.close();
			
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}


	private void printSirResults(PrintWriter cWriter){
		List<String> versionsList = new ArrayList<String>();
		
		File[] versions = new File(rootDir, subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name);
			}});
		Arrays.sort(versions, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(o1.getName().substring(1))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(1))));
			}});
		
		for(File version: versions){
			File[] subversions = version.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 11);
				}});
			Arrays.sort(subversions, new Comparator<File>(){

				@Override
				public int compare(File o1, File o2) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(o1.getName().substring(4))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(4))));
				}});
			
			for(File subversion: subversions){
				String vi = version.getName() + "_" + subversion.getName();
				versionsList.add(vi);
				System.out.println(vi);
				cWriter.println(vi);
				
				SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File(subversion, vi + "_f.sites")));
				PredicateProfile[] fProfiles = new PredicateProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained"), sInfo.getSites()).readProfiles();
//				getFullMethodsList(sInfo, new File(subversion, "adaptive"));
				
				FunctionEntrySites cSites = new FunctionEntrySites(new File(subversion, vi + "_c.sites"));
				FunctionEntryProfile[] cProfiles = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), cSites).readFunctionEntryProfiles();
				
				CBIClients cs = null;
				IterativeFunctionClient client = null;
				Set<Integer> versionsSet = new LinkedHashSet<Integer>(); 
				Set<Integer> versionsSetX = new LinkedHashSet<Integer>(); 
				for(int i = 0; i < round; i++){
					while(true){
						cs = new CBIClients(sInfo, fProfiles, new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_cbi.out"), percent);
						if(cs.iszFlag()){
							break;
						}
					} 
					
					client = new IterativeFunctionClient(cSites, 
							cProfiles, 
							new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_function.out"), 
							sInfo, 
							cs.getFullInstrumentedCBIClient(), 
							cs.getClientsMap(), 
							cWriter, 
							new File(subversion, "adaptive"));
					
					if(cs.iscFlag() && client.ispFlag()){
						versionsSet.add(i);
						if(!pResults.containsKey(vi) || client.getpResult()[Score.H_2.ordinal()][0] > pResults.get(vi)[Score.H_2.ordinal()][0]){
							results.put(vi, client.getResult());
							pResults.put(vi, client.getpResult());
							cResults.put(vi, client.getcResult());
						}
					}
					if(client.iscPFlag()){
						assert(cs.iscFlag() && client.ispFlag());
						versionsSetX.add(i);
						if(!pResultsX.containsKey(vi) || client.getpResult()[Score.H_2.ordinal()][0] > pResultsX.get(vi)[Score.H_2.ordinal()][0]){
							resultsX.put(vi, client.getResult());
							pResultsX.put(vi, client.getpResult());
							cResultsX.put(vi, client.getcResult());
						}
					}
					
				}
				assert(versionsSet.containsAll(versionsSetX));
				statistics.put(vi, versionsSet);
				statisticsX.put(vi, versionsSetX);
				
				cWriter.println();	
			}
			
		}
		
		
		printFinalResults(cWriter, results, pResults, cResults, statistics);
		printRoundsInfo(cWriter, statistics);
		cWriter.println("\n");
		printFinalResults(cWriter, resultsX, pResultsX, cResultsX, statisticsX);
		printRoundsInfo(cWriter, statisticsX);
	}
	

	private void printRoundsInfo(PrintWriter cWriter, Map<String, Set<Integer>> statistics) {
		cWriter.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		// TODO Auto-generated method stub
		int sumSize = 0;
		cWriter.println("The stabilization information is as follows:\n--------------------------------------------------------------");
		for(String version: statistics.keySet()){
			int size = statistics.get(version).size();
			sumSize += size;
			cWriter.println(String.format("%-15s", version) 
					+ String.format("%-10s", size)
					+ String.format("%-10s", new DecimalFormat(".##").format((double)size / round))
					+ CBIClient.compressNumbers(statistics.get(version))
					);
		}
		cWriter.println();
		double mean = (double)sumSize / statistics.size();
		cWriter.println("The average information is as follows:\n--------------------------------------------------------------");
		cWriter.println(String.format("%-10s", new DecimalFormat(".##").format(mean))
				+ String.format("%-10s", new DecimalFormat(".##").format(mean / round))
				);
	}
	
//	/**print the results ordered by results[H_2][Less_First][1][0]
//	 * @param cWriter
//	 */
//	private void printFinalResults(PrintWriter cWriter, Map<String, double[][][][]> results, Map<String, double[][]> pResults, Map<String, int[]> cResults) {
//		// TODO Auto-generated method stub
//		List<Map.Entry<String, double[][][][]>> rList = new ArrayList<Map.Entry<String, double[][][][]>>(results.entrySet());
//		Collections.sort(rList, new Comparator<Map.Entry<String, double[][][][]>>(){
//
//			private double getSortValue(Entry<String, double[][][][]> entry) {
//				// TODO Auto-generated method stub
//				double[][][][] array = entry.getValue();
//				return array[Score.H_2.ordinal()][Order.LESS_FIRST.ordinal()][1][0];
//			}
//
//			@Override
//			public int compare(Entry<String, double[][][][]> o1,
//					Entry<String, double[][][][]> o2) {
//				// TODO Auto-generated method stub
//				double d1 = getSortValue(o1),
//						d2 = getSortValue(o2);
//				return new Double(d1).compareTo(new Double(d2));
//			}});
//		
//		Set<String> versions = new LinkedHashSet<String>();
//		double[][][][] result = new double[Score.values().length][Order.values().length][2][5];
//		double[][] pResult = new double[Score.values().length][5];
//		int[] cResult = new int[3];
//		for(int i = 0; i < rList.size(); i++){
//			Entry<String, double[][][][]> entry = rList.get(i);
//			versions.add(entry.getKey());
//			assert(i + 1 == versions.size());
//			accumulateResult(result, results.get(entry.getKey()));
//			accumulatePResult(pResult, pResults.get(entry.getKey()));
//			accumulateCResult(cResult, cResults.get(entry.getKey()));
//			print(versions, result, pResult, cResult, cWriter);
//		}
//	}

	/**print the results ordered by pResutls[H_2][0]
	 * @param cWriter
	 */
	/**
	 * @param cWriter
	 * @param statistics 
	 * @param cResults 
	 * @param pResults
	 * @param results 
	 */
	private void printFinalResults(PrintWriter cWriter, Map<String, double[][][][]> results, Map<String, double[][]> pResults, Map<String, int[]> cResults, Map<String, Set<Integer>> statistics) {
		// TODO Auto-generated method stub
		cWriter.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		List<Map.Entry<String, double[][]>> pRList = new ArrayList<Map.Entry<String, double[][]>>(pResults.entrySet());
		Collections.sort(pRList, new Comparator<Map.Entry<String, double[][]>>(){

			private double getSortValue(Entry<String, double[][]> entry) {
				// TODO Auto-generated method stub
				double[][] array = entry.getValue();
				return array[Score.H_2.ordinal()][0];
			}

			@Override
			public int compare(Entry<String, double[][]> o1,
					Entry<String, double[][]> o2) {
				// TODO Auto-generated method stub
				double d1 = getSortValue(o1),
						d2 = getSortValue(o2);
				return new Double(d1).compareTo(new Double(d2));
			}});
		
		Set<String> versions = new LinkedHashSet<String>();
		double[][][][] result = new double[Score.values().length][Order.values().length][2][5];
		double[][] pResult = new double[Score.values().length][5];
		int[] cResult = new int[3];
		int sumRounds = 0;
		for(int i = 0; i < pRList.size(); i++){
			String version = pRList.get(i).getKey();
			versions.add(version);
			assert(i + 1 == versions.size());
			accumulateResult(result, results.get(version));
			accumulatePResult(pResult, pResults.get(version));
			accumulateCResult(cResult, cResults.get(version));
			sumRounds += statistics.get(version).size();
			print(versions, result, pResult, cResult, sumRounds, cWriter);
		}
	}

	private void accumulateCResult(int[] cResult, int[] is) {
		// TODO Auto-generated method stub
		assert(cResult.length == is.length);
		for (int i = 0; i < cResult.length; i++) {
			cResult[i] += is[i];
		}
	}

	private void accumulatePResult(double[][] pResult, double[][] ds) {
		// TODO Auto-generated method stub
		assert(pResult.length == ds.length && pResult.length == Score.values().length);
		for(int i = 0; i < pResult.length; i++){
			for (int j = 0; j < pResult[i].length; j++) {
				pResult[i][j] += ds[i][j];
			}
		}
	}
	
	private void accumulateResult(double[][][][] result, double[][][][] ds) {
		// TODO Auto-generated method stub
		assert(result.length == ds.length && result.length == Score.values().length);
		for(int i = 0; i < result.length; i++){
			for (int j = 0; j < result[i].length; j++) {
				for(int p = 0; p < result[i][j].length; p++){
					for (int q = 0; q < result[i][j][p].length; q++) {
						result[i][j][p][q] += ds[i][j][p][q];
					}
				}
			}
		}
	}

	private void print(Set<String> versions, double[][][][] result, double[][] pResult, int[] cResult, int sumRounds, PrintWriter cWriter) {
		// TODO Auto-generated method stub
		cWriter.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
		cWriter.println("On average:\t" 
				+ String.format("%-20s", "methods:" + cResult[0] / versions.size())
				+ String.format("%-20s", "sites:" + cResult[1] / versions.size())
				+ String.format("%-20s", "predicates:" + cResult[2] / versions.size())
				+ String.format("%-20s", "rounds:" + new DecimalFormat(".##").format((double)sumRounds / versions.size()))
				+ String.format("%-20s", "rounds%:" + new DecimalFormat(".##").format((double)sumRounds / versions.size() / round))
				);
		cWriter.println("\n==============================================================");
		for (int m = 0; m < result.length; m++) {
			for (int n = 0; n < result[m].length; n++) {
				String mode = "<" + Score.values()[m] + "," + Order.values()[n] + ">";
				cWriter.println("The information of average sites and predicates need to be instrumented " + mode + " are as follows:\n--------------------------------------------------------------");
				cWriter.println("Excluding\t" 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(result[m][n][0][0] / versions.size()))
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(result[m][n][0][1] / versions.size()))
						+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(result[m][n][0][2] / versions.size())) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(result[m][n][0][3] / versions.size())) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(result[m][n][0][4] / versions.size())) 
						);
				cWriter.println("Including\t" 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(result[m][n][1][0] / versions.size()))
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(result[m][n][1][1] / versions.size()))
						+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(result[m][n][1][2] / versions.size())) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(result[m][n][1][3] / versions.size())) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(result[m][n][1][4] / versions.size())) 
						);
				cWriter.println();
			}
			
			cWriter.println("==============================================================");
			cWriter.println("The prune case by <" + Score.values()[m] + ">:\t\t"
							+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(pResult[m][0] / versions.size()))
							+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pResult[m][1] / versions.size()))
							+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(pResult[m][2] / versions.size()))
							+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(pResult[m][3] / versions.size())) 
							+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(pResult[m][4] / versions.size())));
			cWriter.println("\n");
		}
		cWriter.println("\n");
	}

	

	public static void main(String[] args) {
		String[][] argvs = {
				{"809", "grep"},
				{"213", "gzip"},
				{"363", "sed"},
				{"13585", "space"},
				{"1608", "tcas"},
				{"1052", "totinfo"},
				{"5542", "replace"},
				{"4130", "printtokens"},
				{"4115", "printtokens2"},
				{"2650", "schedule"},
				{"2710", "schedule2"}
		};
		
		if(args.length != 6 && args.length != 5){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
			}
			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) rootDir subject consoleDir(excluding /) round percent" +
					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir consoleDir(excluding /) round percent");
			return;
		}
		
		if(args.length == 6){
			Client c = new Client(new File(args[1]), args[2], new File(args[3] + "_" + args[4] + "_" + args[5]), Integer.parseInt(args[4]), Double.parseDouble(args[5]));
			if(Integer.parseInt(args[0]) == 0){
				c.computeSiemensResults();
			}
			else if(Integer.parseInt(args[0]) == 1){
				c.computeSirResults();
			}
		}
		else if(args.length == 5){
			assert(Integer.parseInt(args[0]) == 0);
			for(int i = 4; i < argvs.length; i++){
				Client c = new Client(new File(args[1]), argvs[i][1], new File(args[2] + "_" + args[3] + "_" + args[4], argvs[i][1]), Integer.parseInt(args[3]), Double.parseDouble(args[4]));
				c.computeSiemensResults();
			}
		}

//		Client cc;
//		String s = "";
//		for(int j = 1; j < 2; j++){
//			s += "_";
//			
//			cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "gzip", 
//					new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/gzip" + s + Client.percent + "/"));
//			cc.computeSirResults();
//			cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "sed", 
//					new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/sed" + s + Client.percent + "/"));
//			cc.computeSirResults();
//			cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "grep", 
//					new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/grep" + s + Client.percent + "/"));
//			cc.computeSirResults();
//			cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "space", 
//					new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/space" + s + Client.percent + "/"));
//			cc.computeSiemensResults();
//			for(int i = 4; i < argvs.length; i++){
//				cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/Siemens/"), argvs[i][1], 
//						new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/Siemens" + s + Client.percent + "/" + argvs[i][1] + "/"));
//				cc.computeSiemensResults();
//			}
//		}
	}
	
}
