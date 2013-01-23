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
	
	public GenRunCoarseGrainedInstrumentScript(String dir, String sub, String ver, String failing) {
		super(dir, sub, ver);
		this.failingTests = FileUtility.readInputsArray(failing);
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		
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
		
		printToFile(code.toString(), scriptsDir + "runCoarseGrainedInstrument", version + ".sh");
	}

}
