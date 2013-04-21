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

import sir.mts.MakeTestScript;
import zuo.processor.functionentry.client.Client;
import zuo.processor.genscript.sir.AbstractGenRunAllScript;
import zuo.processor.genscript.sir.AbstractGenRunScript;
import zuo.processor.genscript.sir.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.GenRunAllAdaptiveInstrumentedScript;
import zuo.processor.genscript.sir.GenRunAllInstrumentedScript;
import zuo.processor.genscript.sir.GenRunAllScript;
import zuo.processor.genscript.sir.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.sir.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.GenRunSubjectScript;
import zuo.processor.genscript.sir.GenRunVersionsScript;
import zuo.processor.splitinputs.SirSplitInputs;
import zuo.processor.splitinputs.SplitInputs;
import zuo.util.file.FileUtility;

public class GenSirScriptClient {
	final static String rootDir = "/home/sunzzq/Research/Automated_Debugging/Subjects/";
	final static String subject = "grep";
	public final static String sourceName = "grep";
	final static String version = "v1";
	final String subVersion;
	final static String inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
	final static String inputCompScript = rootDir + subject + "/scripts/" + subject + "Comp.sh";
	
	public final static String inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
	public final static String inputsCompMapFile = rootDir + subject + "/testplans.alt/" + "inputsComp.map";

	
	final static String ssourceDir = rootDir + subject + "/versions.alt/versions.orig/" + version + "/";
	final static String sexecuteDir = rootDir + subject + "/source/" + version + "/";
	final static String soutputDir = rootDir + subject + "/outputs.alt/" + version + "/" + subject + "/";
	
	final static String vsourceDir = rootDir + subject + "/versions.alt/versions.seeded/" + version + "/";
	final String vexecuteDir;
	final String voutputDir;
	final String vfoutputDir;
	final String vcoutputDir;
	final String vftraceDir;
	final String vctraceDir;
	final String vafoutputDir;
	final String vaftraceDir;
	
	final static String scriptDir = rootDir + subject + "/scripts/";
	
	final static String compileSubject = "gcc " 
			+ ssourceDir + sourceName + ".c" 
			+ " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1"
			+ " -o " + sexecuteDir + version + ".exe" 
			+ " -I" + ssourceDir
//			+ " -lm"
			;
	final String compileVersion;
	final String compileFGInstrument;
	final String compileCGInstrument;
	
	public final static String outCompFile = "comp.out";
	
	final static Map<Integer, String> faults = new HashMap<Integer, String>();
	
	
	public GenSirScriptClient(String subVer){
		subVersion = subVer;
		
		vexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/";
		voutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/outputs/";
		vfoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained/";
		vafoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained-adaptive/";
		vcoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/coarse-grained/";
		vftraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained/";
		vaftraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained-adaptive/";
		vctraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/coarse-grained/";
		
		
		compileVersion = "gcc " 
				+ vsourceDir + sourceName + ".c"
				+ " $COMPILE_PARAMETERS"
				+ " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1"
				+ " -o " + vexecuteDir + version + ".exe"
				+ " -I" + vsourceDir
//				+ " -lm"
				;
		compileFGInstrument = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=float-kinds -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
				+ vsourceDir + sourceName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1"
				+ " -o " + vexecuteDir + subVersion + "_finst.exe"
				+ " -I" + vsourceDir
//				+ " -lm"
				;
		compileCGInstrument = "sampler-cc -fsampler-scheme=function-entries -fno-sample "
				+ vsourceDir + sourceName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1"
				+ " -o " + vexecuteDir + subVersion + "_cinst.exe"
				+ " -I" + vsourceDir
//				+ " -lm"
				;
		
	}
	
	
	
	public static void main(String[] args) throws IOException {
		AbstractGenRunScript gs;
		GenSirScriptClient gc;
		AbstractGenRunAllScript ga;
		String setEnv = "export experiment_root=" + rootDir + "\n";
		String sf = rootDir + subject + "/testplans.alt/universe";
		
		//read faults (subversion)
		readFaults();
		System.out.println(faults.toString());
		
		
		
//		//generate test scripts
//		String[] argvs = {"-sf", sf, "-sn", inputScript, "-en", AbstractGenRunScript.EXE, "-ed", rootDir + subject, "-tg", "bsh", "-nesc"};
//		MakeTestScript.main(argvs);
//		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
//		
//		String[] argvsC = {"-sf", sf, "-sn", inputCompScript, "-en", AbstractGenRunScript.EXE, "-ed", rootDir + subject, "-c", soutputDir, "-tg", "bsh", "-nesc"};
//		MakeTestScript.main(argvsC);
//		FileUtility.constructSIRInputsMapFile(inputCompScript, inputsCompMapFile);//read inputsMap
//		
//		//generate run subject and subversion scripts
//		gs = new GenRunSubjectScript(subject, version, setEnv + compileSubject, ssourceDir, sexecuteDir, soutputDir, scriptDir);
//		gs.genRunScript();
//		
//		for(int index: faults.keySet()){
//			gc = new GenSirScriptClient("subv" + index);
//			
//			String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
//			System.out.println("generating run script for subVersion" + index);
//			new GenRunVersionsScript(subject, version, gc.subVersion, setEnv + export + gc.compileVersion, gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
//		}
//		
//		//generate run all scripts  
//		assert(FileUtility.readInputsMap(inputsMapFile).size() == FileUtility.readInputsMap(inputsCompMapFile).size());
//		ga = new GenRunAllScript(version, subject, scriptDir, faults.size());
//		ga.genRunAllScript();
		
		
		//=========================================================================================================================================================================//
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			gc = new GenSirScriptClient("subv" + index);
			
//			SirSplitInputs split = new SirSplitInputs(inputsMapFile, gc.vexecuteDir, outCompFile);
//			split.split();
//			//collect the triggered faults
//			if(split.getFailingTests().size() > 1){
//				subs.add(index);
//			}
			
			String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
			System.out.println("generating run instrument script for subv" + index);
//			gs = new GenRunFineGrainedInstrumentScript(subject, version, gc.subVersion, setEnv + export + gc.compileFGInstrument, gc.vsourceDir, gc.vexecuteDir, gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
//			gs.genRunScript();
//			gs = new GenRunCoarseGrainedInstrumentScript(subject, version, gc.subVersion, setEnv + export + gc.compileCGInstrument, gc.vsourceDir, gc.vexecuteDir, gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
//			gs.genRunScript();
			if(new File(gc.vexecuteDir).listFiles().length == 12 || new File(gc.vexecuteDir).listFiles().length == 11){
				gs = new GenRunAdaptiveFineGrainedInstrumentScript(subject, version, gc.subVersion, setEnv + export, gc.vsourceDir, gc.vexecuteDir, gc.vafoutputDir, gc.scriptDir, gc.vaftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", gc.vexecuteDir + "adaptive/" + Client.METHODS);
				gs.genRunScript();
				subs.add(index);
			}
			
		}
		
//		//generate run all instrumented triggered subversion scripts
//		ga = new GenRunAllInstrumentedScript(version, subject, scriptDir, subs);
//		ga.genRunAllScript();

		//generate run all instrumented triggered subversion scripts
		ga = new GenRunAllAdaptiveInstrumentedScript(version, subject, scriptDir, subs);
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
