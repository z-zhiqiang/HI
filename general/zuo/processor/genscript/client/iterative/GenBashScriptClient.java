package zuo.processor.genscript.client.iterative;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import zuo.processor.genscript.bash.iterative.AbstractGenRunAllScript;
import zuo.processor.genscript.bash.iterative.AbstractGenRunScript;
import zuo.processor.genscript.bash.iterative.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.iterative.GenRunAllAdaptiveInstrumentedScript;
import zuo.processor.genscript.bash.iterative.GenRunAllInstrumentedScript;
import zuo.processor.genscript.bash.iterative.GenRunAllSampledInstrumentedScript;
import zuo.processor.genscript.bash.iterative.GenRunAllScript;
import zuo.processor.genscript.bash.iterative.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.bash.iterative.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.iterative.GenRunSampledFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.iterative.GenRunSubjectScript;
import zuo.processor.genscript.bash.iterative.GenRunVersionsScript;
import zuo.processor.splitinputs.SplitInputs;

public class GenBashScriptClient {
	public final static String rootDir = "/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/";
	
	public final static String setEnv = "export experiment_root=" + rootDir + "\n"; 
	public final static String exeFile = rootDir + "bash/source/bin/" + "bash ";
	public final static String inputsDir = rootDir + "bash/testplans.alt/testplans.fine/";
	
	public final String subject;
	public final String version;
	public final String subVersion;
	
	public final String inputsMapFile;
	
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
	
    final static Map<Integer, String> faults = new HashMap<Integer, String>();
	
	public GenBashScriptClient(String sub, String ver, String subVer){
		subject = sub;
		version = ver;
		subVersion = subVer;
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		
		sexecuteDir = rootDir + subject + "/source.alt/" + version + "/";
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
		
	}

	
	public static void main(String[] args) throws IOException {
		for(int j = 1; j <= 6; j++){
			GenBashScriptClient gc = new GenBashScriptClient("bash", "v" + j, null);
			gc.gen();
			faults.clear();
		}
	}



	private void gen() throws IOException {
		AbstractGenRunScript gs;
		AbstractGenRunAllScript ga;

		//read faults (subversion)
		readFaults();
		System.out.println(faults.toString());
		
		//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------//
		
		constructBashInputsMapFile(new File(inputsDir), inputsMapFile);
		//generate run subject and subversion scripts
		String subjectCompile = setEnv + "./makevers " + version.substring(1);
		gs = new GenRunSubjectScript(subject, version, subjectCompile, sexecuteDir, soutputDir, scriptDir);
		gs.genRunScript();
		
		for(int index: faults.keySet()){
			GenBashScriptClient gc = new GenBashScriptClient(subject, version, "subv" + index);
			
			System.out.println("generating run script for subVersion" + index);
			String versionCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
					+ " CC=gcc ";
			new GenRunVersionsScript(gc.subject, gc.version, gc.subVersion, versionCompile, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
		}
		
		//generate run all scripts  
		ga = new GenRunAllScript(version, subject, scriptDir, faults.size());
		ga.genRunAllScript();
		
		
		//=========================================================================================================================================================================//
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			GenBashScriptClient gc = new GenBashScriptClient(subject, version, "subv" + index);
			
			System.out.println("sliptting inputs for subversion" + index);
			SplitInputs split = new SplitInputs(gc.inputsMapFile, gc.soutputDir, gc.voutputDir, gc.vexecuteDir);
			split.split();
			if(split.getFailingTests().size() > 1){
				subs.add(index);
				assert(new File(gc.vexecuteDir).listFiles().length == 12);
			}
			
			System.out.println("generating run instrument script for subv" + index);
			
			String fgCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
					+ " CC=\"sampler-cc -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fno-sample \"";
			gs = new GenRunFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, fgCompile, gc.vexecuteDir, 
					gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
			gs.genRunScript();
			
			String cgCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
					+ " CC=\"sampler-cc -fsampler-scheme=function-entries -fno-sample \"";
			gs = new GenRunCoarseGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, cgCompile, gc.vexecuteDir, 
					gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array");
			gs.genRunScript();
			
			
			String sampleCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
					+ " CC=\"sampler-cc -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fsample -fsampler-random=fixed \"";
			gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, sampleCompile, gc.vsexecuteDir, gc.vsfoutputDir, 
					gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 1);
			gs.genRunScript();
			gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, sampleCompile, gc.vsexecuteDir, gc.vsfoutputDir, 
					gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 100);
			gs.genRunScript();
			gs = new GenRunSampledFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, sampleCompile, gc.vsexecuteDir, gc.vsfoutputDir, 
					gc.scriptDir, gc.vsftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", 10000);
			gs.genRunScript();
			
			if(new File(gc.vexecuteDir).listFiles().length == 12){
//				FileUtility.removeDirectory(new File(gc.vsexecuteDir));
//				FileUtility.removeDirectory(new File(gc.vaexecuteDir));
				
				String adaptiveCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index);
				gs = new GenRunAdaptiveFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, adaptiveCompile, gc.vaexecuteDir, 
						gc.vafoutputDir, gc.scriptDir, gc.vaftraceDir, gc.vexecuteDir + "failingInputs.array", gc.vexecuteDir + "passingInputs.array", "full");
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
	
	
	private static Map<Integer, String> constructBashInputsMapFile(File inputsDir, String inputsMapFile) {
		// TODO Auto-generated method stub
		Map<Integer, String> inputsmap = new LinkedHashMap<Integer, String>();
		
		int count = 0;
		File[] files = inputsDir.listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File arg0, String arg1) {
				// TODO Auto-generated method stub
				return arg1.endsWith(".test");
			}
		});
		
		for(File file: files){
			inputsmap.put(++count, file.getName());
		}
		
		
		ObjectOutputStream out = null;
    	try{
    		out = new ObjectOutputStream(new FileOutputStream(inputsMapFile));
    		out.writeObject(inputsmap);
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	finally{
    		try {
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
		return Collections.unmodifiableMap(inputsmap);
	}



	private void readFaults(){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(vsourceDir, "FaultSeeds.h")));
			String line;
			int index = 0;
			while((line = reader.readLine()) != null){
				assert(line.split(" ").length == 2 && line.split(" ")[1].startsWith("F"));
				faults.put(++index, line.substring(2));
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
