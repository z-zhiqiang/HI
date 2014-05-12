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
import zuo.processor.split.PredicateSplittingSiteProfile;

public class Client_Ranking {
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final int startVersion;
	final int endVersion;
	
	private final Map<String, Map<String, List<Object>>> correlationDataMap;
	private final Map<String, List<Object>> correlationResultsMap;
	private final Map<String, List<Object>> convergenceResultsMap;
	
	
	public Client_Ranking(File rootDir, String subject, File consoleFolder, int startV, int endV) {
		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		
		this.startVersion = startV;
		this.endVersion = endV;
		
		this.correlationDataMap = new LinkedHashMap<String, Map<String, List<Object>>>();
		this.correlationResultsMap = new LinkedHashMap<String, List<Object>>();
		this.convergenceResultsMap = new LinkedHashMap<String, List<Object>>();
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
			
			FunctionEntrySites cSites = new FunctionEntrySites(new File(version, vi + "_c.sites"));
			FunctionEntryProfile[] cProfiles = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + vi + "/coarse-grained"), cSites).readFunctionEntryProfiles();
			
			File fgSitesFile = new File(version, vi + "_f.sites");
			InstrumentationSites fSites = new InstrumentationSites(fgSitesFile);
			File fgProfilesFolder = new File(rootDir, subject + "/traces/" + vi + "/fine-grained");
			PredicateProfile[] fProfiles = null;
			
			if(Client.needRefine(fSites, cSites.getFunctions())){
				File transformProfilesFolder = new File(fgProfilesFolder.getParentFile(), "transform");
				File transformSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 't'));
				PredicateSplittingSiteProfile transformSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, transformSitesFile, transformProfilesFolder, cSites.getFunctions());
				transformSplit.split();
				
				fSites = new InstrumentationSites(transformSitesFile);
				fProfiles = new PredicateProfileReader(transformProfilesFolder, fSites).readProfiles();
			}
			else{
				fProfiles = new PredicateProfileReader(fgProfilesFolder, fSites).readProfiles();
			}
			SitesInfo sInfo = new SitesInfo(fSites);
//			printMethodsList(sInfo.getMap().keySet(), new File(new File(version, "adaptive"), "full"));
			
			runForRankingInfo(vi, sInfo, fProfiles, cProfiles);
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
				
				FunctionEntrySites cSites = new FunctionEntrySites(new File(subversion, vi + "_c.sites"));
				FunctionEntryProfile[] cProfiles = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), cSites).readFunctionEntryProfiles();
				
				File fgSitesFile = new File(subversion, vi + "_f.sites");
				InstrumentationSites fSites = new InstrumentationSites(fgSitesFile);
				File fgProfilesFolder = new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained");
				PredicateProfile[] fProfiles = null;
				
				if(Client.needRefine(fSites, cSites.getFunctions())){
					File transformProfilesFolder = new File(fgProfilesFolder.getParentFile(), "transform");
					File transformSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 't'));
					PredicateSplittingSiteProfile transformSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, transformSitesFile, transformProfilesFolder, cSites.getFunctions());
					transformSplit.split();
					
					fSites = new InstrumentationSites(transformSitesFile);
					fProfiles = new PredicateProfileReader(transformProfilesFolder, fSites).readProfiles();
				}
				else{
					fProfiles = new PredicateProfileReader(fgProfilesFolder, fSites).readProfiles();
				}
				SitesInfo sInfo = new SitesInfo(fSites);
