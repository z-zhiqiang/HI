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
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.processor.split.PredicateSplittingSiteProfile;
import zuo.util.file.FileCollection;
import zuo.util.file.FileUtil;

public class CClient {
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final int round;
	
	final int[] ks;
	final int start;
	
	final Map<String, Statistic[][]> statisticsMap;
	final Map<String, int[]> cResutlsMap;
	
	
	public CClient(int[] ks, File rootDir, String subject, File consoleFolder) {
		this.ks = ks;

		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		
		this.round = 1;
		this.start = 10;

		
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
//						&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion
						;
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
			FunctionEntryProfileReader functionEntryProfileReader = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + vi + "/coarse-grained"), cSites);
			FunctionEntryProfile[] cProfiles = functionEntryProfileReader.readFunctionEntryProfiles();
			
			File fgSitesFile = new File(version, vi + "_f.sites");
			InstrumentationSites fSites = new InstrumentationSites(fgSitesFile);
			File fgProfilesFolder = new File(rootDir, subject + "/traces/" + vi + "/fine-grained");
			PredicateProfileReader predicateProfileReader = null;
			
			if(needRefine(fSites, cSites.getFunctions())){
				File refineProfilesFolder = new File(fgProfilesFolder.getParentFile(), "refine");
				File refineSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'r'));
				PredicateSplittingSiteProfile refineSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, refineSitesFile, refineProfilesFolder, cSites.getFunctions());
				refineSplit.split();
				
				fSites = new InstrumentationSites(refineSitesFile);
				predicateProfileReader = new PredicateProfileReader(refineProfilesFolder, fSites);
			}
			else{
				predicateProfileReader = new PredicateProfileReader(fgProfilesFolder, fSites);
			}
			PredicateProfile[] fProfiles = predicateProfileReader.readProfiles();
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
			
			//check profiles consistency and compute totalPositive & totalNegative
			//-------------------------------------------------------------------------------------------------------------
			int totalNeg = 0;
			int totalPos = 0;
			
			File[] fgProfiles = predicateProfileReader.getProfileFolder().listFiles(FileUtil.createProfileFilter());
			Arrays.sort(fgProfiles, new FileUtil.FileComparator());
			File[] cgProfiles = functionEntryProfileReader.getProfileFolder().listFiles(FileUtil.createProfileFilter());
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
			
			int totalPositive = 0;
			int totalNegative = 0;
			
			for(PredicateProfile fgProfile: fProfiles){
				if(fgProfile.isCorrect()){
					totalPositive++;
				}
				else{
					totalNegative++;
				}
			}
			assert(totalPositive == totalPos && totalNegative == totalNeg);
			
			//-------------------------------------------------------------------------------------------------------------
			
			//compute matrix of Cp and Cr
			double C_matrix[][] = SelectingProcessor.computeCMatrix(totalNegative, totalPositive);
			
			//simulate for multiple rounds
			CBIClients cs = null;
			IterativeFunctionClient client = null;
			for(int i = 0; i < round; i++){
				System.out.println(i);
				while(true){
					cs = new CBIClients(sInfo, fProfiles, start, 0);
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
						this.ks,
						C_matrix);
				
				solveOneRoundResults(statistics, client.getResults(), i);
			}
			
			System.gc();
			System.out.println();
		}
		
		//print out the final results
//		printReadableResultsByMode();
		printResultsByModeToExcel();
