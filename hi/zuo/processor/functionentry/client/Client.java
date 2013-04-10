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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.client.FunctionClient.Order;
import zuo.processor.functionentry.client.FunctionClient.Score;

public class Client {
	final int runs;
	final String rootDir;
	final String subject;
	final String consoleFolder;
	
	final Map<String, double[][][][]> results;
	
	public Client(int runs, String rootDir, String subject, String consoleFolder) {
		this.runs = runs;
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		this.results = new HashMap<String, double[][][][]>();
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
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 10 || new File(dir, name).listFiles().length == 11);
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
			CBIClient c = new CBIClient(runs, FunctionClient.TOP_K, sInfo.getSites().getSitesFile(), 
					rootDir + subject + "/traces/" + vi +"/fine-grained", consoleFolder + subject + "_" + vi + "_cbi.out");
			FunctionClient client = new FunctionClient(runs, new File(version.getAbsolutePath(), vi + "_c.sites"), 
					rootDir + subject + "/traces/" + vi + "/coarse-grained", consoleFolder + subject + "_" + vi + "_function.out", sInfo, c.getPredictorEntryList(), cWriter);
			results.put(vi, client.getResult());
			
			for (int i = 0; i < results.get(vi).length; i++) {
				for (int j = 0; j < results.get(vi)[i].length; j++) {
					for (int p = 0; p < results.get(vi)[i][j].length; p++) {
						for (int q = 0; q < results.get(vi)[i][j][p].length; q++) {
							System.out.print(String.format("%-25s", results.get(vi)[i][j][p][q]));
						}
					}
					System.out.println();
				}
				System.out.println();
			}
			
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
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length == 11 || new File(dir, name).listFiles().length == 10);
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
				CBIClient c = new CBIClient(runs, FunctionClient.TOP_K, sInfo.getSites().getSitesFile(), 
						rootDir + subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained", consoleFolder + subject + "_" + vi + "_cbi.out");
				FunctionClient client = new FunctionClient(runs, new File(subversion.getAbsolutePath(), vi + "_c.sites"), 
						rootDir + subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained", consoleFolder + subject + "_" + vi + "_function.out", sInfo, c.getPredictorEntryList(), cWriter);
				results.put(vi, client.getResult());
				
				for (int i = 0; i < results.get(vi).length; i++) {
					for (int j = 0; j < results.get(vi)[i].length; j++) {
						for (int p = 0; p < results.get(vi)[i][j].length; p++) {
							for (int q = 0; q < results.get(vi)[i][j][p].length; q++) {
								System.out.print(String.format("%-25s", results.get(vi)[i][j][p][q]));
							}
						}
						System.out.println();
					}
					System.out.println();
				}
				
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
				double sum = 0;
				for(int i = 0; i < 2; i++){
					sum += array[i][0][1][0];
				}
				return sum;
			}});
		
		Set<String> versions = new LinkedHashSet<String>();
		double[][][][] result = new double[Score.values().length][Order.values().length][2][5];
		for(int i = 0; i < rList.size(); i++){
			Entry<String, double[][][][]> entry = (Entry<String, double[][][][]>) rList.get(i);
			versions.add(entry.getKey());
			accumulate(result, entry.getValue());
			print(i, versions, result, cWriter);
		}
	}
	

	private void print(int i, Set<String> versions, double[][][][] result, PrintWriter cWriter) {
		// TODO Auto-generated method stub
		assert(i + 1 == versions.size());
		System.out.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
		cWriter.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
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
		}
		System.out.println("\n");
		cWriter.println("\n");
	}

	private void accumulate(double[][][][] result, double[][][][] ds) {
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
//		String[][] argvs = {
//				{"809", "grep"},
//				{"213", "gzip"},
//				{"363", "sed"},
//				{"13585", "space"},
//				{"1608", "tcas"},
//				{"1052", "totinfo"},
//				{"5542", "replace"},
//				{"4130", "printtokens"},
//				{"4115", "printtokens2"},
//				{"2650", "schedule"},
//				{"2710", "schedule2"}
//		};
//		
//		if(args.length != 5 && args.length != 3){
//			System.out.println("The characteristics of subjects are as follows:");
//			for(int i = 0; i < argvs.length; i++){
//				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
//			}
//			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) numTests rootDir(including '/') subject consoleDir(excluding '/') " +
//					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir(including '/') consoleDir(excluding '/')");
//			return;
//		}
//		
//		if(args.length == 5){
//			Client c = new Client(Integer.parseInt(args[1]), args[2], args[3], args[4] + "/");
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
//				Client c = new Client(Integer.parseInt(argvs[i][0]), args[1], argvs[i][1], args[2] + "/" + argvs[i][1] + "/");
//				c.computeSiemensResults();
//			}
//		}

		Client cc = new Client(363, "/home/sunzzq/Research/Automated_Debugging/Subjects/", "sed", "/home/sunzzq/Console/sed1/");
		cc.computeSirResults();	
		
	}
	
}
