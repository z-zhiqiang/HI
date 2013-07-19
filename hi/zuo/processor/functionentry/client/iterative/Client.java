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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Order;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class Client {
	final int runs;
	final String rootDir;
	final String subject;
	final String consoleFolder;
	
	final Map<String, double[][][][]> results;
	final Map<String, double[][]> pResults;
	final Map<String, double[][]> wResults;
	final Map<String, int[]> cResults;
	
	public Client(int runs, String rootDir, String subject, String consoleFolder) {
		this.runs = runs;
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		this.results = new HashMap<String, double[][][][]>();
		this.pResults = new HashMap<String, double[][]>();
		this.wResults = new HashMap<String, double[][]>();
		this.cResults = new HashMap<String, int[]>();
	}

	/**
	 * compute and print out the results for Sir subject excluding Siemens and space
	 */
	private void computeSirResults() {
		PrintWriter clientWriter = null;
		try {
			File file = new File(this.consoleFolder);
			if(!file.exists()){
				file.mkdirs();
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
			File file = new File(this.consoleFolder);
			if(!file.exists()){
				file.mkdirs();
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
		File[] versions = new File(rootDir + subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 11);
			}});
		Arrays.sort(versions, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(((File) arg0).getName().substring(1))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(1))));
			}});
		List<String> versionsList = new ArrayList<String>();
		for(File version: versions){
			String vi = version.getName();
			versionsList.add(vi);
			System.out.println(vi);
			cWriter.println(vi);
			SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File(version.getAbsolutePath(), vi + "_f.sites")));
			CBIClient c = new CBIClient(runs, IterativeFunctionClient.TOP_K, sInfo.getSites().getSitesFile(), 
					rootDir + subject + "/traces/" + vi +"/fine-grained", consoleFolder + subject + "_" + vi + "_cbi.out");
			IterativeFunctionClient client = new IterativeFunctionClient(runs, new File(version.getAbsolutePath(), vi + "_c.sites"), 
					new File(rootDir + subject + "/traces/" + vi + "/coarse-grained"), consoleFolder + subject + "_" + vi + "_function.out", 
					sInfo, c.getPredictorEntryList(), c.getMethodsMap(), cWriter, version.getAbsolutePath() + "/adaptive/");
			FunctionEntrySites sites = new FunctionEntrySites(client.getSitesFile());
			results.put(vi, client.getResult());
			pResults.put(vi, client.getpResult());
			wResults.put(vi, client.getwResult());
			cResults.put(vi, client.getcResult());
			
