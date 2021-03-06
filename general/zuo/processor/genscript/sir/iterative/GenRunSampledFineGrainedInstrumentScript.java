package zuo.processor.genscript.sir.iterative;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileUtility;


public class GenRunSampledFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	final int sample;
	
	
	public GenRunSampledFineGrainedInstrumentScript(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, int sample) {
		super(sub, srcN, ver, subV, cc, sD, eD, oD + sample + "/", scD);
		this.traceDir = tD + sample + "/";
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		this.sample = sample;
		this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		String includeC = "";
		String paraC = "";
		if(subject.equals("gzip")){
			paraC = " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1";
		}
		if(subject.equals("grep")){
			includeC = " -I" + sourceDir;
		}
		
		String instrumentCommand = compileCommand 
				+ "sampler-cc "
				+ "-fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs "
				+ "-fsample -fsampler-random=fixed "
				+ sourceDir + srcName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ paraC
				+ " -o " + executeDir + subVersion + "_finst__" + sample + ".exe"
				+ includeC
				;
		
		StringBuffer code = new StringBuffer();
		code.append(instrumentCommand + "\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}
		code.append(endTimeCommand + " >& " + outputDir + "time\n");
		
		code.append("rm ../outputs/*\n");
		code.append("rm $TRACESDIR/o*profile\n");
		
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_s" + sample + ".sh");
	}


	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + sample + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst__" + sample + ".exe "));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + sample + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst__" + sample + ".exe "));
			code.append("\n");
		}
	}


	@Override
	protected void mkOutDir() {
		File fe = new File(executeDir);
		if(!fe.exists()){
			fe.mkdirs();
		}
		
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
