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
	final String sitesFile;
	final String profilesFile;
	final String consoleFile;
	
	public CBIClient(int runs, int k, String sitesFile, String profilesFile, String consoleF) {
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
		CBIClient client = new CBIClient(2717, 10, "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v38/v38_f.sites", 
				"/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/v38/fine-grained", "/home/sunzzq/Console/space_v38_cbi.out");
	}
	
	private void printResults(PrintWriter writer){
		InstrumentationSites sites = new InstrumentationSites(new File(sitesFile));
		PredicateProfileReader reader = new PredicateProfileReader(profilesFile, sites);
		PredicateProfile[] profiles = reader.readProfiles(runs);
		Processor p = new Processor(profiles);
		p.process();
		
		
		System.out.println("\n");
		System.out.println("The general runs information are as follows:\n==============================================================");
		System.out.println("Total number of runs:\t\t\t\t" + runs);
		System.out.println("Total number of negative runs:\t\t\t" + p.getTotalNegative());
		System.out.println("Total number of positive runs:\t\t\t" + p.getTotalPositive());
		
		assert(p.getTotalNegative() + p.getTotalPositive() == runs);
		
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println("Total number of runs:\t\t\t\t" + runs);
		writer.println("Total number of negative runs:\t\t\t" + p.getTotalNegative());
		writer.println("Total number of positive runs:\t\t\t" + p.getTotalPositive());
	
		System.out.println("\n");
		writer.println("\n");
		printTopKPredictors(p.getPredictors(), k, writer);
		
	}

	public void printTopKPredictors(Map<PredicateItem, Double> predictors, int k, PrintWriter writer){
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
		
		System.out.println("The top " + k + " predicates are as follows:\n==============================================================");
		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
		for (int i = 0; i < k && i < list.size(); i++) {
			Entry<PredicateItem, Double> entry = (Entry<PredicateItem, Double>) list.get(i);
			System.out.println("(" + (i + 1) + "): " + entry.getValue() + "\n" + entry.getKey().toString());
			System.out.println();
			
			writer.println("(" + (i + 1) + "): " + entry.getValue() + "\n" + entry.getKey().toString());
			writer.println();
			
			//collect the method
			String method = entry.getKey().getSite().getFunctionName();
			if(!methods.contains(method)){
				methods.add(method);
			}
		}
		
		System.out.println();
		System.out.println("The corresponding top " + methods.size() + " methods are as follows:\n--------------------------------------------------------------");
		System.out.println(methods.toString());
		
		writer.println();
		writer.println("The corresponding top " + methods.size() + " methods are as follows:\n--------------------------------------------------------------");
		writer.println(methods.toString());
	}

}
