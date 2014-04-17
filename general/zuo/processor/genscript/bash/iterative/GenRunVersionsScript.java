package zuo.processor.genscript.bash.iterative;

import java.io.File;
import java.util.Iterator;

import zuo.processor.genscript.client.iterative.GenBashScriptClient;



public class GenRunVersionsScript extends AbstractGenRunScript {
	public GenRunVersionsScript(String sub, String ver, String subV, String cc, String eD, String oD, String scD) {
		super(sub, ver, subV, cc, eD, oD, scD);
	    this.mkOutDir();
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");// compiling
		code.append("mv " + GenBashScriptClient.exeFile + executeDir + "/" + version + ".exe\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		code.append("export INPUTSDIR=" + GenBashScriptClient.inputsDir + "\n");
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for (int j = 0; j < ROUNDS; j++) {
			stmts(code);
		}
		code.append(endTimeCommand + " >& " + outputDir + "/time\n");
		
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + ".sh");
		
	}

	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("$VERSIONSDIR/" + version + ".exe ");//executables
			code.append("$INPUTSDIR/" + inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".out\n");//output file
			code.append("\n");
		}
	}
	
	protected void mkOutDir(){
		File fp = new File(outputDir);
		if(!fp.exists()){
			fp.mkdirs();
		}
		File fo = new File(executeDir);
		if(!fo.exists()){
			fo.mkdirs();
		}
	}

}
