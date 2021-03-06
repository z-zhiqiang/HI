package zuo.processor.genscript.client.iterative;

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
import zuo.processor.genscript.sir.iterative.AbstractGenRunAllScript;
import zuo.processor.genscript.sir.iterative.AbstractGenRunScript;
import zuo.processor.genscript.sir.iterative.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.GenRunAllAdaptiveInstrumentedScript;
import zuo.processor.genscript.sir.iterative.GenRunAllInstrumentedScript;
import zuo.processor.genscript.sir.iterative.GenRunAllSampledInstrumentedScript;
import zuo.processor.genscript.sir.iterative.GenRunAllScript;
import zuo.processor.genscript.sir.iterative.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.GenRunSampledFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.GenRunSubjectScript;
import zuo.processor.genscript.sir.iterative.GenRunVersionsScript;
import zuo.processor.splitinputs.SirSplitInputs;
import zuo.util.file.FileUtility;

public class GenSirScriptClient extends AbstractGenSirScriptClient {
	
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
    final static Map<Integer, String> faults = new HashMap<Integer, String>();
	
	public GenSirScriptClient(String sub, String srcName, String ver, String subVer){
		subject = sub;
		sourceName = srcName;
		version = ver;
		subVersion = subVer;
		
		inputScript = rootDir + subject + "/scripts/" + subject + ".sh";
		inputCompScript = rootDir + subject + "/scripts/" + subject + "Comp.sh";
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		inputsCompMapFile = rootDir + subject + "/testplans.alt/" + "inputsComp.map";

		
		ssourceDir = rootDir + subject + "/versions.alt/versions.orig/" + version + "/";
		sexecuteDir = rootDir + subject + "/source/" + version + "/";
		soutputDir = rootDir + subject + "/outputs.alt/" + version + "/" + subject + "/";
		
		vsourceDir = rootDir + subject + "/versions.alt/versions.seeded/" + version + "/";
		
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
		String includeSC = "", includeVC = "";
		String paraC = "";
		if(subject.equals("gzip")){
			paraC = " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1";
		}
		if(subject.equals("grep")){
			includeSC = " -I" + ssourceDir;
			includeVC = " -I" + vsourceDir;
		}
		
		compileSubject = "gcc " 
				+ ssourceDir + sourceName + ".c" 
				+ paraC
				+ " -o " + sexecuteDir + version + ".exe" 
				+ includeSC
				;
		compileVersion = "gcc " 
				+ vsourceDir + sourceName + ".c"
				+ " $COMPILE_PARAMETERS"
				+ paraC 
				+ " -o " + vexecuteDir + version + ".exe"
				+ includeVC
				;
		compileFGInstrument = "sampler-cc "
				+ "-fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs "
				+ "-fno-sample "
				+ vsourceDir + sourceName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ paraC
				+ " -o " + vexecuteDir + subVersion + "_finst.exe"
				+ includeVC
				;
		compileCGInstrument = "sampler-cc "
				+ "-fsampler-scheme=function-entries "
				+ "-fno-sample "
				+ vsourceDir + sourceName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ paraC
				+ " -o " + vexecuteDir + subVersion + "_cinst.exe"
				+ includeVC
				;
	}
	
	
	
	public static void main(String[] args) throws IOException {
		String[][] subjects = {
				{"grep", "grep", "4"}, // grep v1_subv14
				{"gzip", "allfile", "5"},
				{"sed", "sed", "3"}, // sed v2_subv5
		};
		for (int i = 0; i < subjects.length; i++) {
			for(int j = 1; j <= Integer.parseInt(subjects[i][2]); j++){
				GenSirScriptClient gc = new GenSirScriptClient(subjects[i][0], subjects[i][1], "v" + j, null);
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

		//read faults (subversion)
		readFaults();
		System.out.println(faults.toString());
		
		//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
		
		//generate test scripts
		String[] argvs = {"-sf", sf, "-sn", inputScript, "-en", AbstractGenRunScript.EXE, "-ed", rootDir + subject, "-tg", "bsh", "-nesc"};
		MakeTestScript.main(argvs);
		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
		
		String[] argvsC = {"-sf", sf, "-sn", inputCompScript, "-en", AbstractGenRunScript.EXE, "-ed", rootDir + subject, "-c", soutputDir, "-tg", "bsh", "-nesc"};
		MakeTestScript.main(argvsC);
		FileUtility.constructSIRInputsMapFile(inputCompScript, inputsCompMapFile);//read inputsMap
		
		//generate run subject and subversion scripts
		gs = new GenRunSubjectScript(subject, sourceName, version, setEnv + compileSubject, ssourceDir, sexecuteDir, soutputDir, scriptDir);
		gs.genRunScript();
		
		for(int index: faults.keySet()){
			GenSirScriptClient gc = new GenSirScriptClient(subject, sourceName, version, "subv" + index);
			
			String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
			System.out.println("generating run script for subVersion" + index);
			new GenRunVersionsScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + export + gc.compileVersion, gc.vsourceDir, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
		}
		
		//generate run all scripts  
		assert(FileUtility.readInputsMap(inputsMapFile).size() == FileUtility.readInputsMap(inputsCompMapFile).size());
		ga = new GenRunAllScript(version, subject, scriptDir, faults.size());
		ga.genRunAllScript();
		
		
		//=========================================================================================================================================================================//
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			GenSirScriptClient gc = new GenSirScriptClient(subject, sourceName, version, "subv" + index);
			
			SirSplitInputs split = new SirSplitInputs(gc.inputsMapFile, gc.vexecuteDir, outCompFile);
			split.split();
			//collect the triggered faults
			if(split.getFailingTests().size() > 1 
					&& !(gc.subject.equals("grep") && gc.version.equals("v1") && gc.subVersion.equals("subv14"))
					&& !(gc.subject.equals("sed") && gc.version.equals("v2") && gc.subVersion.equals("subv5"))){
				subs.add(index);
//				assert(new File(gc.vexecuteDir).listFiles().length == 13);
				
				String export = "export COMPILE_PARAMETERS=-D" + faults.get(index) + "\n";
				System.out.println("generating run instrument script for subv" + index);
				
				gs = new GenRunFineGrainedInstrumentScript(gc.subject, gc.sourceName, gc.version, gc.subVersion, setEnv + export + gc.compileFGInstrument, gc.vsourceDir, gc.vexecuteDir, 
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
						gc.vafoutputDir, gc.scriptDir, gc.vaftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", gc.version + "_" + gc.subVersion + "_C_LESS_FIRST_1_average");
				gs.genRunScript();
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
		
		//generate run all adaptive instrumented triggered subversion scripts
		ga = new GenRunAllAdaptiveInstrumentedScript(version, subject, scriptDir, subs);
		ga.genRunAllScript();
	}
	
	
	private void readFaults(){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(vsourceDir, "FaultSeeds.h")));
			String line;
			int index = 0;
			while((line = reader.readLine()) != null){
				assert(line.split(" ").length == 4 && line.split(" ")[2].startsWith("F"));
				faults.put(++index, line.split(" ")[2]);
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

}
