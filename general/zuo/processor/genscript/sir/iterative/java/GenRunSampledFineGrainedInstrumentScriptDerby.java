package zuo.processor.genscript.sir.iterative.java;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileUtility;


public class GenRunSampledFineGrainedInstrumentScriptDerby extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	final int sample;
	
	
	public GenRunSampledFineGrainedInstrumentScriptDerby(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, int sample) {
		super(sub, srcN, ver, subV, cc, sD, eD, oD + sample + "/", scD);
		this.traceDir = tD + sample + "/";
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		this.sample = sample;
		this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		
//		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}
		code.append(endTimeCommand + " > " + outputDir + "time 2>&1\n");
		
		code.append("rm -f " + scriptDir + "../outputs/*\n");
//		code.append("rm -f $TRACESDIR/o*profile\n");
		
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_s" + sample + ".sh");
	}


	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + sample + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(insertSetEnv(inputsMap.get(index)));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + sample + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(insertSetEnv(inputsMap.get(index)));
			code.append("\n");
		}
	}

	private String insertSetEnv(String test){
		String unset = "unset SAMPLER_FILE";
		StringBuilder builder = new StringBuilder();
		
		String[] lines = test.split("\n");
		for(String line: lines){
			builder.append(line).append("\n");
			if(line.endsWith("2>&1")){
				System.out.println(line);
				builder.append(unset).append("\n");
			}
		}
		
		return builder.toString();
	}
	
	
	@Override
	protected void mkOutDir() {
		File fe = new File(executeDir);
		FileUtility.removeDirectory(fe);
		if(!fe.exists()){
			fe.mkdirs();
		}
		
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
		
//		File od = new File(outputDir);
//		if(od.isDirectory() && od.exists()){
//			boolean flag = FileUtility.removeDirectory(od);
//			assert(flag == true);
//		}
//		
//		File td = new File(traceDir);
//		if(td.isDirectory() && td.exists()){
//			boolean flag = FileUtility.removeDirectory(td);
//			assert(flag == true);
//		}
	}

}
