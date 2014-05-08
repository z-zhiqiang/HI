package zuo.processor.genscript.bash.twopass;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.processor.genscript.client.twopass.GenBashScriptClient;
import zuo.util.file.FileUtility;


public class GenRunFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	
	
	public GenRunFineGrainedInstrumentScript(String sub, String ver, String subV, String cc, String eD, String oD, String scD, String tD, String failing, String passing) {
		super(sub, ver, subV, cc, eD, oD, scD);
		this.traceDir = tD;
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");
		code.append("mv " + GenBashScriptClient.exeFile + executeDir + "/" + subVersion + "_finst.exe\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		code.append("export INPUTSDIR=" + GenBashScriptClient.inputsDir + "\n");
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}
		code.append(endTimeCommand + " >& " + outputDir + "/time\n");
		
		code.append("rm $OUTPUTSDIR/o*out\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg.sh");
	}


	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append("$VERSIONSDIR/" + subVersion + "_finst.exe ");//executables
			code.append("$INPUTSDIR/" + inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".fout\n");//output file
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append("$VERSIONSDIR/" + subVersion + "_finst.exe ");//executables
			code.append("$INPUTSDIR/" + inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".pout\n");//output file
			code.append("\n");
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
