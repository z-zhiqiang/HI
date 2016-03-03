package zuo.util.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;


public class TraceCompressor {
	public static void main(String[] args) {
		File root = new File("/home/icuzzq/Research/Automated_Debugging/Console/Compress");
		File[] dirs = root.listFiles();
//		for(File dir: dirs){
//			String subject = dir.getName();
//			System.out.println(subject);
//			for(File ddir: dir.listFiles()){
//				String approach = ddir.getName();
//				System.out.println(approach);
//				for(File dddir: ddir.listFiles()){
//					String version = dddir.getName();
//					System.out.println(version);
//					for(File ddddir: dddir.listFiles()){
//						String worker = ddddir.getName();
//						System.out.println(worker);
//						for(File file: ddddir.listFiles()){
//							compressPredicateProfile(file, new File(root, subject + "_comp/" + approach + "/" + version + "/" + worker + "/" + file.getName()));
//						}
//					}
//				}
//			}
//		}
		
		for(File dir: dirs){
			String subject = dir.getName();
			System.out.print(subject + "\t");
			for(File ddir: dir.listFiles()){
				String approach = ddir.getName();
//				System.out.println(approach);
//				ddir.gets
				System.out.print(FileUtils.sizeOfDirectory(ddir) + "\t");
			}
			System.out.println();
		}
//		File input = new File("/home/icuzzq/Research/Automated_Debugging/Console/Compress/siena/ARBI/siena_v1_subv2_cg/worker_1/o29.pprofile");
//		compressPredicateProfile(input, new File(""));
	}
	
	public static void compressPredicateProfile(File profilePath, File outFile) {
		StringBuilder builder = new StringBuilder();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(profilePath));
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				if (line.contains("<report id=\"samples\">")) {
					// read the report.
					builder.append(line).append("\n");
					readReport(reader, builder);
					builder.append("</report>").append("\n");
//					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
//		System.out.println(builder.toString());
		PrintWriter out = null;
		try{
			if(!outFile.getParentFile().exists()){
				outFile.getParentFile().mkdirs();
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
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

	private static void readReport(BufferedReader reader, StringBuilder builder) throws IOException {
		for (String line = reader.readLine(); line != null && !line.equals("</report>"); line = reader.readLine()) {
			if (line.startsWith("<samples")) {
				builder.append(line).append("\n");
				if (line.contains("scheme=\"returns\"")) {
					readScalarPairs(reader, 3, builder);
				} 
				else if (line.contains("scheme=\"branches\"")) {
					readScalarPairs(reader, 2, builder);
				} 
				else if (line.contains("scheme=\"scalar-pairs\"")) {
					readScalarPairs(reader, 3, builder);
				} 
				else if (line.contains("scheme=\"method-entries\"")) {
					readScalarPairs(reader, 1, builder);
				} 
				else {
					throw new RuntimeException();
				}
			}
		}
	}
	
	
	private static void readScalarPairs(BufferedReader reader, int num, StringBuilder builder) {
		try {
			int index = 0;
			for (String line = reader.readLine(); line != null; line = reader.readLine(), index++) {
				if (line.contains("</samples>")){
					builder.append(line).append("\n");
					return;
				}
				line = line.trim();
				if (line.length() == 0)
					continue;
				String[] counters = line.split("\\s+");
				if (counters.length != num) {
					throw new RuntimeException(line);
				}
				for(int i = 0; i < counters.length; i++){
					int c = Integer.parseInt(counters[i]);
					if(c != 0){
						builder.append(index).append("\t").append(line).append("\n");
						break;
					}
				}
				
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	
}
