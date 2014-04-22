package zuo.processor.functionentry.client.iterative;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.processor.SelectingProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.processor.importance.processor.PredicateImportanceInfoWithinFunction;
import zuo.processor.importance.processor.ProcessorPreImportanceInfoWithinFun;

public class Client_Ranking {
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final int startVersion;
	final int endVersion;
	
	private final Map<String, Map<String, List<Object>>> correlationDataMap;
	private final Map<String, List<Object>> correlationResultsMap;
	
	
	public Client_Ranking(File rootDir, String subject, File consoleFolder, int startV, int endV) {
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		
		this.startVersion = startV;
		this.endVersion = endV;
		
		this.correlationDataMap = new LinkedHashMap<String, Map<String, List<Object>>>();
		this.correlationResultsMap = new LinkedHashMap<String, List<Object>>();
	}

	
	/**compute and print out the results of Siemens' subject including space
	 * 
	 */
	public void runSiemens(){
		File[] versions = new File(rootDir, subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length >= 10) 
						&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion;
			}});
		Arrays.sort(versions, new Comparator<File>(){

			@Override
			public int compare(File arg0, File arg1) {
				return new Integer(Integer.parseInt(arg0.getName().substring(1))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(1))));
			}});
		for(File version: versions){
//			FileUtility.clearFiles(new File(version, "adaptive"));
//			FileUtility.removeDirectory(new File(version, "adaptive"));
			
			String vi = version.getName();
			System.out.println(vi);
			
			SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File(version, vi + "_f.sites")));
			PredicateProfile[] fProfiles = new PredicateProfileReader(new File(rootDir, subject + "/traces/" + vi +"/fine-grained"), sInfo.getSites()).readProfiles();
//			printMethodsList(sInfo.getMap().keySet(), new File(new File(version, "adaptive"), "full"));
			
			FunctionEntrySites cSites = new FunctionEntrySites(new File(version, vi + "_c.sites"));
			FunctionEntryProfile[] cProfiles = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + vi + "/coarse-grained"), cSites).readFunctionEntryProfiles();
			
			runCorrelationData(vi, sInfo, fProfiles, cProfiles);
		}
		
		printCorrelationToExcel();
	}


	/**compute and print out the results for Sir subject excluding Siemens and space
	 * 
	 */
	public void runSir(){
		File[] versions = new File(rootDir, subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("v[0-9]*", name) 
						&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion;
			}});
		Arrays.sort(versions, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				return new Integer(Integer.parseInt(o1.getName().substring(1))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(1))));
			}});
		
		for(File version: versions){
			File[] subversions = version.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 11);
				}});
			Arrays.sort(subversions, new Comparator<File>(){

				@Override
				public int compare(File o1, File o2) {
					return new Integer(Integer.parseInt(o1.getName().substring(4))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(4))));
				}});
			
			for(File subversion: subversions){
//				FileUtility.clearFiles(new File(subversion, "adaptive"));
//				FileUtility.removeDirectory(new File(subversion, "adaptive"));
				
				String vi = version.getName() + "_" + subversion.getName();
				System.out.println(vi);
				
				SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File(subversion, vi + "_f.sites")));
				PredicateProfile[] fProfiles = new PredicateProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained"), sInfo.getSites()).readProfiles();
