package zuo.processor.genscript.sir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zuo.processor.genscript.client.GenSirScriptClient;
import zuo.util.file.FileUtility;


public class GenRunAdaptiveFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private List<String> methods;
	private String methodsFile;
	
	public GenRunAdaptiveFineGrainedInstrumentScript(String sub, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, String methodsF) {
		super(sub, ver, subV, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		
		this.methods = new ArrayList<String>();
		this.methodsFile = methodsF;
		readMethods();
		mkOutDir();
	}


	private void readMethods() {
		// TODO Auto-generated method stub
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(new File(this.methodsFile)));
			while((line = reader.readLine()) != null){
				methods.add(line.trim());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}


	@Override
	public void genRunScript() {
		StringBuffer code = new StringBuffer();
		String instrumentCommand;
		for(String method: methods){
			instrumentCommand = compileCommand 
					+ "sampler-cc -fsampler-scheme=branches -fsampler-scheme=float-kinds -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
					+ "-finclude-function=" + method + " -fexclude-function='*' "
					+ sourceDir + GenSirScriptClient.sourceName + ".c" 
					+ " $COMPILE_PARAMETERS"
					+ " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1"
					+ " -o " + executeDir + subVersion + "_finst_" + method + ".exe"
					+ " -I" + sourceDir
//					+ " -lm"
					;
			
			code.append(instrumentCommand + "\n");
			code.append(startTimeCommand + "\n");
			code.append("echo script: " + subVersion + "\n");
			code.append("export VERSIONSDIR=" + executeDir + "\n");
			code.append("export OUTPUTSDIR=" + outputDir + "\n");
			
			for (Iterator it = failingTests.iterator(); it.hasNext();) {
				int index = (Integer) it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("export SAMPLER_FILE=" + traceDir + method + "/" + "o" + index + ".fprofile\n");
				code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst_" + method + ".exe "));
				code.append("\n");
			}
			
			for (Iterator it = passingTests.iterator(); it.hasNext();) {
				int index = (Integer) it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("export SAMPLER_FILE=" + traceDir + method + "/" + "o" + index + ".pprofile\n");
				code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst_" + method + ".exe "));
				code.append("\n");
			}
			
			code.append(endTimeCommand + " >& " + outputDir + "time_" + method);
			code.append("\n\n");
		}
		
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_a.sh");
	}


	@Override
	protected void mkOutDir() {
		//make directory for outputs
		File fo = new File(outputDir);
		if(!fo.exists()){
			fo.mkdirs();
		}
		
		//make directory for traces
		for(String method: methods){
			File ft = new File(traceDir + method + "/");
			if(!ft.exists()){
				ft.mkdirs();
			}
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
