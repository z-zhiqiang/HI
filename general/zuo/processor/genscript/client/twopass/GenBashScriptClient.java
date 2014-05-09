package zuo.processor.genscript.client.twopass;

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

import zuo.processor.genscript.bash.twopass.AbstractGenRunAllScript;
import zuo.processor.genscript.bash.twopass.AbstractGenRunScript;
import zuo.processor.genscript.bash.twopass.GenRunAllInstrumentedScript;
import zuo.processor.genscript.bash.twopass.GenRunAllScript;
import zuo.processor.genscript.bash.twopass.GenRunBoostFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.twopass.GenRunCoarseFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.twopass.GenRunCoarseGrainedInstrumentScript;
import zuo.processor.genscript.bash.twopass.GenRunFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.twopass.GenRunPruneFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.twopass.GenRunPruneMinusBoostFineGrainedInstrumentScript;
import zuo.processor.genscript.bash.twopass.GenRunSubjectScript;
import zuo.processor.genscript.bash.twopass.GenRunVersionsScript;
import zuo.processor.splitinputs.SplitInputs;

public class GenBashScriptClient {
	private static final String CG_INDICES = "indices.txt";
	
	public static final String mode = "2_0.05";
	private static final String BOOST_FUNCTIONS = "boost_functions_" + mode + ".txt";
	private static final String PRUNE_MINUS_BOOST_FUNCTIONS = "prune_minus_boost_functions_" + mode + ".txt";
	private static final String PRUNE_FUNCTIONS = "prune_functions_" + mode + ".txt";
	
	public final static String rootDir = "/home/sunzzq2/Data/IResearch/Automated_Bug_Isolation/Twopass/Subjects";
	
	public final static String setEnv = "export experiment_root=" + rootDir 
			+ "\nexport TESTS_SRC=" + rootDir + "/bash/testplans.alt/testplans.fine\n"; 
	public final static String exeFile = rootDir + "/bash/source/bin/" + "bash ";
	public final static String inputsDir = rootDir + "/bash/testplans.alt/testplans.fine";
	public final static String inputsMapFile = rootDir + "/bash/testplans.alt/" + "inputs.map";
	
	public final String subject;
	public final String version;
	public final String subVersion;
	
	final String sexecuteDir;
	final String soutputDir;
	
	final String vsourceDir;
	
	final String vexecuteDir;
	
	final String voutputDir;
	final String vfoutputDir;
	final String vcoutputDir;
	final String vcfoutputDir;
	final String vboostoutputDir;
	final String vpruneminusboostoutputDir;
	final String vpruneoutputDir;
	
	final String vftraceDir;
	final String vctraceDir;
	final String vcftraceDir;
	final String vboosttraceDir;
	final String vpruneminusboosttraceDir;
	final String vprunetraceDir;
	
    final String cgIndicesDir;
	final String boostFunctionsDir;
	final String pruneMinusBoostFunctionsDir;
	final String pruneFunctionsDir;
	
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
		
		sexecuteDir = rootDir + "/" + subject + "/source.alt/" + version;
		soutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/" + subject;
		
		vsourceDir = rootDir + "/" + subject + "/versions.alt/versions.seeded/" + version;
		
		vexecuteDir = rootDir + "/" + subject + "/versions/" + version + "/" + subVersion;
		
		voutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/outputs";
		vfoutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/fine-grained";
		vcoutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/coarse-grained";
		vcfoutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/coarse-fine-grained";
		vboostoutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/boost";
		vpruneminusboostoutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/prune-minus-boost";
		vpruneoutputDir = rootDir + "/" + subject + "/outputs.alt/" + version + "/versions/" + subVersion + "/prune";
		
		vftraceDir = rootDir + "/" + subject + "/traces/" + version + "/" + subVersion + "/fine-grained";
		vctraceDir = rootDir + "/" + subject + "/traces/" + version + "/" + subVersion + "/coarse-grained";
		vcftraceDir = rootDir + "/" + subject + "/traces/" + version + "/" + subVersion + "/coarse-fine-grained";
		vboosttraceDir = rootDir + "/" + subject + "/traces/" + version + "/" + subVersion + "/boost";
		vpruneminusboosttraceDir = rootDir + "/" + subject + "/traces/" + version + "/" + subVersion + "/prune-minus-boost";
		vprunetraceDir = rootDir + "/" + subject + "/traces/" + version + "/" + subVersion + "/prune";
		
		cgIndicesDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/predicate-dataset/cg";
		boostFunctionsDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/predicate-dataset/boost";
		pruneMinusBoostFunctionsDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/predicate-dataset/pruneMinusBoost";
		pruneFunctionsDir = rootDir + subject + "/versions/" + version + "/" + subVersion + "/predicate-dataset/prune";
		
