package zuo.processor.splitinputs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import zuo.processor.genscript.version.GenRunVersionsScript;
import zuo.processor.utility.FileUtility;

public class SplitInputs {
	final String subject;
	final String version;
	final String inputsMapFile;
	final String versionsDir;
	public final String rootDir;
	
	public final File oracleOutputFolder;
	public final File testOutputFolder;
	
	private Map<Integer, String> inputsMap = new LinkedHashMap<Integer, String>();
	private List<Integer> failingTests = new ArrayList<Integer>();
	private List<Integer> passingTests = new ArrayList<Integer>();
	
	public SplitInputs(String dir, String sub, String ver) {
		this.rootDir = dir;
		this.subject = sub;
		this.version = ver;
		this.inputsMapFile = rootDir + subject + "/testplans.alt/inputs.map";
		this.versionsDir = rootDir + subject + "/versions/";
		
		this.oracleOutputFolder = new File(rootDir + subject + "/outputs/" + subject);
		this.testOutputFolder = new File(rootDir + subject + "/outputs/versions/" + version + GenRunVersionsScript.outFolder);
		
		this.inputsMap = FileUtility.readInputsMap(inputsMapFile);
		this.splitTestInputs();
		this.writeSplittedTestInputsTextFiles(version);
		this.writeSplittedTestInputsArrayFiles(version);
	}
	public static void main(String[] args) {
		new SplitInputs("/home/sunzzq/Research/", "space", "v7");
	}
	
	private void splitTestInputs(){
		for (PairedOutputFile pairedFile : creatPairedOutputFiles()) {
			int index = pairedFile.getIndex();
			if(FileUtility.contentEqual(pairedFile.getOracle(), pairedFile.getTest())){
				passingTests.add(index);
			}
			else{
				failingTests.add(index);
			}
		}
	}
	

	private PairedOutputFile[] creatPairedOutputFiles(){
		File[] oracleOutputFiles = this.oracleOutputFolder.listFiles(new OutputFilenameFilter());
		File[] testOutputFiles = this.testOutputFolder.listFiles(new OutputFilenameFilter());
		assert(oracleOutputFiles.length == testOutputFiles.length);
		
		PairedOutputFile[] pairedFiles = new PairedOutputFile[oracleOutputFiles.length];
		int j = 0;
		
		Map<String, File> oracleFiles = new HashMap<String, File>();
		for (int i = 0; i < oracleOutputFiles.length; i++) {
			oracleFiles.put(oracleOutputFiles[i].getName(), oracleOutputFiles[i]);
		}
		assert(oracleFiles.size() == oracleOutputFiles.length);
		
		for (int i = 0; i < testOutputFiles.length; i++) {
			File oracle = oracleFiles.remove(testOutputFiles[i].getName());
			if(oracle == null){
				throw new RuntimeException("No corresponding oracle output for this test output " + testOutputFiles[i]);
			}
			pairedFiles[j++] = new PairedOutputFile(oracle, testOutputFiles[i]);
		}
		
		assert(pairedFiles.length == oracleOutputFiles.length);
		return pairedFiles;
	}
	
	
	private void writeSplittedTestInputsArrayFiles(String ver){
		Collections.sort(passingTests);
		Collections.sort(failingTests);
		
		String path = versionsDir + ver;
		
		ObjectOutputStream out = null;
		try{
			out = new ObjectOutputStream(new FileOutputStream(path + "/failingInputs.array"));
			out.writeObject(failingTests);
			out.close();
			
			out = new ObjectOutputStream(new FileOutputStream(path + "/passingInputs.array"));
			out.writeObject(passingTests);
			out.close();
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
	}
	
	private void writeSplittedTestInputsTextFiles(String ver) {
		Collections.sort(passingTests);
		Collections.sort(failingTests);
		
		String path = versionsDir + ver;
				
		PrintWriter out = null;
		try{
			//write the statistics information
			out = new PrintWriter(new BufferedWriter(new FileWriter(path + "/statistics_inputs")));
			out.println("Number of passing inputs: \t" + passingTests.size());
			out.println("Number of failing inputs: \t" + failingTests.size());
			out.println("Total number of inputs: \t" + (passingTests.size() + failingTests.size()));
			out.close();
			
			//write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(path + "/p_inputs")));
			for(int index: passingTests){
				out.println(index + ": " + inputsMap.get(index));
			}
			out.close();
			
			//writing the failing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(path + "/f_inputs")));
			for(int index: failingTests){
				out.println(index + ": " + inputsMap.get(index));
			}
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}
	
	private void printUniverseInputs(){
		PrintWriter out = null;
		try{
			out = new PrintWriter(new BufferedWriter(new FileWriter(rootDir + subject + "/testplans.alt/universe_used")));
			for (int i = 0; i < inputsMap.size(); i++) {
				out.println(i + ": " + inputsMap.get(i));
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			if(out != null){
				out.close();
			}
		}
	}
	
	private static class OutputFilenameFilter implements FilenameFilter{

		@Override
		public boolean accept(File dir, String name) {
			// TODO Auto-generated method stub
			return name.matches("o[0-9]+\\.out");
		}
		
	}
}
