package zuo.processor.cbi.client;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import zuo.processor.cbi.datastructure.CircularList;
import zuo.processor.cbi.datastructure.FixPointStructure;
import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.processor.PredicateItemWithImportance;
import zuo.processor.cbi.processor.Processor;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.predicatesite.BranchPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ReturnPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ScalarPairPredicateSite;

public class CBIClient {
	private final PredicateProfile[] profiles;
	private final Set<String> functions;
	private List<Integer> failings;
	private List<Integer> passings;
	
	private final int start;
	private FixPointStructure fixElement = null;
	
	public CBIClient(PredicateProfile[] profiles, Set<String> functions, List<Integer> failings, List<Integer> passings, final int start) {
		this.profiles = profiles;
		this.functions = functions;
		this.failings = failings;
		this.passings = passings;
		
		this.start = start;
	}
	
	public PredicateProfile[] constructBasePredicateProfiles() {
		// TODO Auto-generated method stub
		PredicateProfile[] baseProfiles = new PredicateProfile[profiles.length];
		
		for(int k = 0; k < baseProfiles.length; k++){
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
			
		    baseProfiles[k] = new PredicateProfile(fullProfile.getPath(), fullProfile.isCorrect(), 
		    		Collections.unmodifiableList(scalarPairs), 
		    		Collections.unmodifiableList(returns), 
		    		Collections.unmodifiableList(branches));
			
		}
		return baseProfiles;
	}
	
	private PredicateProfile[] constructSelectedPredicateProfiles(PredicateProfile[] baseProfiles, Set<Integer> failingSet, Set<Integer> passingSet) {
		// TODO Auto-generated method stub
		PredicateProfile[] pProfiles = new PredicateProfile[failingSet.size() + passingSet.size()];
		
		int j = 0;
		for(int k: failingSet){
			pProfiles[j++] = baseProfiles[k];
		}
		for(int k: passingSet){
			pProfiles[j++] = baseProfiles[k];
		}
		return pProfiles;
	}

	
	public void runIterative(PrintWriter writer){
		CircularList cList = new CircularList(3);
		
		PredicateProfile[] baseProfiles = constructBasePredicateProfiles();
		
		Set<Integer> failingSet = new HashSet<Integer>();
		Set<Integer> passingSet = new HashSet<Integer>();
		
		for(int i = start; i <= 10; i++){
			double per = 0.1 * i;
			increasePartialSamples(failingSet, passingSet, per);
			
			PredicateProfile[] selectedPredicateProfiles = constructSelectedPredicateProfiles(baseProfiles, failingSet, passingSet);
			
			Processor p = new Processor(selectedPredicateProfiles);
			p.process();
			assert(p.getTotalNegative() + p.getTotalPositive() == selectedPredicateProfiles.length);
			
			//sort the list of predictors according to the importance value
			TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors = new TreeMap<Double, SortedSet<PredicateItem>>();
			sortingPreditorsList(sortedPredictors, p.getPredictorsList());
			
			cList.insertElement(new FixPointStructure(sortedPredictors,
					new TreeSet<Integer>(failingSet), new TreeSet<Integer>(passingSet), per));
			
			if(isFixPoint(cList, writer)){
				break;
			}
		}
		this.fixElement = cList.getCurrentElement();
		
	}
	
	private boolean isFixPoint(CircularList cList, PrintWriter writer) {
		// TODO Auto-generated method stub
		if(cList.getNumberOfElements() < cList.getMaxSize()){
			return false;
		}
		FixPointStructure currentElement = cList.getCurrentElement();
		FixPointStructure previous1stElement = cList.getPreviousKthElement(1);
		FixPointStructure previous2ndElement = cList.getPreviousKthElement(2);
		
		if(currentElement.getPercent() == 0.1 * 10){
			writer.println(functions.toString());
			writer.println("==============================================================");
			printElement(currentElement, writer);
			return true;
		}
		if(currentElement.getSortedPredictors().isEmpty() || previous1stElement.getSortedPredictors().isEmpty() || previous2ndElement.getSortedPredictors().isEmpty()){
			return false;
		}
		if(currentElement.getSortedPredictors().lastEntry().getValue().equals(previous1stElement.getSortedPredictors().lastEntry().getValue()) 
				&& currentElement.getSortedPredictors().lastEntry().getValue().equals(previous2ndElement.getSortedPredictors().lastEntry().getValue())){
			writer.println(functions.toString());
			writer.println("==============================================================");
			printElement(currentElement, writer);
			printElement(previous1stElement, writer);
			printElement(previous2ndElement, writer);
			return true;
		}
		return false;
	}

