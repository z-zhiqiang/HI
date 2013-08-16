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

import zuo.processor.cbi.client.CBIClients;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Order;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;

public class Client {
	public final static int fK = 10;
	public final static int iK = 3;
	public final static int fKF = 10;
	public final static int k = 1;
	
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final Map<String, double[][][][]> results;
	final Map<String, double[][]> pResults;
	final Map<String, int[]> cResults;
	
	public Client(File rootDir, String subject, File consoleFolder) {
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		this.results = new HashMap<String, double[][][][]>();
		this.pResults = new HashMap<String, double[][]>();
		this.cResults = new HashMap<String, int[]>();
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
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 10);
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
			
			CBIClients cs = new CBIClients(sInfo, new File(rootDir, subject + "/traces/" + vi +"/fine-grained"), new File(consoleFolder, subject + "_" + vi + "_cbi.out"));
			
			IterativeFunctionClient client = new IterativeFunctionClient(new File(version, vi + "_c.sites"), 
					new File(rootDir, subject + "/traces/" + vi + "/coarse-grained"), 
					new File(consoleFolder, subject + "_" + vi + "_function.out"), 
					sInfo, 
					cs.getFullInstrumentedCBIClient(), 
					cs.getClientsMap(), 
					cWriter, 
					new File(version, "adaptive"));
			results.put(vi, client.getResult());
			pResults.put(vi, client.getpResult());
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
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length == 11);
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
				
				CBIClients cs = new CBIClients(sInfo, new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained"), new File(consoleFolder, subject + "_" + vi + "_cbi.out"));
				IterativeFunctionClient client = new IterativeFunctionClient(new File(subversion, vi + "_c.sites"), 
						new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), 
						new File(consoleFolder, subject + "_" + vi + "_function.out"), 
						sInfo, 
						cs.getFullInstrumentedCBIClient(), 
						cs.getClientsMap(), 
						cWriter, 
						new File(subversion, "adaptive"));
				results.put(vi, client.getResult());
				pResults.put(vi, client.getpResult());
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
		List<Map.Entry<String, double[][][][]>> rList = new ArrayList<Map.Entry<String, double[][][][]>>(results.entrySet());
		Collections.sort(rList, new Comparator<Map.Entry<String, double[][][][]>>(){

			private double getSortValue(Entry<String, double[][][][]> entry) {
				// TODO Auto-generated method stub
				double[][][][] array = entry.getValue();
				return array[4][1][1][0];
			}

			@Override
			public int compare(Entry<String, double[][][][]> o1,
					Entry<String, double[][][][]> o2) {
				// TODO Auto-generated method stub
				double d0 = getSortValue(o1),
						d1 = getSortValue(o2);
				return new Double(d0).compareTo(new Double(d1));
			}});
		
		Set<String> versions = new LinkedHashSet<String>();
		double[][][][] result = new double[Score.values().length][Order.values().length][2][5];
		double[][] pResult = new double[Score.values().length][5];
		int[] cResult = new int[3];
		for(int i = 0; i < rList.size(); i++){
			Entry<String, double[][][][]> entry = (Entry<String, double[][][][]>) rList.get(i);
			versions.add(entry.getKey());
			assert(i + 1 == versions.size());
			accumulateResult(result, entry.getValue());
			accumulatePResult(pResult, pResults.get(entry.getKey()));
			accumulateCResult(cResult, cResults.get(entry.getKey()));
			print(versions, result, pResult, cResult, cWriter);
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

	private void print(Set<String> versions, double[][][][] result, double[][] pResult, int[] cResult, PrintWriter cWriter) {
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
			System.out.println("\n");
			cWriter.println("==============================================================");
			cWriter.println("The prune case by <" + Score.values()[m] + ">:\t\t"
							+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(pResult[m][0] / versions.size()))
							+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pResult[m][1] / versions.size()))
							+ String.format("%-15s", "i:" + new DecimalFormat("#.#").format(pResult[m][2] / versions.size()))
							+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(pResult[m][3] / versions.size())) 
							+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(pResult[m][4] / versions.size())));
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
//		
//		if(args.length != 5 && args.length != 3){
//			System.out.println("The characteristics of subjects are as follows:");
//			for(int i = 0; i < argvs.length; i++){
//				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
//			}
//			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) rootDir(including '/') subject consoleDir(excluding '/') " +
//					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir(including '/') consoleDir(excluding '/')");
//			return;
//		}
//		
//		if(args.length == 4){
//			Client c = new Client(new File(args[2]), args[3], new File(args[4] + "/"));
//			if(Integer.parseInt(args[0]) == 0){
//				c.computeSiemensResults();
//			}
//			else if(Integer.parseInt(args[0]) == 1){
//				c.computeSirResults();
//			}
//		}
//		else if(args.length == 3){
//			assert(Integer.parseInt(args[0]) == 0);
//			for(int i = 4; i < argvs.length; i++){
//				Client c = new Client(new File(args[1]), argvs[i][1], new File(args[2] + "/" + argvs[i][1] + "/"));
//				c.computeSiemensResults();
//			}
//		}

		Client cc;
//		cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "gzip", new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/gzip_________" + CBIClients.percent + "/"));
//		cc.computeSirResults();
//		cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "sed", new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/sed_______" + CBIClients.percent + "/"));
//		cc.computeSirResults();
		cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "grep", new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/grep________" + CBIClients.percent + "/"));
		cc.computeSirResults();	
//		cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/"), "space", new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/space/"));
//		cc.computeSiemensResults();	
//		for(int i = 4; i < argvs.length; i++){
//			cc = new Client(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/Siemens/"), argvs[i][1], new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/Siemens/" + argvs[i][1] + "/"));
//			cc.computeSiemensResults();
//		}
	}
	
}
