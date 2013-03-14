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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;

public class Client {
	final int runs;
	final String rootDir;
	final String subject;
	final String consoleFolder;
	
	final Map<String, int[][]> results;

	public Client(int runs, String rootDir, String subject, String consoleFolder) {
		super();
		this.runs = runs;
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		this.results = new HashMap<String, int[][]>();
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
					System.out.print(results.get(vi)[i][j] + "\t");
				}
				System.out.println();
			}
			System.out.println();
			cWriter.println();	
		}
		
		System.out.println("\n\n\n" + subject + ": " + versionsList + "\n==============================================================");
		cWriter.println("\n\n\n" + subject + ": " + versionsList + "\n==============================================================");
		printTotalResults(cWriter);
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
						System.out.print(results.get(vi)[i][j] + "\t");
					}
					System.out.println();
				}
				System.out.println();
				cWriter.println();	
			}
			
		}
		
		System.out.println("\n\n\n" + subject + ": " + versionsList + "\n==============================================================");
		cWriter.println("\n\n\n" + subject + ": " + versionsList + "\n==============================================================");
		printTotalResults(cWriter);
	}
	
	private void printTotalResults(PrintWriter cWriter) {
		double[][] sum = new double[4][4];
		for(String version: results.keySet()){
			int[][] array = results.get(version);
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length - 2; j++) {
					sum[i][j] += (double) array[i][j]/array[i][j%2 + 4];
				}
			}
		}
		
		System.out.println("\nThe information of average sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[0][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[0][1] / results.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[0][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[0][3] / results.size())));
		System.out.println("\nThe information of average sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[1][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[1][1] / results.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[1][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[1][3] / results.size())));
		System.out.println("\nThe information of average sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[2][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[2][1] / results.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[2][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[2][3] / results.size())));
		System.out.println("\nThe information of average sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		System.out.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[3][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[3][1] / results.size())));
		System.out.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[3][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[3][3] / results.size())));
		
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (F-score) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[0][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[0][1] / results.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[0][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[0][3] / results.size())));
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (Specificity) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[1][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[1][1] / results.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[1][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[1][3] / results.size())));
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (Negative) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[2][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[2][1] / results.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[2][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[2][3] / results.size())));
		cWriter.println("\nThe information of average sites and predicates need to be instrumented (Positive) are as follows:\n--------------------------------------------------------------");
		cWriter.println("Excluding\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[3][0] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[3][1] / results.size())));
		cWriter.println("Including\t" + String.format("%-20s", "s%:" + new DecimalFormat("##.##").format((double) 100 * sum[3][2] / results.size()))
				+ String.format("%-20s", "p%:" + new DecimalFormat("##.##").format((double)100 * sum[3][3] / results.size())));
		
		System.out.println("\n\n");
		for (int i = 0; i < sum.length; i++) {
			for (int j = 0; j < sum[i].length; j++) {
				System.out.print(sum[i][j]/(results.size()) + "\t");
			}
			System.out.println();
		}
	}

	public static void main(String[] args) {
		if(args.length != 5){
			System.err.println("Usage: numTests rootDir subject consoleDir mode(0:Siemens; 1:Sir)");
			return;
		}
		Client c = new Client(Integer.parseInt(args[0]), args[1], args[2], args[3]);
		if(Integer.parseInt(args[4]) == 0){
			c.computeSiemensResults();
		}
		else if(Integer.parseInt(args[4]) == 1){
			c.computeSirResults();
		}

//		Client c = new Client(213, "/home/sunzzq/Research/Automated_Debugging/Subjects/", "gzip", "/home/sunzzq/Console/gzip/");
//		c.computeSirResults();
		
	}
	
}
