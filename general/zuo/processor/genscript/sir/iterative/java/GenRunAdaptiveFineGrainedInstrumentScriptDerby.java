package zuo.processor.genscript.sir.iterative.java;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.processor.genscript.client.iterative.java.AbstractGenSirScriptClient;
import zuo.util.file.FileCollection;
import zuo.util.file.FileUtility;


public class GenRunAdaptiveFineGrainedInstrumentScriptDerby extends AbstractGenRunScript implements GenRunInstrumentScript {
	public final static String Delimiter = "-";
	
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private List<String> methods;
	
	public GenRunAdaptiveFineGrainedInstrumentScriptDerby(String sub, String srcN, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, String methodsF) {
		super(sub, srcN, ver, subV, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		
		this.methods = FileCollection.readMethods(new File(AbstractGenSirScriptClient.rootDir + subject + "/FunctionList/", methodsF));
		mkOutDir();
	}

	@Override
	public void genRunScript() {
		int num = methods.size();
		StringBuffer code = new StringBuffer();
		code.append(compileCommand);
		code.append("tTime=0\n");
		for(int i = 0; i < num; i++){
			String method = GenRunAdaptiveFineGrainedInstrumentScript.transform(methods.get(i));
			
			String paras = " -sampler-scheme=branches -sampler-scheme=returns -sampler-scheme=scalar-pairs"
					+ " -sampler-include-method=" + method
					+ " -sampler-out-sites=" + executeDir + method +  "/output.sites"
					+ " -cp  classes:tools/java/xml-apis.jar:tools/java/xercesImpl:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:"
						+ "xalan.jar:serializer.jar:$JAVA_HOME/jre/lib/jce.jar:tools/java/geronimo-spec-servlet-2.4-rc4.jar:" 
						+ AbstractGenSirScriptClient.rootDir + subject + "/ant/lib/ant.jar:tools/java/junit.jar:tools/java/jakarta-oro-2.0.8.jar"
					+ " -process-dir " + "classes/ "
					+ " -d " + executeDir + method + "/instrumented/"
					+ "\n";
			
			String samplerCommand = "java -ea -cp " + AbstractGenSirScriptClient.jsampler + " edu.uci.jsampler.client.JSampler" + paras;
			String cpCommand = "cp -rf " + executeDir + "source/* " + executeDir + method + "/\n"
					+ "rm -f " + executeDir + method + "/instrumented/*.jimple\n"
					+ "cp -rf " + executeDir + method + "/instrumented/* " + executeDir + method + "/classes/\n"
					+ "rm -rf " + executeDir + method + "/instrumented/\n";
			String cdCommand = "cd " + executeDir + method + "/\n";
			String set_classpath = "unset CLASSPATH\nexport CLASSPATH=" + AbstractGenSirScriptClient.jsampler + ":" + "classes:tools/java/xml-apis.jar:tools/java/xercesImpl:$JAVA_HOME/lib/tools.jar:$JAVA_HOME/jre/lib/rt.jar:"
					+ "xalan.jar:serializer.jar:$JAVA_HOME/jre/lib/jce.jar:tools/java/geronimo-spec-servlet-2.4-rc4.jar:" 
					+ AbstractGenSirScriptClient.rootDir + subject + "/ant/lib/ant.jar:tools/java/junit.jar:tools/java/jakarta-oro-2.0.8.jar\n";
			code.append(samplerCommand + cpCommand + cdCommand + set_classpath + "\n");
			code.append("echo script: " + subVersion + "\n");
			code.append("export VERSIONSDIR=" + executeDir + "\n");
			code.append("export TRACESDIR=" + traceDir + method + "/\n");
			
//			stmts(code, method);
			code.append(startTimeCommand + "\n");
			for(int j = 0; j < ROUNDS; j++){
				stmts(code, method);
			}
			code.append(endTimeCommand + " > " + outputDir + method + "/time 2>&1\n");
			
			code.append("tTime=$((tTime+time))\n");
			code.append("rm -f " + scriptDir + "../outputs/*\n");
			code.append("rm -rf $TRACESDIR/\n");
			
			code.append("cd " + executeDir + "source/\n");
			code.append("rm -rf " + executeDir + method + "/\n");
			code.append("\n\n");
		}
		
		code.append("echo \"Average time in seconds: $((tTime/1000000000/" + num + ")) \nTime in milliseconds: $((tTime/1000000/" + num + "))\"" +
				" > " + outputDir + "time 2>&1\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_a.sh");
	}


	private void stmts(StringBuffer code, String method) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(GenRunFineGrainedInstrumentScriptDerby.insertSetEnv(inputsMap.get(index)));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(GenRunFineGrainedInstrumentScriptDerby.insertSetEnv(inputsMap.get(index)));
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
			method = GenRunAdaptiveFineGrainedInstrumentScript.transform(method);

			File fe = new File(executeDir + method);
			FileUtility.removeDirectory(fe);
			if(!fe.exists()){
				fe.mkdirs();
			}
			
			//make directory for outputs
			File fo = new File(outputDir + method);
			FileUtility.removeDirectory(fo);
			if(!fo.exists()){
				fo.mkdirs();
			}
			
			//make directory for traces
			File ft = new File(traceDir + method);
			FileUtility.removeDirectory(ft);
			if(!ft.exists()){
				ft.mkdirs();
			}
		}
	}

}
