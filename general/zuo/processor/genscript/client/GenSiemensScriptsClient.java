package zuo.processor.genscript.client;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import zuo.processor.genscript.siemens.AbstractGenRunAllScript;
import zuo.processor.genscript.siemens.AbstractGenRunScript;
import zuo.processor.genscript.siemens.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.siemens.GenRunAllAdaptiveInstrumentedScript;
import zuo.processor.genscript.siemens.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.siemens.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.siemens.GenRunSubjectScript;
import zuo.processor.genscript.siemens.GenRunVersionsScript;
import zuo.processor.splitinputs.SplitInputs;
import zuo.util.file.FileUtility;


public class GenSiemensScriptsClient {
	final static String rootDir = "/home/sunzzq/Research/Automated_Debugging/Subjects/";
	final static String subject = "space";
	final String version;
	final static String inputs = rootDir + subject + "/testplans.alt/" + "universe";
	public final static String inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
	
	final String ssourceDir;
	final String sexecuteDir;
	final String soutputDir;
	
	final String vsourceDir;
	final String vexecuteDir;
	final String voutputDir;
	final String vfoutputDir;
	final String vcoutputDir;
	final String vftraceDir;
	final String vctraceDir;
	final String vafoutputDir;
	final String vaftraceDir;
	
	final static String scriptDir = rootDir + subject + "/scripts/";;
	
	final String compileSubject;
	final String compileVersion;
	final String compileFGInstrument;
	final String compileCGInstrument;
	
	public GenSiemensScriptsClient(String ver){
		version = ver;
		
		ssourceDir = rootDir + subject + "/source.alt/source.orig/";
		sexecuteDir = rootDir + subject + "/source/";
		soutputDir = rootDir + subject + "/outputs/" + subject + "/";
		
		vsourceDir = rootDir + subject + "/versions.alt/versions.orig/" + version + "/";
		vexecuteDir = rootDir + subject + "/versions/" + version + "/";
		voutputDir = rootDir + subject + "/outputs/versions/" + version + "/outputs/";
		vfoutputDir = rootDir + subject + "/outputs/versions/" + version + "/fine-grained/";
		vafoutputDir = rootDir + subject + "/outputs/versions/" + version + "/fine-grained-adaptive/";
		vcoutputDir = rootDir + subject + "/outputs/versions/" + version + "/coarse-grained/";
		vftraceDir = rootDir + subject + "/traces/" + version + "/fine-grained/";
		vaftraceDir = rootDir + subject + "/traces/" + version + "/fine-grained-adaptive/";
		vctraceDir = rootDir + subject + "/traces/" + version + "/coarse-grained/";
		
		
		
		compileSubject = "gcc " 
				+ ssourceDir + subject + ".c" 
				+ " -o " + sexecuteDir + subject + ".exe" 
				+ " -I" + ssourceDir
				+ " -lm";
		compileVersion = "gcc " 
				+ vsourceDir + subject + ".c"
				+ " -o " + vexecuteDir + version + ".exe"
				+ " -I" + vsourceDir
				+ " -lm";
		compileFGInstrument = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=float-kinds -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
				+ vsourceDir + subject + ".c" 
				+ " -o " + vexecuteDir + version + "_finst.exe"
				+ " -I" + vsourceDir
				+ " -lm";
		compileCGInstrument = "sampler-cc -fsampler-scheme=function-entries -fno-sample "
				+ vsourceDir + subject + ".c" 
				+ " -o " + vexecuteDir + version + "_cinst.exe"
				+ " -I" + vsourceDir
				+ " -lm";
		
	}
	
	public static void main(String[] args) throws IOException {
		AbstractGenRunScript gs;
		GenSiemensScriptsClient gc;
		AbstractGenRunAllScript ga;
		
//		FileUtility.constructSiemensInputsMapFile(inputs, inputsMapFile);
//		gc = new GenSiemensScriptsClient(subject);
//		gs = new GenRunSubjectScript(subject, gc.version, gc.compileSubject, gc.ssourceDir, gc.sexecuteDir, gc.soutputDir, gc.scriptDir);
//		gs.genRunScript();
		
		Set<Integer> subs = new HashSet<Integer>();
		for(int i = 1; i <= 38; i++){
			gc = new GenSiemensScriptsClient("v" + i);
			
//			System.out.println("generating run script for v" + i);
//			new GenRunVersionsScript(subject, gc.version, gc.compileVersion, gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
			
			//==========================================================================================================================================================//
			
//			System.out.println("sliptting inputs for v" + i);
//			SplitInputs split = new SplitInputs(inputsMapFile, gc.soutputDir, gc.voutputDir, gc.vexecuteDir);
//			split.split();
			
			System.out.println("generating run instrument script for v" + i);
//			gs = new GenRunFineGrainedInstrumentScript(subject, gc.version, gc.compileFGInstrument, gc.vsourceDir, gc.vexecuteDir, gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
//			gs.genRunScript();
//			gs = new GenRunCoarseGrainedInstrumentScript(subject, gc.version, gc.compileCGInstrument, gc.vsourceDir, gc.vexecuteDir, gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
//			gs.genRunScript();
			if(new File(gc.vexecuteDir).listFiles().length == 11){
				gs = new GenRunAdaptiveFineGrainedInstrumentScript(subject, gc.version, gc.vsourceDir, gc.vexecuteDir, gc.vafoutputDir, gc.scriptDir, gc.vaftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", gc.vexecuteDir + "adaptive/methods");
				gs.genRunScript();
				subs.add(i);
			}
		}
		
		ga = new GenRunAllAdaptiveInstrumentedScript(subject, scriptDir, subs);
		ga.genRunAllScript();
	
	}
	

}
