package zuo.processor.genscript.client.iterative.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import zuo.processor.genscript.sir.iterative.java.AbstractGenRunAllScript;
import zuo.processor.genscript.sir.iterative.java.AbstractGenRunScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAdaptiveFineGrainedInstrumentScriptDerby;
import zuo.processor.genscript.sir.iterative.java.GenRunAllAdaptiveInstrumentedScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllInstrumentedScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllSampledInstrumentedScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAllScript;
import zuo.processor.genscript.sir.iterative.java.GenRunCoarseGrainedInstrumentScriptDerby;
import zuo.processor.genscript.sir.iterative.java.GenRunFineGrainedInstrumentScriptDerby;
import zuo.processor.genscript.sir.iterative.java.GenRunSampledFineGrainedInstrumentScriptDerby;
import zuo.processor.genscript.sir.iterative.java.GenRunSubjectScript;
import zuo.processor.genscript.sir.iterative.java.GenRunVersionsScriptDerby;
import zuo.processor.splitinputs.SirSplitInputs;
import zuo.util.file.FileUtility;

public class DerbyGenSirScriptClient_simulate extends AbstractGenSirScriptClient{
	
	public final String subject;
	public final String version;
	public final String subVersion;
	public final String inputScript;
	
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
	
	
	public DerbyGenSirScriptClient_simulate(String sub, String ver, String subVer){
		subject = sub;
		version = ver;
		subVersion = subVer;
		
		inputScript = rootDir + subject + "/scripts/TestScripts/orig/" + subject + "-" + version + ".sh";
		
		inputsMapFile = rootDir + subject + "/testplans.alt/" + "inputs.map";
		inputsCompMapFile = rootDir + subject + "/testplans.alt/" + "inputsComp.map";

		
		ssourceDir = rootDir + subject + "/versions.alt/orig/" + version + "/";
		
		sexecuteDir = rootDir + subject + "/source/" + subject + "/";
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
		
	}


	

	public static void main(String[] args) throws IOException {
		String[][] subjects = {
				{"derby", "5"}, // grep v1_subv14
		};
		for (int i = 0; i < subjects.length; i++) {
			for(int j = 5; j <= Integer.parseInt(subjects[i][2]); j++){
				DerbyGenSirScriptClient_simulate gc = new DerbyGenSirScriptClient_simulate(subjects[i][0], "v" + j, null);
				gc.gen();
			}
		}
		
	}



	private void gen() throws IOException {
		
		//generate test scripts
		FileUtility.constructSIRInputsMapFile(inputScript, inputsMapFile);
		
		int[] array = {8, 30, 61};
		Set<Integer> set = new HashSet<Integer>();
		for(int i: array){
			set.add(i);
		}
		
		
		//=========================================================================================================================================================================//
		Map<Integer, String> map = FileUtility.readInputsMap(inputsMapFile);
		long sleepTime = GenSirScriptClient_simulate.sleepTime(map);
		System.out.println(sleepTime);
		List<Map.Entry<Integer, String>> list = new ArrayList<Map.Entry<Integer, String>>(map.entrySet());
		System.out.println(list);
		Collections.sort(list, new Comparator<Map.Entry<Integer, String>>(){

			@Override
			public int compare(Entry<Integer, String> o1, Entry<Integer, String> o2) {
				// TODO Auto-generated method stub
				return o1.getKey().compareTo(o2.getKey());
			}
			
		});
		
		File[] subversions = new File(rootDir + subject + "/versions/", version).listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 9);
			}});
		Arrays.sort(subversions, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				return new Integer(Integer.parseInt(o1.getName().substring(4))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(4))));
			}});
		
		for(File subversion: subversions){
			GenSirScriptClient_simulate gc = new GenSirScriptClient_simulate(subject, version, subversion.getName());
			int index = Integer.parseInt(gc.subVersion.substring(4));
			if(!set.contains(index)){
				continue;
			}
			
			String vexecuteDir_version = gc.vexecuteDir + "version/";
			File targetDir = new File(rootDir + gc.subject + "/" + gc.subject + "_bin/seeded_" + gc.version + "_" + index);
			if(!targetDir.exists()){
				targetDir.mkdirs();
			}
			FileUtils.copyDirectory(new File(vexecuteDir_version), targetDir);
			
			
			String file = rootDir + gc.subject + "/SimuInfo/" + gc.version + "_" + gc.subVersion + "/R0T1";
			System.out.println(file);
			
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File(file)));
				String line;
				int id = 0;
				StringBuilder meta = new StringBuilder();
				while((line = reader.readLine()) != null){
					String[] comps = line.split("<<<");
					assert(comps.length == 3);
					String fileName = comps[0];
					List<Integer> passingTests = GenSirScriptClient_simulate.readTests(comps[1]);
					List<Integer> failingTests = GenSirScriptClient_simulate.readTests(comps[2]);
					
					String builder = GenSirScriptClient_simulate.generateScripts(list, passingTests, failingTests, gc.subject);
					if(fileName.equals("Full")){
						AbstractGenRunScript.printToFile(builder, rootDir + gc.subject + "/SimuScripts/" + gc.version + "_" + gc.subVersion, gc.version + "_" + gc.subVersion + "_fg.sh");
						AbstractGenRunScript.printToFile(builder, rootDir + gc.subject + "/SimuScripts/" + gc.version + "_" + gc.subVersion, gc.version + "_" + gc.subVersion + "_cg.sh");
					}
					else{
						String[] sigs = fileName.split("->");
						assert(sigs.length == 2);
						String sig = GenRunAdaptiveFineGrainedInstrumentScript.transform(sigs[0]);
						meta.append(++id + "=" + sig + "=" + sigs[1]).append("\n");
						AbstractGenRunScript.printToFile(builder, rootDir + gc.subject + "/SimuScripts/" + gc.version + "_" + gc.subVersion, gc.version + "_" + gc.subVersion + "_m" + id + ".sh");
					}
				}
				AbstractGenRunScript.printToFile(meta.toString(), rootDir + gc.subject + "/SimuScripts/" + gc.version + "_" + gc.subVersion, "map");
				reader.close();
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
	
}
