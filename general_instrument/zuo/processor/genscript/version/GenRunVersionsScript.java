package zuo.processor.genscript.version;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;


public class GenRunVersionsScript extends AbstractGenRunScript {
	public final static String outFolder = "/outputs";
	
	final String source = rootDir + subject + "/versions.alt/versions.orig/" + version + "/" + subject + ".c";
	final String execute = versionsDir + version + "/" + version + ".exe";
	
	public GenRunVersionsScript(String dir, String sub, String ver) {
		super(dir, sub, ver);
	    this.mkOutDir();
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append("gcc " + source + " -o " + execute + " -lm\n");// compiling
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
	
	
	protected void mkOutDir(){
		File fp = new File(outputversionsDir + version + outFolder);
		if(!fp.exists()){
			fp.mkdirs();
		}
		File fo = new File(versionsDir + version);
		if(!fo.exists()){
			fo.mkdirs();
		}
	}

}
