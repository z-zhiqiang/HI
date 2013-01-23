package zuo.processor.genscript.version;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;


public class GenRunSubjectScript extends AbstractGenRunScript {
	public GenRunSubjectScript(String dir, String sub, String ver) {
		super(dir, sub, null);
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append("echo script: " + subject + "\n");
		code.append("export SUBJECTDIR=" + rootDir + subject + "/source/\n");
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
	

}
