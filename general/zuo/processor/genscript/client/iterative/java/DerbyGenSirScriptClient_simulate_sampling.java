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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import zuo.processor.genscript.sir.iterative.java.AbstractGenRunScript;
import zuo.processor.genscript.sir.iterative.java.GenRunAdaptiveFineGrainedInstrumentScript;
import zuo.processor.genscript.sir.iterative.java.GenRunFineGrainedInstrumentScriptDerby;
import zuo.util.file.FileUtility;

public class DerbyGenSirScriptClient_simulate_sampling extends AbstractGenSirScriptClient{
	
	public final static int Round = 0;
	
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
	
	
	public DerbyGenSirScriptClient_simulate_sampling(String sub, String ver, String subVer){
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
			for(int j = 5; j <= Integer.parseInt(subjects[i][1]); j++){
				DerbyGenSirScriptClient_simulate_sampling gc = new DerbyGenSirScriptClient_simulate_sampling(subjects[i][0], "v" + j, null);
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
//		System.out.println(list);
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
			DerbyGenSirScriptClient_simulate_sampling gc = new DerbyGenSirScriptClient_simulate_sampling(subject, version, subversion.getName());
			int index = Integer.parseInt(gc.subVersion.substring(4));
			if(!set.contains(index)){
				continue;
			}
			
			String file = rootDir + gc.subject + "/SimuInfo_sample/" + gc.version + "_" + gc.subVersion + "/R" + Round + "T1";
			System.out.println(file);
			
			String simuScriptsDir = "/SimuScripts" + Round + "_sample/";
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader(new File(file)));
				String line;
				while((line = reader.readLine()) != null){
					String[] comps = line.split("<<<");
					assert(comps.length == 3);
					String fileName = comps[0];
					List<Integer> passingTests = GenSirScriptClient_simulate.readTests(comps[1]);
					List<Integer> failingTests = GenSirScriptClient_simulate.readTests(comps[2]);
					
					String builder = generateScripts(list, passingTests, failingTests, gc.subject);
					if(fileName.equals("Full_Sample_100")){
						AbstractGenRunScript.printToFile(builder, rootDir + gc.subject + simuScriptsDir + gc.version + "_" + gc.subVersion, gc.version + "_" + gc.subVersion + "_fg_s100.sh");
					}
				}
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
	
	
	public static String generateScripts(List<Entry<Integer, String>> list, List<Integer> passingTests,
			List<Integer> failingTests, String subject) {
		StringBuilder code = new StringBuilder();
		// TODO Auto-generated method stub
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			int in = index % list.size();
			int t = index / list.size();
			int num = list.get(in).getKey() + t * list.size();
			code.append(AbstractGenRunScript.runinfo + num + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + 100 + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + num + ".fprofile\n");
			code.append(GenRunFineGrainedInstrumentScriptDerby.insertSetEnv(list.get(in).getValue()));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			int in = index % list.size();
			int t = index / list.size();
			int num = list.get(in).getKey() + t * list.size();
			code.append(AbstractGenRunScript.runinfo + num + "\"\n");// running info
			code.append("export SAMPLER_SPARSITY=" + 100 + "\n");
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + num + ".pprofile\n");
			code.append(GenRunFineGrainedInstrumentScriptDerby.insertSetEnv(list.get(in).getValue()));
			code.append("\n");
		}
		return code.toString();
	}
}
