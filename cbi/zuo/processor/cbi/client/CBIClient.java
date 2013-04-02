package zuo.processor.cbi.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;

public class CBIClient {
	final int runs;
	final int k;
	final File sitesFile;
	final String profilesFile;
	final String consoleFile;
	List<Map.Entry<PredicateItem, Double>> predictorEntryList;
    Set<String> methods;
	
	public CBIClient(int runs, int k, File sitesFile, String profilesFile, String consoleF) {
		this.runs = runs;
		this.k = k;
		this.sitesFile = sitesFile;
		this.profilesFile = profilesFile;
		this.consoleFile = consoleF;
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults(writer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(writer != null){
				writer.close();
			}
		}
	}
	
	public static void main(String[] args) {
		CBIClient client = new CBIClient(9, 1, new File("/home/sunzzq/Downloads/debug/matrix.sites"), 
				"/home/sunzzq/Downloads/debug", "/home/sunzzq/Downloads/debug/m.out");
	}
	
	private void printResults(PrintWriter writer){
		InstrumentationSites sites = new InstrumentationSites(sitesFile);
		PredicateProfileReader reader = new PredicateProfileReader(profilesFile, sites);
		PredicateProfile[] profiles = reader.readProfiles(runs);
		Processor p = new Processor(profiles);
		p.process();
		
		
		assert(p.getTotalNegative() + p.getTotalPositive() == runs);
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of runs:") + runs);
		writer.println(String.format("%-50s", "Total number of negative runs:") + p.getTotalNegative());
		writer.println(String.format("%-50s", "Total number of positive runs:") + p.getTotalPositive());
	
		writer.println("\n");
		printTopKPredictors(p.getPredictors(), k, writer);
	}

	public void printTopKPredictors(Map<PredicateItem, Double> predictors, int k, PrintWriter writer){
		Set<String> topMethods = new LinkedHashSet<String>();
		Set<String> methods = new LinkedHashSet<String>();
		List list = new ArrayList(predictors.entrySet());
		Collections.sort(list, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return ((Map.Entry<PredicateItem, Double>) arg1).getValue()
						.compareTo(((Entry<PredicateItem, Double>) arg0).getValue());
			}
			});
		
		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
		for (int i = 0; i < list.size(); i++) {
			Map.Entry<PredicateItem, Double> entry = (Map.Entry<PredicateItem, Double>) list.get(i);
			String method = entry.getKey().getSite().getFunctionName();
			
			if (i < k) {
				//collect the method
				if (!topMethods.contains(method)) {
					topMethods.add(method);
				}
				writer.println("(" + (i + 1) + "): " + entry.getValue() + "\n"
						+ entry.getKey().toString());
				writer.println();
			}
			
			if(!methods.contains(method)){
				methods.add(method);
			}
		}
		this.predictorEntryList = Collections.unmodifiableList(list);
	    this.methods = Collections.unmodifiableSet(methods);
		
		writer.println();
		writer.println("The corresponding top " + topMethods.size() + " methods are as follows:\n--------------------------------------------------------------");
		writer.println(topMethods.toString());
	}

	public Set<String> getMethods() {
		return methods;
	}

	public void setMethods(Set<String> methods) {
		this.methods = methods;
	}

	public int getRuns() {
		return runs;
	}

	public int getK() {
		return k;
	}

	public File getSitesFile() {
		return sitesFile;
	}

	public String getProfilesFile() {
		return profilesFile;
	}

	public String getConsoleFile() {
		return consoleFile;
	}

	public List<Map.Entry<PredicateItem, Double>> getPredictorEntryList() {
		return predictorEntryList;
	}


}
