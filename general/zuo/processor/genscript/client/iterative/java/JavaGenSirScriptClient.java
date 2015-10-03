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
import zuo.processor.genscript.sir.iterative.java.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllScript;
import zuo.processor.genscript.sir.iterative.java.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunSampledFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunSubjectScript;
import zuo.processor.genscript.sir.iterative.java.GenRunVersionsScript;
import zuo.processor.splitinputs.SirSplitInputs;
import zuo.util.file.FileUtility;

public class JavaGenSirScriptClient {
	public final static String rootDir = "/home/icuzzq/Research/Automated_Debugging/Subjects/";
	
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
	final String vsexecuteDir;
	final String vaexecuteDir;
	
	final String scriptDir;
	
	String compileSubject;
	String compileVersion;
	String compileFGInstrument;
	String compileCGInstrument;
	
	public final static String outCompFile = "comp.out";
    final static Map<Integer, Fault> faults = new HashMap<Integer, Fault>();
	
	public JavaGenSirScriptClient(String sub, String srcName, String ver, String subVer){
		subject = sub;
		sourceName = srcName;
		version = ver;
		subVersion = subVer;
		
		inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
		inputCompScript = rootDir + subject + "/scripts/" + subject + "Comp.sh";
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		inputsCompMapFile = rootDir + subject + "/testplans.alt/" + "inputsComp.map";

		
		ssourceDir = rootDir + subject + "/versions.alt/component/orig/" + version + "/";
		
		sexecuteDir = rootDir + subject + "/source/" + version + "/";
		soutputDir = rootDir + subject + "/outputs.alt/" + version + "/" + subject + "/";
		
		vsourceDir = rootDir + subject + "/versions.alt/component/seeded/" + version + "/";
		
		vexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/";
		vsexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/sampled/";
		vaexecuteDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/adaptive/";
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
		
//		compileSubject = genCompileSubjectCommand(sexecuteDir, 0);
//		
//		compileVersion = "gcc " 
//				+ vsourceDir + sourceName + ".c"
//				+ " $COMPILE_PARAMETERS"
//				+ " -o " + vexecuteDir + version + ".exe"
//				;
		compileFGInstrument = "sampler-cc "
				+ "-fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs "
				+ "-fno-sample "
				+ vsourceDir + sourceName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ " -o " + vexecuteDir + subVersion + "_finst.exe"
				;
		compileCGInstrument = "sampler-cc "
				+ "-fsampler-scheme=function-entries "
				+ "-fno-sample "
				+ vsourceDir + sourceName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ " -o " + vexecuteDir + subVersion + "_cinst.exe"
				;
	}
	
	private String genCompileSubjectCommand(String executeDir, int index){
		String mkdir = "mkdir " + executeDir + "siena/\n";
		String siena_app = "cp -r " + rootDir + subject + "/versions.alt/application/*" + " " + executeDir + "siena/\n";
		String siena_subject = "cp -r " + vsourceDir + "* " + executeDir + "siena/\n";
		String seedNoFaults = seedFaultsCommand(executeDir, index);
		String compileCd = "cd " + executeDir + "\n";
		String compileCC = "find siena/ -name *.java | javac @/dev/stdin/\n";
		String set_classpath = "unset CLASSPATH\nexport CLASSPATH=" + executeDir + ":" + rootDir + subject + "/testdrivers/\n";
		
		return mkdir + siena_app + siena_subject + seedNoFaults + compileCd + compileCC + set_classpath;
	}
	
	private String seedFaultsCommand(String executeDir, int index) {
		StringBuilder builder = new StringBuilder();
		String command = "java -cp /home/icuzzq/bin/ EqualizeLineNumbers ";
		builder.append("cd " + executeDir + "siena/\n");
		for(int i: faults.keySet()){
			String fault_file = faults.get(i).getFault_file();
			builder.append(command).append(fault_file).append(" ").append(0).append(" ").append(fault_file.replaceAll("cpp", "java")).append("\n");
		}
		if(index != 0){
			Fault fault = faults.get(index);
			builder.append(command).append(fault.getFault_file()).append(" ").append(fault.getFault_order()).append(" ").append(fault.getFault_file().replaceAll("cpp", "java")).append("\n");
		}
		builder.append("echo seeding faults done!\n");
		
		return builder.toString();
	}



	public static void main(String[] args) throws IOException {
		String[][] subjects = {
				{"siena", "all", "7"}, // grep v1_subv14
		};
		for (int i = 0; i < subjects.length; i++) {
			for(int j = 1; j <= Integer.parseInt(subjects[i][2]); j++){
				JavaGenSirScriptClient gc = new JavaGenSirScriptClient(subjects[i][0], subjects[i][1], "v" + j, null);
				gc.gen();
				faults.clear();
			}
		}
		
	}



