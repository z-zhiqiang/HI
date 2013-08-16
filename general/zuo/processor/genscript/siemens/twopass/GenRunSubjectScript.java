package zuo.processor.genscript.siemens.twopass;

import java.io.File;
import java.util.Iterator;


public class GenRunSubjectScript extends AbstractGenRunScript {
	
	public GenRunSubjectScript(String sub, String ver, String cc, String sD, String eD, String oD, String scD) {
		super(sub, ver, cc, sD, eD, oD, scD);
		this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n"); // compile subject program
		code.append("echo script: " + subject + "\n");
		code.append("export SUBJECTDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("$SUBJECTDIR/" + subject + ".exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" >& $OUTPUTSDIR/o" + index + ".out\n");//output file
		}
		
		printToFile(code.toString(), scriptDir, subject + ".sh");
		
	}


	@Override
	protected void mkOutDir() {
		File fd = new File(outputDir);
		if(!fd.exists()){
			fd.mkdirs();
		}
	}
	

}
