package zuo.util.readfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public abstract class AbstractTimeReader {
	protected final File rootDir;
	protected final String subject;
	protected final Map<String, List<Object>> timeResultsMap;
	
	protected final String[] timeFolders; 
	
	public AbstractTimeReader(File rootD, String sub, String[] timeFolders){
		this.rootDir = rootD;
		this.subject = sub;
		this.timeFolders = timeFolders;
		this.timeResultsMap = new LinkedHashMap<String, List<Object>>();
	}
	
	public void readAndExportTimeResults(File consoleFolder, String excelFileName){
		if(this.subject.equals("sed") || this.subject.equals("grep") || this.subject.equals("gzip") || this.subject.equals("bash")){
			readSir();
		}
		else{
			readSiemens();
		}
		exportResultToExcel(consoleFolder, excelFileName);
	}
	
	private void readSir() {
		// TODO Auto-generated method stub
		File[] versions = new File(new File(rootDir, subject), "outputs.alt").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 2);
			}});
		Arrays.sort(versions, new Comparator<File>(){
			@Override
			public int compare(File arg0, File arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(arg0.getName().substring(1))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(1))));
			}});
		
		System.out.println("\n" + subject);
		for(File version: versions){
			File[] subversions = new File(version, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length == timeFolders.length);
				}});
			Arrays.sort(subversions, new Comparator<File>(){
				@Override
				public int compare(File arg0, File arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(arg0.getName().substring(4))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(4))));
				}});
			
			for(File subversion: subversions){
				readTimeSir(version.getName() + "_" + subversion.getName(), version, subversion);
			}
		}

	}

	private void readSiemens() {
		// TODO Auto-generated method stub
		File[] versions = new File(new File(rootDir, subject), "outputs/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == timeFolders.length);
			}});
		Arrays.sort(versions, new Comparator<File>(){
			@Override
			public int compare(File arg0, File arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(arg0.getName().substring(1))).compareTo(new Integer(Integer.parseInt(arg1.getName().substring(1))));
			}});
		
		System.out.println("\n" + subject);
		for(File version: versions){
			readTimeSiemens(version.getName(), version);
		}

	}
	
	protected abstract void readTimeSir(String verName, File versionFolder, File subVersionFolder);
	protected abstract void readTimeSiemens(String verName, File versionFolder);
	
	protected static int readTimeFile(File file){
		int time = 0;
		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				String[] ss = line.split(" ");
				time = Integer.parseInt(ss[ss.length - 1].trim());
			}
			reader.close();
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return time;
	}
	
	private void exportResultToExcel(File consoleFolder, String excelFileName){
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("TimeOverhead");
		int columns = addTitle(sheet);
//		System.out.println(columns);
		assert(columns == 1 + this.timeResultsMap.entrySet().iterator().next().getValue().size());
		
		int rownum = sheet.getPhysicalNumberOfRows();
		for(String version: this.timeResultsMap.keySet()){
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			
			Cell cell = row.createCell(cellnum++);
			cell.setCellValue(version);
			
			for(Object object: this.timeResultsMap.get(version)){
				cell = row.createCell(cellnum++);
//				cell.setCellValue(time);
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
			FileOutputStream out = new FileOutputStream(new File(consoleFolder, excelFileName));
			workbook.write(out);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	protected abstract int addTitle(XSSFSheet sheet);

}