	private void gen() throws IOException {
		AbstractGenRunScript gs;
		AbstractGenRunAllScript ga;
		String setEnv = "export experiment_root=" + rootDir + "\n";
		String sf = rootDir + subject + "/testplans.alt/universe";
		
		//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
		
		//generate test scripts
		String[] argvs = {"-sf", sf, "-sn", inputScript, "-en", AbstractGenRunScript.EXE, "-ed", rootDir + subject, "-tg", "bsh", "-nesc", "-j"};
		MakeTestScript.main(argvs);
		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
		
		String[] argvsC = {"-sf", sf, "-sn", inputCompScript, "-en", AbstractGenRunScript.EXE, "-ed", rootDir + subject, "-c", soutputDir, "-tg", "bsh", "-nesc", "-j"};
		MakeTestScript.main(argvsC);
		FileUtility.constructSIRInputsMapFile(inputCompScript, inputsCompMapFile);//read inputsMap
		
		//generate run subject and subversion scripts
		gs = new GenRunSubjectScript(subject, sourceName, version, setEnv + genCompileSubjectCommand(sexecuteDir, 0), ssourceDir, sexecuteDir, soutputDir, scriptDir);
		gs.genRunScript();
		
		for(int index: faults.keySet()){
			JavaGenSirScriptClient gc = new JavaGenSirScriptClient(subject, sourceName, version, "subv" + index);
			
			System.out.println("generating run script for subVersion" + index);
			new GenRunVersionsScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + gc.genCompileSubjectCommand(gc.vexecuteDir, index), gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
		}
		
		//generate run all scripts  
		assert(FileUtility.readInputsMap(inputsMapFile).size() == FileUtility.readInputsMap(inputsCompMapFile).size());
		ga = new GenRunAllScript(version, subject, scriptDir, faults.size());
		ga.genRunAllScript();
		
		
		//=========================================================================================================================================================================//
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			JavaGenSirScriptClient gc = new JavaGenSirScriptClient(subject, sourceName, version, "subv" + index);
			
			SirSplitInputs split = new SirSplitInputs(gc.inputsMapFile, gc.vexecuteDir, outCompFile);
			split.split();
			//collect the triggered faults
			if(split.getFailingTests().size() > 1 
//					&& !(gc.subject.equals("grep") && gc.version.equals("v1") && gc.subVersion.equals("subv14"))
			){
				subs.add(index);
//				assert(new File(gc.vexecuteDir).listFiles().length == 13);
				
//				String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
				System.out.println("generating run instrument script for subv" + index);
				
				gs = new GenRunFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + gc.compileFGInstrument, gc.vsourceDir, gc.vexecuteDir, 
						gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
				gs.genRunScript();
				gs = new GenRunCoarseGrainedInstrumentScript(gc.subject,gc.sourceName, gc.version, gc.subVersion, setEnv + export + gc.compileCGInstrument, gc.vsourceDir, gc.vexecuteDir, 
						gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
				gs.genRunScript();
				
				
				gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + export, gc.vsourceDir, gc.vsexecuteDir, gc.vsfoutputDir, 
						gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 1);
				gs.genRunScript();
				gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + export, gc.vsourceDir, gc.vsexecuteDir, gc.vsfoutputDir, 
						gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 100);
				gs.genRunScript();
				gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + export, gc.vsourceDir, gc.vsexecuteDir, gc.vsfoutputDir, 
						gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 10000);
				gs.genRunScript();
				
				
				gs = new GenRunAdaptiveFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + export, gc.vsourceDir, gc.vaexecuteDir, 
						gc.vafoutputDir, gc.scriptDir, gc.vaftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", "full");
				gs.genRunScript();
			}
			
		}
//		
//		//generate run all instrumented triggered subversion scripts
//		ga = new GenRunAllInstrumentedScript(version, subject, scriptDir, subs);
//		ga.genRunAllScript();
//
//		//generate run all sampled instrumented triggered subversion scripts
//		ga = new GenRunAllSampledInstrumentedScript(version, subject, scriptDir, subs, 1);
//		ga.genRunAllScript();
//		ga = new GenRunAllSampledInstrumentedScript(version, subject, scriptDir, subs, 100);
//		ga.genRunAllScript();
//		ga = new GenRunAllSampledInstrumentedScript(version, subject, scriptDir, subs, 10000);
//		ga.genRunAllScript();
//		
//		//generate run all adaptive instrumented triggered subversion scripts
//		ga = new GenRunAllAdaptiveInstrumentedScript(version, subject, scriptDir, subs);
//		ga.genRunAllScript();
	}
	
	
	private void readFaults(){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(vsourceDir, "FaultSeeds.h")));
			String line;
			int index = 0;
			while((line = reader.readLine()) != null){
				assert(line.split(" ").length == 3 && line.split(" ")[0].startsWith("F"));
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
