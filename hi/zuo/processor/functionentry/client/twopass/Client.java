package zuo.processor.functionentry.client.twopass;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sun.processor.core.IDataSet;
import sun.processor.predicate.PredicateDataSet;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.ds.processor.PredicateDSInfoWithinFunction;
import zuo.processor.ds.processor.ProcessorPreDSInfoWithinFun;
import zuo.processor.functionentry.processor.PruningProcessor;
import zuo.processor.functionentry.processor.PruningProcessor.FrequencyValue;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.processor.split.PredicateSplittingSiteProfile;
import zuo.util.file.FileCollection;
import zuo.util.file.FileUtil;
import zuo.util.file.FileUtility;
import edu.nus.sun.processor.mps.client.DefaultPredicateProcessorWithLabel;

public class Client {
	public static final int k = 1;
	public static final int l = 2;
	
	private static final String DATASET_FOLDER_NAME = "predicate-dataset";
	private static final String mbsOutputFile = "mbs.out";
	
	public static final String root = "/home/sunzzq2/Data/IResearch/Automated_Bug_Isolation/";
	
	private final String subject;
	private final byte mode;
	private final double percent;
	private final Map<String, List<Object>> resultsMap;
	
	private final Map<String, Map<String, List<Object>>> correlationDataMap;
	private final Map<String, List<Object>> correlationResultsMap;
	
	private final int startVersion;
	private final int endVersion;
	
	private final File subjectsFolder;
	private final File consoleFolder;

	public Client(String subject, byte mode, double percent, int start, int end, File subjectsF, File consoleF){
		this.subject = subject;
		this.mode = mode;
		this.percent = percent;
		this.resultsMap = new LinkedHashMap<String, List<Object>>();
		this.correlationDataMap = new LinkedHashMap<String, Map<String, List<Object>>>();
		this.correlationResultsMap = new LinkedHashMap<String, List<Object>>();
		this.startVersion = start;
		this.endVersion = end;
		this.subjectsFolder = subjectsF;
		this.consoleFolder = consoleF;
	}

