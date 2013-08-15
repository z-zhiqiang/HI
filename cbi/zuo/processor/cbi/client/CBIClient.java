package zuo.processor.cbi.client;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.processor.PredicateItemWithImportance;
import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.predicatesite.BranchPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ReturnPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ScalarPairPredicateSite;

public class CBIClient {
	final int k;
	final PredicateProfile[] selectedPredicateProfiles;
	private TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors;
	
	private final Set<String> functions;
	private final Set<Integer> samples; 
	
	
	public CBIClient(int k, PredicateProfile[] profiles, PrintWriter writer, Set<String> functions, Set<Integer> samples) {
		this.k = k;
		this.functions = functions;
		this.samples = samples;
		assert(samples.size() == (int) (profiles.length * CBIClients.percent) || samples.size() == profiles.length);
		this.selectedPredicateProfiles = constructSelectedPredicateProfiles(profiles);
		
		this.sortedPredictors = new TreeMap<Double, SortedSet<PredicateItem>>();
		run(writer, profiles);
	}
	
	private PredicateProfile[] constructSelectedPredicateProfiles(PredicateProfile[] profiles) {
		// TODO Auto-generated method stub
		PredicateProfile[] pProfiles = new PredicateProfile[samples.size()];
		
		int j = 0;
		for(int k: samples){
			PredicateProfile fullProfile = profiles[k];
			
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
	
	private void run(PrintWriter writer, PredicateProfile[] profiles){
		Processor p = new Processor(selectedPredicateProfiles);
		p.process();
		assert(p.getTotalNegative() + p.getTotalPositive() == selectedPredicateProfiles.length);
		//print out the selected profiles' information
		printSelectedPredicateProfilesInformation(writer, profiles);
		
		//sort the list of predictors according to the importance value
		sortingPreditorsList(this.sortedPredictors, p.getPredictorsList());
		
		//print out top-k predictors information
		printTopKPredictors(this.sortedPredictors, this.k, writer);
	}

	private void printSelectedPredicateProfilesInformation(PrintWriter writer, PredicateProfile[] profiles) {
		// TODO Auto-generated method stub
		Set<Integer> neg = new TreeSet<Integer>();
		Set<Integer> pos = new TreeSet<Integer>();
		for(int s: samples){
			PredicateProfile profile = profiles[s];
			if(profile.isCorrect()){
				pos.add(s);
			}
			else{
				neg.add(s);
			}
		}
		assert(pos.size() + neg.size() == selectedPredicateProfiles.length);
		writer.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + (functions.size() == 1 ? functions.toString() : "FULLY INSTRUMENTED"));
		writer.println();
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-40s", "Total number of runs:") + selectedPredicateProfiles.length);
		writer.println(String.format("%-40s", "Total number of negative runs:") + neg.size());
		writer.println(compressNumbers(neg));
		writer.println(String.format("%-40s", "Total number of positive runs:") + pos.size());
		writer.println(compressNumbers(pos));
		writer.println();
	}

	public static String compressNumbers(Set<Integer> set) {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		for(Iterator<Integer> it = set.iterator(); it.hasNext();){
			int start = it.next();
			int current = start;
			while(it.hasNext()){
				int next = it.next();
				if(next - current == 1){
					current = next;
				}
				else{
					if(current == start){
						builder.append(start).append(", ");
					}
					else{
						builder.append(start).append("-").append(current).append(", ");
					}
					start = next;
					current = next;
				}
			}
			if(current == start){
				builder.append(start);
			}
			else{
				builder.append(start).append("-").append(current);
			}
			
		}
		builder.append("]");
		return builder.toString();
	}

	public static void sortingPreditorsList(TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, List<PredicateItemWithImportance> predictorsList) {
		// TODO Auto-generated method stub
		for(PredicateItemWithImportance pWI: predictorsList){
			double importance = pWI.getImportance();
			if(sortedPredictors.containsKey(importance)){
				sortedPredictors.get(importance).add(pWI.getPredicateItem());
			}
			else{
				SortedSet<PredicateItem> set = new TreeSet<PredicateItem>(new Comparator<PredicateItem>(){

					@Override
					public int compare(PredicateItem arg0, PredicateItem arg1) {
						// TODO Auto-generated method stub
						int r = new Integer(arg0.getPredicateSite().getId()).compareTo(new Integer(arg1.getPredicateSite().getId()));
						if(r == 0){
							r = new Integer(arg0.getType()).compareTo(new Integer(arg1.getType()));
						}
						return r;
					}
				});
				set.add(pWI.getPredicateItem());
				sortedPredictors.put(importance, set);
			}
		}
	}

	public static void printTopKPredictors(TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k, PrintWriter writer){
		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
		int i = 1, j = 0;
		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
			double im = it.next();
			if(im == 0){
				break;
			}
			SortedSet<PredicateItem> set = sortedPredictors.get(im);
			if(j < k){
				writer.println("(" + (i++) + "): " + im);
				for(PredicateItem item: set){
					writer.println(item.toString());
					writer.println();
				}
				j += set.size();
			}
			
		}
		writer.println("\n");
	}

	

	public TreeMap<Double, SortedSet<PredicateItem>> getSortedPredictors() {
		return sortedPredictors;
	}

	public Set<Integer> getSamples() {
		return samples;
	}


}
