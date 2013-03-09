package zuo.processor.splitinputs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import zuo.util.file.FileUtility;

public class SirSplitInputs {
	final String outDir;
	final String outFile;
	
	private Map<Integer, String> inputsMap = new LinkedHashMap<Integer, String>();
	private List<Integer> failingTests = new ArrayList<Integer>();
	private List<Integer> passingTests = new ArrayList<Integer>();
	
	public SirSplitInputs(String inputsMapFile, String outFolder, String outFile) {
		this.outDir = outFolder;
		this.outFile = outFile;
		
		this.inputsMap = FileUtility.readInputsMap(inputsMapFile);
	}
	
	public void split(){
		this.readSplittedTestInputs();
		this.writeSplittedTestInputsTextFiles();
		this.writeSplittedTestInputsArrayFiles();
	}
	
	
	private void readSplittedTestInputs() {
		Set<Integer> fails = new TreeSet<Integer>();
		BufferedReader reader = null;
		try {
			String line;
			reader = new BufferedReader(new FileReader(new File(outDir, outFile)));
			while((line = reader.readLine()) != null){
				int index = Integer.parseInt(line.trim());
				fails.add(index);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			System.out.println(outDir + " does not contain " + outFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		for(int i: inputsMap.keySet()){
			if(fails.contains(i)){
				failingTests.add(i);
			}
			else{
				passingTests.add(i);
			}
		}
		assert(failingTests.size() + passingTests.size() == inputsMap.size());
	}

	private void writeSplittedTestInputsArrayFiles(){
		Collections.sort(passingTests);
		Collections.sort(failingTests);
		
		ObjectOutputStream out = null;
		try{
			out = new ObjectOutputStream(new FileOutputStream(outDir + "/failingInputs.array"));
			out.writeObject(failingTests);
			out.close();
			
			out = new ObjectOutputStream(new FileOutputStream(outDir + "/passingInputs.array"));
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
	
	private void writeSplittedTestInputsTextFiles() {
		Collections.sort(passingTests);
		Collections.sort(failingTests);
		
		PrintWriter out = null;
		try{
			//write the statistics information
			out = new PrintWriter(new BufferedWriter(new FileWriter(outDir + "/statistics_inputs")));
			out.println("Number of passing inputs: \t" + passingTests.size());
			out.println("Number of failing inputs: \t" + failingTests.size());
			out.println("Total number of inputs: \t" + (passingTests.size() + failingTests.size()));
			out.close();
			
			//write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(outDir + "/p_inputs")));
			for(int index: passingTests){
				out.println(index + ": " + inputsMap.get(index));
			}
			out.close();
			
			//writing the failing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(outDir + "/f_inputs")));
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
	
	
	public List<Integer> getFailingTests() {
		return failingTests;
	}

	public List<Integer> getPassingTests() {
		return passingTests;
	}

	
}
