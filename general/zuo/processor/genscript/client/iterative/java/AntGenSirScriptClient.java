package zuo.processor.genscript.client.iterative.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sir.mts.MakeTestScript;
import zuo.processor.genscript.sir.iterative.java.AbstractGenRunAllScript;
import zuo.processor.genscript.sir.iterative.java.AbstractGenRunScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllAdaptiveInstrumentedScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllInstrumentedScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllSampledInstrumentedScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllScript;
import zuo.processor.genscript.sir.iterative.java.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunSampledFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunSubjectScript;
import zuo.processor.genscript.sir.iterative.java.GenRunVersionsScript;
import zuo.processor.splitinputs.SirSplitInputs;
import zuo.util.file.FileUtility;

public class AntGenSirScriptClient {
	public final static String RUNNER = "-mx256m -classpath ${CLASSPATH} "
			+ "-Dant.home=bootstrap -Dbuild.tests=build/classes -Dtests-classpath.value=${CLASSPATH} junit.textui.TestRunner";
	
	public final static String rootPath = "/home/icuzzq/";
	public final static String rootDir = rootPath + "Research/Automated_Debugging/Subjects/";
	
	public final static String jsampler = rootPath + "bin/JSampler.jar";
	public final static String seeder = "java -cp " + rootPath + "bin/ EqualizeLineNumbers ";
	
	public final String subject;
	public final String sourceName;
	public final String version;
	public final String subVersion;
	public final String inputScript;
	public final String inputCompScript;
	
	public final String inputsMapFile;
	public final String inputsCompMapFile;
	
	final String ssourceDir;
	final String sexecuteDir;
	final String soutputDir;
	
	final String vsourceDir;
	final String vexecuteDir;
	final String voutputDir;
	final String vfoutputDir;
	final String vcoutputDir;
	final String vftraceDir;
	final String vctraceDir;
	final String vafoutputDir;
	final String vaftraceDir;
	final String vsfoutputDir;
	final String vsftraceDir;
	
	final String scriptDir;
	
	String compileSubject;
	String compileVersion;
	String compileFGInstrument;
	String compileCGInstrument;
	
	public final static String outCompFile = "comp.out";
    final static Map<Integer, Fault> faults = new HashMap<Integer, Fault>();
	
	public AntGenSirScriptClient(String sub, String srcName, String ver, String subVer){
		subject = sub;
		sourceName = srcName;
		version = ver;
		subVersion = subVer;
		
		inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
		inputCompScript = rootDir + subject + "/scripts/" + subject + "Comp.sh";
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		inputsCompMapFile = rootDir + subject + "/testplans.alt/" + "inputsComp.map";

		
		ssourceDir = rootDir + subject + "/versions.alt/orig/" + version + "/";
		
		sexecuteDir = rootDir + subject + "/source/" + version + "/";
		soutputDir = rootDir + subject + "/outputs.alt/" + version + "/" + subject + "/";
		
		vsourceDir = rootDir + subject + "/versions.alt/seeded/" + version + "/";
		
		vexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/";
		voutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/outputs/";
		vfoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained/";
		vsfoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained-sampled-";
		vafoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained-adaptive/";
		vcoutputDir = rootDir + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/coarse-grained/";
		vftraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained/";
		vsftraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained-sampled-";
		vaftraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/fine-grained-adaptive/";
		vctraceDir = rootDir + subject + "/traces/" + version + "/" + subVersion + "/coarse-grained/";
		
		scriptDir = rootDir + subject + "/scripts/";
		
		initialCompileCommand();
	}



	private void initialCompileCommand() {
//		//read faults (subversion)
		readFaults();
		System.out.println(faults.toString());
	}
	
	private String genCompileCommand(String executeDir, String targetExecuteDir, int index, String vsource){
		String setEnv = "export experiment_root=" + rootDir + "\n";
		String rmdir = "rm -rf " + executeDir + "*\n";
		String mkdir = "mkdir -p " + executeDir + "\n";
		String siena_subject = "cp -r " + vsource + "/* " + executeDir + "\n"
				+ "cp -rf " + executeDir + "build/testcases/* " + executeDir + "build/classes/\n"
				+ "rm -rf " + executeDir + "build/testcases/*\n";
		String test_wrapper = ""
				+ "cp -rf " + rootDir + subject + "/junit/ " + executeDir + "build/classes/\n"
				+ "zip -d " + executeDir + "lib/junit3.8.1.jar junit/textui/TestRunner.class\n";
		String cdCm = "cd " + executeDir + "\n";
		String set_classpath = "unset CLASSPATH\nexport CLASSPATH=build/classes:build/testcases:src/testcases:src/etc/testcases:lib/xercesImpl.jar:lib/xml-apis.jar:lib/junit3.8.1.jar:$JAVA_HOME/lib/tools.jar\n";
		String cpCm = executeDir.equals(targetExecuteDir) ? "" : "rm -rf " + targetExecuteDir + "\nmkdir -p " + targetExecuteDir + "\ncp -r " + executeDir + "* " + targetExecuteDir + "\n";
		
		return setEnv + rmdir + mkdir + siena_subject + test_wrapper + cdCm + set_classpath + cpCm;
	}
	

