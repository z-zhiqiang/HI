package zuo.processor.genscript.siemens.iterative;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileUtility;


public class GenRunSampledFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	final int sample;
	
	public GenRunSampledFineGrainedInstrumentScript(String sub, String ver, String sD, String eD, String oD, String scD, String tD, String failing, String passing, int sample) {
		super(sub, ver, null, sD, eD, oD + sample + "/", scD);
		this.traceDir = tD + sample + "/";
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		this.sample = sample;
		this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		String instrumentCommand = "sampler-cc "
				+ "-fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fcompare-constants "
				+ "-fsample -fsampler-random=fixed "
				+ sourceDir + subject + ".c" 
				+ " -o " + executeDir + version + "_finst__" + sample + ".exe"
				+ " -I" + sourceDir
				+ " -lm";
		
		StringBuffer code = new StringBuffer();
		code.append(instrumentCommand + "\n");
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}
		code.append(endTimeCommand + " >& $OUTPUTSDIR/time\n");
		
		code.append("rm $OUTPUTSDIR/o*out\n");
		code.append("rm $TRACESDIR/o*profile\n");
		printToFile(code.toString(), scriptDir, version + "_fg_s" + sample + ".sh");
	}


	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + sample + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append("$VERSIONSDIR/" + version + "_finst__" + sample + ".exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".fout\n");//output file
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + sample + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append("$VERSIONSDIR/" + version + "_finst__" + sample + ".exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".pout\n");//output file
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
