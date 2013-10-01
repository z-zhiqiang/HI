package zuo.processor.functionentry.client.twopass;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import zuo.processor.functionentry.processor.BoundCalculator;
import zuo.split.PredicateSplittingSiteProfile;
import edu.nus.sun.processor.mps.client.DefaultPredicateProcessorWithLabel;

public class Client {
	private static final String DATASET_FOLDER_NAME = "predicate-dataset";
	private static final String mbsOutputFile = "mbs.out";
	private static final String rootDir = "/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/";
	
	private final String subject;
	
	private final Map<String, List<Object>> resultsMap;

	public Client(String subject){
		this.subject = subject;
		this.resultsMap = new HashMap<String, List<Object>>();
	}

	public static void main(String[] args) {
		String[][] argvs = {
//				{"809", "grep"},
				{"213", "gzip"},
//				{"363", "sed"},
//				{"13585", "space"},
//				{"4130", "printtokens"},
//				{"4115", "printtokens2"},
//				{"5542", "replace"},
//				{"2650", "schedule"},
//				{"2710", "schedule2"},
//				{"1608", "tcas"},
//				{"1052", "totinfo"}
		};
		for(int i = 0; i < argvs.length; i++){
			Client client = new Client(argvs[i][1]);
			client.runClient();
			System.out.println("=================================================");
			System.out.println(client.resultsMap);
		}
	}
	
