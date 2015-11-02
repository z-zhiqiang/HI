package zuo.processor.genscript.sir.iterative.java;

import java.io.File;
import java.util.Iterator;

import zuo.processor.genscript.client.iterative.java.NanoxmlGenSirScriptClient;
import zuo.util.file.FileUtility;



public class GenRunVersionsScriptDerby extends AbstractGenRunScript {
	public static final String SUBV = "SUBV";

	public GenRunVersionsScriptDerby(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD) {
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
		code.append(startTimeCommand + "\n");
		for (int j = 0; j < ROUNDS; j++) {
//			stmts(code);
		}
		code.append(endTimeCommand + " > " + outputDir + "time 2>&1\n");
		
		code.append("rm -f " + scriptDir + "../outputs/*\n");
		code.append("\n\n");
		
		code.append("rm -f $VERSIONSDIR/" + NanoxmlGenSirScriptClient.outCompFile + "\n");
		code.append("echo script: " + subVersion + "\n");
		for (Iterator<Integer> it = inputsCompMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append(inputsCompMap.get(index).replace(SUBV, version + "/" + subVersion));//executables
			code.append("cmp -s " + scriptDir + "../outputs/t" + index
					+ " " + scriptDir + "../outputs.alt/" + version + "/" + subject + "/t" + index
					+ " || echo " + index + " >> " + executeDir + NanoxmlGenSirScriptClient.outCompFile + "\n");
			code.append("\n");
		}
		code.append("mv " + scriptDir + "../outputs/* " + outputDir + "\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + ".sh");
		
	}

	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append(inputsMap.get(index));//executables
			code.append("\n");
		}
	}
	
	protected void mkOutDir(){
		File fp = new File(outputDir);
		FileUtility.removeDirectory(fp);
		if(!fp.exists()){
			fp.mkdirs();
		}
		
		File fo = new File(executeDir);
		FileUtility.removeDirectory(fo);
		if(!fo.exists()){
			fo.mkdirs();
		}
	}

}
