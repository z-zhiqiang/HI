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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import zuo.processor.functionentry.processor.BoundCalculator;
import zuo.split.PredicateSplittingSiteProfile;
import zuo.util.file.FileUtil;
import zuo.util.file.FileUtility;
import edu.nus.sun.processor.mps.client.DefaultPredicateProcessorWithLabel;

public class Client {
	private static final int k = 1;
	
	private static final String DATASET_FOLDER_NAME = "predicate-dataset";
	private static final String mbsOutputFile = "mbs.out";
	
	private static final File rootDir = new File("/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/");
	private static final File traceRootDir = new File("/run/media/sunzzq/Research/Research/IResearch/Automated_Bug_Isolation/Twopass/Subjects/");
	private static final File consoleFolder = new File("/run/media/sunzzq/Research/Research/IResearch/Automated_Bug_Isolation/Twopass/Console/");
	
	private final String subject;
	private final byte mode;
	private final double percent;
	private final Map<String, List<Object>> resultsMap;

	public Client(String subject, byte mode, double percent){
		this.subject = subject;
		this.mode = mode;
		this.percent = percent;
		this.resultsMap = new LinkedHashMap<String, List<Object>>();
	}

	public static void main(String[] args) {
//		String[][] argvs = {
//				{"363", "sed"},
//				{"213", "gzip"},
//				{"809", "grep"},
//				{"13585", "space"},
////				{"4130", "printtokens"},
////				{"4115", "printtokens2"},
////				{"5542", "replace"},
////				{"2650", "schedule"},
////				{"2710", "schedule2"},
////				{"1608", "tcas"},
////				{"1052", "totinfo"}
//		};
//		for(int i = 0; i < argvs.length; i++){
//			
//		}
		if(args.length != 3){
			System.err.println("Usage: subject mode(0->F&%; 1->F; 2->%) percent");
			return;
		}
		Client client = new Client(args[0], Byte.parseByte(args[1]), Double.parseDouble(args[2]));
		client.runClientWithConsole();
	}
	public void runClientWithConsole(){
		PrintWriter writer = null;
		try {
			writer =  new PrintWriter(new BufferedWriter(new FileWriter(new File(this.consoleFolder, this.subject + "__" + this.mode + "_" + this.percent + ".console"))));
			runClient(writer);
			
			System.out.println("=================================================");
			System.out.println(this.resultsMap);
			System.out.println("\n\n");
			
			writer.println("=================================================");
			writer.println(this.resultsMap);
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
		File projectRoot = new File(rootDir, this.subject);
		if (!projectRoot.exists()){
			throw new RuntimeException("Project " + projectRoot + " does not exist!");
		}
		File traceProjectRoot = new File(traceRootDir, this.subject);
		if (!traceProjectRoot.exists()){
			throw new RuntimeException("Project " + traceProjectRoot + " does not exist!");
		}

		
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
//				if(!vi.equals("v7")){
//					continue;
//				}
				System.out.println(vi);
				writer.println(vi);
				writer.println();
				
				File fgProfilesFolder = new File(new File(traceProjectRoot, "traces"), vi + "/fine-grained/");
				if (!fgProfilesFolder.exists()) {
					throw new RuntimeException("Fine-grained faulty profiles folder " + fgProfilesFolder + " does not exist.");
				}
				File cgProfilesFolder = new File(new File(traceProjectRoot, "traces"), vi + "/coarse-grained/");
				if (!cgProfilesFolder.exists()) {
					throw new RuntimeException("Coarse-grained faulty profiles folder " + cgProfilesFolder + " does not exist.");
				}
				
				final File fgSitesFile = new File(projectRoot, "versions/" + vi + "/" + vi + "_f.sites");
				final File cgSitesFile = new File(projectRoot, "versions/" + vi + "/" + vi + "_c.sites");
				
				final File resultOutputFolder = new File(traceProjectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
				FileUtility.removeFileOrDirectory(new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME));
				
				List<Object> resultsList = new ArrayList<Object>();
				run(fgProfilesFolder, fgSitesFile, cgProfilesFolder, cgSitesFile, resultOutputFolder, resultsList, writer);
				
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
//					if(!vi.equals("v2/subv1")){
//						continue;
//					}
					System.out.println(vi);
					writer.println(vi);
					writer.println();
					
					//profiles folders
					File fgProfilesFolder = new File(new File(traceProjectRoot, "traces"), vi + "/fine-grained/");
					if (!fgProfilesFolder.exists()) {
						throw new RuntimeException("Fine-grained faulty profiles folder " + fgProfilesFolder + " does not exist.");
					}
					File cgProfilesFolder = new File(new File(traceProjectRoot, "traces"), vi + "/coarse-grained/");
					if (!cgProfilesFolder.exists()) {
						throw new RuntimeException("Coarse-grained faulty profiles folder " + cgProfilesFolder + " does not exist.");
					}
					
					//instrumentation sites files
					final File fgSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_f.sites");
					final File cgSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_c.sites");
					
					//dataset output folder
					final File resultOutputFolder = new File(traceProjectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
					FileUtility.removeFileOrDirectory(new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME));
					
					List<Object> resultsList = new ArrayList<Object>();
					run(fgProfilesFolder, fgSitesFile, cgProfilesFolder, cgSitesFile, resultOutputFolder, resultsList, writer);
					assert(resultsList.size() == 42);
					this.resultsMap.put(vi, resultsList);
				}
			}
		}
		
		printResultToExcel();
	}

	private void run(File fgProfilesFolder, final File fgSitesFile, File cgProfilesFolder, File cgSitesFile, final File resultOutputFolder, List<Object> resultsList, PrintWriter writer) throws IOException {
		int rounds = 3;
		double time = 60;
		
		double threshold = 0;
		String command = "mbs -k " + k + " -n 0.5 -g --refine 2  --metric 0  --dfs  --merge  --cache 9999 --up-limit 2 --print-resource-usage ";
		
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
		
		BoundCalculator bc = new BoundCalculator(totalNeg, totalPos);
		
		resultsList.add(totalNeg);
		resultsList.add(totalPos);
		resultsList.add(bc.IG(totalNeg));
		
		/*=================================================================================================*/
		
		File failingCGProfilesFolder = new File(cgProfilesFolder.getParentFile(), "failingCG");
		splitFailingCGProfiles(cgProfilesFolder, failingCGProfilesFolder);
		
		TwopassFunctionClient funClient = new TwopassFunctionClient(cgSitesFile, failingCGProfilesFolder, fgSitesFile);
		funClient.printEntry(writer);
		assert(funClient.getList().size() == funClient.getsInfo().getMap().size());

		resultsList.add(funClient.getList().size());
		resultsList.add(FileUtils.sizeOf(cgSitesFile));
		resultsList.add(FileUtils.sizeOf(failingCGProfilesFolder));
		
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
		
		runMultiPreprocess(fgProfilesFolder, originalDatasetFolder, fgSitesFile, rounds, time, writer, resultsList);

		runMultiMBS(command, originalDatasetFolder, rounds, time, writer, resultsList);
		
		/*=================================================================================================*/
		
		Set<String> boostFunctionSet = funClient.getBoostFunctionSet(mode, percent);
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
		
		runMultiPreprocess(boostProfilesFolder, boostDatasetFolder, boostSitesFile, rounds, time, writer, resultsList);
		
		FileUtility.removeFileOrDirectory(boostProfilesFolder);
		FileUtility.removeFileOrDirectory(boostSitesFile);
		
		threshold = runMultiMBS(command, boostDatasetFolder, rounds, time, writer, resultsList);
		
		//-------------------------------------------------------------------------------------------------//
		
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
		
		runMultiPreprocess(pruneProfilesFolder, pruneDatasetFolder, pruneSitesFile, rounds, time, writer, resultsList);
		
		FileUtility.removeFileOrDirectory(pruneProfilesFolder);
		FileUtility.removeFileOrDirectory(pruneSitesFile);
		
		runMultiMBS(command, pruneDatasetFolder, rounds, time, writer, resultsList);
		
		//-------------------------------------------------------------------------------------------------//
		
		File originalAll = new File(resultOutputFolder, "original_all");
		FileUtility.removeFileOrDirectory(originalAll);
		File boostAll = new File(resultOutputFolder, "boost_all");
		FileUtility.removeFileOrDirectory(boostAll);
		File pruneAll = new File(resultOutputFolder, "prune_all");
		FileUtility.removeFileOrDirectory(pruneAll);
		
	}

	/**split failing coarse-grained profiles from cgProfilesFolder to failingCGProfilesFolder
	 * @param cgProfilesFolder
	 * @param failingCGProfilesFolder
	 * @throws IOException
	 */
	private void splitFailingCGProfiles(File cgProfilesFolder, File failingCGProfilesFolder) throws IOException {
		File[] cgProfiles = cgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		Arrays.sort(cgProfiles, new FileUtil.FileComparator());
		for(File cgProfile: cgProfiles){
			if(cgProfile.getName().matches(FileUtil.failingProfileFilterPattern())){
				FileUtils.copyFileToDirectory(cgProfile, failingCGProfilesFolder);
			}
		}
	}

	private void runMultiPreprocess(File fgProfilesFolder, File originalDatasetFolder, final File fgSitesFile, int rounds, double time, PrintWriter writer, List<Object> resultsList) {
		Object[] resultsOriginalPre = new Object[6];
		Object[][] resultsArrayOriginalPre = new Object[rounds][6];
		Object[] averageResultsOriginalPre;
		
		DefaultPredicateProcessorWithLabel originalInstance = new DefaultPredicateProcessorWithLabel(fgProfilesFolder, originalDatasetFolder, fgSitesFile);
		originalInstance.run(resultsOriginalPre, writer);
		
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
	}

	private double runMultiMBS(String command, File originalDatasetFolder, int rounds, double time, PrintWriter writer, List<Object> resultsList) {
		Object[] resultsOriginalMine = new Object[3];
		Object[][] resultsArrayOriginalMine = new Object[rounds][3];
		Object[] averageResultsOriginalMine;
		
		double threshold = runMBS(command, originalDatasetFolder, k, resultsOriginalMine, writer);
		
		if(((Double) resultsOriginalMine[1]) < time){
			for(int i = 0; i < resultsArrayOriginalMine.length; i++){
				runMBS(command, originalDatasetFolder, k, resultsArrayOriginalMine[i], writer);
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

	private double runMBS(String command, File datasetFolder, int k, Object[] resultsArray, PrintWriter writer) {
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
					threshold = Double.parseDouble(line.substring(line.lastIndexOf("=") + 1));
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
			System.out.println("\n");
			writer.println("\n");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return threshold;
	}
	
	private void printResultToExcel(){
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Data");
		addTitle(sheet);
		
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
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "__" + this.mode + "_" + this.percent + ".xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		
		String[] tstitles = {"F", "P", "DS_Max"};
		String[] cgtitles = {"#Function", "CGSite_Size", "CGTraces_Size"};
		String[] fgtitles = {"#Function", "FGSite_Size", "FGTraces_Size", "#P_Total", "#P_FIncrease", "#P_FLocal", "#Predicate", "Memory_Pre", "Time_Pre", "DS", "Time_Mine", "Memory_Mine"};
		String[] fgs = {"original", "boost", "prune"};
		
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
			
			for(int j = 0; j < fgtitles.length; j++){
				cell0 = row0.createCell(cellnum0++);
				cell0.setCellValue(fgs[i]);
				cell1 = row1.createCell(cellnum1++);
				cell1.setCellValue(fgtitles[j]);
			}
		}
	}
	
	

}
