package zuo.processor.functionentry.client.iterative;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import zuo.processor.cbi.client.CBIClients;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.FloatKindSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Order;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;
import zuo.processor.functionentry.datastructure.Result;
import zuo.processor.functionentry.datastructure.Statistic;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.processor.split.PredicateSplittingSiteProfile;
import zuo.util.file.FileCollection;

public class Client {
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final int round;
	final int startVersion;
	final int endVersion;
	
	final int[] ks;
	final int start;
	
	final Map<String, Statistic[][]> statisticsMap;
	final Map<String, int[]> cResutlsMap;
	
	
	public Client(int[] ks, File rootDir, String subject, File consoleFolder, int round, final int start, int startV, int endV) {
		this.ks = ks;

		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		
		this.round = round;
		this.start = start;

		this.startVersion = startV;
		this.endVersion = endV;
		
		this.statisticsMap = new LinkedHashMap<String, Statistic[][]>();
		this.cResutlsMap = new LinkedHashMap<String, int[]>();
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
			
			if(needRefine(fSites, cSites.getFunctions())){
				File refineProfilesFolder = new File(fgProfilesFolder.getParentFile(), "refine");
				File refineSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'r'));
				PredicateSplittingSiteProfile refineSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, refineSitesFile, refineProfilesFolder, cSites.getFunctions());
				refineSplit.split();
				
				fSites = new InstrumentationSites(refineSitesFile);
				fProfiles = new PredicateProfileReader(refineProfilesFolder, fSites).readProfiles();
			}
			else{
				fProfiles = new PredicateProfileReader(fgProfilesFolder, fSites).readProfiles();
			}
			SitesInfo sInfo = new SitesInfo(fSites);
//			FileCollection.writeCollection(sInfo.getMap().keySet(), new File(new File(version, "adaptive"), "full"));
			
			this.cResutlsMap.put(vi, new int[3]);
			int[] cResult = this.cResutlsMap.get(vi);
			cResult[0] = cSites.getNumFunctionEntrySites();
			cResult[1] = sInfo.getNumPredicateSites();
			cResult[2] = sInfo.getNumPredicateItems();
			
			this.statisticsMap.put(vi, new Statistic[Score.values().length][Order.values().length]);
			Statistic[][] statistics = this.statisticsMap.get(vi);
			for(int i = 0; i < statistics.length; i++){
				for(int j = 0; j < statistics[i].length; j++){
					statistics[i][j] = new Statistic(this.ks);
				}
			}
			
			CBIClients cs = null;
			IterativeFunctionClient client = null;
			for(int i = 0; i < round; i++){
				System.out.println(i);
				while(true){
					cs = new CBIClients(sInfo, fProfiles, start);
					if(cs.iszFlag()){
						break;
					}
				}
				client = new IterativeFunctionClient(cSites, 
						cProfiles, 
						new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_cbi.out"),
						new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_function.out"), 
						sInfo, 
						cs.getFullInstrumentedCBIClient(), 
						cs.getClientsMap(),
						this.ks);
				
				solveOneRoundResults(statistics, client.getResults(), i);
			}
			
			System.gc();
			System.out.println();
		}
		
		//print out the final results
		printReadableResultsByMode();
		printResultsByModeToExcel();
		printReadableResultsByFlag();
		printResultsByFlagToExcel();
		printOutMethodsListByMode();
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
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 10);
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
				
				if(needRefine(fSites, cSites.getFunctions())){
					File refineProfilesFolder = new File(fgProfilesFolder.getParentFile(), "refine");
					File refineSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'r'));
					PredicateSplittingSiteProfile refineSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, refineSitesFile, refineProfilesFolder, cSites.getFunctions());
					refineSplit.split();
					
					fSites = new InstrumentationSites(refineSitesFile);
					fProfiles = new PredicateProfileReader(refineProfilesFolder, fSites).readProfiles();
				}
				else{
					fProfiles = new PredicateProfileReader(fgProfilesFolder, fSites).readProfiles();
				}
				SitesInfo sInfo = new SitesInfo(fSites);
