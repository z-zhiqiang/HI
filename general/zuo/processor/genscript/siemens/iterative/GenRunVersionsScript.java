package zuo.processor.genscript.siemens.iterative;

import java.io.File;
import java.util.Iterator;


public class GenRunVersionsScript extends AbstractGenRunScript {
	
	public GenRunVersionsScript(String sub, String ver, String cc, String sD, String eD, String oD, String scD) {
		super(sub, ver, cc, sD, eD, oD, scD);
	    this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");// compiling
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export OUTPUTSDIR=" + outputDir + "\n");
		
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < 3; j++){
			for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
				int index = it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("$VERSIONSDIR/" + version + ".exe ");//executables
				code.append(inputsMap.get(index));//parameters
				code.append(" >& $OUTPUTSDIR/o" + index + ".out\n");//output file
			}
		}
		code.append(endTimeCommand + " >& $OUTPUTSDIR/time\n");
		
		printToFile(code.toString(), scriptDir, version + ".sh");
		
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
