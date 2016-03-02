package zuo.processor.genscript.client.iterative.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import java.util.Set;

import sir.mts.MakeTestScript;
import zuo.processor.genscript.sir.iterative.java.AbstractGenRunScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.util.file.FileUtility;

/**
 * @author icuzzq
 *	
 *
 */
public class SimulateStatistics extends AbstractGenSirScriptClient{
	public final static int Round = 0;
	
	
	public final String subject;
	public final String version;
	public final String subVersion;
	public final String inputScript;
	
	public final String inputsMapFile;
	final String vexecuteDir;
	
	String compileSubject;
	String compileVersion;
	String compileFGInstrument;
	String compileCGInstrument;
	
	public SimulateStatistics(String sub, String ver, String subVer){
		subject = sub;
		version = ver;
		subVersion = subVer;
		
		inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		
		vexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/";

	}


	public static void main(String[] args) throws IOException {
		String[][] subjects = {
				{"apache-ant", "8", "8"},
				{"nanoxml", "1", "4"},
				{"siena", "1", "5"},
				{"derby", "5", "5"}
		};
		for (int i = 0; i < subjects.length; i++) {
			System.out.println(subjects[i][0]);
			for(int j = Integer.parseInt(subjects[i][1]); j <= Integer.parseInt(subjects[i][2]); j++){
				SimulateStatistics gc = new SimulateStatistics(subjects[i][0], "v" + j, null);
				gc.gen();
			}
			System.out.println();
		}
		
	}

	private void gen() throws IOException {
		File[] subversions = new File(rootDir + subject + "/versions/", version).listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 9
						&& !(subject.equals("siena") && version.equals("v5") && name.equals("subv2")));
			}});
		Arrays.sort(subversions, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				return new Integer(Integer.parseInt(o1.getName().substring(4))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(4))));
			}});
		
		for(File subversion: subversions){
			SimulateStatistics gc = new SimulateStatistics(subject, version, subversion.getName());
			int index = Integer.parseInt(gc.subVersion.substring(4));
			if(gc.subject.equals("derby") && index != 8 && index != 30 && index != 61){
				continue;
			}
			
			////read the number of tests needed by sampling
			String file_sample = rootDir + gc.subject + "/SimuInfo_sample/" + gc.version + "_" + gc.subVersion + "/R" + Round + "T1";
//			System.out.println(file_sample);
			System.out.print(gc.version + "_" + gc.subVersion + ",\t");
			
			BufferedReader reader_sample = null;
			try {
				reader_sample = new BufferedReader(new FileReader(new File(file_sample)));
				String line;
				while((line = reader_sample.readLine()) != null){
					String[] comps = line.split("<<<");
					assert(comps.length == 3);
					String fileName = comps[0];
					
					List<Integer> passingTests = readTests(comps[1]);
					List<Integer> failingTests = readTests(comps[2]);
					
					if(fileName.equals("Full_Sample_100")){
						System.out.print(passingTests.size() + ",\t" + failingTests.size() + ",\t" + (passingTests.size() + failingTests.size()) + ",\t\t");
					}
				}
				reader_sample.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				if(reader_sample != null){
					try {
						reader_sample.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			//read the number of tests needed by our approach
			String file;
			if(gc.subject.equals("derby")){
				file = rootDir + gc.subject + "/SimuInfo/" + gc.version + "_" + gc.subVersion + "/R" + 1 + "T1";
			}
			else{
				file = rootDir + gc.subject + "/SimuInfo/" + gc.version + "_" + gc.subVersion + "/R" + Round + "T1";
			}
//			System.out.println(file);
			
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File(file)));
				String line;
				int id = 0;
				int sum_p = 0, sum_f = 0;
				while((line = reader.readLine()) != null){
					String[] comps = line.split("<<<");
					assert(comps.length == 3);
					String fileName = comps[0];
					
					List<Integer> passingTests = readTests(comps[1]);
					List<Integer> failingTests = readTests(comps[2]);
					
					if(fileName.equals("Full")){
						System.out.print(passingTests.size() + ",\t" + failingTests.size() + ",\t" + (passingTests.size() + failingTests.size()) + ",\t\t");
					}
					else{
						String[] sigs = fileName.split("->");
						assert(sigs.length == 2);
//						System.out.println(++id + "," + passingTests.size() + "," + failingTests.size());
						sum_p += passingTests.size();
						sum_f += failingTests.size();
					}
				}
				System.out.println(sum_p + ",\t" + sum_f + ",\t" + (sum_p + sum_f));
				
				reader.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				if(reader != null){
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	

	public static List<Integer> readTests(String string) {
		// TODO Auto-generated method stub
		List<Integer> list = new ArrayList<Integer>();
		string = string.substring(1, string.length() -1);
//		System.out.println(string);
		String[] ints = string.split(",");
		for(String in: ints){
			list.add(Integer.parseInt(in.trim()));
		}
		return list;
	}

}