	public static void main(String[] args) {
		String[][] argvs = {
				{"150", "bash", "2"},
				{"363", "sed", "7"},
				{"213", "gzip", "5"},
				{"809", "grep", "5"},
				{"1248", "space", "38"},
//				{"4130", "printtokens", "7"},
//				{"4115", "printtokens2", "10"},
				{"5542", "replace", "32"},
//				{"2650", "schedule", "9"},
//				{"2710", "schedule2", "10"},
//				{"1608", "tcas", "41"},
//				{"1052", "totinfo", "23"}
		};
		if(args.length != 6 && args.length != 4){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
			}
			System.err.println("Usage: rootFile(Twopass_heavy; Twopass_light) subject mode(0->%*f; 1->%*S; 2->%*P) percent startVersion endVersion");
			System.err.println("or");
			System.err.println("Usage: rootFile(Twopass_heavy; Twopass_light) subject mode(0->%*f; 1->%*S; 2->%*P) percent");
			return;
		}
		Client client;
		if(args[1].equals("Siemens")){
			for(int i = 5; i < argvs.length; i++){
				client = new Client(argvs[i][1], Byte.parseByte(args[2]), Double.parseDouble(args[3]), 1, Integer.parseInt(argvs[i][2]), 
						new File(root + args[0], "Subjects/Siemens/"), new File(root + args[0], "Console/Siemens/"));			
				client.runClientWithConsole();				
			}
		}
		else{
			client = new Client(args[1], Byte.parseByte(args[2]), Double.parseDouble(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), 
					new File(root + args[0], "Subjects/"), new File(root + args[0], "Console/"));			
			client.runClientWithConsole();
		}
	}
	public void runClientWithConsole(){
		PrintWriter writer = null;
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			
			writer =  new PrintWriter(new BufferedWriter(new FileWriter(new File(consoleFolder, this.subject + "__" + this.mode + "_" + this.percent + "_v" + this.startVersion + "-v" + this.endVersion + ".console"))));
			runClient(writer);
			
			System.out.println("=================================================");
			System.out.println(this.resultsMap);
			System.out.println("\n");
			System.out.println(this.correlationResultsMap);
			System.out.println("\n\n");
			
			writer.println("=================================================");
			writer.println(this.resultsMap);
			writer.println("\n");
			writer.println(this.correlationResultsMap);
			writer.println("\n\n");
			
			writer.close();
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
	public void runClient(PrintWriter writer) throws IOException{
		File projectRoot = new File(subjectsFolder, this.subject);
		if (!projectRoot.exists()){
			throw new RuntimeException("Project " + projectRoot + " does not exist!");
		}

		if (this.subjectsFolder.getAbsolutePath().contains("Siemens") || this.subject.equals("space")) {
			File[] versions = new File(projectRoot, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length >= 10)
							&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion;
				}});
			Arrays.sort(versions, new Comparator<File>(){
				@Override
				public int compare(File arg0, File arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(arg0.getName().substring(1))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(1))));
				}});
			
			for(File version: versions){
				String vi = version.getName();
//				if(!vi.equals("v7")){
//					continue;
//				}
				System.out.println(vi);
				writer.println(vi);
				writer.println();
				
				File fgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
				if (!fgProfilesFolder.exists()) {
					throw new RuntimeException("Fine-grained profiles folder " + fgProfilesFolder + " does not exist.");
				}
				File cgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/coarse-grained/");
				if (!cgProfilesFolder.exists()) {
					throw new RuntimeException("Coarse-grained profiles folder " + cgProfilesFolder + " does not exist.");
				}
				
				final File fgSitesFile = new File(projectRoot, "versions/" + vi + "/" + vi + "_f.sites");
				final File cgSitesFile = new File(projectRoot, "versions/" + vi + "/" + vi + "_c.sites");
				
				final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
//				FileUtility.removeFileOrDirectory(new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME));
				
				List<Object> resultsList = new ArrayList<Object>();
				Map<String, List<Object>> correlationData = new LinkedHashMap<String, List<Object>>();
				List<Object> correlationResults = new ArrayList<Object>();
				run(fgProfilesFolder, fgSitesFile, cgProfilesFolder, cgSitesFile, resultOutputFolder, resultsList, writer, correlationData, correlationResults);
				
				this.resultsMap.put(vi, resultsList);
				this.correlationDataMap.put(vi, correlationData);
				this.correlationResultsMap.put(vi, correlationResults);
			}
		} 
		else {
			File[] versions = new File(projectRoot, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("v[0-9]*", name)
							&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion;
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
						return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 10);
					}});
				Arrays.sort(subversions, new Comparator<File>(){
					@Override
					public int compare(File arg0, File arg1) {
						// TODO Auto-generated method stub
						return new Integer(Integer.parseInt(arg0.getName().substring(4))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(4))));
					}});
				
				for(File subversion: subversions){
					String vi = version.getName() + "/" + subversion.getName();
//					if(!vi.equals("v2/subv3")){
//						continue;
//					}
					System.out.println(vi);
					writer.println(vi);
					writer.println();
					
					//profiles folders
					File fgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
					if (!fgProfilesFolder.exists()) {
						throw new RuntimeException("Fine-grained profiles folder " + fgProfilesFolder + " does not exist.");
					}
					File cgProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/coarse-grained/");
					if (!cgProfilesFolder.exists()) {
						throw new RuntimeException("Coarse-grained profiles folder " + cgProfilesFolder + " does not exist.");
					}
					
					//instrumentation sites files
					final File fgSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_f.sites");
					final File cgSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_c.sites");
					
					//dataset output folder
					final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
//					FileUtility.removeFileOrDirectory(new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME));
					
					List<Object> resultsList = new ArrayList<Object>();
					Map<String, List<Object>> correlationData = new LinkedHashMap<String, List<Object>>();
					List<Object> correlationResults = new ArrayList<Object>();
					run(fgProfilesFolder, fgSitesFile, cgProfilesFolder, cgSitesFile, resultOutputFolder, resultsList, writer, correlationData, correlationResults);
					
					assert(resultsList.size() == 55);
					this.resultsMap.put(vi, resultsList);
					this.correlationDataMap.put(vi, correlationData);
					assert(correlationResults.size() == 26);
					this.correlationResultsMap.put(vi, correlationResults);
				}
			}
		}
		
		printResultToExcel();
		printCorrelationToExcel();
	}

	private void run(File fgProfilesFolder, File fgSitesFile, File cgProfilesFolder, File cgSitesFile, final File resultOutputFolder, List<Object> resultsList, PrintWriter writer, Map<String, List<Object>> correlationData, List<Object> correlationResults) throws IOException {
		int rounds = 3;
		double time = 60;
		
		double threshold = 0;
		String command = "mbs -k " + k + " -n 0.5 -g --refine 2  --metric 0  --dfs  --merge  --cache 9999 --up-limit " + l + " --print-resource-usage ";
		
		/*=================================================================================================*/
		
		int totalNeg = 0;
		int totalPos = 0;
		
		File[] fgProfiles = fgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		Arrays.sort(fgProfiles, new FileUtil.FileComparator());
		File[] cgProfiles = cgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		Arrays.sort(cgProfiles, new FileUtil.FileComparator());
		if(fgProfiles.length != cgProfiles.length){
			throw new RuntimeException("unequal number of profiles: " + fgProfiles.length + " vs " + cgProfiles.length);
		}
		for(int i = 0; i < fgProfiles.length; i++){
			String fgName = fgProfiles[i].getName();
			String cgName = cgProfiles[i].getName();
			if(!fgName.equals(cgName)){
				throw new RuntimeException("wrong file mapping: " + fgName + " vs " + cgName);
			}
			if(fgName.matches(FileUtil.failingProfileFilterPattern())){
				totalNeg++;
			}
			else{
				totalPos++;
			}
		}
		
		resultsList.add(totalNeg);
		resultsList.add(totalPos);
		resultsList.add(PruningProcessor.IG(totalNeg, 0, totalNeg, totalPos));
		
		/*=================================================================================================*/
		
		FunctionEntrySites cgSites = new FunctionEntrySites(cgSitesFile);
		
		InstrumentationSites fSites = new InstrumentationSites(fgSitesFile);
		if(zuo.processor.functionentry.client.iterative.CClient.needRefine(fSites, cgSites.getFunctions())){
			File refineProfilesFolder = new File(fgProfilesFolder.getParentFile(), "refine");
			File refineSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'r'));
			System.out.println(refineSitesFile.getAbsolutePath());
			System.out.println(refineProfilesFolder.getAbsolutePath());
			PredicateSplittingSiteProfile refineSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, refineSitesFile, refineProfilesFolder, cgSites.getFunctions());
			refineSplit.split();
			
			fSites = new InstrumentationSites(refineSitesFile);
			
			FileUtility.removeFileOrDirectory(fgProfilesFolder);
			FileUtility.removeFileOrDirectory(fgSitesFile);
			
			fgSitesFile = new File(refineSitesFile.getParentFile(), refineSitesFile.getName().replace('r', 'f'));
			fgProfilesFolder = new File(refineProfilesFolder.getParentFile(), "fine-grained");
			System.out.println(fgSitesFile.getAbsolutePath());
			System.out.println(fgProfilesFolder.getAbsolutePath());
			System.out.println(fgSitesFile.exists());
			System.out.println(fgProfilesFolder.exists());
			
			refineSitesFile.renameTo(fgSitesFile);
			refineProfilesFolder.renameTo(fgProfilesFolder);
			System.out.println(refineSitesFile.getAbsolutePath());
			System.out.println(refineProfilesFolder.getAbsolutePath());
			System.out.println(fgSitesFile.exists());
			System.out.println(fgProfilesFolder.exists());
		}
		
		SitesInfo fgSitesInfo = new SitesInfo(fSites);
		resultsList.add(FileUtils.sizeOf(fgSitesFile));
		resultsList.add(fgSitesInfo.getNumPredicateSites());
		resultsList.add(fgSitesInfo.getNumPredicateItems());
		resultsList.add(FileUtils.sizeOf(fgProfilesFolder));
		resultsList.add(cgSites.getNumFunctionEntrySites());
		
		File originalDatasetFolder = new File(resultOutputFolder, "original");
		if(!originalDatasetFolder.exists()){
			originalDatasetFolder.mkdirs();
		}
		
		IDataSet dataset = runMultiPreprocess(fgProfilesFolder, originalDatasetFolder, fgSitesFile, rounds, time, writer, resultsList);
		runMultiMBS(command, originalDatasetFolder, rounds, time, writer, resultsList, totalNeg, totalPos);
		
		/*=================================================================================================*/
		//with saving in coarse-grained phase-- running partial passing tests
		
