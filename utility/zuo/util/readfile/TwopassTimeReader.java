package zuo.util.readfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
		List<Object> results = new ArrayList<Object>();
		
		System.out.print(verName + "\t");
		
		File mbsOutFile = new File(new File(new File(this.rootDir, subject), "versions"), versionFolder.getName() + "/" + subVersionFolder.getName() + "/predicate-dataset/original/mbs.out");
        double ds = readDS(mbsOutFile);	
        results.add(ds);
		System.out.print(ds + "\t");
		
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
		
//		File cgProfilesFolder = new File(new File(new File(this.rootDir, subject), "traces"), versionFolder.getName() + "/" + subVersionFolder.getName() + "/coarse-grained/");
//		if (!cgProfilesFolder.exists()) {
//			throw new RuntimeException("Coarse-grained profiles folder " + cgProfilesFolder + " does not exist.");
//		}
//		File[] cgProfiles = cgProfilesFolder.listFiles(FileUtil.createProfileFilter());
//		assert(indices.size() == cgProfiles.length);
		
		for(int i = 0; i < timeFolders.length; i++){
			int time = readTimeFile(new File(new File(subVersionFolder, timeFolders[i]), "time"));
			results.add(time);
			System.out.print(time + "\t");
		}
		System.out.println();
		
		this.timeResultsMap.put(verName, results);
		
	}
	
	private double readDS(File mbsOutFile) {
		// TODO Auto-generated method stub
		BufferedReader reader = null;
		double threshold = -1;
		try {
			reader = new BufferedReader(new FileReader(mbsOutFile));
			String line = reader.readLine();
			assert(line.matches("TOP-.*(" + Client.k + ").*SUP=.*Metric=.*"));
			threshold = Double.parseDouble(line.substring(line.lastIndexOf("=") + 1));
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return threshold;
	}

	@Override
	protected void readTimeSiemens(String verName, File versionFolder) {
		// TODO Auto-generated method stub
		List<Object> results = new ArrayList<Object>();
		
		System.out.print(verName + "\t");
		
		File mbsOutFile = new File(new File(new File(this.rootDir, subject), "versions"), versionFolder.getName() + "/predicate-dataset/original/mbs.out");
        double ds = readDS(mbsOutFile);	
        results.add(ds);
		System.out.print(ds + "\t");
		
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
		
//		File cgProfilesFolder = new File(new File(new File(this.rootDir, subject), "traces"), versionFolder.getName() + "/coarse-grained/");
//		if (!cgProfilesFolder.exists()) {
//			throw new RuntimeException("Coarse-grained profiles folder " + cgProfilesFolder + " does not exist.");
//		}
//		File[] cgProfiles = cgProfilesFolder.listFiles(FileUtil.createProfileFilter());
//		assert(indices.size() == cgProfiles.length);
		
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
		
		String[] titles = {"DS", "tests", "partial_tests", "outputs", "cg", "fg", "cfg", "boost", "prune_minus_boost", "prune"};
		
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
		File rootDir = new File(Client.root + args[0]);
		for(int i = 0; i < argvs.length; i++){
			File subjectsFolder, consoleFolder;
			String mode;
			if(i <= 4){
				subjectsFolder = new File(rootDir, "Subjects");
				consoleFolder = new File(rootDir, "Console");
				mode = "2_0.05";
			}
			else{
				subjectsFolder = new File(rootDir, "Subjects/Siemens");
				consoleFolder = new File(rootDir, "Console/Siemens");
				mode = "2_0.05";
			}
			AbstractTimeReader timeReader = new TwopassTimeReader(subjectsFolder, argvs[i][1]);
			timeReader.readAndExportTimeResults(consoleFolder, argvs[i][1] + "_" + mode + "_overhead.xlsx");
			
		}
	}

	

}
