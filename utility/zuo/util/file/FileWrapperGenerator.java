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
import java.util.List;

public class FileWrapperGenerator {

	
	private static final String WRAPPER = "_Wrapper";


	public static void transformTSL(File tslFile){
		List<String> lines = new ArrayList<String>();
		
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(tslFile));
			while((line = reader.readLine()) != null){
				System.out.println(line);
//				lines.add(line);
				if(line.startsWith("-D")){
					String driver = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
					System.out.println(driver);
					line = line.replaceFirst(driver, driver + WRAPPER);
					System.out.println(line);
				}
				
				System.out.println();
				lines.add(line);
				
				
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
		
		FileCollection.writeCollection(lines, new File(tslFile.getParentFile(), tslFile.getName() + "_wrapper"));
	}
	
	
	
	public static void genWrappers(File dir){
		File[] files = dir.listFiles();
		for(File file: files){
			if(file.getName().endsWith(".java")){
				System.out.println(file.getName());
				generateWrapper(file);
			}
		}
	}
	
	
	private static void generateWrapper(File javaFile){
		String wrapperName = getWrapperClassName(javaFile);
		StringBuilder builder = new StringBuilder();
		builder.append("public class " + wrapperName + " {\n	"
				+ "public static void main(String args[]){\n		"
				+ "try{" + getClassName(javaFile) + ".main(args);}\n		"
				+ "catch(Exception e){e.printStackTrace();System.exit(0);}" + "\n	}\n}");
		
		
		PrintWriter out = null;
		try{
			out = new PrintWriter(new BufferedWriter(new FileWriter(new File(javaFile.getParentFile(), getWrapperFileName(javaFile)))));
			out.println(builder.toString());
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}

	private static String getClassName(File className) {
		// TODO Auto-generated method stub
		return className.getName().split("\\.")[0];
	}

	private static String getWrapperClassName(File name) {
		// TODO Auto-generated method stub
		return getClassName(name) + WRAPPER;
	}
	
	private static String getWrapperFileName(File javaFile){
		return getWrapperClassName(javaFile) + ".java";
	}
	
	
	public static void main(String[] args) {
//		File file = new File("/home/icuzzq/Workspace/program/HI/", "CheckLeaf1_wy_v1.java");
//		System.out.println(getClassName(file));
//		
//		System.out.println(getWrapperClassName(file));
//		
//		generateWrapper(file);
		
		genWrappers(new File("/home/icuzzq/Research/Automated_Debugging/Subjects/nanoxml/testdrivers/v4"));
		
		transformTSL(new File("/home/icuzzq/Research/Automated_Debugging/Subjects/nanoxml/testplans.alt/component/v4/universe.extended.tsl"));
		
	}
	
}
