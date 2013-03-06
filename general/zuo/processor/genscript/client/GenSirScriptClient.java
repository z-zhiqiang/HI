package zuo.processor.genscript.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import zuo.processor.genscript.sir.AbstractGenRunAllScript;
import zuo.processor.genscript.sir.AbstractGenRunScript;
import zuo.processor.genscript.sir.GenRunAllInstrumentedScript;
import zuo.processor.genscript.sir.GenRunAllScript;
import zuo.processor.genscript.sir.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.sir.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.GenRunSubjectScript;
import zuo.processor.genscript.sir.GenRunVersionsScript;
import zuo.processor.splitinputs.SplitInputs;
import zuo.util.file.FileUtility;

public class GenSirScriptClient {
	final static String rootDir = "/home/sunzzq/Research/";
	final static String subject = "grep";
	final static String version = "v1";
	final String subVersion;
	final static String inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
	public final static String inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
	
	final static String ssourceDir = rootDir + subject + "/versions.alt/versions.orig/" + version + "/";
	final static String sexecuteDir = rootDir + subject + "/source/" + version + "/";
	final static String soutputDir = rootDir + subject + "/outputs/" + version + "/" + subject + "/";
	
	final static String vsourceDir = rootDir + subject + "/versions.alt/versions.seeded/" + version + "/";
	final String vexecuteDir;
	final String voutputDir;
	final String vfoutputDir;
	final String vcoutputDir;
	final String vftraceDir;
	final String vctraceDir;
	
	final static String scriptDir = rootDir + subject + "/scripts/";
	
	final static String compileSubject = "gcc " 
			+ ssourceDir + subject + ".c" 
			+ " -o " + sexecuteDir + version + ".exe" 
			+ " -I" + ssourceDir
			+ " -lm";
	final String compileVersion;
	final String compileFGInstrument;
	final String compileCGInstrument;
	
	final static Map<Integer, String> faults = new HashMap<Integer, String>();
	
	public GenSirScriptClient(String subVer){
		subVersion = subVer;
		
		vexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/";
		voutputDir = rootDir + subject + "/outputs/" + version + "/versions/" + subVersion + "/outputs/";
		vfoutputDir = rootDir + subject + "/outputs/" + version + "/versions/" + subVersion + "/fine-grained/";
		vcoutputDir = rootDir + subject + "/outputs/" + version + "/versions/" + subVersion + "/coarse-grained/";
		vftraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained/";
		vctraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/coarse-grained/";
		
		
		
		compileVersion = "gcc " 
				+ vsourceDir + subject + ".c"
				+ " $COMPILE_PARAMETERS"
				+ " -o " + vexecuteDir + subVersion + ".exe"
				+ " -I" + vsourceDir
				+ " -lm";
		compileFGInstrument = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=float-kinds -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
				+ vsourceDir + subject + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ " -o " + vexecuteDir + subVersion + "_finst.exe"
				+ " -I" + vsourceDir
				+ " -lm";
		compileCGInstrument = "sampler-cc -fsampler-scheme=function-entries -fno-sample "
				+ vsourceDir + subject + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ " -o " + vexecuteDir + subVersion + "_cinst.exe"
				+ " -I" + vsourceDir
				+ " -lm";
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		AbstractGenRunScript gs;
		GenSirScriptClient gc;
		AbstractGenRunAllScript ga;
		
		//read inputsMap
//		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
		
		//read faults (subversion)
		readFaults();
		System.out.println(faults.toString());
		
		//generate run subject and subversion scripts
//		gs = new GenRunSubjectScript(subject, version, compileSubject, ssourceDir, sexecuteDir, soutputDir, scriptDir);
//		gs.genRunScript();
//		for(int index: faults.keySet()){
//			gc = new GenSirScriptClient("subv" + index);
//			
//			String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
//			System.out.println("generating run script for subVersion" + index);
//			new GenRunVersionsScript(subject, version, gc.subVersion, export + gc.compileVersion, gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
//		}
//		
//		//generate run all scripts  
//		ga = new GenRunAllScript(version, subject, FileUtility.readInputsMap(inputsMapFile).size(), scriptDir, faults.size());
//		ga.genRunAllScript();
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			gc = new GenSirScriptClient("subv" + index);
			
			System.out.println("sliptting inputs for subv" + index);
			SplitInputs split = new SplitInputs(inputsMapFile, soutputDir, gc.voutputDir, gc.vexecuteDir);
			split.split();
			
			//collect the triggered faults
			if(split.getFailingTests().size() != 0){
				subs.add(index);
			}
			
			String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
			System.out.println("generating run instrument script for subv" + index);
			gs = new GenRunFineGrainedInstrumentScript(subject, version, gc.subVersion, export + gc.compileFGInstrument, gc.vsourceDir, gc.vexecuteDir, gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
			gs.genRunScript();
			gs = new GenRunCoarseGrainedInstrumentScript(subject, version, gc.subVersion, export + gc.compileCGInstrument, gc.vsourceDir, gc.vexecuteDir, gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 100000);
			gs.genRunScript();
		}
		
		//generate run all instrumented triggered subversion scripts
		ga = new GenRunAllInstrumentedScript(version, subject, FileUtility.readInputsMap(inputsMapFile).size(), scriptDir, subs);
		ga.genRunAllScript();
	}
	
	
	
	
	private static void readFaults(){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(vsourceDir, "FaultSeeds.h")));
			String line;
			int index = 0;
			while((line = reader.readLine()) != null){
				assert(line.split(" ").length == 4 && line.split(" ")[2].startsWith("F"));
				faults.put(++index, line.split(" ")[2]);
			}
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
