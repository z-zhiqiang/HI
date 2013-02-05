package zuo.processor.genscript.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import zuo.processor.utility.FileUtility;

public class GenRunCoarseGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	public final static String cgFolder = "/coarse-grained";
	
	final List<Integer> failingTests;
	final List<Integer> passingTests;
	final int pNum;
	
	final String source = rootDir + subject + "/versions.alt/versions.orig/" + version + "/" + subject + ".c";
	final String execute = versionsDir + version + "/" + version + "_cinst.exe";
	final String instrumentCoarseGrained = "sampler-cc -fsampler-scheme=function-entries -fno-sample " + source + " -o " + execute + " -lm";
	
	public GenRunCoarseGrainedInstrumentScript(String dir, String sub, String ver, String failing, String passing, int pN) {
		super(dir, sub, ver);
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		this.pNum = pN;
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(instrumentCoarseGrained + "\n");
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + versionsDir + version + "\n");
		code.append("export OUTPUTSDIR=" + outputversionsDir + version + cgFolder + "\n");
		
		for (Iterator it = failingTests.iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=" + tracesDir + version + cgFolder + "/o" + index + ".fprofile\n");
			code.append("$VERSIONSDIR/" + version + "_cinst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" > $OUTPUTSDIR/o" + index + ".fout\n");//output file
		}
		
		for (int i = 0; i < passingTests.size() && i < pNum; i++) {
			int index = passingTests.get(i);
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=" + tracesDir + version + cgFolder + "/o" + index + ".pprofile\n");
			code.append("$VERSIONSDIR/" + version + "_cinst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" > $OUTPUTSDIR/o" + index + ".pout\n");//output file
		}
		
		printToFile(code.toString(), scriptsDir + "runCoarseGrainedInstrument", version + ".sh");
	}

	@Override
	protected void mkOutDir() {
		//make directory for outputs
		File fo = new File(outputversionsDir + version + cgFolder);
		if(!fo.exists()){
			fo.mkdirs();
		}
		
		//make directory for traces
		File ft = new File(tracesDir + version + cgFolder);
		if(!ft.exists()){
			ft.mkdirs();
		}
	}

}
