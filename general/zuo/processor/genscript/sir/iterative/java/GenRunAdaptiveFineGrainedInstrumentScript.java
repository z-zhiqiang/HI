package zuo.processor.genscript.sir.iterative.java;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.processor.genscript.client.iterative.java.JavaGenSirScriptClient;
import zuo.util.file.FileCollection;
import zuo.util.file.FileUtility;


public class GenRunAdaptiveFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	public final static String Delimiter = "-";
	
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
		int num = methods.size();
		StringBuffer code = new StringBuffer();
		code.append("ttTime=0\n");
		for(int i = 0; i < num; i++){
			String method = transform(methods.get(i));
//			System.out.println(method);
			
			String paras = " -sampler-scheme=branches -sampler-scheme=returns -sampler-scheme=scalar-pairs"
					+ " -sampler-include-method=" + method
					+ " -sampler-out-sites=" + executeDir + method +  "/output.sites"
//						+ " -cp " + executeDir 
					+ " -process-dir " + executeDir 
					+ " -d " + executeDir + method + "/"
					+ "\n";
			
			String counterCommand = "java -ea -cp " + JavaGenSirScriptClient.jsampler + ":" + executeDir + " edu.uci.jsampler.client.JCounter" + paras; 
			String rmCommand = "rm -rf " + executeDir + method + "/\n";
			String samplerCommand = "java -ea -cp " + JavaGenSirScriptClient.jsampler + ":" + executeDir + " edu.uci.jsampler.client.JSampler" + paras;
			String set_classpath = "unset CLASSPATH\nexport CLASSPATH=" + executeDir + method + "/:" + JavaGenSirScriptClient.jsampler + "\n";
			
			code.append(compileCommand + counterCommand + rmCommand + samplerCommand + set_classpath + "\n");
			code.append("echo script: " + subVersion + "\n");
			code.append("export VERSIONSDIR=" + executeDir + "\n");
			code.append("export TRACESDIR=" + traceDir + method + "/\n");
			
//			stmts(code, method);
//			code.append(startTimeCommand + "\n");
			code.append("tTime=0\n");
			for(int j = 0; j < ROUNDS; j++){
				stmts(code, method);
			}
//			code.append(endTimeCommand + " > " + outputDir + method + "/time 2>&1\n");
			code.append("echo \"Time in seconds: $((tTime/1000000000)) \nTime in milliseconds: $((tTime/1000000))\""  + " > " + outputDir + method + "/time 2>&1\n");
			
			code.append("ttTime=$((ttTime+tTime))\n");
			code.append("cd " + scriptDir + "\n");
			code.append("rm ../outputs/*\n");
			code.append("rm -rf $TRACESDIR/\n");
			
			code.append(rmCommand);
			code.append("\n\n");
		}
		
		code.append("echo \"Average time in seconds: $((ttTime/1000000000/" + num + ")) \nTime in milliseconds: $((ttTime/1000000/" + num + "))\"" +
				" > " + outputDir + "time 2>&1\n");
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_fg_a.sh");
	}


	private String transform(String string) {
		return string.replaceAll(" ", Delimiter)
				.replaceAll("\\(", Delimiter).replaceAll("\\)", Delimiter)
				.replaceAll(":", Delimiter)
				.replaceAll("<", Delimiter).replaceAll(">", Delimiter);
//		return "\"" + string + "\"";
	}

	private void stmts(StringBuffer code, String method) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(addTimingCode(inputsMap.get(index)));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(addTimingCode(inputsMap.get(index)));
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
			method = transform(method);

			File fe = new File(executeDir + method);
			if(!fe.exists()){
				fe.mkdirs();
			}
//			FileUtility.removeDirectory(fe);
			
			//make directory for outputs
			File fo = new File(outputDir + method);
			if(!fo.exists()){
				fo.mkdirs();
			}
//			FileUtility.removeDirectory(fo);
			
			//make directory for traces
			File ft = new File(traceDir + method);
			if(!ft.exists()){
				ft.mkdirs();
			}
//			FileUtility.removeDirectory(ft);
		}
	}

}