//				printMethodsList(sInfo.getMap().keySet(), new File(new File(subversion, "adaptive"), "full"));
				
				FunctionEntrySites cSites = new FunctionEntrySites(new File(subversion, vi + "_c.sites"));
				FunctionEntryProfile[] cProfiles = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), cSites).readFunctionEntryProfiles();
				
				runCorrelationData(vi, sInfo, fProfiles, cProfiles);
			}
		}
		
		printCorrelationToExcel();
	}

	
	private void runCorrelationData(String vi, SitesInfo sInfo,
			PredicateProfile[] fProfiles, FunctionEntryProfile[] cProfiles) {
		//divide profiles
		int passing = 0, failing = 0;
		assert(fProfiles.length == cProfiles.length);
		for(int i = 0; i < fProfiles.length; i++){
			assert(fProfiles[i].isCorrect() == cProfiles[i].isCorrect());
			if(fProfiles[i].isCorrect()){
				passing++;
			}
			else{
				failing++;
			}
		}
		
		//fine-grained analysis
		Processor fgProcessor = new Processor(fProfiles, failing, passing);
		fgProcessor.process();
		ProcessorPreImportanceInfoWithinFun imProcessor = new ProcessorPreImportanceInfoWithinFun(fgProcessor.getPredictorsList());
		Map<String, PredicateImportanceInfoWithinFunction> ImportanceInfo = imProcessor.getImportanceInfoMap();
		
		//coarse-grained analysis
		SelectingProcessor cgProcessor = new SelectingProcessor(failing, passing, cProfiles);
		cgProcessor.process();
		
		//filter out methods within which no predicates are instrumented
		filterFrequencyMap(cgProcessor.getFrequencyMap(), sInfo);
		assert(cgProcessor.getFrequencyMap().size() == sInfo.getMap().size());
		List<Entry<FunctionEntrySite, FrequencyValue>> list = sortFunctionEntrySiteMap(cgProcessor.getFrequencyMap(), sInfo);
		
		Map<String, List<Object>> correlationData = new LinkedHashMap<String, List<Object>>();
		List<Object> correlationResults = new ArrayList<Object>();
		processDSCorrelation(ImportanceInfo, list, correlationData, correlationResults);

		this.correlationDataMap.put(vi, correlationData);
		this.correlationResultsMap.put(vi, correlationResults);
	}
	
	private List<Entry<FunctionEntrySite, FrequencyValue>> sortFunctionEntrySiteMap(
			Map<FunctionEntrySite, FrequencyValue> frequencyMap, final SitesInfo sInfo) {
		// TODO Auto-generated method stub
		List<Entry<FunctionEntrySite, FrequencyValue>> list = new ArrayList<Entry<FunctionEntrySite, FrequencyValue>>(frequencyMap.entrySet());
		Collections.sort(list, new Comparator<Entry<FunctionEntrySite, FrequencyValue>>(){

			@Override
			public int compare(Entry<FunctionEntrySite, FrequencyValue> arg0,
					Entry<FunctionEntrySite, FrequencyValue> arg1) {
				// TODO Auto-generated method stub
				
				int r = new Double(arg1.getValue().getH_2()).compareTo(new Double(arg0.getValue().getH_2()));
				if(r == 0){
					String method0 = arg0.getKey().getFunctionName();
					String method1 = arg1.getKey().getFunctionName();
					r = new Integer(sInfo.getMap().get(method0).getNumSites()).compareTo(new Integer(sInfo.getMap().get(method1).getNumSites()));
					if(r == 0){
						r = new Integer(sInfo.getMap().get(method0).getNumPredicates()).compareTo(new Integer(sInfo.getMap().get(method1).getNumPredicates()));
					}
				}
				return r;
			}});
		return list;
	}
	
	
	/**filter out the methods having no instrumented predicates
	 * @param frequencyMap
	 * @param sInfo 
	 */
	public static void filterFrequencyMap(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo) {
		for(Iterator<FunctionEntrySite> it = frequencyMap.keySet().iterator(); it.hasNext();){
			String function = it.next().getFunctionName();
			if(!sInfo.getMap().containsKey(function)){
				it.remove();
			}
		}
	}
	
	private void processDSCorrelation(Map<String, PredicateImportanceInfoWithinFunction> ImportanceInfo, List<Map.Entry<FunctionEntrySite, FrequencyValue>> list,
			Map<String, List<Object>> correlationData, List<Object> correlationResults) {
		//construct the DS map
		for(int i = 0; i < list.size(); i++){
			Map.Entry<FunctionEntrySite, FrequencyValue> entry = list.get(i);
			String function = entry.getKey().getFunctionName();
			List<Object> array = new ArrayList<Object>();
			FrequencyValue value = entry.getValue();
			array.add((double) (list.size() - i - 1));
			array.add(value.getH_2());
			array.add((double) value.getNegative());
			array.add((double) value.getPositive());
			if(ImportanceInfo.containsKey(function)){
				PredicateImportanceInfoWithinFunction dsInfo = ImportanceInfo.get(function);
				array.add(dsInfo.getMax_Importance());
				array.add(dsInfo.getMean_Importance());
				array.add(dsInfo.getMedian_Importance());
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
	
	private void printCorrelationToExcel(){
		XSSFWorkbook workbook = new XSSFWorkbook();
		printCorrelationResultsToExcel(workbook);
		printCorrelationDataToExcel(workbook);
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "_correlation.xlsx"));
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

	public static void main(String[] args) {
		String[][] argvs = {
				{"809", "grep"},
				{"213", "gzip"},
				{"363", "sed"},
				{"13585", "space"},
				{"4130", "printtokens"},
				{"4115", "printtokens2"},
				{"5542", "replace"},
				{"2650", "schedule"},
				{"2710", "schedule2"},
				{"1608", "tcas"},
				{"1052", "totinfo"}
		};
		
		if(args.length != 6 && args.length != 5){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
			}
			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) rootDir subject consoleDir(excluding /) startVersion endVersion" +
					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir consoleDir(excluding /) startVersion endVersion");
			return;
		}
		long time0 = System.currentTimeMillis();
		if(args.length == 6){
			Client_Ranking c = new Client_Ranking(new File(args[1]), args[2], new File(args[3] + "_v" + args[6] + "-v" + args[7]), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
			if(Integer.parseInt(args[0]) == 0){
				c.runSiemens();
			}
			else if(Integer.parseInt(args[0]) == 1){
				c.runSir();
			}
		}
		else if(args.length == 5){
			assert(Integer.parseInt(args[0]) == 0);
			for(int i = 4; i < argvs.length; i++){
				Client_Ranking c = new Client_Ranking(new File(args[1]), argvs[i][1], new File(args[2] + "_v" + args[5] + "-v" + args[6], argvs[i][1]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
				c.runSiemens();
			}
		}
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");

	}
	
}
