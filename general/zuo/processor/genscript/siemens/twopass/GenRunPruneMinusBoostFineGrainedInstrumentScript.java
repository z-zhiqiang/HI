package zuo.processor.genscript.siemens.twopass;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import zuo.util.file.FileCollection;
import zuo.util.file.FileUtility;


public class GenRunPruneMinusBoostFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private final Set<String> pruneMinusBoostFunctions;
	
	public GenRunPruneMinusBoostFineGrainedInstrumentScript(String sub, String ver, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, File pruneMinusBoost) {
		super(sub, ver, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		this.pruneMinusBoostFunctions = FileCollection.readFunctions(pruneMinusBoost);
	}


	@Override
	public void genRunScript() {
		String instrumentCommand = compileCommand
				+ "sampler-cc "
				+ "-fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fcompare-constants "
				+ "-fno-sample "
				+ functionFiltering()
				+ sourceDir + subject + ".c" 
				+ " -o " + executeDir + version + "_pmbinst.exe"
//				+ " -I" + sourceDir
				+ " -lm";
		
		StringBuffer code = new StringBuffer();
		code.append(instrumentCommand + "\n");
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		code.append("rm $TRACESDIR/o*profile\n");
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}		
		code.append(endTimeCommand + " >& $OUTPUTSDIR/time\n");
		
		code.append("rm $OUTPUTSDIR/o*out\n");
		code.append("rm $TRACESDIR/o*profile\n");
		
		printToFile(code.toString(), scriptDir, version + "_pruneMinusBoost.sh");
	}

	private String functionFiltering() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		for(String function: this.pruneMinusBoostFunctions){
			builder.append("-finclude-function=").append(function).append(" ");
		}
		builder.append("-fexclude-function=* ");
		return builder.toString();
	}

	private void stmts(StringBuffer code) {
		if(this.pruneMinusBoostFunctions.isEmpty()){
			return;
		}
		
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append("$VERSIONSDIR/" + version + "_pmbinst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".fout\n");//output file
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append("$VERSIONSDIR/" + version + "_pmbinst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".pout\n");//output file
		}
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