//				printMethodsList(sInfo.getMap().keySet(), new File(new File(subversion, "adaptive"), "full"));
				
				runForRankingInfo(vi, sInfo, fProfiles, cProfiles);
			}
		}
		
		printCorrelationToExcel();
	}

	
	private void runForRankingInfo(String vi, SitesInfo sInfo,
			PredicateProfile[] fProfiles, FunctionEntryProfile[] cProfiles) {
		
		//fine-grained analysis
		Processor fgProcessor = new Processor(fProfiles);
		fgProcessor.process();
		ProcessorPreImportanceInfoWithinFun imProcessor = new ProcessorPreImportanceInfoWithinFun(fgProcessor.getPredictorsList());
		imProcessor.process();
		Map<String, PredicateImportanceInfoWithinFunction> ImportanceInfo = imProcessor.getImportanceInfoMap();
		
		//coarse-grained analysis
		SelectingProcessor cgProcessor = new SelectingProcessor(cProfiles);
		cgProcessor.process();
		
		//filter out methods within which no predicates are instrumented
		filterFrequencyMap(cgProcessor.getFrequencyMap(), sInfo);
		assert(cgProcessor.getFrequencyMap().size() == sInfo.getMap().size());
		List<Entry<FunctionEntrySite, FrequencyValue>> list = sortFunctionEntrySiteMap(cgProcessor.getFrequencyMap(), sInfo);
		assert(ImportanceInfo.size() == list.size());
		
		Map<String, List<Object>> correlationData = new LinkedHashMap<String, List<Object>>();
		List<Object> correlationResults = new ArrayList<Object>();
		List<Object> convergenceResults = new ArrayList<Object>();
		processImportanceCorrelationConvergence(ImportanceInfo, list, correlationData, correlationResults, convergenceResults, sInfo);

		this.correlationDataMap.put(vi, correlationData);
		this.correlationResultsMap.put(vi, correlationResults);
		this.convergenceResultsMap.put(vi, convergenceResults);
	}
	
	/**sort functions according to H_2 and LESS_FIRST
	 * @param frequencyMap
	 * @param sInfo
	 * @return
	 */
	public static List<Entry<FunctionEntrySite, FrequencyValue>> sortFunctionEntrySiteMap(
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
	
	private static void processImportanceCorrelationConvergence(Map<String, PredicateImportanceInfoWithinFunction> ImportanceInfo, List<Map.Entry<FunctionEntrySite, FrequencyValue>> list,
			Map<String, List<Object>> correlationData, List<Object> correlationResults, List<Object> convergenceResults, SitesInfo sInfo) {
		
		//construct the Importance map
		constructData(ImportanceInfo, list, correlationData, sInfo);
		
		//compute the Correlation Coefficient
		computeCorrelationResults(correlationData, correlationResults);
		
		//compute the convergence information of importance
		computeConvergenceResults(correlationData, convergenceResults, sInfo);
	}


	private static void computeConvergenceResults(Map<String, List<Object>> correlationData, List<Object> convergenceResults, SitesInfo sInfo) {
		// TODO Auto-generated method stub
		double max_importance = 0;
		
		double area_full_functions = 0.0D;
		double area_partial_functions = 0.0D;
		
		double area_full_sites = 0.0D;
		double area_partial_sites = 0.0D;
		
		int functions_full = 0;
		int functions_partial = 0;
		
		int sites_full = 0;
		int sites_partial = 0;

		for(String function: correlationData.keySet()){
			area_full_functions += max_importance;
			functions_full++;
			
			int numSites = sInfo.getMap().get(function).getNumSites();
			area_full_sites += numSites * max_importance;
			sites_full += numSites;
			
			if(!trivialCase(correlationData, function)){
				area_partial_functions += max_importance;
				functions_partial++;
				
				area_partial_sites += numSites * max_importance;
				sites_partial += numSites; 
			}
			
			//update top importance value
			double importance = (Double) correlationData.get(function).get(4);
			if(importance > max_importance){
				max_importance = importance;
			}
		}
		
		assert(functions_full == correlationData.size());
		assert(functions_partial == getPartialSize(correlationData));
		assert(sites_full == sInfo.getNumPredicateSites());
		
		
		//add results data
		convergenceResults.add(max_importance);
		
		convergenceResults.add(area_full_functions);
		convergenceResults.add((functions_full) * max_importance);
		convergenceResults.add(area_full_functions / ((functions_full) * max_importance));
		convergenceResults.add(area_full_sites);
		convergenceResults.add((sites_full) * max_importance);
		convergenceResults.add(area_full_sites / ((sites_full) * max_importance));
		
		convergenceResults.add(area_partial_functions);
		convergenceResults.add((functions_partial) * max_importance);
		convergenceResults.add(area_partial_functions / ((functions_partial) * max_importance));
		convergenceResults.add(area_partial_sites);
		convergenceResults.add((sites_partial) * max_importance);
		convergenceResults.add(area_partial_sites / ((sites_partial) * max_importance));
		
		
	}


	private static boolean trivialCase(Map<String, List<Object>> correlationData, String function) {
		double h = (Double) correlationData.get(function).get(1);
		return Math.abs(h - 0) < 0.000001;
	}


	/**compute the correlation coefficient between different values
	 * @param correlationData
	 * @param correlationResults
	 */
	private static void computeCorrelationResults(
			Map<String, List<Object>> correlationData,
			List<Object> correlationResults) {
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


	/**construct importance information within each function with H value
	 * @param ImportanceInfo
	 * @param list
	 * @param sInfo 
	 * @param correlationData: 0:index; 1:H; 2:neg; 3:pos; 4:max_importance; 5:mean_importance; 6:median_importance 
	 */
	private static void constructData(
			Map<String, PredicateImportanceInfoWithinFunction> ImportanceInfo,
			List<Map.Entry<FunctionEntrySite, FrequencyValue>> list,
			Map<String, List<Object>> correlationData, SitesInfo sInfo) {
		for(int i = 0; i < list.size(); i++){
			Map.Entry<FunctionEntrySite, FrequencyValue> entry = list.get(i);
			String function = entry.getKey().getFunctionName();
			List<Object> array = new ArrayList<Object>();
			FrequencyValue value = entry.getValue();
			
			array.add((double) (list.size() - i - 1));
			array.add(value.getH_2());
			array.add((double) value.getNegative());
			array.add((double) value.getPositive());
			
			assert(ImportanceInfo.containsKey(function));
			PredicateImportanceInfoWithinFunction importanceInfo = ImportanceInfo.get(function);
			array.add(importanceInfo.getMax_Importance());
			array.add(importanceInfo.getMean_Importance());
			array.add(importanceInfo.getMedian_Importance());
			array.add(sInfo.getMap().get(function).getNumSites());
			array.add(sInfo.getMap().get(function).getNumPredicates());
			
 			if(correlationData.containsKey(function)){
				throw new RuntimeException("multiple functions error");
			}
			correlationData.put(function, array);
		}
	}

	public static double computeTValueOfCC(double cc, int size) {
		// TODO Auto-generated method stub
		return cc / Math.sqrt((1 - cc * cc) / (size - 2));
	}

	private static int getPartialSize(Map<String, List<Object>> correlationData) {
		// TODO Auto-generated method stub
		int size = 0;
		for(String function: correlationData.keySet()){
			if(trivialCase(correlationData, function)){
				continue;
			}
			size++;
		}
		
		return size;
	}

	/**
	 * @param correlationData
	 * @param i: variable
	 * @param j: variable
	 * @return
	 */
	public static double computeCorrelationCoefficient(Map<String, List<Object>> correlationData, int i, int j, boolean fullList) {
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
		printConvergenceResultsToExcel(workbook);
		printCorrelationResultsToExcel(workbook);
		printDataToExcel(workbook);
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
	
	private void printConvergenceResultsToExcel(XSSFWorkbook workbook) {
		// TODO Auto-generated method stub
		XSSFSheet sheet = workbook.createSheet("Results_Convergence");
		addConvergenceResultsTitle(sheet);
		
		int rownum = sheet.getPhysicalNumberOfRows();
		for(String version: this.convergenceResultsMap.keySet()){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(version);
			
			for(Object object: this.convergenceResultsMap.get(version)){
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

	private void addConvergenceResultsTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		
		String[] ftitles = {"Full", "Partial"};
		String[] titles = {"Area_functions", "Area_total_functions", "Percent_functions","Area_sites", "Area_total_sites", "Percent_sites"};
		
		Cell cell0 = row0.createCell(cellnum0++);
		cell0.setCellValue(" ");
		Cell cell1 = row1.createCell(cellnum1++);
		cell1.setCellValue(" ");
		
		cell0 = row0.createCell(cellnum0++);
		cell0.setCellValue(" ");
		cell1 = row1.createCell(cellnum1++);
		cell1.setCellValue("Max_importance");
		
		for(int j = 0; j < ftitles.length; j++){
			for(int i = 0; i < titles.length; i++){
				cell0 = row0.createCell(cellnum0++);
				cell0.setCellValue(ftitles[j]);
				cell1 = row1.createCell(cellnum1++);
				cell1.setCellValue(titles[i]);
			}
		}
	}
	
	private void printCorrelationResultsToExcel(XSSFWorkbook workbook) {
		// TODO Auto-generated method stub
		XSSFSheet sheet = workbook.createSheet("Results_Correlation");
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
		String[] titles = {"Size", "Index-Max", "T", "H-Max", "T", "Index-Mean", "T",
				"H-Mean", "T", "Index-Median", "T", "H-Median", "T"};
		
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

	private void printDataToExcel(XSSFWorkbook workbook) {
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
		
		String[] titles = {" ", "Index", "H", "Negative", "Positive","Max_Importance", "Mean_Importance", "Median_Importance", "Sites", "Predicates"};
		
		for(int i = 0; i < titles.length; i++){
			Cell cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(titles[i]);
		}
		
	}

	public static void main(String[] args) {
		String[][] argvs = {
				{"363", "sed", "7"},
				{"213", "gzip", "5"},
				{"809", "grep", "5"},
				{"4130", "printtokens", "7"},
				{"4115", "printtokens2", "10"},
				{"5542", "replace", "32"},
				{"2650", "schedule", "9"},
				{"2710", "schedule2", "10"},
				{"1608", "tcas", "41"},
				{"1052", "totinfo", "23"},
				{"13585", "space", "38"}
		};
		
		if(args.length != 6 && args.length != 3){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][0]) + String.format("%-20s", argvs[i][1]) + argvs[i][2]);
			}
			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) rootDir subject consoleDir startVersion endVersion" +
					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir consoleDir");
			return;
		}
		long time0 = System.currentTimeMillis();
		if(args.length == 6){
			Client_Ranking c = new Client_Ranking(new File(args[1]), args[2], new File(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
			if(Integer.parseInt(args[0]) == 0){
				c.runSiemens();
			}
			else if(Integer.parseInt(args[0]) == 1){
				c.runSir();
			}
		}
		else if(args.length == 3){
			assert(Integer.parseInt(args[0]) == 0);
			for(int i = 3; i < argvs.length - 1; i++){
				Client_Ranking c = new Client_Ranking(new File(args[1]), argvs[i][1], new File(args[2]), 1, Integer.parseInt(argvs[i][2]));
				c.runSiemens();
			}
		}
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");
		
//		for(int i = 0; i < argvs.length; i++){
//			if(i < 3){
//				Client_Ranking client = new Client_Ranking(new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Subjects\\"), argvs[i][1], new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Console_Ranking\\"), 1, Integer.parseInt(argvs[i][2]));
//				client.runSir();
//			}
//			else if(i < 10){
//				Client_Ranking client = new Client_Ranking(new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Subjects\\Siemens\\"), argvs[i][1], new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Console_Ranking\\"), 1, Integer.parseInt(argvs[i][2]));
//				client.runSiemens();
//			}
//			else if(i == 10){
//				Client_Ranking client = new Client_Ranking(new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Subjects\\"), argvs[i][1], new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Console_Ranking\\"), 1, Integer.parseInt(argvs[i][2]));
//				client.runSiemens();
//			}
//			else{
//				System.err.println("length error");
//			}
//		}
	}
	
}