		scriptDir = rootDir + "/" + subject + "/scripts";
		
	}

	
	public static void main(String[] args) throws IOException {
		constructBashInputsMapFile(new File(inputsDir), inputsMapFile);
		for(int j = 1; j <= 2; j++){
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
		
		//generate run subject and subversion scripts
//		String subjectCompile = setEnv + "./makevers " + version.substring(1);
//		gs = new GenRunSubjectScript(subject, version, subjectCompile, sexecuteDir, soutputDir, scriptDir);
//		gs.genRunScript();
//		
//		for(int index: faults.keySet()){
//			GenBashScriptClient gc = new GenBashScriptClient(subject, version, "subv" + index);
//			
//			System.out.println("generating run script for subVersion" + index);
//			String versionCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
//					+ " CC=gcc ";
//			new GenRunVersionsScript(gc.subject, gc.version, gc.subVersion, versionCompile, gc.vexecuteDir, gc.voutputDir, gc.scriptDir).genRunScript();
//		}
//		
//		//generate run all scripts  
//		ga = new GenRunAllScript(version, subject, scriptDir, faults.size());
//		ga.genRunAllScript();
		
		
		//=========================================================================================================================================================================//
		
		Set<Integer> subs = new HashSet<Integer>();
		//split inputs and generate run instrumented subversion scripts 
		for(int index: faults.keySet()){
			GenBashScriptClient gc = new GenBashScriptClient(subject, version, "subv" + index);
			
			System.out.println("sliptting inputs for subversion" + index);
			SplitInputs split = new SplitInputs(inputsMapFile, gc.soutputDir, gc.voutputDir, gc.vexecuteDir);
			split.split();
			if(split.getFailingTests().size() >= 1){
				subs.add(index);
				
				System.out.println("generating run instrument script for subv" + index);
				
				String fgCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
						+ " CC=\"\\\"sampler-cc -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fcompare-constants -fsampler-scheme=float-kinds -fno-sample \\\"\"";
				gs = new GenRunFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, fgCompile, gc.vexecuteDir, 
						gc.vfoutputDir, gc.scriptDir, gc.vftraceDir, gc.vexecuteDir + "/failingInputs.array", gc.vexecuteDir + "/passingInputs.array");
				gs.genRunScript();
				
				String cgCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
						+ " CC=\"\\\"sampler-cc -fsampler-scheme=function-entries -fno-sample \\\"\"";
				gs = new GenRunCoarseGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, cgCompile, gc.vexecuteDir, 
						gc.vcoutputDir, gc.scriptDir, gc.vctraceDir, gc.vexecuteDir + "/failingInputs.array", gc.vexecuteDir + "/passingInputs.array");
				gs.genRunScript();
				
//				String cfgCompile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index) 
//						+ " CC=\"\\\"sampler-cc -fsampler-scheme=function-entries -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fcompare-constants -fsampler-scheme=float-kinds -fno-sample \\\"\"";
//				gs = new GenRunCoarseFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, cfgCompile, gc.vexecuteDir, 
//						gc.vcfoutputDir, gc.scriptDir, gc.vcftraceDir, gc.vexecuteDir + "/failingInputs.array", gc.vexecuteDir + "/passingInputs.array");
//				gs.genRunScript();
//				
//				//=================================================================================================//
//				String compile = setEnv + "./compile " + version.substring(1) + " " + faults.get(index);
//				
//				gs = new GenRunBoostFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, compile, gc.vexecuteDir, 
//						gc.vboostoutputDir, gc.scriptDir, gc.vboosttraceDir, gc.vexecuteDir + "/failingInputs.array", gc.vexecuteDir + "/passingInputs.array", new File(gc.boostFunctionsDir, BOOST_FUNCTIONS));
//				gs.genRunScript();
//				gs = new GenRunPruneMinusBoostFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, compile, gc.vexecuteDir, 
//						gc.vpruneminusboostoutputDir, gc.scriptDir, gc.vpruneminusboosttraceDir, gc.vexecuteDir + "/failingInputs.array", gc.vexecuteDir + "/passingInputs.array", new File(gc.pruneMinusBoostFunctionsDir, PRUNE_MINUS_BOOST_FUNCTIONS));
//				gs.genRunScript();
//				gs = new GenRunPruneFineGrainedInstrumentScript(gc.subject, gc.version, gc.subVersion, compile, gc.vexecuteDir, 
//						gc.vpruneoutputDir, gc.scriptDir, gc.vprunetraceDir, gc.vexecuteDir + "/failingInputs.array", gc.vexecuteDir + "/passingInputs.array", new File(gc.pruneFunctionsDir, PRUNE_FUNCTIONS));
//				gs.genRunScript();
			}
			
		}
		
		//generate run all instrumented triggered subversion scripts
		ga = new GenRunAllInstrumentedScript(version, subject, scriptDir, subs);
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
				faults.put(++index, line.split(" ")[1]);
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
