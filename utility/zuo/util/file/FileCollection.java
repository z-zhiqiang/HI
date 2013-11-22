package zuo.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
	
	public static Set<String> readSet(File file) {
		// TODO Auto-generated method stub
		Set<String> collections = new LinkedHashSet<String>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				collections.add(line.trim());
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

}
