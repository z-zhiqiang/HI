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
	final static String inputs = rootDir + subject + "/testplans.alt/universe";
	final static String inputsMapFile = rootDir + subject + "/testplans.alt/inputs.map";
	final static String versionsDir = rootDir + subject + "/versions/";
	
	
	public static void main(String[] args) throws IOException {
//		FileUtility.constructSpaceInputsMapFile(inputs, inputsMapFile);
		AbstractGenRunScript gs;
//		gs = new GenRunSubjectScript(rootDir, "space", "");
//		gs.genRunScript();
//		gs = new GenRunVersionsScript(rootDir, "space", "v7");
//		gs.genRunScript();
		
		
		gs = new GenRunFineGrainedInstrumentScript(rootDir, "space", "v7", versionsDir + "v7/failingInputs.array", versionsDir + "v7/passingInputs.array");
		gs.genRunScript();
//		gs = new GenRunCoarseGrainedInstrumentScript(rootDir, "space", "v7", versionsDir + "v7/failingInputs.array");
//		gs.genRunScript();
	}

}
