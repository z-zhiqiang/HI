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
	final static String subject = "space";
	final static String version = "v20";
	final static String inputs = rootDir + subject + "/testplans.alt/universe";
	final static String inputsMapFile = rootDir + subject + "/testplans.alt/inputs.map";
	final static String outDir = rootDir + subject + "/versions/";
	
	
	public static void main(String[] args) throws IOException {
		AbstractGenRunScript gs;
//		FileUtility.constructSpaceInputsMapFile(inputs, inputsMapFile);
//		gs = new GenRunSubjectScript(rootDir, subject);
//		gs.genRunScript();
//		gs = new GenRunVersionsScript(rootDir, subject, version);
//		gs.genRunScript();
		
//		SplitInputs split = new SplitInputs(inputsMapFile, rootDir + subject + "/outputs/" + subject,
//				rootDir + subject + "/outputs/versions/" + version + "/outputs", outDir + version);
//		split.split();
		gs = new GenRunFineGrainedInstrumentScript(rootDir, subject, version, outDir + version + "/failingInputs.array", outDir + version + "/passingInputs.array");
		gs.genRunScript();
		gs = new GenRunCoarseGrainedInstrumentScript(rootDir, subject, version, outDir + version + "/failingInputs.array", outDir + version + "/passingInputs.array", 100000);
		gs.genRunScript();
	}

}
