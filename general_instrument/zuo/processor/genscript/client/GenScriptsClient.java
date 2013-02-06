package zuo.processor.genscript.client;

import java.io.IOException;
import java.util.ArrayList;

import zuo.processor.genscript.version.AbstractGenRunScript;
import zuo.processor.genscript.version.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.version.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.version.GenRunSubjectScript;
import zuo.processor.genscript.version.GenRunVersionsScript;
import zuo.processor.splitinputs.SplitInputs;
import zuo.processor.utility.FileUtility;


public class GenScriptsClient {
	final static String rootDir = "/home/sunzzq/Research/";
	final static String subject = "grep";
	final static String version = "v1";
	final static String inputs = rootDir + subject + "/testplans.alt/" + version + "/universe";
	public final static String inputsMapFile = rootDir + subject + "/testplans.alt/" + version + "/inputs.map";
	
	final static String ssourceDir = rootDir + subject + "/versions.alt/versions.orig/v0/";
	final static String sexecuteDir = rootDir + subject + "/source/";
	final static String soutputDir = rootDir + subject + "/outputs/" + subject + "/";
	
	final static String vsourceDir = rootDir + subject + "/versions.alt/versions.seeded/" + version + "/";
	final static String vexecuteDir = rootDir + subject + "/versions/" + version + "/";
	final static String voutputDir = rootDir + subject + "/outputs/versions/" + version + "/outputs/";
	final static String vfoutputDir = rootDir + subject + "/outputs/versions/" + version + "/fine-grained/";
	final static String vcoutputDir = rootDir + subject + "/outputs/versions/" + version + "/coarse-grained/";
	final static String vftraceDir = rootDir + subject + "/traces/" + version + "/fine-grained/";
	final static String vctraceDir = rootDir + subject + "/traces/" + version + "/coarse-grained/";
	
	final static String sscriptDir = rootDir + subject + "/scripts/runSubject/";
	final static String vscriptDir = rootDir + subject + "/scripts/runVersions/";
	final static String cgscriptDir = rootDir + subject + "/scripts/runCoarseGrained/";
	final static String fgscriptDir = rootDir + subject + "/scripts/runFineGrained/";
	
	final static String compileSubject = "gcc " 
			+ ssourceDir + subject + ".c" 
			+ " -o " + sexecuteDir + subject + ".exe" 
			+ " -I" + ssourceDir;
	final static String compileVersion = "gcc " 
			+ vsourceDir + subject + ".c"
			+ " -o " + vexecuteDir + version + ".exe"
			+ " -I" + vsourceDir;
	final static String compileFGInstrument = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=float-kinds -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
			+ vsourceDir + subject + ".c" 
			+ " -o " + vexecuteDir + version + "_finst.exe"
			+ " -I" + vsourceDir;
	final static String compileCGInstrument = "sampler-cc -fsampler-scheme=function-entries -fno-sample "
			+ vsourceDir + subject + ".c" 
			+ " -o " + vexecuteDir + version + "_cinst.exe"
			+ " -I" + vsourceDir;
	
	
	public static void main(String[] args) throws IOException {
		AbstractGenRunScript gs;
//		FileUtility.constructGrepInputsMapFile(inputs, inputsMapFile);
//		gs = new GenRunSubjectScript(subject, version, compileSubject, ssourceDir, sexecuteDir, soutputDir, sscriptDir);
//		gs.genRunScript();
//		gs = new GenRunVersionsScript(subject, version, compileVersion, vsourceDir, vexecuteDir, voutputDir, vscriptDir);
//		gs.genRunScript();
		
		SplitInputs split = new SplitInputs(inputsMapFile, soutputDir, voutputDir, vexecuteDir);
		split.split();
		gs = new GenRunFineGrainedInstrumentScript(subject, version, compileFGInstrument, vsourceDir, vexecuteDir, vfoutputDir, fgscriptDir, vftraceDir, vexecuteDir + "failingInputs.array", vexecuteDir + "passingInputs.array");
		gs.genRunScript();
		gs = new GenRunCoarseGrainedInstrumentScript(subject, version, compileCGInstrument, vsourceDir, vexecuteDir, vcoutputDir, cgscriptDir, vctraceDir, vexecuteDir + "failingInputs.array", vexecuteDir + "passingInputs.array", 100000);
		gs.genRunScript();
	}

}
