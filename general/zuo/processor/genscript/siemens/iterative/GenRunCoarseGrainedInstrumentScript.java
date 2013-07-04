package zuo.processor.genscript.siemens.iterative;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileUtility;


public class GenRunCoarseGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	
	final List<Integer> failingTests;
	final List<Integer> passingTests;
//	final int pNum;
	
	
	public GenRunCoarseGrainedInstrumentScript(String sub, String ver, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing) {
		super(sub, ver, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		code.append(startTimeCommand + "\n");
		
		for (Iterator it = failingTests.iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append("$VERSIONSDIR/" + version + "_cinst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".fout\n");//output file
		}
		
		for (int i = 0; i < passingTests.size(); i++) {
			int index = passingTests.get(i);
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append("$VERSIONSDIR/" + version + "_cinst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".pout\n");//output file
		}
		
		code.append(endTimeCommand + " >& $OUTPUTSDIR/time\n");
		code.append("rm $OUTPUTSDIR/o*out\n");
		printToFile(code.toString(), scriptDir, version + "_cg.sh");
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