	public static void main(String[] args) throws IOException {
		String[][] subjects = {
				{"apache-ant", null, "8"}, // grep v1_subv14
		};
		for (int i = 0; i < subjects.length; i++) {
			for(int j = 5; j <= Integer.parseInt(subjects[i][2]); j++){
				AntGenSirScriptClient gc = new AntGenSirScriptClient(subjects[i][0], subjects[i][1], "v" + j, null);
				gc.gen();
				faults.clear();
			}
		}
		
	}



	private void gen() throws IOException {
		AbstractGenRunScript gs;
		AbstractGenRunAllScript ga;
		String sf = rootDir + subject + "/testplans.alt/" + version + "/" + version + ".class.junit.universe.all";
		
		//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
		
		//generate test scripts
		String[] argvs = {"-sf", sf, "-sn", inputScript, "-en", RUNNER, "-ed", rootDir + subject, "-tg", "bsh", "-nesc", "-j"};
		MakeTestScript.main(argvs);
		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
		
		String[] argvsC = {"-sf", sf, "-sn", inputCompScript, "-en", RUNNER, "-ed", rootDir + subject, "-c", soutputDir, "-tg", "bsh", "-nesc", "-j"};
		MakeTestScript.main(argvsC);
		FileUtility.constructSIRInputsMapFile(inputCompScript, inputsCompMapFile);//read inputsMap
		
		//generate run subject and subversion scripts
		gs = new GenRunSubjectScript(subject, sourceName, version, genCompileCommand(sexecuteDir, sexecuteDir, 0, rootDir + subject + "/ant-bin/noseed_" + version), ssourceDir, sexecuteDir, soutputDir, scriptDir);
		gs.genRunScript();
		
		for(int index: faults.keySet()){
			AntGenSirScriptClient gc = new AntGenSirScriptClient(subject, sourceName, version, "subv" + index);
			
			System.out.println("generating run script for subVersion" + index);
			String vexecuteDir_version = gc.vexecuteDir + "version/";
			new GenRunVersionsScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, gc.genCompileCommand(sexecuteDir, vexecuteDir_version, index, rootDir + subject + "/ant-bin/seeded_" + version + "_" + index), gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
		}
		
		//generate run all scripts  
		assert(FileUtility.readInputsMap(inputsMapFile).size() == FileUtility.readInputsMap(inputsCompMapFile).size());
		ga = new GenRunAllScript(version, subject, scriptDir, faults.size());
		ga.genRunAllScript();
		
		
		//=========================================================================================================================================================================//
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			AntGenSirScriptClient gc = new AntGenSirScriptClient(subject, sourceName, version, "subv" + index);
			
			SirSplitInputs split = new SirSplitInputs(gc.inputsMapFile, gc.vexecuteDir, outCompFile);
			split.split();
			//collect the triggered faults
			if(split.getFailingTests().size() > 1 && split.getPassingTests().size() > 0
//					&& !(gc.subject.equals("grep") && gc.version.equals("v1") && gc.subVersion.equals("subv14"))
			){
				subs.add(index);
				
				System.out.println("generating run instrument script for subv" + index);
				
				String vexecuteDir_fg = gc.vexecuteDir + "fine-grained/";
				gs = new GenRunFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, gc.genInstrumentCommand(vexecuteDir_fg, index, "fg"), gc.vsourceDir, vexecuteDir_fg, 
						gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
				gs.genRunScript();
				
				String vexecuteDir_cg = gc.vexecuteDir + "coarse-grained/";
				gs = new GenRunCoarseGrainedInstrumentScript(gc.subject,gc.sourceName, gc.version, gc.subVersion, gc.genInstrumentCommand(vexecuteDir_cg, index, "cg"), gc.vsourceDir, vexecuteDir_cg, 
						gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
				gs.genRunScript();
				
				
				String vexecuteDir_s1 = gc.vexecuteDir + "sample_1/";
				gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, gc.genInstrumentCommand(vexecuteDir_s1, index, "sample"), gc.vsourceDir, vexecuteDir_s1, gc.vsfoutputDir, 
						gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 1);
				gs.genRunScript();
				String vexecuteDir_s100 = gc.vexecuteDir + "sample_100/";
				gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, gc.genInstrumentCommand(vexecuteDir_s100, index, "sample"), gc.vsourceDir, vexecuteDir_s100, gc.vsfoutputDir, 
						gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 100);
				gs.genRunScript();
				String vexecuteDir_s10000 = gc.vexecuteDir + "sample_10000/";
				gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, gc.genInstrumentCommand(vexecuteDir_s10000, index, "sample"), gc.vsourceDir, vexecuteDir_s10000, gc.vsfoutputDir, 
						gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 10000);
				gs.genRunScript();
				
				
//				String vexecuteDir_adaptive = gc.vexecuteDir + "adaptive/";
//				gs = new GenRunAdaptiveFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, gc.genCompileCommand(vexecuteDir_adaptive, index), gc.vsourceDir, vexecuteDir_adaptive, 
//						gc.vafoutputDir, gc.scriptDir, gc.vaftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", gc.version + "_" + gc.subVersion + "_C_LESS_FIRST_1_average");
//				gs.genRunScript();
			}
			
		}
		
		//generate run all instrumented triggered subversion scripts
		ga = new GenRunAllInstrumentedScript(version, subject, scriptDir, subs);
		ga.genRunAllScript();

		//generate run all sampled instrumented triggered subversion scripts
		ga = new GenRunAllSampledInstrumentedScript(version, subject, scriptDir, subs, 1);
		ga.genRunAllScript();
		ga = new GenRunAllSampledInstrumentedScript(version, subject, scriptDir, subs, 100);
		ga.genRunAllScript();
		ga = new GenRunAllSampledInstrumentedScript(version, subject, scriptDir, subs, 10000);
		ga.genRunAllScript();
		
