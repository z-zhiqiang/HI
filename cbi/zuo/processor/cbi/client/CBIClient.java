package zuo.processor.cbi.client;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import zuo.processor.cbi.processor.PredicateItemWithImportance;
import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.predicatesite.BranchPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ReturnPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ScalarPairPredicateSite;

public class CBIClient {
	final int k;
	final PredicateProfile[] selectedPredicateProfiles;
	private List<PredicateItemWithImportance> sortedPredictorsList;
	
	private final Set<String> functions;
	private final Set<Integer> samples; 
	
	
	public CBIClient(int k, PredicateProfile[] profiles, PrintWriter writer, Set<String> functions, Set<Integer> samples) {
		this.k = k;
		this.functions = functions;
		this.samples = samples;
		this.selectedPredicateProfiles = constructSelectedPredicateProfiles(profiles);
		
		run(writer);
	}
	
	private PredicateProfile[] constructSelectedPredicateProfiles(PredicateProfile[] profiles) {
		// TODO Auto-generated method stub
		PredicateProfile[] pProfiles = new PredicateProfile[samples.size()];
		
		int j = 0;
		for(int k: samples){
			PredicateProfile fullProfile = pProfiles[k];
			
			List<ScalarPairPredicateSite> scalarPairs = new ArrayList<ScalarPairPredicateSite>();
			List<ReturnPredicateSite> returns = new ArrayList<ReturnPredicateSite>();
			List<BranchPredicateSite> branches = new ArrayList<BranchPredicateSite>();
			
			for(int i = 0; i < fullProfile.getScalarPredicateSites().size(); i++){
				ScalarPairPredicateSite scalarPairPSite = fullProfile.getScalarPredicateSites().get(i);
				if(this.functions.contains(scalarPairPSite.getSite().getFunctionName())){
					scalarPairs.add(scalarPairPSite);
				}
			}
			for(int i = 0; i < fullProfile.getReturnPredicateSites().size(); i++){
				ReturnPredicateSite returnPSite = fullProfile.getReturnPredicateSites().get(i);
				if(this.functions.contains(returnPSite.getSite().getFunctionName())){
					returns.add(returnPSite);
				}
			}
			for(int i = 0; i < fullProfile.getBranchPredicateSites().size(); i++){
				BranchPredicateSite branchPSite = fullProfile.getBranchPredicateSites().get(i);
				if(this.functions.contains(branchPSite.getSite().getFunctionName())){
					branches.add(branchPSite);
				}
			}
			
		    pProfiles[j++] = new PredicateProfile(fullProfile.getPath(), fullProfile.isCorrect(), 
		    		Collections.unmodifiableList(scalarPairs), 
		    		Collections.unmodifiableList(returns), 
		    		Collections.unmodifiableList(branches), 
		    		fullProfile.getSites());
			
		}
		assert(j == samples.size());
		return pProfiles;
	}

	public static void main(String[] args) {
//		CBIClient client = new CBIClient(363, 10, new File("/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/sed/versions/v2/subv1/v2_subv1_f.sites"), 
//				"/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Subjects/sed/traces/v2/subv1/fine-grained/", 
//				"/home/sunzzq/Research/Automated_Bug_Isolation/Iterative/Console/m3.out");
	}
	
	private void run(PrintWriter writer){
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
		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
		for (int i = 0; i < sortedPredictorsList.size(); i++) {
			PredicateItemWithImportance pItemWI = sortedPredictorsList.get(i);
			if (i < k) {
				writer.println("(" + (i + 1) + "): " + pItemWI.toString());
				writer.println();
			}
		}
		
	}

	
	
	public List<PredicateItemWithImportance> getSortedPredictorsList() {
		return sortedPredictorsList;
	}


}
