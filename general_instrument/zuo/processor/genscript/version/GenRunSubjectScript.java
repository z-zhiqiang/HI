package zuo.processor.genscript.version;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;


public class GenRunSubjectScript extends AbstractGenRunScript {
	final String source = rootDir + subject + "/source.alt/source.orig/" + subject + ".c";
	final String execute = subjectDir + subject + ".exe";
	
	public GenRunSubjectScript(String dir, String sub) {
		super(dir, sub, null);
		this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append("gcc " + source + " -o " + execute + " -lm\n"); // compile subject program
		code.append("echo script: " + subject + "\n");
		code.append("export SUBJECTDIR=" + subjectDir + "\n");
		code.append("export OUTPUTSDIR=" + outputsDir + subject + "\n");
		
		for (Iterator it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("$SUBJECTDIR/" + subject + ".exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" > $OUTPUTSDIR/o" + index + ".out\n");//output file
		}
		
		printToFile(code.toString(), scriptsDir + "runSubject", subject + ".sh");
		
	}


	@Override
	protected void mkOutDir() {
		File fd = new File(outputsDir + subject);
		if(!fd.exists()){
			fd.mkdirs();
		}
	}
	

}
