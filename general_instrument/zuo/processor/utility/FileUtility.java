package zuo.processor.utility;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileUtility {
	
	public static boolean contentEqual(File f1, File f2) {
		if (!f1.getName().equals(f2.getName())) {
			throw new RuntimeException("file name should be the same.");
		}
		return testEquality(f1, f2);
	}

	private static boolean testEquality(File first, File second) {
		BufferedInputStream stream1 = null;
		BufferedInputStream stream2 = null;
		try {
			stream1 = new BufferedInputStream(new FileInputStream(first));
			stream2 = new BufferedInputStream(new FileInputStream(second));
			byte[] buffer1 = new byte[1024];
			byte[] buffer2 = new byte[1024];
			int eof1 = -5;
			int eof2 = -5;
			while (true) {
				eof1 = stream1.read(buffer1);
				eof2 = stream2.read(buffer2);
				if (!Arrays.equals(buffer1, buffer2)) {
					return false;
				}
				if (eof1 == -1 || eof2 == -1) {
					break;
				}
			}
			if (eof1 != eof2) {
				return false;
			} else {
				return Arrays.equals(buffer1, buffer2);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (stream1 != null) {
				try {
					stream1.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (stream2 != null) {
				try {
					stream2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void constructInputsMapFile(String inputs, String mapFile) {
		Map<Integer, String> inputsmap = new LinkedHashMap<Integer, String>();

		
		
	}
	
//	public static Map<Integer, String> constructTcasInputsMapFile(String inputs, String mapFile){
//		Map<Integer, String> inputsmap = new LinkedHashMap<Integer, String>();
//    	int count = 0;
//		BufferedReader in = null;
//		try{
//			String line;
//			in = new BufferedReader(new FileReader(new File(inputs)));
//			while ((line = in.readLine()) != null) {
////				if(line.split(" ").length >= 12){
////				    inputsmap.put(count++, line);
////				}
//				inputsmap.put(++count, line);
//			}
//		}
//		catch(IOException e){
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				in.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		ObjectOutputStream out = null;
//    	try{
//    		out = new ObjectOutputStream(new FileOutputStream(mapFile));
//    		out.writeObject(inputsmap);
//    	}
//    	catch(Exception e){
//    		e.printStackTrace();
//    	}
//    	finally{
//    		try {
//				out.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
//    	
//		return Collections.unmodifiableMap(inputsmap);
//	}
	
//	public static Map<Integer, String> constructGrepInputsMapFile(String inputs, String mapFile){
//		Map<Integer, String> inputsmap = new LinkedHashMap<Integer, String>();
//    	int count = 0;
//		BufferedReader in = null;
//		try{
//			String line;
//			in = new BufferedReader(new FileReader(new File(inputs)));
//			while ((line = in.readLine()) != null) {
//				if(line.contains("../")){
//					line = line.replaceAll("\\.\\.", "../..");
//				}
////				if(line.contains("\\\"")){
////					System.out.println(line);
////					line = line.replaceAll("\\\\\"", "\\\\\"");
////				}
//				inputsmap.put(count++, line.substring(line.indexOf("[") + 1, line.length() - 1));
//			}
//		}
//		catch(IOException e){
//			e.printStackTrace();
//		}
//		finally{
//			try {
//				in.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//		ObjectOutputStream out = null;
//    	try{
//    		out = new ObjectOutputStream(new FileOutputStream(mapFile));
//    		out.writeObject(inputsmap);
//    	}
//    	catch(Exception e){
//    		e.printStackTrace();
//    	}
//    	finally{
//    		try {
//				out.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//    	}
//    	
//		return Collections.unmodifiableMap(inputsmap);
//	}

	public static Map<Integer, String> constructSiemensInputsMapFile(String inputs, String mapFile){
		Map<Integer, String> inputsmap = new LinkedHashMap<Integer, String>();
    	int count = 0;
		BufferedReader in = null;
		try{
			String line;
			in = new BufferedReader(new FileReader(new File(inputs)));
			while ((line = in.readLine()) != null) {
//				if(line.contains("../")){
//					line = line.replaceAll("\\.\\.", "../..");
//				}
				inputsmap.put(++count, line);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		ObjectOutputStream out = null;
    	try{
    		out = new ObjectOutputStream(new FileOutputStream(mapFile));
    		out.writeObject(inputsmap);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	finally{
    		try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
		return Collections.unmodifiableMap(inputsmap);
	}
	
	
	public static Map<Integer, String> readInputsMap(String inputsMapFile){
		Map<Integer, String> inputsMap = null;
		ObjectInputStream in = null;
		try {
		    in = new ObjectInputStream(new FileInputStream(inputsMapFile));
			inputsMap = (Map<Integer, String>) in.readObject();
		}
		catch (FileNotFoundException e){
			throw new RuntimeException("Not such file: " + inputsMapFile);
		}
		catch (Exception ee){
			ee.printStackTrace();
		}
		finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableMap(inputsMap);
	}
	
	public static List<Integer> readInputsArray(String inputsArrayFile){
		List<Integer> inputsArray = null;
		ObjectInputStream in = null;
		try {
		    in = new ObjectInputStream(new FileInputStream(inputsArrayFile));
			inputsArray = (List<Integer>) in.readObject();
		}
		catch (FileNotFoundException e){
			throw new RuntimeException("Not such file: " + inputsArrayFile);
		}
		catch (Exception ee){
			ee.printStackTrace();
		}
		finally{
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return Collections.unmodifiableList(inputsArray);
	}
	
	public static String getUnitID(String line){
		String[] segs = line.split("\\s+");
		String unit = segs[1];
		if(!unit.contains("unit=")){
			throw new RuntimeException("wrong unit extraction");
		}
		String unitid = unit.substring(unit.indexOf("\"") + 1, unit.lastIndexOf("\"")); 
	    assert(unitid.length() == 32);
		return unitid;
	}

}
