package zuo.processor.genscript.sir.iterative.java;

import java.io.File;
import java.util.Iterator;

import zuo.processor.genscript.client.iterative.java.JavaGenSirScriptClient;



public class GenRunVersionsScript extends AbstractGenRunScript {
	public static final String SUBV = "SUBV";

	public GenRunVersionsScript(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD) {
		super(sub, srcN, ver, subV, cc, sD, eD, oD, scD);
	    this.mkOutDir();
	}

	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");// compiling
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		
//		stmts(code);
//		code.append(startTimeCommand + "\n");
		code.append("tTime=0\n");
		for (int j = 0; j < ROUNDS; j++) {
			stmts(code);
		}
//		code.append(endTimeCommand + " > " + outputDir + "time 2>&1\n");
		code.append("echo \"Time in seconds: $((tTime/1000000000)) \nTime in milliseconds: $((tTime/1000000))\""  + " > " + outputDir + "time 2>&1\n");
		
		code.append("cd " + scriptDir + "\n");
		code.append("rm ../outputs/*\n");
		code.append("\n\n");
		
//		code.append("rm $VERSIONSDIR/" + JavaGenSirScriptClient.outCompFile + "\n");
//		code.append("echo script: " + subVersion + "\n");
//		for (Iterator<Integer> it = inputsCompMap.keySet().iterator(); it.hasNext();) {
//			int index = it.next();
//			code.append(runinfo + index + "\"\n");// running info
//			code.append(inputsCompMap.get(index).replace(SUBV, version + "/" + subVersion));//executables
//			code.append("\n");
//		}
//		code.append("cd " + scriptDir + "\n");
//		code.append("mv ../outputs/* " + outputDir + "\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + ".sh");
		
	}

	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append(addTimingCode(inputsMap.get(index)));//executables
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