//				FileCollection.writeCollection(sInfo.getMap().keySet(), new File(new File(subversion, "adaptive"), "full"));
				
				this.cResutlsMap.put(vi, new int[3]);
				int[] cResult = this.cResutlsMap.get(vi);
				cResult[0] = cSites.getNumFunctionEntrySites();
				cResult[1] = sInfo.getNumPredicateSites();
				cResult[2] = sInfo.getNumPredicateItems();
				
				this.statisticsMap.put(vi, new Statistic[Score.values().length][Order.values().length]);
				Statistic[][] statistics = this.statisticsMap.get(vi);
				for(int i = 0; i < statistics.length; i++){
					for(int j = 0; j < statistics[i].length; j++){
						statistics[i][j] = new Statistic(this.ks);
					}
				}
				
				CBIClients cs = null;
				IterativeFunctionClient client = null;
				for(int i = 0; i < round; i++){
					System.out.println(i);
//					long time0 = System.currentTimeMillis();
					while(true){
						cs = new CBIClients(sInfo, fProfiles, start);
						if(cs.iszFlag()){
							break;
						}
					} 
//					long time1 = System.currentTimeMillis();
//					System.out.println("CBIClients:\t" + (time1 - time0));
					client = new IterativeFunctionClient(cSites, 
							cProfiles, 
							new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_cbi.out"),
							new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_function.out"), 
							sInfo, 
							cs.getFullInstrumentedCBIClient(), 
							cs.getClientsMap(), 
							this.ks);
//					long time2 = System.currentTimeMillis();
//					System.out.println("IterativeFunctionClient:\t" + (time2 - time1));
					
					solveOneRoundResults(statistics, client.getResults(), i);
				}
				
				System.gc();
				System.out.println();
			}
		}
		
		printReadableResultsByMode();
		printResultsByModeToExcel();
		printReadableResultsByFlag();
		printResultsByFlagToExcel();
		printOutMethodsListByMode();
	}

	public static boolean needRefine(InstrumentationSites fSites, Set<String> functions) {
		// TODO Auto-generated method stub
		for(String unit: fSites.getBranchSites().keySet()){
			for(BranchSite site: fSites.getBranchSites().get(unit)){
				if(!functions.contains(site.getFunctionName())){
					return true;
				}
			}
		}
		
		for(String unit: fSites.getReturnSites().keySet()){
			for(ReturnSite site: fSites.getReturnSites().get(unit)){
				if(!functions.contains(site.getFunctionName())){
					return true;
				}
			}
		}
		
		for(String unit: fSites.getFloatSites().keySet()){
			for(FloatKindSite site: fSites.getFloatSites().get(unit)){
				if(!functions.contains(site.getFunctionName())){
					return true;
				}
			}
		}
		
		for(String unit: fSites.getScalarSites().keySet()){
			for(ScalarSite site: fSites.getScalarSites().get(unit)){
				if(!functions.contains(site.getFunctionName())){
					return true;
				}
			}
		}
		
		return false;
	}


	private void printOutMethodsListByMode() {
		// TODO Auto-generated method stub
		for(String version: this.cResutlsMap.keySet()){
			Statistic[][] statistics = this.statisticsMap.get(version);
			for(int i = 0; i < statistics.length; i++){
				for(int j = 0; j < statistics[i].length; j++){
					String mode = version + "_" + Score.values()[i] + "_" + Order.values()[j];
					FileCollection.writeCollection(statistics[i][j].getlCFlagStatistics().getbMethods(), 
							new File(new File(this.consoleFolder, "FunctionList"), mode + "_local_best"));
					FileCollection.writeCollection(statistics[i][j].getlCFlagStatistics().getaMethods(), 
							new File(new File(this.consoleFolder, "FunctionList"), mode + "_local_average"));
					
					FileCollection.writeCollection(statistics[i][j].getgCFlagStatistics().getbMethods(), 
							new File(new File(this.consoleFolder, "FunctionList"), mode + "_global_best"));
					FileCollection.writeCollection(statistics[i][j].getgCFlagStatistics().getaMethods(), 
							new File(new File(this.consoleFolder, "FunctionList"), mode + "_global_average"));
					
					for(int k: ks){
						FileCollection.writeCollection(statistics[i][j].getpFlagStatisticsMap().get(k).getbMethods(), 
								new File(new File(this.consoleFolder, "FunctionList"), mode + "_" + k + "_best"));
						FileCollection.writeCollection(statistics[i][j].getpFlagStatisticsMap().get(k).getaMethods(), 
								new File(new File(this.consoleFolder, "FunctionList"), mode + "_" + k + "_average"));
					}
				}
			}
		}	
	}


	private void solveOneRoundResults(Statistic[][] statistics, Result[][] results, int round) {
		for(int i = 0; i < statistics.length; i++){
			for(int j = 0; j < statistics[i].length; j++){
				Result result = results[i][j];
				statistics[i][j].solveOneResult(result, round);
			}
		}
	}
	
	private void printResultsByFlagToExcel() {
		// TODO Auto-generated method stub
		//Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook(); 
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Data By Flag");
        
        //add title: the first row
        addTitleRowByFlag(sheet);
        
        //add content
        int rownum = sheet.getPhysicalNumberOfRows();
		for(String version: this.cResutlsMap.keySet()){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(version);
			
			int[] cResult = this.cResutlsMap.get(version);
			for(int index = 0; index < cResult.length; index++){
				cell = row.createCell(cellnum++);
				cell.setCellValue(cResult[index]);
			}
			
			Statistic[][] statistics = this.statisticsMap.get(version);
			for(int i = 0; i < statistics.length; i++){
				for(int j = 0; j < statistics[i].length; j++){
					statistics[i][j].getlCFlagStatistics().incertOneFlagStatisticToExcel(row);
				}
			}
			for(int i = 0; i < statistics.length; i++){
				for(int j = 0; j < statistics[i].length; j++){
					statistics[i][j].getgCFlagStatistics().incertOneFlagStatisticToExcel(row);
				}
			}
			for(int k: ks){
				for(int i = 0; i < statistics.length; i++){
					for(int j = 0; j < statistics[i].length; j++){
						statistics[i][j].getpFlagStatisticsMap().get(k).incertOneFlagStatisticToExcel(row);
					}
				}
			}
		}
		
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "_" + this.round + "_" + this.start + "_v" + this.startVersion + "-v" + this.endVersion + "_f.xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void addTitleRowByFlag(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		Row row2 = sheet.createRow(rownum++);
		int cellnum2 = 0;
		Row row3 = sheet.createRow(rownum++);
		int cellnum3 = 0;
		
		Cell cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue(" ");
		cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue("methods");
		cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue("sites");
		cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue("predicates");
		for(int i = 0; i < 4; i++){
			Cell cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(" ");
			Cell cell1 = row1.createCell(cellnum1++);
			cell1.setCellValue(" ");
			Cell cell2 = row2.createCell(cellnum2++);
			cell2.setCellValue(" ");
		}
		
		
		List<String> flags = new ArrayList<String>();
		flags.add("Local");
		flags.add("Global");
		
		for(int q = 0; q < 2; q++){
			String flag = flags.get(q);
			for(int i = 0; i < Score.values().length; i++){
				Score score = Score.values()[i];
				for(int j = 0; j < Order.values().length; j++){
					Order order = Order.values()[j];
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("rounds#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("best");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bs%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bp%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bi");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bafp");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("as%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ap%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ai");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aafp");
					
					for(int p = 0; p < 14; p++){
						Cell cell0 = row0.createCell(cellnum0++);
						cell0.setCellValue(flag);
						Cell cell1 = row1.createCell(cellnum1++);
						cell1.setCellValue(String.valueOf(score));
						Cell cell2 = row2.createCell(cellnum2++);
						cell2.setCellValue(String.valueOf(order));
					}
				}
			}
		}
		//for prune case
		for(int q = 0; q < ks.length; q++){
			String flag = "Top " + ks[q];
			for(int i = 0; i < Score.values().length; i++){
				Score score = Score.values()[i];
				for(int j = 0; j < Order.values().length; j++){
					Order order = Order.values()[j];
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("roundsCI2#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("roundsCI0#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("rounds#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("best");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bs%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bp%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bi");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bafp");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("as%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ap%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ai");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aafp");
					
					for(int p = 0; p < 16; p++){
						Cell cell0 = row0.createCell(cellnum0++);
						cell0.setCellValue(flag);
						Cell cell1 = row1.createCell(cellnum1++);
						cell1.setCellValue(String.valueOf(score));
						Cell cell2 = row2.createCell(cellnum2++);
						cell2.setCellValue(String.valueOf(order));
					}
				}
			}
		}
	}


	private void printResultsByModeToExcel() {
		// TODO Auto-generated method stub
		//Blank workbook
        XSSFWorkbook workbook = new XSSFWorkbook(); 
        //Create a blank sheet
        XSSFSheet sheet = workbook.createSheet("Data By Mode");
        
        //add title: the first row
        addTitleRowByMode(sheet);
        
        //add content
        int rownum = sheet.getPhysicalNumberOfRows();
		for(String version: this.cResutlsMap.keySet()){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(version);
			
			int[] cResult = this.cResutlsMap.get(version);
			for(int index = 0; index < cResult.length; index++){
				cell = row.createCell(cellnum++);
				cell.setCellValue(cResult[index]);
			}
			
			Statistic[][] statistics = this.statisticsMap.get(version);
			for(int i = 0; i < statistics.length; i++){
				for(int j = 0; j < statistics[i].length; j++){
					statistics[i][j].incertOneStatisticToExcel(row);
				}
			}
		}
		
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "_" + this.round + "_" + this.start + "_v" + this.startVersion + "-v" + this.endVersion + "_m.xlsx"));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addTitleRowByMode(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		Row row2 = sheet.createRow(rownum++);
		int cellnum2 = 0;
		Row row3 = sheet.createRow(rownum++);
		int cellnum3 = 0;
		
		Cell cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue(" ");
		cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue("methods");
		cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue("sites");
		cell3 = row3.createCell(cellnum3++);
		cell3.setCellValue("predicates");
		for(int i = 0; i < 4; i++){
			Cell cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(" ");
			Cell cell1 = row1.createCell(cellnum1++);
			cell1.setCellValue(" ");
			Cell cell2 = row2.createCell(cellnum2++);
			cell2.setCellValue(" ");
		}
		
		
		List<String> flags = new ArrayList<String>();
		flags.add("Local");
		flags.add("Global");
		
		for(int i = 0; i < Score.values().length; i++){
			Score score = Score.values()[i];
			for(int j = 0; j < Order.values().length; j++){
				Order order = Order.values()[j];
				for(int q = 0; q < 2; q++){
					String flag = flags.get(q);
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("rounds#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("best");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bs%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bp%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bi");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bafp");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("as%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ap%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ai");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aafp");
					
					for(int p = 0; p < 14; p++){
						Cell cell0 = row0.createCell(cellnum0++);
						cell0.setCellValue(String.valueOf(score));
						Cell cell1 = row1.createCell(cellnum1++);
						cell1.setCellValue(String.valueOf(order));
						Cell cell2 = row2.createCell(cellnum2++);
						cell2.setCellValue(flag);
					}
				}
				//for prune case
				for(int q = 0; q < ks.length; q++){
					String flag = "Top " + ks[q];
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("roundsCI2#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("roundsCI0#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("rounds#");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("best");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bs%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bp%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bi");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("bafp");
					
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("as%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ap%");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("ai");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aas");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aap");
					cell3 = row3.createCell(cellnum3++);
					cell3.setCellValue("aafp");
					
					for(int p = 0; p < 16; p++){
						Cell cell0 = row0.createCell(cellnum0++);
						cell0.setCellValue(String.valueOf(score));
						Cell cell1 = row1.createCell(cellnum1++);
						cell1.setCellValue(String.valueOf(order));
						Cell cell2 = row2.createCell(cellnum2++);
						cell2.setCellValue(flag);
					}
				}
			}
		}
	}


	private void printReadableResultsByFlag() {
		// TODO Auto-generated method stub
		PrintWriter cWriter = null;
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			cWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.consoleFolder, this.subject + "_" + this.round + "_" + this.start + "_v" + this.startVersion + "-v" + this.endVersion + ".ftxt"))));

			for(String version: this.cResutlsMap.keySet()){
				int[] cResult = this.cResutlsMap.get(version);
				cWriter.println(version);
				cWriter.println("==============================================================");
				cWriter.println(String.format("%-20s", "methods:" + cResult[0])
						+ String.format("%-20s", "sites:" + cResult[1])
						+ String.format("%-20s", "predicates:" + cResult[2]));
				cWriter.println();
				cWriter.println("==============================================================");
				
				
				Statistic[][] statistics = this.statisticsMap.get(version);
				cWriter.println("Local Consistency:");
				cWriter.println("--------------------------------------------------------------");
				for(int i = 0; i < statistics.length; i++){
					for(int j = 0; j < statistics[i].length; j++){
						String mode = "<" + Score.values()[i] + "," + Order.values()[j] + ">";
						cWriter.println(String.format("%-25s", mode) + statistics[i][j].getlCFlagStatistics().toString());
					}
					cWriter.println();
				}
				
				cWriter.println("Global Consistency:");
				cWriter.println("--------------------------------------------------------------");
				for(int i = 0; i < statistics.length; i++){
					for(int j = 0; j < statistics[i].length; j++){
						String mode = "<" + Score.values()[i] + "," + Order.values()[j] + ">";
						cWriter.println(String.format("%-25s", mode) + statistics[i][j].getgCFlagStatistics().toString());
					}
					cWriter.println();
				}
				
				for(int k: ks){
					cWriter.println("Prune " + k + " Consistency:");
					cWriter.println("--------------------------------------------------------------");
					for(int i = 0; i < statistics.length; i++){
						for(int j = 0; j < statistics[i].length; j++){
							String mode = "<" + Score.values()[i] + "," + Order.values()[j] + ">";
							cWriter.println(String.format("%-25s", mode) + statistics[i][j].getpFlagStatisticsMap().get(k).toString());
						}
						cWriter.println();
					}
				}
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(cWriter != null){
				cWriter.close();
			}
		}
	}

	private void printReadableResultsByMode() {
		// TODO Auto-generated method stub
		PrintWriter cWriter = null;
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			cWriter = new PrintWriter(new BufferedWriter(new FileWriter(new File(this.consoleFolder, this.subject + "_" + this.round + "_" + this.start + "_v" + this.startVersion + "-v" + this.endVersion + ".mtxt"))));
			
			for(String version: this.cResutlsMap.keySet()){
				int[] cResult = this.cResutlsMap.get(version);
				cWriter.println(version);
				cWriter.println("==============================================================");
				cWriter.println(String.format("%-20s", "methods:" + cResult[0])
						+ String.format("%-20s", "sites:" + cResult[1])
						+ String.format("%-20s", "predicates:" + cResult[2]));
				cWriter.println();
				cWriter.println("==============================================================");
				
				Statistic[][] statistics = this.statisticsMap.get(version);
				for(int i = 0; i < statistics.length; i++){
					for(int j = 0; j < statistics[i].length; j++){
						String mode = "<" + Score.values()[i] + "," + Order.values()[j] + ">";
						cWriter.println(mode);
						cWriter.println("--------------------------------------------------------------");
						cWriter.println(statistics[i][j].toString());
						cWriter.println();
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			if(cWriter != null){
				cWriter.close();
			}
		}
	}

	
	public static void main(String[] args) {
		String[][] argvs = {
				{"363", "sed"},
				{"809", "grep"},
				{"213", "gzip"},
				{"13585", "space"},
				{"4130", "printtokens"},
				{"4115", "printtokens2"},
				{"5542", "replace"},
				{"2650", "schedule"},
				{"2710", "schedule2"},
				{"1608", "tcas"},
				{"1052", "totinfo"}
		};
		
		if(args.length != 8 && args.length != 7){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
			}
			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) rootDir subject consoleDir(excluding /) round start([1, 10]) startVersion endVersion" +
					"\nor Usage: subjectMode(0:Siemens; 1:Sir) rootDir consoleDir(excluding /) round start([1, 10]) startVersion endVersion");
			return;
		}
		int[] ks = {1};
		long time0 = System.currentTimeMillis();
		if(args.length == 8){
			Client c = new Client(ks, new File(args[1]), args[2], new File(new File(args[3]), args[2] + "_" + args[4] + "_" + args[5] + "_v" + args[6] + "-v" + args[7]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
			if(Integer.parseInt(args[0]) == 0){
				c.runSiemens();
			}
			else if(Integer.parseInt(args[0]) == 1){
				c.runSir();
			}
		}
		else if(args.length == 7){
			assert(Integer.parseInt(args[0]) == 0);
			for(int i = 4; i < argvs.length; i++){
				Client c = new Client(ks, new File(args[1]), argvs[i][1], new File(new File(args[2]), argvs[i][1] + "_" + args[3] + "_" + args[4] + "_v" + args[5] + "-v" + args[6]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]));
				c.runSiemens();
			}
		}
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");

	}
	
}
