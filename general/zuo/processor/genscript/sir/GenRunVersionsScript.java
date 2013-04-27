package zuo.processor.genscript.sir;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

import zuo.util.file.FileUtility;


public class GenRunVersionsScript extends AbstractGenRunScript {
	
	public static final String SUBV = "SUBV";


	public GenRunVersionsScript(String sub, String ver, String subV, String cc, String sD, String eD, String oD, String scD) {
		super(sub, ver, subV, cc, sD, eD, oD, scD);
	    this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(compileCommand + "\n");// compiling
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append(startTimeCommand + "\n");
		for (Iterator it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + version + ".exe "));//executables
			code.append("\n");
		}
		code.append(endTimeCommand + " >& " + outputDir + "time\n");
		code.append("rm ../outputs/*\n");
		code.append("\n\n");
		
		code.append("echo script: " + subVersion + "\n");
		for (Iterator it = inputsCompMap.keySet().iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append(inputsCompMap.get(index).replace(EXE, "$VERSIONSDIR/" + version + ".exe ").replace(SUBV, version + "/" + subVersion));//executables
			code.append("\n");
		}
		code.append("mv ../outputs/* " + outputDir + "\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + ".sh");
		
	}
	
	
//	public void genCompileScript(){
//		StringBuffer code = new StringBuffer();
//		code.append("echo compiling script " + subject + "\n");
//		code.append("export ROOTDIR=" + rootDir + subject + "\n");
//		
//		code.append(compileInfo + subject + "\"\n");
//		code.append("gcc $ROOTDIR/source/" + subject + ".c -o $ROOTDIR/source/" + subject + ".exe\n");
//		
//		File fd = new File(rootDir + subject + "/versions");
//		String[] fs = fd.list(new VersionFoldernameFilter());
//		Arrays.sort(fs, new FoldernameComparator());
//		
//		for (int i = 0; i < fs.length; i++) {
//			code.append(compileInfo + fs[i] + "\"\n");
//			String dir = "$ROOTDIR/versions/" + fs[i] + "/";
//			code.append("gcc " + dir + subject + ".c -o " + dir + fs[i] + ".exe\n");
//		}
//		
//		printToFile(code.toString(), scriptsDir, "compile.sh");
//		
//	}
	
//	public void genRunAllScripts(){
//		StringBuffer code = new StringBuffer();
//		code.append("echo running all scripts \n");
//		code.append("export ROOTDIR=" + rootDir + subject + "\n");
//		
//		code.append("echo running script " + subject + ".sh\n");
//		code.append("sh $ROOTDIR/scripts/runOriginal/" + subject + ".sh\n");
//		
//		File f = new File(rootDir + subject + "/scripts/runOriginal/");
//		String[] fs = f.list(new VersionShFilenameFilter());
//		Arrays.sort(fs, new ShFilenameComparator());
//		
//		for (int i = 0; i < fs.length; i++) {
//			code.append("echo running script " + fs[i] + "\n");
//			code.append("sh $ROOTDIR/scripts/runOriginal/" + fs[i] + "\n");
//		}
//		code.append("echo Over\n");
//		
//		printToFile(code.toString(), rootDir + subject + "/scripts", "runOriginalAllScripts.sh");
//	}
	
	
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