//		//generate run all adaptive instrumented triggered subversion scripts
//		ga = new GenRunAllAdaptiveInstrumentedScript(version, subject, scriptDir, subs);
//		ga.genRunAllScript();
	}
	
	
	private String genInstrumentCommand(String executeDir, int index, String string) {
		String paras = null;
		
		if(string.equals("fg")){
			paras = " -sampler-scheme=branches -sampler-scheme=returns -sampler-scheme=scalar-pairs"
					+ " -sampler-out-sites=" + executeDir + "output.sites"
					+ " -cp build/classes:src/testcases:src/etc/testcases:lib/xercesImpl.jar:lib/xml-apis.jar:lib/junit3.8.1.jar:$JAVA_HOME/lib/tools.jar"
					+ " -process-dir " + executeDir + "build/classes/ "
					+ " -d " + executeDir + "instrumented/"
					+ "\n";
		}
		else if(string.equals("cg")){
			paras = " -sampler-scheme=method-entries"
					+ " -sampler-out-sites=" + executeDir + "output.sites"
					+ " -cp build/classes:src/testcases:src/etc/testcases:lib/xercesImpl.jar:lib/xml-apis.jar:lib/junit3.8.1.jar:$JAVA_HOME/lib/tools.jar"
					+ " -process-dir " + executeDir + "build/classes/ " 
					+ " -d " + executeDir + "instrumented/"
					+ "\n";
		}
		else if(string.equals("sample")){
			paras = " -sampler"
					+ " -sampler-scheme=branches -sampler-scheme=returns -sampler-scheme=scalar-pairs"
					+ " -sampler-out-sites=" + executeDir + "output.sites"
					+ " -cp build/classes:src/testcases:src/etc/testcases:lib/xercesImpl.jar:lib/xml-apis.jar:lib/junit3.8.1.jar:$JAVA_HOME/lib/tools.jar"
					+ " -process-dir " + executeDir + "build/classes/ " 
					+ " -d " + executeDir + "instrumented/"
					+ "\n";
		}
		else{
			System.err.println("Wrong para!");
		}
		
		String compileCommand = genCompileCommand(executeDir, executeDir, index, rootDir + subject + "/ant-bin/seeded_" + version + "_" + index);
		String samplerCommand = "java -ea -cp " + jsampler + " edu.uci.jsampler.client.JSampler" + " -validate" + paras;
		String cpCommand = "rm -f " + executeDir + "instrumented/*.jimple\n"
				+ "cp -rf " + executeDir + "instrumented/* " + executeDir + "build/classes/\n"
				+ "rm -rf " + executeDir + "instrumented/\n";
		String set_classpath = "unset CLASSPATH\nexport CLASSPATH=" + jsampler + ":" + executeDir + "build/classes:src/testcases:src/etc/testcases:lib/xercesImpl.jar:lib/xml-apis.jar:lib/junit3.8.1.jar:$JAVA_HOME/lib/tools.jar\n";
		
		return compileCommand + samplerCommand + cpCommand + set_classpath;
	}



	private void readFaults(){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(rootDir + subject + "/ant-bin/noseed_" + version + "/src/main/org/apache/tools/ant/", "FaultSeeds.h.fl")));
			String line;
			int index = 0;
			while((line = reader.readLine()) != null){
				assert(line.split(" ").length == 3 
//						&& line.split(" ")[0].startsWith("F")
						);
				faults.put(++index, new Fault(line.split(" ")[1], line.split(" ")[2]));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
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
	
	
	static class Fault{
		final String fault_order;
		final String fault_file;
		
		public Fault(String order, String file){
			this.fault_order = order;
			this.fault_file = file;
		}

		public String getFault_order() {
			return fault_order;
		}

		public String getFault_file() {
			return fault_file;
		}
		
		public String toString(){
			return this.fault_order + "\t" + this.fault_file;
		}
	}

}
