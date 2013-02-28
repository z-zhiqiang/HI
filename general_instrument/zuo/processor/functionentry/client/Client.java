package zuo.processor.functionentry.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.processor.SelectingProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class Client {
	final int runs;
	final String sitesFile;
	final String profilesFile;
	final String consoleFile;
	
	
	public Client(int runs, String sitesFile, String profilesFile, String consoleFile) {
		this.runs = runs;
		this.sitesFile = sitesFile;
		this.profilesFile = profilesFile;
		
		this.consoleFile = consoleFile;
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
		Client client = new Client(2717, "/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v38/v38_c.sites", 
				"/home/sunzzq/Research/Automated_Debugging/Subjects/space/traces/v38/coarse-grained", "/home/sunzzq/Console/space_v38_function.out");
	}
	
	private void printResults(PrintWriter writer){
		SitesInfo sInfo = new SitesInfo(new InstrumentationSites(new File("/home/sunzzq/Research/Automated_Debugging/Subjects/space/versions/v38/v38_f.sites")));
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFile, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(runs);
		SelectingProcessor processor = new SelectingProcessor(profiles);
		processor.process();
		
		System.out.println("\n");
		System.out.println("The general runs information are as follows:\n==============================================================");
		System.out.println("Total number of runs:\t\t\t\t" + runs);
		System.out.println("Total number of negative runs:\t\t\t" + processor.getTotalNegative());
		System.out.println("Total number of positive runs:\t\t\t" + processor.getTotalPositive());
		
		assert(processor.getTotalNegative() + processor.getTotalPositive() == runs);
		
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println("Total number of runs:\t\t\t\t" + runs);
		writer.println("Total number of negative runs:\t\t\t" + processor.getTotalNegative());
		writer.println("Total number of positive runs:\t\t\t" + processor.getTotalPositive());
		
		System.out.println("\n");
		writer.println("\n");
		printSitesInfo(sInfo, writer);
		
		System.out.println("\n");
		System.out.println("The general methods information are as follows:\n==============================================================");
		System.out.println("Total number of methods instrumented:\t\t" + sites.getNumFunctionEntrySites());
		
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		
		writer.println("\n");
		writer.println("The general methods information are as follows:\n==============================================================");
		writer.println("Total number of methods instrumented:\t\t" + sites.getNumFunctionEntrySites());
		
		System.out.println("\n");
		writer.println("\n");
		printByFScoreOrder(processor.getFrequencyMap(), sInfo, writer);
		System.out.println("\n");
		writer.println("\n");
		printByNegative(processor.getFrequencyMap(), sInfo, writer);
		System.out.println("\n");
		writer.println("\n");
		printByPositive(processor.getFrequencyMap(), sInfo, writer);
		
	}


	private void printByPositive(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getPositive())
				.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getPositive()));
				if(r == 0){
					return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				}
				return r;
			}});
		System.out.println("The methods ordered by positive are as follows:\n--------------------------------------------------------------");
		writer.println("The methods ordered by positive are as follows:\n--------------------------------------------------------------");
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				System.out.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
				writer.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				System.out.println(method + "    \t" + entry.getValue().toString());
				writer.println(method + "    \t" + entry.getValue().toString());
			}
		}
	}


	private void printByNegative(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				int r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getNegative())
				.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getNegative()));
				if(r == 0){
					return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
				}
				return r;
			}});
		System.out.println("The methods ordered by negative are as follows:\n--------------------------------------------------------------");
		writer.println("The methods ordered by negative are as follows:\n--------------------------------------------------------------");
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				System.out.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
				writer.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				System.out.println(method + "    \t" + entry.getValue().toString());
				writer.println(method + "    \t" + entry.getValue().toString());
			}
		}
	}


	private void printByFScoreOrder(Map<FunctionEntrySite, FrequencyValue> frequencyMap, SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
			}});
		System.out.println("The methods ordered by F-score are as follows:\n--------------------------------------------------------------");
		writer.println("The methods ordered by F-score are as follows:\n--------------------------------------------------------------");
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				System.out.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
				writer.println(method + "    \t" + entry.getValue().toString() + "    \t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				System.out.println(method + "    \t" + entry.getValue().toString());
				writer.println(method + "    \t" + entry.getValue().toString());
			}
		}
	}
	
	public static void printSitesInfo(SitesInfo sInfo, PrintWriter writer) {
		// TODO Auto-generated method stub
		System.out.println("The general sites information are as follows:\n==============================================================");
		System.out.println("Total number of sites instrumented:\t\t\t" + sInfo.getNumPredicateSites());
		System.out.println("Total number of predicates instrumented:\t\t" + sInfo.getNumPredicateItems());
		System.out.println("Total number of methods having sites instrumented:\t" + sInfo.getMap().size());
		System.out.println();
		System.out.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		
		writer.println("The general sites information are as follows:\n==============================================================");
		writer.println("Total number of sites instrumented:\t\t\t" + sInfo.getNumPredicateSites());
		writer.println("Total number of predicates instrumented:\t\t" + sInfo.getNumPredicateItems());
		writer.println("Total number of methods having sites instrumented:\t" + sInfo.getMap().size());
		writer.println();
		writer.println("The information of sites and predicates in each method:\n--------------------------------------------------------------");
		for(String method: sInfo.getMap().keySet()){
			System.out.println(method + "     \t:" + sInfo.getMap().get(method).getNumSites() + "\t:" + sInfo.getMap().get(method).getNumPredicates());
			writer.println(method + "     \t:" + sInfo.getMap().get(method).getNumSites() + "\t:" + sInfo.getMap().get(method).getNumPredicates());
		}
		
	}
	

}
