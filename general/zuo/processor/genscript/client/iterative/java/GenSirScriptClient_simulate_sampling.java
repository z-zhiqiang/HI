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


import sir.mts.MakeTestScript;
import zuo.processor.genscript.sir.iterative.java.AbstractGenRunScript;
import zuo.util.file.FileUtility;

public class GenSirScriptClient_simulate_sampling extends AbstractGenSirScriptClient{
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
	
	public GenSirScriptClient_simulate_sampling(String sub, String ver, String subVer){
		subject = sub;
		version = ver;
		subVersion = subVer;
		
		inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		
		vexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/";

	}


	public static void main(String[] args) throws IOException {
		String[][] subjects = {
//				{"apache-ant", "8", "8"},
				{"nanoxml", "1", "4"},
//				{"siena", "1", "5"}
		};
		for (int i = 0; i < subjects.length; i++) {
			for(int j = Integer.parseInt(subjects[i][1]); j <= Integer.parseInt(subjects[i][2]); j++){
				GenSirScriptClient_simulate_sampling gc = new GenSirScriptClient_simulate_sampling(subjects[i][0], "v" + j, null);
				gc.gen();
			}
		}
		
	}



	private void gen() throws IOException {
		String sf = null;
		String run = null;
		
		if(subject.equals("apache-ant")){
			sf = rootDir + subject + "/testplans.alt/" + version + "/" + version + ".class.junit.universe.all.refine";
			run = "-mx256m -classpath ${CLASSPATH} "
					+ "-Dant.home=bootstrap -Dbuild.tests=build/classes -Dtests-classpath.value=${CLASSPATH} junit.textui.TestRunner";
		}
		else if(subject.equals("nanoxml")){
			sf = rootDir + subject + "/testplans.alt/component/" + version + "/universe.extended.tsl_wrapper";
			run = AbstractGenRunScript.EXE;
		}
		else if(subject.equals("siena")){
			sf = rootDir + subject + "/testplans.alt/universe";
			run = AbstractGenRunScript.EXE;
		}
		
		String[] argvs = {"-sf", sf, "-sn", inputScript, "-en", run, "-ed", rootDir + subject, "-tg", "bsh", "-nesc", "-j"};
		MakeTestScript.main(argvs);
		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
		
		
		//=========================================================================================================================================================================//
		Map<Integer, String> map = FileUtility.readInputsMap(inputsMapFile);
		long sleepTime = sleepTime(map);
		System.out.println(sleepTime);
		List<Map.Entry<Integer, String>> list = new ArrayList<Map.Entry<Integer, String>>(map.entrySet());
//		System.out.println(list);
		Collections.sort(list, new Comparator<Map.Entry<Integer, String>>(){

			@Override
			public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
				// TODO Auto-generated method stub
				return o1.getKey().compareTo(o2.getKey());
			}
			
		});
		
		
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
			GenSirScriptClient_simulate_sampling gc = new GenSirScriptClient_simulate_sampling(subject, version, subversion.getName());
			
			String file = rootDir + gc.subject + "/SimuInfo_sample/" + gc.version + "_" + gc.subVersion + "/R" + Round + "T1";
			System.out.println(file);
			
			String simuScriptsDir = "/SimuScripts" + Round + "_sample/";
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File(file)));
				String line;
				while((line = reader.readLine()) != null){
					String[] comps = line.split("<<<");
					assert(comps.length == 3);
					String fileName = comps[0];
					List<Integer> passingTests = readTests(comps[1]);
					List<Integer> failingTests = readTests(comps[2]);
					
					String builder = generateScripts(list, passingTests, failingTests, gc.subject);
					if(fileName.equals("Full_Sample_100")){
						AbstractGenRunScript.printToFile(builder, rootDir + gc.subject + simuScriptsDir + gc.version + "_" + gc.subVersion, gc.version + "_" + gc.subVersion + "_fg_s100.sh");
					}
				}
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
	
	public static long sleepTime(Map<Integer, String> inputsMap){
		long time = 0;
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			String input = inputsMap.get(index);
//			System.out.println(input);
			String newInput = input;
			String[] lines = input.split("\n");
			for(String line: lines){
				if(line.startsWith("sleep")){
					String[] tokens = line.split(" ");
					int t = Integer.parseInt(tokens[1]);
					int nt = t;
					if(t > 3){
						nt = t / 2;
					}
					newInput = newInput.replaceAll("sleep " + t, "sleep " + nt);
					
//					System.out.println(line);
					time += nt;
//					System.out.println(nt);
				}
			}
			inputsMap.put(index, newInput);
//			System.out.println(newInput);
//			System.out.println();
		}
			
		return time;
	}
	
	public static String generateScripts(List<Entry<Integer, String>> list, List<Integer> passingTests,
			List<Integer> failingTests, String subject) {
		StringBuilder code = new StringBuilder();
		// TODO Auto-generated method stub
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			int in = index % list.size();
			int t = index / list.size();
			int num = list.get(in).getKey() + t * list.size();
			code.append(AbstractGenRunScript.runinfo + num + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + 100 + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + num + ".fprofile\n");
			code.append(refineString(list.get(in).getValue(), subject));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			int in = index % list.size();
			int t = index / list.size();
			int num = list.get(in).getKey() + t * list.size();
			code.append(AbstractGenRunScript.runinfo + num + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + 100 + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + num + ".pprofile\n");
			code.append(refineString(list.get(in).getValue(), subject));
			code.append("\n");
		}
		return code.toString();
	}


	public static String refineString(String in, String subject){
		return in
				.replaceAll(rootDir + subject + "/inputs", "\\$INPUTSDIR")
				.replaceAll("\\.\\./inputs", "\\$INPUTSDIR")
				.replaceAll("> " + rootDir + subject + "/outputs/.*2>&1", "")
				.replaceAll(rootDir + subject + "/testplans\\.alt/testscripts/RemoveTime\\.sh .*\\\n", "")
				;
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
