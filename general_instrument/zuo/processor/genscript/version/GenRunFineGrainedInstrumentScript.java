package zuo.processor.genscript.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import zuo.processor.utility.FileUtility;


public class GenRunFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	public final static String fgFolder = "/fine-grained";
	
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	
	final String source = rootDir + subject + "/versions.alt/versions.orig/" + version + "/" + subject + ".c";
	final String execute = versionsDir + version + "/" + version + "_finst.exe";
	final String instrumentFineGrained = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=float-kinds " +
			"-fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample " + source + " -o " + execute + " -lm";
	
	public GenRunFineGrainedInstrumentScript(String dir, String sub, String ver, String failing, String passing) {
		super(dir, sub, ver);
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		code.append(instrumentFineGrained + "\n");
		code.append("echo script: " + version + "\n");
		code.append("export VERSIONSDIR=" + versionsDir + version + "\n");
		code.append("export OUTPUTSDIR=" + outputversionsDir + version + fgFolder + "\n");
		
		for (Iterator it = failingTests.iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=" + tracesDir + version + fgFolder + "/o" + index + ".fprofile\n");
			code.append("$VERSIONSDIR/" + version + "_finst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" > $OUTPUTSDIR/o" + index + ".fout\n");//output file
		}
		
		for (Iterator it = passingTests.iterator(); it.hasNext();) {
			int index = (Integer) it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=" + tracesDir + version + fgFolder + "/o" + index + ".pprofile\n");
			code.append("$VERSIONSDIR/" + version + "_finst.exe ");//executables
			code.append(inputsMap.get(index));//parameters
			code.append(" > $OUTPUTSDIR/o" + index + ".pout\n");//output file
		}
		
		printToFile(code.toString(), scriptsDir + "runFineGrainedInstrument", version + ".sh");
	}


	@Override
	protected void mkOutDir() {
		//make directory for outputs
		File fo = new File(outputversionsDir + version + fgFolder);
		if(!fo.exists()){
			fo.mkdirs();
		}
		
		//make directory for traces
		File ft = new File(tracesDir + version + fgFolder);
		if(!ft.exists()){
			ft.mkdirs();
		}
	}
	
	
//	public void genInstrumentPredicateScripts(){
//		final String instrumentCommand = "sampler-cc $SCHEME -fno-sample "; 
//		final String extractSiteCommand = "$EXTRACTDIR/extract-section .debug_site_info ";
//		
//		StringBuffer code = new StringBuffer();
//		code.append("echo instrumenting script: " + subject + "\n");
//		code.append("export ROOTDIR=" + rootDir + subject + "\n");
//		code.append("export EXTRACTDIR=" + extractToolsDir + "\n");
//		code.append("export SCHEME=\"" + finerGrainedScheme + "\"\n");
//		
//		String[] fs = new File(rootDir + subject, "/versions").list(new VersionFoldernameFilter());
//		Arrays.sort(fs, new FoldernameComparator());
//		for (int i = 0; i < fs.length; i++) {
//			code.append(instrumentInfo + fs[i] + "\"\n");
//			String dir = "$ROOTDIR/versions/" + fs[i] + "/";
//			code.append(instrumentCommand + dir + subject + ".c -o " + dir + fs[i] + "_inst.exe\n");
//			code.append(extractSiteCommand + dir + fs[i] + "_inst.exe > " + dir + "sites.txt\n");
//		}
//		code.append("echo instrumentation finished\n");
//		
//		printToFile(code.toString(), rootDir + subject + "/scripts", "fineGrainedInstrument.sh");
//	}

}
