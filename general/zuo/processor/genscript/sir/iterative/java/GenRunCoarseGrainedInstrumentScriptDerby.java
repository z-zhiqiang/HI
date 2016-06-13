package zuo.processor.genscript.sir.iterative.java;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileUtility;


public class GenRunCoarseGrainedInstrumentScriptDerby extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	
	final List<Integer> failingTests;
	final List<Integer> passingTests;
	
	
	public GenRunCoarseGrainedInstrumentScriptDerby(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing) {
		super(sub, srcN, ver, subV, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		code.append("rm -f " + traceDir + "/*\n");
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}
		code.append(endTimeCommand + " > " + outputDir + "time 2>&1\n");
		
		code.append("rm -f " + scriptDir + "../outputs/*\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_cg.sh");
	}

	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(GenRunFineGrainedInstrumentScriptDerby.insertSetEnv(inputsMap.get(index)));
			code.append("\n");
		}
		
		for (int i = 0; i < passingTests.size(); i++) {
			int index = passingTests.get(i);
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(GenRunFineGrainedInstrumentScriptDerby.insertSetEnv(inputsMap.get(index)));
			code.append("\n");
		}
	}

	
	
	@Override
	protected void mkOutDir() {
		//make directory for outputs
		File fo = new File(outputDir);
		FileUtility.removeDirectory(fo);
		if(!fo.exists()){
			fo.mkdirs();
		}
		
		//make directory for traces
		File ft = new File(traceDir);
		FileUtility.removeDirectory(ft);
		if(!ft.exists()){
			ft.mkdirs();
		}
	}

}