//		printReadableResultsByFlag();
//		printResultsByFlagToExcel();
//		printOutMethodsListByMode();
	}


	/**compute and print out the results for Sir subject excluding Siemens and space
	 * 
	 */
	public void runSir(){
		File[] versions = new File(rootDir, subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("v[0-9]*", name) 
//						&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion
						;
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
				FunctionEntryProfileReader functionEntryProfileReader = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), cSites);
				FunctionEntryProfile[] cProfiles = functionEntryProfileReader.readFunctionEntryProfiles();
				
				File fgSitesFile = new File(subversion, vi + "_f.sites");
				InstrumentationSites fSites = new InstrumentationSites(fgSitesFile);
				File fgProfilesFolder = new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained");
				PredicateProfileReader predicateProfileReader = null;
				
				if(needRefine(fSites, cSites.getFunctions())){
					File refineProfilesFolder = new File(fgProfilesFolder.getParentFile(), "refine");
					File refineSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'r'));
					PredicateSplittingSiteProfile refineSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, refineSitesFile, refineProfilesFolder, cSites.getFunctions());
					refineSplit.split();
					
					fSites = new InstrumentationSites(refineSitesFile);
					predicateProfileReader = new PredicateProfileReader(refineProfilesFolder, fSites);
				}
				else{
					predicateProfileReader = new PredicateProfileReader(fgProfilesFolder, fSites);
				}
				PredicateProfile[] fProfiles = predicateProfileReader.readProfiles();
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
				
				//check profiles consistency and compute totalPositive & totalNegative
				//-------------------------------------------------------------------------------------------------------------
				int totalNeg = 0;
				int totalPos = 0;
				
				File[] fgProfiles = predicateProfileReader.getProfileFolder().listFiles(FileUtil.createProfileFilter());
				Arrays.sort(fgProfiles, new FileUtil.FileComparator());
				File[] cgProfiles = functionEntryProfileReader.getProfileFolder().listFiles(FileUtil.createProfileFilter());
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
				
				int totalPositive = 0;
				int totalNegative = 0;
				
				for(PredicateProfile fgProfile: fProfiles){
					if(fgProfile.isCorrect()){
						totalPositive++;
					}
					else{
						totalNegative++;
					}
				}
				assert(totalPositive == totalPos && totalNegative == totalNeg);
				
				//-------------------------------------------------------------------------------------------------------------
				
				//compute matrix of Cp and Cr
				double C_matrix[][] = SelectingProcessor.computeCMatrix(totalNegative, totalPositive);
				
				//simulate for multiple rounds
				CBIClients cs = null;
				IterativeFunctionClient client = null;
				for(int i = 0; i < round; i++){
					System.out.println(i);
//					long time0 = System.currentTimeMillis();
					while(true){
						cs = new CBIClients(sInfo, fProfiles, start, 0);
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
							this.ks,
							C_matrix);
//					long time2 = System.currentTimeMillis();
//					System.out.println("IterativeFunctionClient:\t" + (time2 - time1));
					
					solveOneRoundResults(statistics, client.getResults(), i);
				}
				
				System.gc();
				System.out.println();
			}
		}
		
//		printReadableResultsByMode();
		printResultsByModeToExcel();
//		printReadableResultsByFlag();
//		printResultsByFlagToExcel();
//		printOutMethodsListByMode();
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
					if(!(i == Score.C.ordinal() && j == Order.LESS_FIRST.ordinal())){
						continue;
					}
					statistics[i][j].incertOneStatisticToExcel(row, this.round);
				}
			}
		}
		
		try {
			if(!consoleFolder.exists()){
				consoleFolder.mkdirs();
			}
			// Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File(this.consoleFolder, this.subject + "_m.xlsx"));
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
				if(!(score == Score.C && order == Order.LESS_FIRST)){
					continue;
				}
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

	
	public static void main(String[] args) {
		String[][] argvs = {
				{"300", "bash", "1"},
				{"363", "sed", "7"},
				{"213", "gzip", "5"},
				{"809", "grep", "5"},
				{"1248", "space", "38"},
//				{"4130", "printtokens", "7"},
//				{"4115", "printtokens2", "10"},
//				{"5542", "replace", "32"},
//				{"2650", "schedule", "9"},
//				{"2710", "schedule2", "10"},
//				{"1608", "tcas", "41"},
//				{"1052", "totinfo", "23"}
		};
		
		if(args.length != 3){
			System.out.println("The characteristics of subjects are as follows:");
			for(int i = 0; i < argvs.length; i++){
				System.out.println(String.format("%-20s", argvs[i][1]) + argvs[i][0]);
			}
			System.err.println("\nUsage: rootDir subject consoleDir");
			return;
		}
		int[] ks = {1, 3, 5, 10};
		long time0 = System.currentTimeMillis();
		if(args.length == 3){
			CClient c = new CClient(ks, new File(args[0]), args[1], new File(new File(args[2]), args[1]));
			if(args[1].equals("space")){
				c.runSiemens();
			}
			else{
				c.runSir();
			}
		}
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");

	}
	
}