	public void runClient(){
		File projectRoot = new File(rootDir, this.subject);
		if (!projectRoot.exists())
			throw new RuntimeException("Project " + projectRoot + " does not exist!");

		if (this.subject.equals("space")) {
			File[] versions = new File(projectRoot, "versions").listFiles(new FilenameFilter(){
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
			
			for(File version: versions){
				String vi = version.getName();
				System.out.println(vi);
				
				File fgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
				if (!fgProfilesFolder.exists()) {
					throw new RuntimeException("Fine-grained faulty profiles folder " + fgProfilesFolder + " does not exist.");
				}
				File cgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/coarse-grained/");
				if (!cgProfilesFolder.exists()) {
					throw new RuntimeException("Coarse-grained faulty profiles folder " + cgProfilesFolder + " does not exist.");
				}
				
				final File fgSitesFile = new File(projectRoot, "versions/" + vi + "/" + vi + "_f.sites");
				final File cgSitesFile = new File(projectRoot, "versions/" + vi + "/" + vi + "_c.sites");
				
				final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
				
				List<Object> resultsList = new ArrayList<Object>();
				run(fgProfilesFolder, fgSitesFile, cgProfilesFolder, cgSitesFile, resultOutputFolder, resultsList);
				
				this.resultsMap.put(vi, resultsList);
			}
		} 
		else {
			File[] versions = new File(projectRoot, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("v[0-9]*", name);
				}});
			Arrays.sort(versions, new Comparator<File>(){
				@Override
				public int compare(File arg0, File arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(arg0.getName().substring(1))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(1))));
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
					public int compare(File arg0, File arg1) {
						// TODO Auto-generated method stub
						return new Integer(Integer.parseInt(arg0.getName().substring(4))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(4))));
					}});
				
				for(File subversion: subversions){
					String vi = version.getName() + "/" + subversion.getName();
					System.out.println(vi);
					
					//profiles folders
					File fgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
					if (!fgProfilesFolder.exists()) {
						throw new RuntimeException("Fine-grained faulty profiles folder " + fgProfilesFolder + " does not exist.");
					}
					File cgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/coarse-grained/");
					if (!cgProfilesFolder.exists()) {
						throw new RuntimeException("Coarse-grained faulty profiles folder " + cgProfilesFolder + " does not exist.");
					}
					
					//instrumentation sites files
					final File fgSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_f.sites");
					final File cgSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_c.sites");
					
					//dataset output folder
					final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
					
					List<Object> resultsList = new ArrayList<Object>();
					run(fgProfilesFolder, fgSitesFile, cgProfilesFolder, cgSitesFile, resultOutputFolder, resultsList);
					assert(resultsList.size() == 33);
					this.resultsMap.put(vi, resultsList);
				}
			}
		}
	}

	private void run(File fgProfilesFolder, final File fgSitesFile, File cgProfilesFolder, File cgSitesFile, final File resultOutputFolder, List<Object> resultsList) {
		double threshold = 0;
		int k = 1;
		String command = "mbs -k " + k + " -n 0.5 -g --refine 2  --metric 0  --dfs  --merge  --cache 9999 --up-limit 2 ";
		
		/*=================================================================================================*/
		
		TwopassFunctionClient funClient = new TwopassFunctionClient(cgSitesFile, cgProfilesFolder, fgSitesFile);
		funClient.printEntry();
		
		assert(funClient.getList().size() == funClient.getsInfo().getMap().size());
		
		resultsList.add(funClient.getList().size());
		resultsList.add(FileUtils.sizeOf(cgSitesFile));
		resultsList.add(FileUtils.sizeOf(cgProfilesFolder));
		
		/*=================================================================================================*/
		
		Set<String> originalFunctionSet = funClient.getFunctionSet(0);
		assert(originalFunctionSet.size() == funClient.getList().size());
		
		resultsList.add(originalFunctionSet.size());
		resultsList.add(FileUtils.sizeOf(fgSitesFile));
		resultsList.add(FileUtils.sizeOf(fgProfilesFolder));
		
		File originalDatasetFolder = new File(resultOutputFolder, "original");
		if(!originalDatasetFolder.exists()){
			originalDatasetFolder.mkdirs();
		}
		DefaultPredicateProcessorWithLabel originalInstance = new DefaultPredicateProcessorWithLabel(fgProfilesFolder, originalDatasetFolder, fgSitesFile);
		originalInstance.run(resultsList);
		
		runMBS(command, originalDatasetFolder, k, resultsList);
		
		/*=================================================================================================*/
		
		Set<String> boostFunctionSet = funClient.getBoostFunctionSet((byte)0, 0.1f);
		File boostProfilesFolder = new File(fgProfilesFolder.getParentFile(), "boost");
		File boostSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'b'));
		PredicateSplittingSiteProfile boostSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, boostSitesFile, boostProfilesFolder, boostFunctionSet);
		boostSplit.split();
		
		resultsList.add(boostFunctionSet.size());
		resultsList.add(FileUtils.sizeOf(boostSitesFile));
		resultsList.add(FileUtils.sizeOf(boostProfilesFolder));
		
		File boostDatasetFolder = new File(resultOutputFolder, "boost");
		if(!boostDatasetFolder.exists()){
			boostDatasetFolder.mkdirs();
		}
		DefaultPredicateProcessorWithLabel boostInstance = new DefaultPredicateProcessorWithLabel(boostProfilesFolder, boostDatasetFolder, boostSitesFile);
		boostInstance.run(resultsList);
		
		threshold = runMBS(command, boostDatasetFolder, k, resultsList);
		
		//-------------------------------------------------------------------------------------------------//
		
		BoundCalculator bc = new BoundCalculator(funClient.getProcessor().getTotalNegative(), funClient.getProcessor().getTotalPositive());
		assert(threshold != 0);
		Set<String> pruneFunctionSet = funClient.getFunctionSet(bc.computeIGBound(threshold));
		File pruneProfilesFolder = new File(fgProfilesFolder.getParentFile(), "prune");
		File pruneSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'p'));
		PredicateSplittingSiteProfile pruneSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, pruneSitesFile, pruneProfilesFolder, pruneFunctionSet);
		pruneSplit.split();
		
		resultsList.add(pruneFunctionSet.size());
		resultsList.add(FileUtils.sizeOf(pruneSitesFile));
		resultsList.add(FileUtils.sizeOf(pruneProfilesFolder));
		
		File pruneDatasetFolder = new File(resultOutputFolder, "prune");
		if(!pruneDatasetFolder.exists()){
			pruneDatasetFolder.mkdirs();
		}
		DefaultPredicateProcessorWithLabel pruneInstance = new DefaultPredicateProcessorWithLabel(pruneProfilesFolder, pruneDatasetFolder, pruneSitesFile);
		pruneInstance.run(resultsList);
		
		runMBS(command, pruneDatasetFolder, k, resultsList);
		
		//-------------------------------------------------------------------------------------------------//
		
	}

	private double runMBS(String command, File datasetFolder, int k, List<Object> resultsList) {
		double threshold = 0;
		double time = 0;
		try {
			Process process = Runtime.getRuntime().exec(command + "-o " 
					+ new File(datasetFolder, mbsOutputFile).getAbsolutePath() + " " 
					+ new File(datasetFolder, DefaultPredicateProcessorWithLabel.MPS_PB).getAbsolutePath());
//			process.waitFor();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			while((line = reader.readLine()) != null){
				if(line.matches("TOP-.*(" + k + ").*SUP=.*Metric=.*")){
					System.out.println(line);
					threshold = Double.parseDouble(line.substring(line.lastIndexOf("=") + 1));
					resultsList.add(threshold);
				}
				if(line.matches("time-cost.*=.*")){
					System.out.println(line);
					time = Double.parseDouble(line.substring(line.lastIndexOf("=") + 1).trim());
					resultsList.add(time);
				}
			}
			System.out.println("\n");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return threshold;
	}
	
	private void printResultToExcel(){
		
	}

}
