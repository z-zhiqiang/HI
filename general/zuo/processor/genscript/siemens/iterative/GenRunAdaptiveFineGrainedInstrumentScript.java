package zuo.processor.genscript.siemens.iterative;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;
import zuo.processor.genscript.client.iterative.GenSiemensScriptsClient;
import zuo.processor.genscript.client.iterative.GenSirScriptClient;
import zuo.util.file.FileUtility;


public class GenRunAdaptiveFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private List<String> methods;
	private Score methodsFile;
	
	
	public GenRunAdaptiveFineGrainedInstrumentScript(String sub, String ver, String sD, String eD, String oD, String scD, String tD, String failing, String passing, Score methodsF) {
		super(sub, ver, null, sD, eD, oD + methodsF + "/", scD);
		this.traceDir = tD + methodsF + "/";
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
			reader = new BufferedReader(new FileReader(new File(executeDir + methodsFile)));
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
		int num = methods.size();
		StringBuilder code = new StringBuilder();
		code.append("tTime=0\n");
		String instrumentCommand;
		for(int i = 0; i < num; i++){
			String method = methods.get(i);
			instrumentCommand = "sampler-cc -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
					+ "-finclude-function=" + method + " -fexclude-function=* "
					+ sourceDir + subject + ".c" 
					+ " -o " + executeDir + version + "_finst__" + methodsFile + "__" + method + ".exe"
					+ " -I" + sourceDir
					+ " -lm"
					;
			
			code.append(instrumentCommand + "\n");
			code.append("echo script: " + version + "\n");
			code.append("export VERSIONSDIR=" + executeDir + "\n");
			code.append("export OUTPUTSDIR=" + outputDir + method + "/\n");
			code.append("export TRACESDIR=" + traceDir + method + "/\n");
			code.append(startTimeCommand + "\n");
			
			for (Iterator it = failingTests.iterator(); it.hasNext();) {
				int index = (Integer) it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
				code.append("$VERSIONSDIR/" + version + "_finst__" + methodsFile + "__" + method + ".exe ");//executables
				code.append(inputsMap.get(index));//parameters
				code.append(" >& $OUTPUTSDIR/o" + index + ".fout\n");//output file
			}
			
			for (Iterator it = passingTests.iterator(); it.hasNext();) {
				int index = (Integer) it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
				code.append("$VERSIONSDIR/" + version + "_finst__" + methodsFile + "__" + method + ".exe ");//executables
				code.append(inputsMap.get(index));//parameters
				code.append(" >& $OUTPUTSDIR/o" + index + ".pout\n");//output file
			}
			
			code.append(endTimeCommand + " >& $OUTPUTSDIR/time\n");
			code.append("tTime=$((tTime+time))\n");
			code.append("rm $OUTPUTSDIR/o*out\n");
			if (i != num - 1) {
				code.append("rm $TRACESDIR/o*profile\n");
				code.append("\n\n");
			}
			
			printToFile(code.toString(), scriptDir, version + "_fg_a" + methodsFile + ".sh");
			code = new StringBuilder();
			
		}
		code.append("echo \"Average time in seconds: $((tTime/1000000000/" + num + ")) \nTime in milliseconds: $((tTime/1000000/" + num + "))\"" +
				" >& " + outputDir + "time\n");
		printToFile(code.toString(), scriptDir, version + "_fg_a" + methodsFile + ".sh");
	}


	@Override
	protected void mkOutDir() {
//		File aod = new File(GenSiemensScriptsClient.rootDir + subject + "/outputs/versions/" + version + "/fine-grained-adaptive/");
//		if(aod.isDirectory() && aod.exists()){
//			boolean flag = FileUtility.removeDirectory(aod);
//			assert(flag == true);
//		}
//		File atd = new File(GenSiemensScriptsClient.rootDir + subject + "/traces/" + version + "/fine-grained-adaptive/");
//		if(atd.isDirectory() && atd.exists()){
//			boolean flag = FileUtility.removeDirectory(atd);
//			assert(flag == true);
//		}
//		
//		File od = new File(outputDir);
//		if(od.isDirectory() && od.exists()){
//			boolean flag = FileUtility.removeDirectory(od);
//			assert(flag == true);
//		}
//		
//		File td = new File(traceDir);
//		if(td.isDirectory() && td.exists()){
//			boolean flag = FileUtility.removeDirectory(td);
//			assert(flag == true);
//		}
		
		for(String method: methods){
			//make directory for outputs
			File fo = new File(outputDir + method + "/");
			if(!fo.exists()){
				fo.mkdirs();
			}
			
			//make directory for traces
			File ft = new File(traceDir + method + "/");
			if(!ft.exists()){
				ft.mkdirs();
			}
		}
	}
	
}
