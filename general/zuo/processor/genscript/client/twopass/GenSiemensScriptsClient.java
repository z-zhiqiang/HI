package zuo.processor.genscript.client.twopass;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import zuo.processor.genscript.siemens.twopass.AbstractGenRunAllScript;
import zuo.processor.genscript.siemens.twopass.AbstractGenRunScript;
import zuo.processor.genscript.siemens.twopass.GenRunAllInstrumentedScript;
import zuo.processor.genscript.siemens.twopass.GenRunAllScript;
import zuo.processor.genscript.siemens.twopass.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.siemens.twopass.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.siemens.twopass.GenRunSubjectScript;
import zuo.processor.genscript.siemens.twopass.GenRunVersionsScript;
import zuo.processor.splitinputs.SplitInputs;
import zuo.util.file.FileUtility;


public class GenSiemensScriptsClient {
	public final static String rootDir = "/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/";
	
	public final String subject;
	public final int vers;
	public final String version;
	public final String inputs;
	public final String inputsMapFile;
	
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
	
	final String scriptDir;
	
	final String compileSubject;
	final String compileVersion;
	final String compileFGInstrument;
	final String compileCGInstrument;
	
	public GenSiemensScriptsClient(String sub, int vs, String ver){
		subject = sub;
		vers = vs;
		version = ver;
		
		inputs = rootDir + subject + "/testplans.alt/" + "universe";
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		
		ssourceDir = rootDir + subject + "/source.alt/source.orig/";
		sexecuteDir = rootDir + subject + "/source/";
		soutputDir = rootDir + subject + "/outputs/" + subject + "/";
		
		vsourceDir = rootDir + subject + "/versions.alt/versions.orig/" + version + "/";
		vexecuteDir = rootDir + subject + "/versions/" + version + "/";
		voutputDir = rootDir + subject + "/outputs/versions/" + version + "/outputs/";
		vfoutputDir = rootDir + subject + "/outputs/versions/" + version + "/fine-grained/";
		vcoutputDir = rootDir + subject + "/outputs/versions/" + version + "/coarse-grained/";
		vftraceDir = rootDir + subject + "/traces/" + version + "/fine-grained/";
		vctraceDir = rootDir + subject + "/traces/" + version + "/coarse-grained/";
		
		scriptDir = rootDir + subject + "/scripts/";;
		
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
		compileFGInstrument = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
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
		String[][] subjects = {
				{"space", "38"},
//				{"printtokens", "7"},
//				{"printtokens2", "10"},
//				{"replace", "32"},
//				{"schedule", "9"},
//				{"schedule2", "10"},
//				{"tcas", "41"},
//				{"totinfo", "23"}
		};
		
		for(int i = 0; i < subjects.length; i++){
			GenSiemensScriptsClient gc = new GenSiemensScriptsClient(subjects[i][0], Integer.parseInt(subjects[i][1]), null);
			gc.gen();
		}
	
	}
	
	
	public void gen() throws IOException {
		AbstractGenRunScript gs;
		AbstractGenRunAllScript ga;
		
		
		FileUtility.constructSiemensInputsMapFile(inputs, inputsMapFile);
		gs = new GenRunSubjectScript(subject, compileSubject, ssourceDir, sexecuteDir, soutputDir, scriptDir);
		gs.genRunScript();
		
		for(int i = 1; i <= vers; i++){
			GenSiemensScriptsClient gc = new GenSiemensScriptsClient(subject, vers, "v" + i);
			System.out.println("generating run script for v" + i);
			new GenRunVersionsScript(gc.subject, gc.version, gc.compileVersion, gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
		}
		ga = new GenRunAllScript(subject, scriptDir, vers);
		ga.genRunAllScript();
		
		
		//==========================================================================================================================================================//
		
		
		Set<Integer> subs = new HashSet<Integer>();
		for(int i = 1; i <= vers; i++){	
			GenSiemensScriptsClient gc = new GenSiemensScriptsClient(subject, vers, "v" + i);
			
			System.out.println("sliptting inputs for v" + i);
			SplitInputs split = new SplitInputs(gc.inputsMapFile, gc.soutputDir, gc.voutputDir, gc.vexecuteDir);
			split.split();
			
			if(split.getFailingTests().size() >= 1){
				subs.add(i);
			}
			
			System.out.println("generating run instrument script for v" + i);
			gs = new GenRunFineGrainedInstrumentScript(gc.subject, gc.version, gc.compileFGInstrument, gc.vsourceDir, gc.vexecuteDir, 
					gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
			gs.genRunScript();
			gs = new GenRunCoarseGrainedInstrumentScript(gc.subject, gc.version, gc.compileCGInstrument, gc.vsourceDir, gc.vexecuteDir, 
					gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
			gs.genRunScript();
		}
		
		//generate run all instrumented triggered version scripts
		ga = new GenRunAllInstrumentedScript(subject, scriptDir, subs);
		ga.genRunAllScript();
		
	
	}
	

}
