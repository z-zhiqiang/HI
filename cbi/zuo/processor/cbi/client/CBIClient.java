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
import java.util.Set;

import zuo.processor.cbi.processor.PredicateItemWithImportance;
import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.site.SitesInfo;

public class CBIClient {
	final int k;
	final PredicateProfile[] selectedPredicateProfiles;
	private List<PredicateItemWithImportance> sortedPredictorsList;
	
	private final Set<String> functions;
	private final Set<Integer> samples; 
	
	
	public CBIClient(int k, PredicateProfile[] profiles, File consoleFile, Set<String> functions, Set<Integer> samples) {
		this.k = k;
		
		this.functions = functions;
		this.samples = samples;
		
		this.selectedPredicateProfiles = constructSelectedPredicateProfiles(profiles);
		
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(consoleFile)));
			run(writer);
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
	
	private PredicateProfile[] constructSelectedPredicateProfiles(PredicateProfile[] profiles) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args) {
//		CBIClient client = new CBIClient(363, 10, new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/sed/versions/v2/subv1/v2_subv1_f.sites"), 
//				"/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/sed/traces/v2/subv1/fine-grained/", 
//				"/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/m3.out");
	}
	
	private void run(PrintWriter writer){
//		PredicateProfileReader reader = new PredicateProfileReader(profilesFolder, sitesInfo.getSites(), functions, samples);
//		PredicateProfile[] profiles = reader.readProfiles(runs);
		Processor p = new Processor(selectedPredicateProfiles);
		p.process();
		
		
		assert(p.getTotalNegative() + p.getTotalPositive() == selectedPredicateProfiles.length);
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of runs:") + selectedPredicateProfiles.length);
		writer.println(String.format("%-50s", "Total number of negative runs:") + p.getTotalNegative());
		writer.println(String.format("%-50s", "Total number of positive runs:") + p.getTotalPositive());
	
		//sort the list of predictors according to the importance value
		sortingPreditorsList(p.getPredictorsList());
		
		//print out top-k predictors information
		writer.println("\n");
		printTopKPredictors(writer);
	}

	private void sortingPreditorsList(List<PredicateItemWithImportance> predictorsList) {
		sortedPredictorsList = new ArrayList<PredicateItemWithImportance>(predictorsList);
		// TODO Auto-generated method stub
		Collections.sort(sortedPredictorsList, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Double(((PredicateItemWithImportance) arg1).getImportance())
				.compareTo(new Double(((PredicateItemWithImportance) arg0).getImportance()));
			}
			
		});
	}

	public void printTopKPredictors(PrintWriter writer){
		Set<String> topMethods = new LinkedHashSet<String>();
		
		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
		for (int i = 0; i < sortedPredictorsList.size(); i++) {
			PredicateItemWithImportance pItemWI = sortedPredictorsList.get(i);
			String method = pItemWI.getPredicateItem().getPredicateSite().getSite().getFunctionName();
			double value = pItemWI.getImportance();
			
			if (i < k) {
				//collect the method
				if (!topMethods.contains(method)) {
					topMethods.add(method);
				}
				writer.println("(" + (i + 1) + "): " + pItemWI.toString());
				writer.println();
			}
		}
		
//		writer.println();
//		writer.println("The corresponding top " + topMethods.size() + " of " + methodsM.size() + " methods are as follows:\n--------------------------------------------------------------");
//		writer.println(topMethods.toString());
	}

	
	
	public List<PredicateItemWithImportance> getSortedPredictorsList() {
		return sortedPredictorsList;
	}


}
