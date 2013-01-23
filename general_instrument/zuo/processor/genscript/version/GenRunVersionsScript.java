package zuo.processor.genscript.version;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;


public class GenRunVersionsScript extends AbstractGenRunScript {
	private final static String compileInfo = "echo \">>>>>>> compiling file ";
	public final static String outFolder = "/outputs";
	
	public GenRunVersionsScript(String dir, String sub, String ver) {
		super(dir, sub, ver);
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + versionsDir + version + "\n");
		code.append("export OUTPUTSDIR=" + outputversionsDir + version + outFolder + "\n");
		
		for (Iterator it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("$VERSIONSDIR/" + version + ".exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" > $OUTPUTSDIR/o" + index + ".out\n");//output file
		}
		
		printToFile(code.toString(), scriptsDir + "runVersions", version + ".sh");
		
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
	
	
	public void mkOutDir(){
		File fp = new File(rootDir + subject + "/outputs/versions");
		if(!fp.exists()){
			fp.mkdir();
		}
		File fo = new File(rootDir + subject + "/versions");
		String[] fs = fo.list();
		for (int i = 0; i < fs.length; i++) {
			File fd = new File(rootDir + subject + "/outputs/versions/" + fs[i]);
			if(!fd.exists()){
				fd.mkdir();
			}
		}
		File fd = new File(rootDir + subject + "/outputs/" + subject);
		if(!fd.exists()){
			fd.mkdir();
		}
	}

}
