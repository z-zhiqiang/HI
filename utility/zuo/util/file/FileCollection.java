package zuo.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import zuo.processor.functionentry.site.FunctionEntrySite;

public class FileCollection {
	
	public static <T> void writeCollection(Collection<T> collection, File file){
		PrintWriter out = null;
		try{
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			//write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			for(T element: collection){
				out.println(element);
			}
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}
	
	public static List<String> readMethods(File file) {
		// TODO Auto-generated method stub
		List<String> methods = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				String uniqueFunctionName = line.trim();
				String functionName = uniqueFunctionName.split(FunctionEntrySite.DELIMITER, 2)[0];
				methods.add(functionName);
			}
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
		
		return methods;
	}
	
	public static Set<String> readFunctions(File file) {
		// TODO Auto-generated method stub
		Set<String> collections = new LinkedHashSet<String>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				String uniqueFunctionName = line.trim();
				String functionName = uniqueFunctionName.split(FunctionEntrySite.DELIMITER, 2)[0];
				collections.add(functionName);
			}
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
		return collections;
	}
	
	public static Set<Integer> readIndices(File file){
		Set<Integer> indices = new LinkedHashSet<Integer>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				indices.add(Integer.parseInt(line.trim()));
			}
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
		return indices;
	}

}
