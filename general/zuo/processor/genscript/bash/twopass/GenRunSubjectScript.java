package zuo.processor.genscript.bash.twopass;

import java.io.File;
import java.util.Iterator;

import zuo.processor.genscript.client.twopass.GenBashScriptClient;


public class GenRunSubjectScript extends AbstractGenRunScript {
	
	public GenRunSubjectScript(String sub, String ver, String cc, String eD, String oD, String scD) {
		super(sub, ver, null, cc, eD, oD, scD);
		this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n"); // compile subject program
		code.append("mv " + GenBashScriptClient.exeFile + executeDir + "/" + version + ".exe\n");
		code.append("echo script: " + subject + "_" + version + "\n");
		code.append("export SUBJECTDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		code.append("export INPUTSDIR=" + GenBashScriptClient.inputsDir + "\n");
		
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("$SUBJECTDIR/" + version + ".exe ");//executables
			code.append("$INPUTSDIR/" + inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".out\n");//output file
			code.append("\n");
		}
		printToFile(code.toString(), scriptDir, version + ".sh");
		
	}


	@Override
	protected void mkOutDir() {
		File fd = new File(outputDir);
		if(!fd.exists()){
			fd.mkdirs();
		}
		File fdx = new File(executeDir);
		if(!fdx.exists()){
			fdx.mkdirs();
		}
	}
	

}