//			for (int i = 0; i < results.get(vi).length; i++) {
//				for (int j = 0; j < results.get(vi)[i].length; j++) {
//					for (int p = 0; p < results.get(vi)[i][j].length; p++) {
//						for (int q = 0; q < results.get(vi)[i][j][p].length; q++) {
//							System.out.print(String.format("%-25s", results.get(vi)[i][j][p][q]));
//						}
//					}
//					System.out.println();
//				}
//				System.out.println();
//			}
			
			System.out.println();
			cWriter.println();	
		}
		
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		cWriter.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		printFinalResults(cWriter);
	}

	private void printSirResults(PrintWriter cWriter){
		List<String> versionsList = new ArrayList<String>();
		
		File[] versions = new File(rootDir + subject + "/versions").listFiles(new FilenameFilter(){
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
			File[] subversions = version.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length == 12);
				}});
			Arrays.sort(subversions, new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(((File) arg0).getName().substring(4))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(4))));
				}});
			
			for(File subversion: subversions){
				String vi = version.getName() + "_" + subversion.getName();
				versionsList.add(vi);
				System.out.println(vi);
				cWriter.println(vi);
				SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File(subversion.getAbsolutePath(), vi + "_f.sites")));
				CBIClient c = new CBIClient(runs, IterativeFunctionClient.TOP_K, sInfo.getSites().getSitesFile(), 
						rootDir + subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained", consoleFolder + subject + "_" + vi + "_cbi.out");
				IterativeFunctionClient client = new IterativeFunctionClient(runs, new File(subversion.getAbsolutePath(), vi + "_c.sites"), 
						new File(rootDir + subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), consoleFolder + subject + "_" + vi + "_function.out", 
						sInfo, c.getPredictorEntryList(), c.getMethodsMap(), cWriter, subversion.getAbsolutePath() + "/adaptive/");
				results.put(vi, client.getResult());
				pResults.put(vi, client.getpResult());
				wResults.put(vi, client.getwResult());
				cResults.put(vi, client.getcResult());
				
//				for (int i = 0; i < results.get(vi).length; i++) {
//					for (int j = 0; j < results.get(vi)[i].length; j++) {
//						for (int p = 0; p < results.get(vi)[i][j].length; p++) {
//							for (int q = 0; q < results.get(vi)[i][j][p].length; q++) {
//								System.out.print(String.format("%-25s", results.get(vi)[i][j][p][q]));
//							}
//						}
//						System.out.println();
//					}
//					System.out.println();
//				}
				
				System.out.println();
				cWriter.println();	
			}
			
		}
		
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		cWriter.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		printFinalResults(cWriter);
	}
	
	private void printFinalResults(PrintWriter cWriter) {
		// TODO Auto-generated method stub
		List rList = new ArrayList(results.entrySet());
		Collections.sort(rList, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				Entry<String, double[][][][]> entry0 = (Entry<String, double[][][][]>) arg0,
						entry1 = (Entry<String, double[][][][]>) arg1;
				double d0 = getSortValue(entry0),
						d1 = getSortValue(entry1);
				return new Double(d0).compareTo(new Double(d1));
			}

			private double getSortValue(Entry<String, double[][][][]> entry) {
				// TODO Auto-generated method stub
				double[][][][] array = entry.getValue();
				return array[4][1][1][0];
			}});
		
		Set<String> versions = new LinkedHashSet<String>();
		double[][][][] result = new double[Score.values().length][Order.values().length][2][5];
		double[][] pResult = new double[Score.values().length][5];
		double[][] wResult = new double[Score.values().length][5];
		int[] cResult = new int[3];
		for(int i = 0; i < rList.size(); i++){
			Entry<String, double[][][][]> entry = (Entry<String, double[][][][]>) rList.get(i);
			versions.add(entry.getKey());
			assert(i + 1 == versions.size());
			accumulateResult(result, entry.getValue());
			accumulatePWResult(pResult, pResults.get(entry.getKey()));
			accumulatePWResult(wResult, wResults.get(entry.getKey()));
			accumulateCResult(cResult, cResults.get(entry.getKey()));
			print(versions, result, pResult, wResult, cResult, cWriter);
		}
	}
	

	private void accumulateCResult(int[] cResult, int[] is) {
		// TODO Auto-generated method stub
		assert(cResult.length == is.length);
		for (int i = 0; i < cResult.length; i++) {
			cResult[i] += is[i];
		}
	}

	private void accumulatePWResult(double[][] wResult, double[][] ds) {
		// TODO Auto-generated method stub
		assert(wResult.length == ds.length && wResult.length == Score.values().length);
		for(int i = 0; i < wResult.length; i++){
			for (int j = 0; j < wResult[i].length; j++) {
				wResult[i][j] += ds[i][j];
			}
		}
	}

	private void print(Set<String> versions, double[][][][] result, double[][] pResult, double[][] wResult, int[] cResult, PrintWriter cWriter) {
		// TODO Auto-generated method stub
		System.out.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
		cWriter.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
		System.out.println("On average:\t" 
				+ String.format("%-20s", "methods:" + cResult[0] / versions.size())
				+ String.format("%-20s", "sites:" + cResult[1] / versions.size())
				+ String.format("%-20s", "predicates:" + cResult[2] / versions.size())
				);
		System.out.println("\n==============================================================");
		cWriter.println("On average:\t" 
				+ String.format("%-20s", "methods:" + cResult[0] / versions.size())
				+ String.format("%-20s", "sites:" + cResult[1] / versions.size())
				+ String.format("%-20s", "predicates:" + cResult[2] / versions.size())
				);
		cWriter.println("\n==============================================================");
		for (int m = 0; m < result.length; m++) {
			for (int n = 0; n < result[m].length; n++) {
				String mode = "<" + Score.values()[m] + "," + Order.values()[n] + ">";
				System.out.println("The information of average sites and predicates need to be instrumented " + mode + " are as follows:\n--------------------------------------------------------------");
				System.out.println("Excluding\t" 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(result[m][n][0][0] / versions.size()))
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(result[m][n][0][1] / versions.size()))
						+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(result[m][n][0][2] / versions.size())) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(result[m][n][0][3] / versions.size())) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(result[m][n][0][4] / versions.size())) 
						);
				System.out.println("Including\t" 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(result[m][n][1][0] / versions.size()))
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(result[m][n][1][1] / versions.size()))
						+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(result[m][n][1][2] / versions.size())) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(result[m][n][1][3] / versions.size())) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(result[m][n][1][4] / versions.size())) 
						);
				System.out.println();
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
			System.out.println("==============================================================");
			System.out.println("The prune case by <" + Score.values()[m] + ">:\t\t"
							+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(pResult[m][0] / versions.size()))
							+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pResult[m][1] / versions.size()))
							+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(pResult[m][2] / versions.size()))
							+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(pResult[m][3] / versions.size())) 
							+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(pResult[m][4] / versions.size())));
			System.out.println();
			cWriter.println("==============================================================");
			cWriter.println("The prune case by <" + Score.values()[m] + ">:\t\t"
							+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(pResult[m][0] / versions.size()))
							+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pResult[m][1] / versions.size()))
							+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(pResult[m][2] / versions.size()))
							+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(pResult[m][3] / versions.size())) 
							+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(pResult[m][4] / versions.size())));
			cWriter.println();
			System.out.println("==============================================================");
			System.out.println("The worst case by <" + Score.values()[m] + ">:\t\t"
							+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(wResult[m][0] / versions.size()))
							+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(wResult[m][1] / versions.size()))
							+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(wResult[m][2] / versions.size()))
							+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(wResult[m][3] / versions.size())) 
							+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(wResult[m][4] / versions.size())));
			System.out.println("\n");
			cWriter.println("==============================================================");
			cWriter.println("The worst case by <" + Score.values()[m] + ">:\t\t"
							+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(wResult[m][0] / versions.size()))
							+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(wResult[m][1] / versions.size()))
							+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(wResult[m][2] / versions.size()))
							+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(wResult[m][3] / versions.size())) 
							+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(wResult[m][4] / versions.size())));
			cWriter.println("\n");
		}
		System.out.println("\n");
		cWriter.println("\n");
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
		
		if(args.length != 5 && args.length != 3){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
			}
			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) numTests rootDir(including '/') subject consoleDir(excluding '/') " +
					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir(including '/') consoleDir(excluding '/')");
			return;
		}
		
		if(args.length == 5){
			Client c = new Client(Integer.parseInt(args[1]), args[2], args[3], args[4] + "/");
			if(Integer.parseInt(args[0]) == 0){
				c.computeSiemensResults();
			}
			else if(Integer.parseInt(args[0]) == 1){
				c.computeSirResults();
			}
		}
		else if(args.length == 3){
			assert(Integer.parseInt(args[0]) == 0);
			for(int i = 4; i < argvs.length; i++){
				Client c = new Client(Integer.parseInt(argvs[i][0]), args[1], argvs[i][1], args[2] + "/" + argvs[i][1] + "/");
				c.computeSiemensResults();
			}
		}

//		Client cc;
//		cc = new Client(213, "/home/sunzzq/Research/Automated_Debugging/Subjects/", "gzip", "/home/sunzzq/Console/gzip3/");
//		cc.computeSirResults();	
//		cc = new Client(363, "/home/sunzzq/Research/Automated_Debugging/Subjects/", "sed", "/home/sunzzq/Console/sed3/");
//		cc.computeSirResults();	
//		cc = new Client(5434, "/home/sunzzq/Research/Automated_Debugging/Subjects/", "space", "/home/sunzzq/Console/space2/");
//		cc.computeSiemensResults();	
	}
	
}