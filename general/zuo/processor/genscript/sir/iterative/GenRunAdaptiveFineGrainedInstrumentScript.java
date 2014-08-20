package zuo.processor.genscript.sir.iterative;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileCollection;
import zuo.util.file.FileUtility;


public class GenRunAdaptiveFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private List<String> methods;
	
	public GenRunAdaptiveFineGrainedInstrumentScript(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, String methodsF) {
		super(sub, srcN, ver, subV, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		
		this.methods = FileCollection.readMethods(new File(executeDir, methodsF));
		mkOutDir();
	}

	@Override
	public void genRunScript() {
		String includeC = "";
		String paraC = "";
		if(subject.equals("gzip")){
			paraC = " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1";
		}
		if(subject.equals("grep")){
			includeC = " -I" + sourceDir;
		}
		
		int num = methods.size();
		StringBuffer code = new StringBuffer();
		code.append("tTime=0\n");
		String instrumentCommand;
		for(int i = 0; i < num; i++){
			String method = methods.get(i);
			instrumentCommand = compileCommand 
					+ "sampler-cc "
					+ "-fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs "
					+ "-fno-sample "
					+ "-finclude-function=" + method + " -fexclude-function=* "
					+ sourceDir + srcName + ".c" 
					+ " $COMPILE_PARAMETERS"
					+ paraC
					+ " -o " + executeDir + subVersion + "_finst__" + method + ".exe"
					+ includeC//for grep
					;
			
			code.append(instrumentCommand + "\n");
			code.append("echo script: " + subVersion + "\n");
			code.append("export VERSIONSDIR=" + executeDir + "\n");
			code.append("export TRACESDIR=" + traceDir + method + "/\n");
			
			stmts(code, method);
			code.append(startTimeCommand + "\n");
			for(int j = 0; j < ROUNDS; j++){
				stmts(code, method);
			}
			code.append(endTimeCommand + " >& " + outputDir + method + "/time\n");
			
			code.append("tTime=$((tTime+time))\n");
			code.append("rm ../outputs/*\n");
			code.append("rm -rf $TRACESDIR/\n");
			code.append("\n\n");
		}
		
		code.append("echo \"Average time in seconds: $((tTime/1000000000/" + num + ")) \nTime in milliseconds: $((tTime/1000000/" + num + "))\"" +
				" >& " + outputDir + "time\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_a.sh");
	}


	private void stmts(StringBuffer code, String method) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst__" + method + ".exe "));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_finst__" + method + ".exe "));
			code.append("\n");
		}
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