//		File selectedCGProfilesFolder = new File(cgProfilesFolder.getParentFile(), "cg");
//		Set<Integer> indices = selectCGProfiles(cgProfilesFolder, selectedCGProfilesFolder, totalNeg, totalPos, resultsList);
//		
//		resultsList.add(FileUtils.sizeOf(cgSitesFile));
//		resultsList.add(new FunctionEntrySites(cgSitesFile).getNumFunctionEntrySites());
//		resultsList.add(FileUtils.sizeOf(selectedCGProfilesFolder));
//		
//		File cgFolder = new File(resultOutputFolder, "cg");
//		if(!cgFolder.exists()){
//			cgFolder.mkdirs();
//		}
//		FileCollection.writeCollection(indices, new File(cgFolder, "indices.txt" ));
//		
//		TwopassFunctionClient funClient = runMultiCGClient(cgSitesFile, selectedCGProfilesFolder, totalPos, fgSitesFile, rounds, time, writer, resultsList);
//		
//		assert(funClient.getList().size() == funClient.getsInfo().getMap().size());
//		funClient.printEntry(writer);
//
//		FileUtility.removeFileOrDirectory(selectedCGProfilesFolder);
		
		
		//-------------------------------------------------------------------------------------------------//
		//without saving in coarse-grained phase-- running all the passing tests
		resultsList.add(totalNeg);
		resultsList.add(totalPos);
		
		resultsList.add(FileUtils.sizeOf(cgSitesFile));
		resultsList.add(new FunctionEntrySites(cgSitesFile).getNumFunctionEntrySites());
		resultsList.add(FileUtils.sizeOf(cgProfilesFolder));
		
		TwopassFunctionClient funClient = runMultiCGClient(cgSitesFile, cgProfilesFolder, totalPos, fgSitesFile, rounds, time, writer, resultsList);
		
		assert(funClient.getList().size() == funClient.getsInfo().getMap().size());
		funClient.printEntry(writer);

		
		//-------------------------------------------------------------------------------------------------//
		assert(dataset instanceof PredicateDataSet);
		ProcessorPreDSInfoWithinFun processorDSInfo = new ProcessorPreDSInfoWithinFun((PredicateDataSet) dataset);
		processorDSInfo.process();
		Map<String, PredicateDSInfoWithinFunction> DSInfo = processorDSInfo.getDSInfoMap();
		List<Map.Entry<FunctionEntrySite, FrequencyValue>> list = funClient.getList();
		assert(list.size() >= DSInfo.size());
		
		processDSCorrelation(DSInfo, list, correlationData, correlationResults);
		//-------------------------------------------------------------------------------------------------//
		
		/*=================================================================================================*/
		
		Set<String> boostFunctionSet = funClient.getBoostFunctionSet(mode, percent);
		File boostProfilesFolder = new File(fgProfilesFolder.getParentFile(), "boost");
		File boostSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'b'));
		PredicateSplittingSiteProfile boostSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, boostSitesFile, boostProfilesFolder, boostFunctionSet);
		boostSplit.split();
		
		SitesInfo boostSitesInfo = new SitesInfo(new InstrumentationSites(boostSitesFile));
		resultsList.add(FileUtils.sizeOf(boostSitesFile));
		resultsList.add(boostSitesInfo.getNumPredicateSites());
		resultsList.add(boostSitesInfo.getNumPredicateItems());
		resultsList.add(FileUtils.sizeOf(boostProfilesFolder));
		resultsList.add(boostFunctionSet.size());
		
		File boostDatasetFolder = new File(resultOutputFolder, "boost");
		if(!boostDatasetFolder.exists()){
			boostDatasetFolder.mkdirs();
		}
		FileCollection.writeCollection(boostFunctionSet, new File(boostDatasetFolder, "boost_functions_" + mode + "_" + percent + ".txt" ));
		
		runMultiPreprocess(boostProfilesFolder, boostDatasetFolder, boostSitesFile, rounds, time, writer, resultsList);
		threshold = runMultiMBS(command, boostDatasetFolder, rounds, time, writer, resultsList, totalNeg, totalPos);

		FileUtility.removeFileOrDirectory(boostProfilesFolder);
		FileUtility.removeFileOrDirectory(boostSitesFile);
		
		
		//-------------------------------------------------------------------------------------------------//
		
		Set<String> pruneFunctionSet = funClient.getFunctionSet(threshold);
		Set<String> pruneMinusBoostFunctionSet = new LinkedHashSet<String>(pruneFunctionSet);
		pruneMinusBoostFunctionSet.removeAll(boostFunctionSet);
		
		if(pruneMinusBoostFunctionSet.isEmpty()){
			resultsList.add(0L);
			resultsList.add(0);
			resultsList.add(0);
			resultsList.add(0L);
			resultsList.add(0);
			
			File pruneMinusBoostDatasetFolder = new File(resultOutputFolder, "pruneMinusBoost");
			if(!pruneMinusBoostDatasetFolder.exists()){
				pruneMinusBoostDatasetFolder.mkdirs();
			}
			FileCollection.writeCollection(pruneMinusBoostFunctionSet, new File(pruneMinusBoostDatasetFolder, "prune_minus_boost_functions_" + mode + "_" + percent + ".txt" ));
			
			//--------------------------------------------//
			
			resultsList.add(pruneFunctionSet.size());
			
			File pruneDatasetFolder = new File(resultOutputFolder, "prune");
			if(!pruneDatasetFolder.exists()){
				pruneDatasetFolder.mkdirs();
			}
			FileCollection.writeCollection(pruneFunctionSet, new File(pruneDatasetFolder, "prune_functions_" + mode + "_" + percent + ".txt" ));
			
			assignResultsListForPreprocessAndMBS(resultsList);
		}
		else{
			File pruneMinusBoostProfilesFolder = new File(fgProfilesFolder.getParentFile(), "pruneMinusBoost");
			File pruneMinusBoostSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'm'));
			PredicateSplittingSiteProfile pruneMinusBoostSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, pruneMinusBoostSitesFile, pruneMinusBoostProfilesFolder, pruneMinusBoostFunctionSet);
			pruneMinusBoostSplit.split();
			
			SitesInfo pruneMinusBoostSitesInfo = new SitesInfo(new InstrumentationSites(pruneMinusBoostSitesFile));
			resultsList.add(FileUtils.sizeOf(pruneMinusBoostSitesFile));
			resultsList.add(pruneMinusBoostSitesInfo.getNumPredicateSites());
			resultsList.add(pruneMinusBoostSitesInfo.getNumPredicateItems());
			resultsList.add(FileUtils.sizeOf(pruneMinusBoostProfilesFolder));
			resultsList.add(pruneMinusBoostFunctionSet.size());
			
			File pruneMinusBoostDatasetFolder = new File(resultOutputFolder, "pruneMinusBoost");
			if(!pruneMinusBoostDatasetFolder.exists()){
				pruneMinusBoostDatasetFolder.mkdirs();
			}
			FileCollection.writeCollection(pruneMinusBoostFunctionSet, new File(pruneMinusBoostDatasetFolder, "prune_minus_boost_functions_" + mode + "_" + percent + ".txt" ));

			//--------------------------------------------//
			
			File pruneProfilesFolder = new File(fgProfilesFolder.getParentFile(), "prune");
			File pruneSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'p'));
			PredicateSplittingSiteProfile pruneSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, pruneSitesFile, pruneProfilesFolder, pruneFunctionSet);
			pruneSplit.split();
			
			resultsList.add(pruneFunctionSet.size());
			
			File pruneDatasetFolder = new File(resultOutputFolder, "prune");
			if(!pruneDatasetFolder.exists()){
				pruneDatasetFolder.mkdirs();
			}
			FileCollection.writeCollection(pruneFunctionSet, new File(pruneDatasetFolder, "prune_functions_" + mode + "_" + percent + ".txt" ));

			if(threshold == 0){
				runMultiPreprocess(pruneMinusBoostProfilesFolder, pruneDatasetFolder, pruneMinusBoostSitesFile, rounds, time, writer, resultsList);
			}
			else{
				runMultiPreprocess(pruneProfilesFolder, pruneDatasetFolder, pruneSitesFile, rounds, time, writer, resultsList);
			}
			runMultiMBS(command, pruneDatasetFolder, rounds, time, writer, resultsList, totalNeg, totalPos);
			
			FileUtility.removeFileOrDirectory(pruneMinusBoostProfilesFolder);
			FileUtility.removeFileOrDirectory(pruneMinusBoostSitesFile);
			FileUtility.removeFileOrDirectory(pruneProfilesFolder);
			FileUtility.removeFileOrDirectory(pruneSitesFile);
		}

		
		//-------------------------------------------------------------------------------------------------//
		
		File originalAll = new File(resultOutputFolder, "original_all");
		FileUtility.removeFileOrDirectory(originalAll);
		File boostAll = new File(resultOutputFolder, "boost_all");
		FileUtility.removeFileOrDirectory(boostAll);
		File pruneAll = new File(resultOutputFolder, "prune_all");
		FileUtility.removeFileOrDirectory(pruneAll);
		
	}

	private void processDSCorrelation(Map<String, PredicateDSInfoWithinFunction> DSInfo, List<Map.Entry<FunctionEntrySite, FrequencyValue>> list,
			Map<String, List<Object>> correlationData, List<Object> correlationResults) {
		//construct the DS map
		for(int i = 0; i < list.size(); i++){
			Map.Entry<FunctionEntrySite, FrequencyValue> entry = list.get(i);
			String function = entry.getKey().getFunctionName();
			List<Object> array = new ArrayList<Object>();
			FrequencyValue value = entry.getValue();
			array.add((double) (list.size() - i - 1));
			array.add(value.getC_r());
			array.add((double) value.getNegative());
			array.add((double) value.getPositive());
			if(DSInfo.containsKey(function)){
				PredicateDSInfoWithinFunction dsInfo = DSInfo.get(function);
				array.add(dsInfo.getMax_DS());
				array.add(dsInfo.getMean_DS());
				array.add(dsInfo.getMedian_DS());
			}
			else{
				array.add(0.0D);
				array.add(0.0D);
				array.add(0.0D);
			}
			if(correlationData.containsKey(function)){
				throw new RuntimeException("multiple functions error");
			}
			correlationData.put(function, array);
		}
		
		//compute the Correlation Coefficient
		double cc;
		
		int fullSize = correlationData.size();
		correlationResults.add(fullSize);
		
		cc = computeCorrelationCoefficient(correlationData, 0, 4, true);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, fullSize));
		
		cc = computeCorrelationCoefficient(correlationData, 1, 4, true);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, fullSize));
		
		cc = computeCorrelationCoefficient(correlationData, 0, 5, true);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, fullSize));
		
		cc = computeCorrelationCoefficient(correlationData, 1, 5, true);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, fullSize));
		
		cc = computeCorrelationCoefficient(correlationData, 0, 6, true);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, fullSize));
		
		cc = computeCorrelationCoefficient(correlationData, 1, 6, true);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, fullSize));
		
		
		int partialSize = getPartialSize(correlationData);
		correlationResults.add(partialSize);
		
		cc = computeCorrelationCoefficient(correlationData, 0, 4, false);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, partialSize));
		
		cc = computeCorrelationCoefficient(correlationData, 1, 4, false);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, partialSize));
		
		cc = computeCorrelationCoefficient(correlationData, 0, 5, false);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, partialSize));
		
		cc = computeCorrelationCoefficient(correlationData, 1, 5, false);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, partialSize));
		
		cc = computeCorrelationCoefficient(correlationData, 0, 6, false);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, partialSize));
		
		cc = computeCorrelationCoefficient(correlationData, 1, 6, false);
		correlationResults.add(cc);
		correlationResults.add(computeTValueOfCC(cc, partialSize));
		
	}

	public static double computeTValueOfCC(double cc, int size) {
		// TODO Auto-generated method stub
		return cc / Math.sqrt((1 - cc * cc) / (size - 2));
	}

	private int getPartialSize(Map<String, List<Object>> correlationData) {
		// TODO Auto-generated method stub
		int size = 0;
		for(String function: correlationData.keySet()){
			List<Object> list = correlationData.get(function);
			if(Math.abs((Double) list.get(2) - 0) < 0.0000001){
				break;
			}
			size++;
		}
		
		return size;
	}

	/**
	 * @param correlationData
	 * 0:ordering index; 1:ds; 2:neg; 3:pos; 4:max_ds; 5:mean_ds; 6:median_ds;
	 * @param i: variable
	 * @param j: variable
	 * @return
	 */
	private double computeCorrelationCoefficient(Map<String, List<Object>> correlationData, int i, int j, boolean fullList) {
		// TODO Auto-generated method stub
		double sumi = 0, sumj = 0, sumii = 0, sumjj = 0, sumij = 0;
		int size = 0;
		if(fullList){//all the functions
			for(String function: correlationData.keySet()){
				List<Object> list = correlationData.get(function);
				size++;
				sumi += (Double)list.get(i);
				sumj += (Double)list.get(j);
				sumii += (Double)list.get(i) * (Double)list.get(i);
				sumjj += (Double)list.get(j) * (Double)list.get(j);
				sumij += (Double)list.get(i) * (Double)list.get(j);
			}
			assert(size == correlationData.size());
		}
		else{//all the functions whose negative support is bigger than 0
			for(String function: correlationData.keySet()){
				List<Object> list = correlationData.get(function);
				if(Math.abs((Double) list.get(2) - 0) < 0.0000001){
					break;
				}
				size++;
				sumi += (Double)list.get(i);
				sumj += (Double)list.get(j);
				sumii += (Double)list.get(i) * (Double)list.get(i);
				sumjj += (Double)list.get(j) * (Double)list.get(j);
				sumij += (Double)list.get(i) * (Double)list.get(j);
			}
		}
		
		double ij = sumij - sumi * sumj / size;
		double ii = sumii - sumi * sumi / size;
		double jj = sumjj - sumj * sumj / size;
		
		return ij / Math.sqrt(ii * jj);
	}

	private void assignResultsListForPreprocessAndMBS(List<Object> resultsList) {
		//for preprocessing
		resultsList.add(0);
		resultsList.add(0);
		resultsList.add(0);
		resultsList.add(0);
		resultsList.add(0L);
		resultsList.add(0.0D);
		//for mining
		resultsList.add(0.0D);
		resultsList.add(0.0D);
		resultsList.add(0L);
	}

	private TwopassFunctionClient runMultiCGClient(File cgSitesFile, File selectedCGProfilesFolder, int totalPos, final File fgSitesFile, int rounds, double time, PrintWriter writer, List<Object> resultsList) {
		Object[] resultsCG = new Object[4];
		Object[][] resultsArrayCG = new Object[rounds][4];
		Object[] averageResultsCG;
		
		TwopassFunctionClient funClient = new TwopassFunctionClient(cgSitesFile, selectedCGProfilesFolder, totalPos, fgSitesFile, resultsCG, writer);
		if(((Double) resultsCG[3]) < time){
			for(int i = 0; i < resultsArrayCG.length; i++){
				new TwopassFunctionClient(cgSitesFile, selectedCGProfilesFolder, totalPos, fgSitesFile, resultsArrayCG[i], writer);
			}
			averageResultsCG = computeAverageResults(resultsArrayCG);
		}
		else{
			averageResultsCG = resultsCG;
		}
		for(int i = 0; i < averageResultsCG.length; i++){
			resultsList.add(averageResultsCG[i]);
		}
		return funClient;
	}

	/**select coarse-grained profiles from cgProfilesFolder to selectedCGProfilesFolder
	 * @param cgProfilesFolder
	 * @param selectedCGProfilesFolder
	 * @param totalPos 
	 * @param totalNeg 
	 * @param resultsList 
	 * @throws IOException
	 */
	private Set<Integer> selectCGProfiles(File cgProfilesFolder, File selectedCGProfilesFolder, int totalNeg, int totalPos, List<Object> resultsList) throws IOException {
		double percent = 0.1;
		Set<Integer> files = new LinkedHashSet<Integer>();
		
		File[] cgFailingProfiles = cgProfilesFolder.listFiles(FileUtil.createFailingProfileFilter());
		Arrays.sort(cgFailingProfiles, new FileUtil.FileComparator());
		for(File cgFailingProfile: cgFailingProfiles){
			FileUtils.copyFileToDirectory(cgFailingProfile, selectedCGProfilesFolder);
			files.add(FileUtil.getIndex(cgFailingProfile.getName()));
		}
		assert(cgFailingProfiles.length == files.size());
		resultsList.add(cgFailingProfiles.length);
		
		File[] cgPassingProfiles = cgProfilesFolder.listFiles(FileUtil.createPassingProfileFilter());
		assert(cgPassingProfiles.length == totalPos);
		Arrays.sort(cgPassingProfiles, new FileUtil.FileComparator());
		//p == Min{Max{percent * (totalPos + totalNeg), totalNeg}, totalPos}
		int p;
		if(totalNeg <= percent * (totalNeg + totalPos)){
			p = (int) (percent * (totalNeg + totalPos));
			if(p > totalPos){
				p = totalPos;
			}
		}
		else if(totalNeg >= totalPos){
			p = totalPos;
		}
		else{
			p = totalNeg;
		}
		for(int i = 0; i < totalPos / p * p; i += totalPos / p){
			File cgPassingProfile = cgPassingProfiles[i];
			FileUtils.copyFileToDirectory(cgPassingProfile, selectedCGProfilesFolder);
			files.add(FileUtil.getIndex(cgPassingProfile.getName()));
		}
		resultsList.add(p);
		assert(p + totalNeg == files.size());
		
		return files;
	}

	private IDataSet runMultiPreprocess(File fgProfilesFolder, File originalDatasetFolder, final File fgSitesFile, int rounds, double time, PrintWriter writer, List<Object> resultsList) {
		Object[] resultsOriginalPre = new Object[6];
		Object[][] resultsArrayOriginalPre = new Object[rounds][6];
		Object[] averageResultsOriginalPre;
		
		DefaultPredicateProcessorWithLabel originalInstance = new DefaultPredicateProcessorWithLabel(fgProfilesFolder, originalDatasetFolder, fgSitesFile);
		IDataSet dataset = originalInstance.run(resultsOriginalPre, writer);
		
		if(((Double) resultsOriginalPre[5]) < time){
			for(int i = 0; i < resultsArrayOriginalPre.length; i++){
				originalInstance = new DefaultPredicateProcessorWithLabel(fgProfilesFolder, originalDatasetFolder, fgSitesFile);
				originalInstance.run(resultsArrayOriginalPre[i], writer);
			}
			averageResultsOriginalPre = computeAverageResults(resultsArrayOriginalPre);
		}
		else{
			averageResultsOriginalPre = resultsOriginalPre;
		}
		for(int i = 0; i < averageResultsOriginalPre.length; i++){
			resultsList.add(averageResultsOriginalPre[i]);
		}
		
		return dataset;
	}

	private double runMultiMBS(String command, File originalDatasetFolder, int rounds, double time, PrintWriter writer, List<Object> resultsList, int totalNeg, int totalPos) {
		Object[] resultsOriginalMine = new Object[3];
		Object[][] resultsArrayOriginalMine = new Object[rounds][3];
		Object[] averageResultsOriginalMine;
		
		initializeResultsMine(resultsOriginalMine);
		double threshold = runMBS(command, originalDatasetFolder, k, resultsOriginalMine, writer, totalNeg, totalPos);
		
		if(((Double) resultsOriginalMine[1]) < time){
			for(int i = 0; i < resultsArrayOriginalMine.length; i++){
				initializeResultsMine(resultsArrayOriginalMine[i]);
				runMBS(command, originalDatasetFolder, k, resultsArrayOriginalMine[i], writer, totalNeg, totalPos);
			}
			averageResultsOriginalMine = computeAverageResults(resultsArrayOriginalMine);
		}
		else{
			averageResultsOriginalMine = resultsOriginalMine;
		}
		for(int i = 0; i < averageResultsOriginalMine.length; i++){
			resultsList.add(averageResultsOriginalMine[i]);
		}
		
		return threshold;
	}
	
	private void initializeResultsMine(Object[] resultsMine){
		resultsMine[0] = 0.0D;
		resultsMine[1] = 0.0D;
		resultsMine[2] = 0L;
	}

	private Object[] computeAverageResults(Object[][] resultsArray) {
		// TODO Auto-generated method stub
		Object[] averageResults = new Object[resultsArray[0].length];
		for(int i = 0; i < resultsArray[0].length; i++){
			Object object = resultsArray[0][i];
			if(object instanceof Double){
				double sum = 0;
				for(int j = 0; j < resultsArray.length; j++){
					sum += (Double) resultsArray[j][i];
				}
				averageResults[i] = sum / (resultsArray.length);
			}
			else if(object instanceof Integer){
				int sum = 0;
				for(int j = 0; j < resultsArray.length; j++){
					sum += (Integer) resultsArray[j][i];
				}
				averageResults[i] = sum / (resultsArray.length);
			}
			else if(object instanceof Long){
				long sum = 0;
				for(int j = 0; j < resultsArray.length; j++){
					sum += (Long) resultsArray[j][i];
				}
				averageResults[i] = sum / (resultsArray.length);
			}
			else{
				throw new RuntimeException("abnormal data type");
			}
		}
		
		return averageResults;
	}

	private double runMBS(String command, File datasetFolder, int k, Object[] resultsArray, PrintWriter writer, int totalNeg, int totalPos) {
		double threshold = 0;
		double time = 0;
		long memory = 0;
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
					writer.println(line);
//					threshold = Double.parseDouble(line.substring(line.lastIndexOf("=") + 1));
					threshold = PruningProcessor.IG(Integer.parseInt(line.substring(line.lastIndexOf('-') + 1, line.lastIndexOf(')')).trim()), 
							Integer.parseInt(line.substring(line.lastIndexOf('+') + 1, line.lastIndexOf('/')).trim()), totalNeg, totalPos);
					resultsArray[0] = threshold;
					System.out.println(threshold);
				}
				if(line.matches("time-cost.*=.*")){
					System.out.println(line);
					writer.println(line);
				}
				if(line.contains("user CUP time used,")){
					System.out.println(line);
					writer.println(line);
					time = Double.parseDouble(line.substring(line.lastIndexOf(",") + 1, line.lastIndexOf("(")).trim());
				}
				if(line.contains("system CUP time used,")){
					System.out.println(line);
					writer.println(line);
					time += Double.parseDouble(line.substring(line.lastIndexOf(",") + 1, line.lastIndexOf("(")).trim());
					String timeFormat = new DecimalFormat("#.###").format(time);
					resultsArray[1] = Double.parseDouble(timeFormat);
					System.out.println(timeFormat);
				}
				if(line.contains("maximum resident set size,")){
					System.out.println(line);
					writer.println(line);
					memory = Long.parseLong(line.substring(line.lastIndexOf(",") + 1, line.lastIndexOf("(")).trim());
					resultsArray[2] = memory;
					System.out.println(memory);
				}
			}
			System.out.println();
			writer.println();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return threshold;
	}
	
	private void printResultToExcel(){
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Data");
		addResultTitle(sheet);
		
		int rownum = sheet.getPhysicalNumberOfRows();
		for(String version: this.resultsMap.keySet()){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(version);
			
			for(Object object: this.resultsMap.get(version)){
				cell = row.createCell(cellnum++);
				if(object instanceof Integer){
					cell.setCellValue((Integer) object);
				}
				else if(object instanceof Double){
					cell.setCellValue((Double) object);
				}
				else if(object instanceof Long){
					cell.setCellValue((Long) object);
				}
				else if(object instanceof String){
					cell.setCellValue((String) object);
				}
			}
		}
		
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "__" + this.mode + "_" + this.percent + "_v" + this.startVersion + "-v" + this.endVersion + ".xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addResultTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		
		String[] tstitles = {"F", "P", "DS_Max"};
		String[] cgtitles = {"F", "P", "CGSite_Size", "#FunctionEntry", "CGTraces_Size","#Function", "#TFFunction", "Memory", "Time"};
		String[] fgtitles = {"FGSite_Size", "#Site_Static", "#Predicate_Static", "FGTraces_Size", "#Function", "#P_Total", "#P_FIncrease", "#P_FLocal", "#Predicate", "Memory_Pre", "Time_Pre", "DS", "Time_Mine", "Memory_Mine"};
		String[] pruneMinusBoosttitles = {"FGSite_Size", "#Site_Static", "#Predicate_Static", "FGTraces_Size", "#Function"};
		String[] prunetitles = {"#Function", "#P_Total", "#P_FIncrease", "#P_FLocal", "#Predicate", "Memory_Pre", "Time_Pre", "DS", "Time_Mine", "Memory_Mine"};
		String[] fgs = {"boost", "pruneMinusBoost", "prune"};
		
		Cell cell1 = row1.createCell(cellnum1++);
		cell1.setCellValue(" ");
		Cell cell0 = row0.createCell(cellnum0++);
		cell0.setCellValue(" ");
		
		for(int i = 0; i < tstitles.length; i++){
			cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(" ");
			cell1 = row1.createCell(cellnum1++);
			cell1.setCellValue(tstitles[i]);
		}
		for(int i = 0; i < fgtitles.length; i++){
			cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue("original");
			cell1 = row1.createCell(cellnum1++);
			cell1.setCellValue(fgtitles[i]);
		}
		for(int i = 0; i < cgtitles.length; i++){
			cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue("cg");
			cell1 = row1.createCell(cellnum1++);
			cell1.setCellValue(cgtitles[i]);
		}
		for(int i = 0; i < fgs.length; i++){
//			cell1 = row1.createCell(cellnum1++);
//			cell1.setCellValue(" ");
//			cell0 = row0.createCell(cellnum0++);
//			cell0.setCellValue(" ");
			if(fgs[i].equals("pruneMinusBoost")){
				for(int j = 0; j < pruneMinusBoosttitles.length; j++){
					cell0 = row0.createCell(cellnum0++);
					cell0.setCellValue(fgs[i]);
					cell1 = row1.createCell(cellnum1++);
					cell1.setCellValue(pruneMinusBoosttitles[j]);
				}
			}
			else if(fgs[i].equals("prune")){
				for(int j = 0; j < prunetitles.length; j++){
					cell0 = row0.createCell(cellnum0++);
					cell0.setCellValue(fgs[i]);
					cell1 = row1.createCell(cellnum1++);
					cell1.setCellValue(prunetitles[j]);
				}
			}
			else{
				for(int j = 0; j < fgtitles.length; j++){
					cell0 = row0.createCell(cellnum0++);
					cell0.setCellValue(fgs[i]);
					cell1 = row1.createCell(cellnum1++);
					cell1.setCellValue(fgtitles[j]);
				}
			}
		}
	}
	
	private void printCorrelationToExcel(){
		XSSFWorkbook workbook = new XSSFWorkbook();
		printCorrelationResultsToExcel(workbook);
		printCorrelationDataToExcel(workbook);
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "_v" + this.startVersion + "-v" + this.endVersion + "_correlation.xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void printCorrelationResultsToExcel(XSSFWorkbook workbook) {
		// TODO Auto-generated method stub
		XSSFSheet sheet = workbook.createSheet("Results");
		addCorrelationResultsTitle(sheet);
		
		int rownum = sheet.getPhysicalNumberOfRows();
		for(String version: this.correlationResultsMap.keySet()){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(version);
			
			for(Object object: this.correlationResultsMap.get(version)){
				cell = row.createCell(cellnum++);
				if(object instanceof Integer){
					cell.setCellValue((Integer) object);
				}
				else if(object instanceof Double){
					cell.setCellValue((Double) object);
				}
				else if(object instanceof Long){
					cell.setCellValue((Long) object);
				}
				else if(object instanceof String){
					cell.setCellValue((String) object);
				}
			}
		}
	}

	private void addCorrelationResultsTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		
		String[] ftitles = {"Full", "Partial"};
		String[] titles = {"Size", "Index-Max", "T", "DS-Max", "T", "Index-Mean", "T",
				"DS-Mean", "T", "Index-Median", "T", "DS-Median", "T"};
		
		Cell cell0 = row0.createCell(cellnum0++);
		cell0.setCellValue(" ");
		Cell cell1 = row1.createCell(cellnum1++);
		cell1.setCellValue(" ");
		
		for(int j = 0; j < ftitles.length; j++){
			for(int i = 0; i < titles.length; i++){
				cell0 = row0.createCell(cellnum0++);
				cell0.setCellValue(ftitles[j]);
				cell1 = row1.createCell(cellnum1++);
				cell1.setCellValue(titles[i]);
			}
		}
	}

	private void printCorrelationDataToExcel(XSSFWorkbook workbook) {
		// TODO Auto-generated method stub
		for(String version: this.correlationDataMap.keySet()){
			XSSFSheet sheet = workbook.createSheet(version.replace('/', '_'));
			addCorrelationDataTitle(sheet);
			
			int rownum = sheet.getPhysicalNumberOfRows();
			for(String function: this.correlationDataMap.get(version).keySet()){
				Row row = sheet.createRow(rownum++);
				int cellnum = 0;
				
				Cell cell = row.createCell(cellnum++);
				cell.setCellValue(function);
				
				for(Object object: this.correlationDataMap.get(version).get(function)){
					cell = row.createCell(cellnum++);
					if(object instanceof Integer){
						cell.setCellValue((Integer) object);
					}
					else if(object instanceof Double){
						cell.setCellValue((Double) object);
					}
					else if(object instanceof Long){
						cell.setCellValue((Long) object);
					}
					else if(object instanceof String){
						cell.setCellValue((String) object);
					}
				}
			}
		}
		
	}

	private void addCorrelationDataTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		
		String[] titles = {" ", "Index", "DS", "Negative", "Positive","Max_DS", "Mean_DS", "Median_DS"};
		
		for(int i = 0; i < titles.length; i++){
			Cell cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(titles[i]);
		}
		
	}
	

}
