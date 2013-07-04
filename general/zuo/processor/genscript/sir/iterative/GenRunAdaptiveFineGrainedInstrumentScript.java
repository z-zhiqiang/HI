package zuo.processor.genscript.sir.iterative;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import zuo.processor.functionentry.client.FunctionClient.Score;
import zuo.processor.genscript.client.iterative.GenSirScriptClient;
import zuo.util.file.FileUtility;


public class GenRunAdaptiveFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private List<String> methods;
	private Score methodsFile;
	
	public GenRunAdaptiveFineGrainedInstrumentScript(String sub, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, Score methodsF) {
		super(sub, ver, subV, cc, sD, eD, oD + methodsF + "/", scD);
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
		StringBuffer code = new StringBuffer();
		code.append("tTime=0\n");
		String instrumentCommand;
		for(int i = 0; i < num; i++){
			String method = methods.get(i);
			instrumentCommand = compileCommand 
					+ "sampler-cc -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample "
					+ "-finclude-function=" + method + " -fexclude-function=* "
					+ sourceDir + GenSirScriptClient.sourceName + ".c" 
					+ " $COMPILE_PARAMETERS"
					+ " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1"
					+ " -o " + executeDir + subVersion + "_finst__" + methodsFile + "__" + method + ".exe"
					+ " -I" + sourceDir
					+ " -lm"
					;
			
			code.append(instrumentCommand + "\n");
			code.append("echo script: " + subVersion + "\n");
			code.append("export VERSIONSDIR=" + executeDir + "\n");
			code.append("export TRACESDIR=" + traceDir + method + "/\n");
			code.append(startTimeCommand + "\n");
			
			for (Iterator it = failingTests.iterator(); it.hasNext();) {
				int index = (Integer) it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
				code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst__" + methodsFile + "__" + method + ".exe "));
				code.append("\n");
			}
			
			for (Iterator it = passingTests.iterator(); it.hasNext();) {
				int index = (Integer) it.next();
				code.append(runinfo + index + "\"\n");// running info
				code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
				code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst__" + methodsFile + "__" + method + ".exe "));
				code.append("\n");
			}
			
			code.append(endTimeCommand + " >& " + outputDir + method + "/time\n");
			code.append("tTime=$((tTime+time))\n");
			code.append("rm ../outputs/*\n");
			if(i != num - 1){
				code.append("rm $TRACESDIR/o*profile\n");
				code.append("\n\n");
			}
		}
		
		code.append("echo \"Average time in seconds: $((tTime/1000000000/" + num + ")) \nTime in milliseconds: $((tTime/1000000/" + num + "))\"" +
				" >& " + outputDir + "time\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_a" + methodsFile + ".sh");
	}


	@Override
	protected void mkOutDir() {
//		File aod = new File(GenSirScriptClient.rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained-adaptive/");
//		if(aod.isDirectory() && aod.exists()){
//			boolean flag = FileUtility.removeDirectory(aod);
//			assert(flag == true);
//		}
//		File atd = new File(GenSirScriptClient.rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained-adaptive/");
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