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

public class Client {
	final int runs;
	final String rootDir;
	final String subject;
	final String consoleFolder;
	
	final Map<String, double[][]> results;
	
	final int orderMode;

	public Client(int runs, String rootDir, String subject, String consoleFolder, int oMode) {
		this.runs = runs;
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		this.results = new HashMap<String, double[][]>();
		
		this.orderMode = oMode;
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
					rootDir + subject + "/traces/" + vi + "/coarse-grained", consoleFolder + subject + "_" + vi + "_function.out", sInfo, c.getPredictorEntryList(), cWriter, orderMode);
			results.put(vi, client.getResult());
			
//			for (int i = 0; i < results.get(vi).length; i++) {
//				for (int j = 0; j < results.get(vi)[i].length; j++) {
//					System.out.print(results.get(vi)[i][j] + "\t");
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
						rootDir + subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained", consoleFolder + subject + "_" + vi + "_function.out", sInfo, c.getPredictorEntryList(), cWriter, orderMode);
				results.put(vi, client.getResult());
				
//				for (int i = 0; i < results.get(vi).length; i++) {
//					for (int j = 0; j < results.get(vi)[i].length; j++) {
//						System.out.print(results.get(vi)[i][j] + "\t");
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
				Entry<String, double[][]> entry0 = (Entry<String, double[][]>) arg0,
						entry1 = (Entry<String, double[][]>) arg1;
				double d0 = getSortValue(entry0),
						d1 = getSortValue(entry1);
				return new Double(d0).compareTo(new Double(d1));
			}

			private double getSortValue(Entry<String, double[][]> entry) {
				// TODO Auto-generated method stub
				double[][] array = entry.getValue();
				return array[0][2] + array[1][2] + array[2][2];
			}});
		
		Set<String> versions = new LinkedHashSet<String>();
		double[][] result = new double[4][4];
		for(int i = 0; i < rList.size(); i++){
			Entry<String, double[][]> entry = (Entry<String, double[][]>) rList.get(i);
			versions.add(entry.getKey());
			accumulate(result, entry.getValue());
			print(i, versions, result, cWriter);
		}
	}
	

	private void print(int i, Set<String> versions, double[][] result, PrintWriter cWriter) {
		// TODO Auto-generated method stub
		assert(i + 1 == versions.size());
		System.out.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
		System.out.println("The information of average sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[0][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[0][1] / versions.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[0][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[0][3] / versions.size())));
		System.out.println("\nThe information of average sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[1][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[1][1] / versions.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[1][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[1][3] / versions.size())));
		System.out.println("\nThe information of average sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[2][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[2][1] / versions.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[2][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[2][3] / versions.size())));
		System.out.println("\nThe information of average sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[3][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[3][1] / versions.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[3][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[3][3] / versions.size())));
		System.out.println();
		
		cWriter.println(versions.size() + "\n" + subject + ": " + versions + "\n==============================================================");
		cWriter.println("The information of average sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[0][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[0][1] / versions.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[0][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[0][3] / versions.size())));
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[1][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[1][1] / versions.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[1][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[1][3] / versions.size())));
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[2][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[2][1] / versions.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[2][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[2][3] / versions.size())));
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[3][0] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[3][1] / versions.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format(result[3][2] / versions.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format(result[3][3] / versions.size())));
		cWriter.println();
	}

	private void accumulate(double[][] result, double[][] value) {
		// TODO Auto-generated method stub
		assert(result.length == value.length && result.length == 4);
		for(int i = 0; i < result.length; i++){
			for (int j = 0; j < result[i].length; j++) {
				result[i][j] += value[i][j];
			}
		}
	}

	public static void main(String[] args) {
//		if(args.length != 6){
//			System.err.println("Usage: subjectMode(0:Siemens; 1:Sir) numTests rootDir subject consoleDir orderMode(0:random; 1:best; 2:less first)");
//			return;
//		}
//		Client c = new Client(Integer.parseInt(args[1]), args[2], args[3], args[4], Integer.parseInt(args[5]));
//		if(Integer.parseInt(args[0]) == 0){
//			c.computeSiemensResults();
//		}
//		else if(Integer.parseInt(args[0]) == 1){
//			c.computeSirResults();
//		}

//		Client c = new Client(363, "/home/sunzzq/Research/Automated_Debugging/Subjects/", "sed", "/home/sunzzq/Console/sed1/");
//		c.computeSirResults();
		
		String[][] argvs = {
//				{"1608", "tcas"},
//				{"1052", "totinfo"},
//				{"5542", "replace"},
//				{"4130", "printtokens"},
//				{"4115", "printtokens2"},
				{"2650", "schedule"},
				{"2710", "schedule2"}
				};
		for(int i = 0; i < argvs.length; i++){
			Client c = new Client(Integer.parseInt(argvs[i][0]), "/home/sunzzq/Research/Automated_Debugging/Subjects/Siemens/", argvs[i][1], "/home/sunzzq/Console/Siemens3/" + argvs[i][1] + "/", 0);
			c.computeSiemensResults();	
			System.out.println("\n\n");
		}
		
	}
	
}
