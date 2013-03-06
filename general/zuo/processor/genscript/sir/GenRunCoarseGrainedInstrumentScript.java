package zuo.processor.genscript.sir;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileUtility;


public class GenRunCoarseGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	
	final List<Integer> failingTests;
	final List<Integer> passingTests;
	final int pNum;
	
	
	public GenRunCoarseGrainedInstrumentScript(String sub, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, int pN) {
		super(sub, ver, subV, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		this.pNum = pN;
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");
		code.append(startTimeCommand + "\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		
		for (Iterator it = failingTests.iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=" + traceDir + "o" + index + ".fprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_cinst.exe "));
			code.append("\n");
		}
		
		for (int i = 0; i < passingTests.size() && i < pNum; i++) {
			int index = passingTests.get(i);
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=" + traceDir + "o" + index + ".pprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_cinst.exe "));
			code.append("\n");
		}
		
		code.append(endTimeCommand + " >& " + outputDir + "time");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_cg.sh");
	}

	@Override
	protected void mkOutDir() {
		//make directory for outputs
		File fo = new File(outputDir);
		if(!fo.exists()){
			fo.mkdirs();
		}
		
		//make directory for traces
		File ft = new File(traceDir);
		if(!ft.exists()){
			ft.mkdirs();
		}
	}

}
