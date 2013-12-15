package zuo.util.readfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import zuo.processor.functionentry.client.twopass.Client;
import zuo.util.file.FileCollection;
import zuo.util.file.FileUtil;

public class TwopassTimeReader extends AbstractTimeReader {
	
	final static String[] timeFolders = {"outputs", "coarse-grained", "fine-grained", "coarse-fine-grained", 
		"boost", "prune-minus-boost", "prune"};

	public TwopassTimeReader(File rootD, String sub) {
		super(rootD, sub, timeFolders);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void readTimeSir(String verName, File versionFolder, File subVersionFolder) {
		// TODO Auto-generated method stub
		List<Integer> results = new ArrayList<Integer>();
		
		System.out.print(verName + "\t");
		
		File fgProfilesFolder = new File(new File(new File(this.rootDir, subject), "traces"), versionFolder.getName() + "/" + subVersionFolder.getName() + "/fine-grained/");
		if (!fgProfilesFolder.exists()) {
			throw new RuntimeException("Fine-grained profiles folder " + fgProfilesFolder + " does not exist.");
		}
		File[] fgProfiles = fgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		results.add(fgProfiles.length);
		System.out.print(fgProfiles.length + "\t");
		
		Set<Integer> indices = FileCollection.readIndices(new File(new File(this.rootDir, subject), "/versions/" + versionFolder.getName() + "/" + subVersionFolder.getName() + "/predicate-dataset/cg/indices.txt"));
		results.add(indices.size());
		System.out.print(indices.size() + "\t");
		
		File cgProfilesFolder = new File(new File(new File(this.rootDir, subject), "traces"), versionFolder.getName() + "/" + subVersionFolder.getName() + "/coarse-grained/");
		if (!cgProfilesFolder.exists()) {
			throw new RuntimeException("Coarse-grained profiles folder " + cgProfilesFolder + " does not exist.");
		}
		File[] cgProfiles = cgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		assert(indices.size() == cgProfiles.length);
		
		for(int i = 0; i < timeFolders.length; i++){
			int time = readTimeFile(new File(new File(subVersionFolder, timeFolders[i]), "time"));
			results.add(time);
			System.out.print(time + "\t");
		}
		System.out.println();
		
		this.timeResultsMap.put(verName, results);
		
	}
	
	@Override
	protected void readTimeSiemens(String verName, File versionFolder) {
		// TODO Auto-generated method stub
		List<Integer> results = new ArrayList<Integer>();
		
		System.out.print(verName + "\t");
		
		File fgProfilesFolder = new File(new File(new File(this.rootDir, subject), "traces"), versionFolder.getName() + "/fine-grained/");
		if (!fgProfilesFolder.exists()) {
			throw new RuntimeException("Fine-grained profiles folder " + fgProfilesFolder + " does not exist.");
		}
		File[] fgProfiles = fgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		results.add(fgProfiles.length);
		System.out.print(fgProfiles.length + "\t");
		
		Set<Integer> indices = FileCollection.readIndices(new File(new File(this.rootDir, subject), "/versions/" + versionFolder.getName() + "/predicate-dataset/cg/indices.txt"));
		results.add(indices.size());
		System.out.print(indices.size() + "\t");
		
		File cgProfilesFolder = new File(new File(new File(this.rootDir, subject), "traces"), versionFolder.getName() + "/coarse-grained/");
		if (!cgProfilesFolder.exists()) {
			throw new RuntimeException("Coarse-grained profiles folder " + cgProfilesFolder + " does not exist.");
		}
		File[] cgProfiles = cgProfilesFolder.listFiles(FileUtil.createProfileFilter());
		assert(indices.size() == cgProfiles.length);
		
		for(int i = 0; i < timeFolders.length; i++){
			int time = readTimeFile(new File(new File(versionFolder, timeFolders[i]), "time"));
			results.add(time);
			System.out.print(time + "\t");
		}
		System.out.println();
		
		this.timeResultsMap.put(verName, results);
	}

	@Override
	protected int addTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		
		String[] titles = {"tests", "partial_tests", "outputs", "cg", "fg", "cfg", "boost", "prune_minus_boost", "prune"};
		
		Cell cell0 = row0.createCell(cellnum0++);
		cell0.setCellValue(" ");
		
		for(int i = 0; i < titles.length; i++){
			cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(titles[i]);
		}
		return cellnum0;
	}
	
	
	public static void main(String[] args) {
		String[][] argvs = {
				{"363", "sed", "7"},
				{"213", "gzip", "5"},
				{"809", "grep", "5"},
				{"13585", "space", "38"},
//				{"4130", "printtokens", "7"},
//				{"4115", "printtokens2", "10"},
				{"5542", "replace", "32"},
//				{"2650", "schedule", "9"},
//				{"2710", "schedule2", "10"},
//				{"1608", "tcas", "41"},
//				{"1052", "totinfo", "23"}
		};
		for(int i = 0; i < argvs.length; i++){
			File rootDir, consoleFolder;
			String mode;
			if(i <= 3){
				rootDir = Client.sirRootDir;
				consoleFolder = Client.sirConsoleFolder;
				mode = "2_0.05";
			}
			else{
				rootDir = Client.siemensRootDir;
				consoleFolder = Client.siemensConsoleFolder;
				mode = "2_0.1";
			}
			AbstractTimeReader timeReader = new TwopassTimeReader(rootDir, argvs[i][1]);
			timeReader.readAndExportTimeResults(consoleFolder, argvs[i][1] + "_" + mode + "_overhead.xlsx");
			
		}
	}

	

}