	public void runFull(){
		Processor p = new Processor(profiles);
		p.process();
		assert(p.getTotalNegative() + p.getTotalPositive() == profiles.length);
		
		//sort the list of predictors according to the importance value
		TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors = new TreeMap<Double, SortedSet<PredicateItem>>();
		sortingPreditorsList(sortedPredictors, p.getPredictorsList());
		
		this.fixElement = new FixPointStructure(sortedPredictors,
				new TreeSet<Integer>(failings), new TreeSet<Integer>(passings) , 1.0);
	}

	
	private void increasePartialSamples(Set<Integer> failingSet, Set<Integer> passingSet, double percent) {
		Random randomFGenerator = new Random();
		int fs = (int) (failings.size() * percent);
		for(; failingSet.size() < (fs > 2 ? fs : 2);){
			int fSampleIndex = randomFGenerator.nextInt(failings.size());
			failingSet.add(failings.get(fSampleIndex));
		}
		
		Random randomPGenerator = new Random();
		int ps = (int) (passings.size() * percent);
		for(; passingSet.size() < ps;){
			int pSampleIndex = randomPGenerator.nextInt(passings.size());
			passingSet.add(passings.get(pSampleIndex));
		}
	}
	
	public void printElement(FixPointStructure element, PrintWriter writer){
		printSelectedPredicateProfilesInformation(element, writer);
		printTopK(element.getSortedPredictors(), CBIClients.iK, writer);
	}

	public void printSelectedPredicateProfilesInformation(FixPointStructure element, PrintWriter writer) {
		// TODO Auto-generated method stub
		Set<Integer> neg = element.getFailingSet();
		Set<Integer> pos = element.getPassingSet();
		writer.println("The general runs information are as follows:");
		writer.println("--------------------------------------------------------------");
		writer.println(String.format("%-40s", "Percentage:") + new DecimalFormat("#.#").format(element.getPercent()));
		writer.println(String.format("%-40s", "Total number of runs:") + (neg.size() + pos.size()));
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
			if(importance == 0){
				continue;
			}
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

	public static void printTopK(TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k, PrintWriter writer){
//		printTopKPredictors(sortedPredictors, k, writer);
		printTopKImportances(sortedPredictors, k, writer);
	}

//	/**print out the top k predictors
//	 * @param sortedPredictors
//	 * @param k
//	 * @param writer
//	 */
//	private static void printTopKPredictors(TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k, PrintWriter writer){
//		writer.println("The top " + k + " predicates are as follows:\n==============================================================");
//		int i = 1, j = 0;
//		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
//			double im = it.next();
//			if(im == 0){
//				break;
//			}
//			SortedSet<PredicateItem> set = sortedPredictors.get(im);
//			if(j < k){
//				writer.println("(" + (i++) + "): " + im);
//				for(PredicateItem item: set){
//					writer.println(item.toString());
//					writer.println();
//				}
//				j += set.size();
//			}
//			else{
//				break;
//			}
//		}
//		writer.println("\n");
//	}
	
	/**print out the predictors with the top k importance values
	 * @param sortedPredictors
	 * @param k
	 * @param writer
	 */
	private static void printTopKImportances(TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k, PrintWriter writer){
		writer.println("The predictors with top " + k + " Importance values are as follows:");
		writer.println("--------------------------------------------------------------");
		int i = 1;
		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
			double im = it.next();
			if(im == 0){
				break;
			}
			if(i <= k){
				writer.println("(" + (i++) + "): " + im);
				for(PredicateItem item: sortedPredictors.get(im)){
					writer.println(item.toString());
					writer.println();
				}
			}
			else{
				break;
			}
		}
		writer.println("\n");
	}
	

	public FixPointStructure getFixElement(PrintWriter writer) {
		if(fixElement == null){
			assert(functions.size() == 1);
			runIterative(writer);
		}
		return fixElement;
	}

	public FixPointStructure getFullFixElement(){
		if(fixElement == null){
			runFull();
		}
		return fixElement;
	}
}
