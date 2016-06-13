package zuo.util.readfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Order;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;
import zuo.util.file.FileCollection;

public class IterativeTimeReader extends AbstractTimeReader {
	
	final static String[] timeFolders = {"outputs", "fine-grained-sampled-1", "fine-grained-sampled-100", "fine-grained-sampled-10000", 
		"coarse-grained", "fine-grained-adaptive"};

	public IterativeTimeReader(File rootD, String sub) {
		super(rootD, sub, timeFolders);
	}

	@Override
	protected void readTimeSir(String verName, File versionFolder, File subVersionFolder) {
		// TODO Auto-generated method stub
		List<Object> results = new ArrayList<Object>();
		
		System.out.print(verName + "\t");
		for(int i = 0; i < timeFolders.length; i++){
			int time = readTimeFile(new File(new File(subVersionFolder, timeFolders[i]), "time"));
			results.add(time);
			System.out.print(time + "\t");
		}
//		System.out.print("\t");
//		for(int i = 0; i < Score.values().length; i++){
//			for(int j = 0; j < Order.values().length; j++){
//				String mode = verName + "_" + Score.values()[i] + "_" + Order.values()[j];
//				int time = 0;
////				time = readModeAverageTime(new File(subVersionFolder, "fine-grained-adaptive"), new File(new File(versionFolder.getParentFile(), "FunctionList"), mode + "_local_average"));
//				results.add(time);
//				System.out.print(time + "\t");
//			}
//		}
//		System.out.print("\t");
//		for(int i = 0; i < Score.values().length; i++){
//			for(int j = 0; j < Order.values().length; j++){
//				String mode = verName + "_" + Score.values()[i] + "_" + Order.values()[j];
//				int time = 0;
////				time = readModeAverageTime(new File(subVersionFolder, "fine-grained-adaptive"), new File(new File(versionFolder.getParentFile(), "FunctionList"), mode + "_1_average"));
//				results.add(time);
//				System.out.print(time + "\t");
//			}
//		}
		System.out.println();
		
		this.timeResultsMap.put(verName, results);
	}
	
	@Override
	protected void readTimeSiemens(String verName, File versionFolder) {
		// TODO Auto-generated method stub
		List<Object> results = new ArrayList<Object>();
		
		System.out.print(verName + "\t");
		for(int i = 0; i < timeFolders.length; i++){
			int time = readTimeFile(new File(new File(versionFolder, timeFolders[i]), "time"));
			results.add(time);
			System.out.print(time + "\t");
		}
//		System.out.print("\t");
//		for(int i = 0; i < Score.values().length; i++){
//			for(int j = 0; j < Order.values().length; j++){
//				String mode = verName + "_" + Score.values()[i] + "_" + Order.values()[j];
//				int time = readModeAverageTime(new File(versionFolder, "fine-grained-adaptive"), new File(new File(versionFolder.getParentFile(), "FunctionList"), mode + "_local_average"));
//				results.add(time);
//				System.out.print(time + "\t");
//			}
//		}
//		System.out.print("\t");
//		for(int i = 0; i < Score.values().length; i++){
//			for(int j = 0; j < Order.values().length; j++){
//				String mode = verName + "_" + Score.values()[i] + "_" + Order.values()[j];
//				int time = readModeAverageTime(new File(versionFolder, "fine-grained-adaptive"), new File(new File(versionFolder.getParentFile(), "FunctionList"), mode + "_1_average"));
//				results.add(time);
//				System.out.print(time + "\t");
//			}
//		}
		System.out.println();
		
		this.timeResultsMap.put(verName, results);
	}
	
	private int readModeAverageTime(File timeFolder, File functionListFile) {
		// TODO Auto-generated method stub
		int time = 0;
		Set<String> functions = FileCollection.readFunctions(functionListFile);
		if(functions.isEmpty()){
			return 0;
		}
		for(String function: functions){
			time += readTimeFile(new File(new File(timeFolder, function), "time"));
		}
		return time / functions.size();
	}

	@Override
	protected int addTitle(XSSFSheet sheet) {
		// TODO Auto-generated method stub
		int rownum = sheet.getPhysicalNumberOfRows();
		
		Row row0 = sheet.createRow(rownum++);
		int cellnum0 = 0;
		Row row1 = sheet.createRow(rownum++);
		int cellnum1 = 0;
		Row row2 = sheet.createRow(rownum++);
		int cellnum2 = 0;
		
		String[] titles = {"outputs", "s1", "s100", "s10000", "cg", "iterative"};
		String[] iterativetitles = {"local", "top_1"};
		
		Cell cell0 = row0.createCell(cellnum0++);
		cell0.setCellValue(" ");
		Cell cell1 = row1.createCell(cellnum1++);
		cell1.setCellValue(" ");
		Cell cell2 = row2.createCell(cellnum2++);
		cell2.setCellValue(" ");
		
		for(int i = 0; i < titles.length; i++){
			cell0 = row0.createCell(cellnum0++);
			cell0.setCellValue(" ");
			cell1 = row1.createCell(cellnum1++);
			cell1.setCellValue(" ");
			cell2 = row2.createCell(cellnum2++);
			cell2.setCellValue(titles[i]);
		}
		
//		for(int m = 0; m < iterativetitles.length; m++){
//			for(int i = 0; i < Score.values().length; i++){
//				for(int j = 0; j < Order.values().length; j++){
//					cell0 = row0.createCell(cellnum0++);
//					cell0.setCellValue(iterativetitles[m]);
//					cell1 = row1.createCell(cellnum1++);
//					cell1.setCellValue(Score.values()[i].toString());
//					cell2 = row2.createCell(cellnum2++);
//					cell2.setCellValue(Order.values()[j].toString());
//				}
//			}
//		}
		
		return cellnum0;
	}
	
	public static void main(String[] args) {
		if(args.length != 3){
			System.err.println("Usage: subjectDir subject consoleDir");
			return;
		}
		AbstractTimeReader timeReader = new IterativeTimeReader(new File(args[0]), args[1]);
		timeReader.readAndExportTimeResults(new File(args[2]), args[1] + "_overhead.xlsx");
		
	}

	

}
