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
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;

public class CBIClient_sampling {
//	private static final int FACTOR = 100;
	
	private final PredicateProfile[] profiles;
	private List<Integer> failings;
	private List<Integer> passings;
	
	private final int start;
	private final int factor;
	private FixPointStructure fixElement = null;
	
	public CBIClient_sampling(PredicateProfile[] profiles, final int start, final int factor) {
		this.profiles = profiles;
		this.factor = factor;

		divideProfiles();
		
		this.start = start;
	}
	
	private void divideProfiles() {
		System.out.println(this.factor);
		// TODO Auto-generated method stub
		List<Integer> passings = new ArrayList<Integer>();
		List<Integer> failings = new ArrayList<Integer>();
		for(int i = 0; i < profiles.length; i++){
			if(profiles[i].isCorrect()){
//				passings.add(i);
				for(int k = 0; k < this.factor; k++){
					passings.add(i + k * profiles.length);
				}
			}
			else{
//				failings.add(i);
				for(int k = 0; k < this.factor; k++){
					failings.add(i + k * profiles.length);
				}
			}
		}
		assert(passings.size() + failings.size() == profiles.length * this.factor);
		this.failings = Collections.unmodifiableList(failings);
		this.passings = Collections.unmodifiableList(passings);
	}
	
//	public PredicateProfile[] constructBasePredicateProfiles() {
//		// TODO Auto-generated method stub
//		PredicateProfile[] baseProfiles = new PredicateProfile[profiles.length * this.factor];
//		
//		for(int k = 0; k < baseProfiles.length; k++){
//			PredicateProfile fullProfile = profiles[k % profiles.length];
//			baseProfiles[k] = constructSampledProfile(fullProfile, 100);
//		}
//		return baseProfiles;
//	}
	
	private PredicateProfile constructSampledProfile(PredicateProfile fullProfile, int over_sample) {
		// TODO Auto-generated method stub
		List<ScalarPairPredicateSite> scalarPairs = new ArrayList<ScalarPairPredicateSite>();
		List<ReturnPredicateSite> returns = new ArrayList<ReturnPredicateSite>();
		List<BranchPredicateSite> branches = new ArrayList<BranchPredicateSite>();
		
		for(int i = 0; i < fullProfile.getScalarPredicateSites().size(); i++){
			ScalarPairPredicateSite scalarPairPSite = fullProfile.getScalarPredicateSites().get(i);
			ScalarPairPredicateSite samplePredicateSite = new ScalarPairPredicateSite(scalarPairPSite.getId(), ((ScalarSite) scalarPairPSite.getSite()), 
					sampling(scalarPairPSite.getLessCount(), over_sample), sampling(scalarPairPSite.getEqualCount(), over_sample), sampling(scalarPairPSite.getGreaterCount(), over_sample));
			scalarPairs.add(samplePredicateSite);
		}
		for(int i = 0; i < fullProfile.getReturnPredicateSites().size(); i++){
			ReturnPredicateSite returnPSite = fullProfile.getReturnPredicateSites().get(i);
			ReturnPredicateSite samplePSite = new ReturnPredicateSite(returnPSite.getId(), (ReturnSite) returnPSite.getSite(), 
					sampling(returnPSite.getLessCount(), over_sample), sampling(returnPSite.getEqualCount(), over_sample), sampling(returnPSite.getGreaterCount(), over_sample));
			returns.add(samplePSite);
		}
		for(int i = 0; i < fullProfile.getBranchPredicateSites().size(); i++){
			BranchPredicateSite branchPSite = fullProfile.getBranchPredicateSites().get(i);
			BranchPredicateSite samplePSite = new BranchPredicateSite(branchPSite.getId(), (BranchSite) branchPSite.getSite(), 
					sampling(branchPSite.getTrueCount(), over_sample), sampling(branchPSite.getFalseCount(), over_sample));
			branches.add(samplePSite);
		}
		
		PredicateProfile profile = new PredicateProfile(fullProfile.getPath(), fullProfile.isCorrect(), 
	    		Collections.unmodifiableList(scalarPairs), 
	    		Collections.unmodifiableList(returns), 
	    		Collections.unmodifiableList(branches));
	    
		return profile;
	}

	private int sampling(int count, int over_sample) {
		if(count == 0){
			return 0;
		}
		if(count >= over_sample){
			return 1;
		}
		Random random = new Random();
		int s = random.nextInt(over_sample) + 1;
		if(count >= s){
			return 1;
		}
		return 0;
	}
	
	/**generate base profiles -- only used when no enough memory
	 * @return
	 */
	public PredicateProfile[] constructBaseSamplePredicateProfiles() {
		// TODO Auto-generated method stub
		PredicateProfile[] baseProfiles = new PredicateProfile[profiles.length];

		for (int k = 0; k < baseProfiles.length; k++) {
			PredicateProfile fullProfile = profiles[k];
			baseProfiles[k] = constructSampledProfile(fullProfile, this.factor);
		}
		return baseProfiles;
	}
	
	/**construct selected profiles -- only used when no enough memory
	 * @param baseSampleProfiles
	 * @param failingSet
	 * @param passingSet
	 * @return
	 */
	private PredicateProfile[] constructSelectedPredicateProfiles(PredicateProfile[] baseSampleProfiles, Set<Integer> failingSet, Set<Integer> passingSet) {
		// TODO Auto-generated method stub
		PredicateProfile[] pProfiles = new PredicateProfile[failingSet.size() + passingSet.size()];
		
		int j = 0;
		
		for(int k: failingSet){
			pProfiles[j++] = baseSampleProfiles[k % profiles.length];
		}
		for(int k: passingSet){
			pProfiles[j++] = baseSampleProfiles[k % profiles.length];
		}
		
		assert(pProfiles.length == j);
		return pProfiles;
	}

	private PredicateProfile[] constructSelectedPredicateProfiles(PredicateProfile[] previousProfiles, Set<Integer> failingSet, Set<Integer> passingSet, Set<Integer> previousSet) {
		// TODO Auto-generated method stub
		PredicateProfile[] pProfiles = new PredicateProfile[failingSet.size() + passingSet.size()];
		
		int j = 0;
		
		for (int k = 0; k < previousProfiles.length; k++) {
			pProfiles[j++] = previousProfiles[k];

		}
		for(int k: failingSet){
			if (previousSet.contains(k)) {
				continue;
			}
			PredicateProfile fullProfile = profiles[k % profiles.length];
			pProfiles[j++] = constructSampledProfile(fullProfile, this.factor);
		}
		for(int k: passingSet){
			if (previousSet.contains(k)) {
				continue;
			}
			PredicateProfile fullProfile = profiles[k % profiles.length];
			pProfiles[j++] = constructSampledProfile(fullProfile, this.factor);
		}
		
		assert(pProfiles.length == j);
		return pProfiles;
	}

	
	public void runIterative(PrintWriter writer){
		CircularList cList = new CircularList(3);
		
		//only used when no enough memory
//		PredicateProfile[] baseProfiles = constructBaseSamplePredicateProfiles();
		
		PredicateProfile[] selectedPredicateProfiles = new PredicateProfile[0];
		
		Set<Integer> failingSet = new HashSet<Integer>();
		Set<Integer> passingSet = new HashSet<Integer>();
		
		Set<Integer> previousSet = new HashSet<Integer>();
		
		for(int i = start; i <= this.factor; i+=2){
			double per = (((double) 1) / this.factor) * i;
			System.out.println(per);
			increasePartialSamples(failingSet, passingSet, per, previousSet);
			
			selectedPredicateProfiles = constructSelectedPredicateProfiles(selectedPredicateProfiles, failingSet, passingSet, previousSet);
			
			//only used when no enough memory
//			selectedPredicateProfiles = constructSelectedPredicateProfiles(baseProfiles, failingSet, passingSet);
			
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
		System.out.println(this.fixElement.getPercent());
		System.out.println(this.fixElement.getFailingSet().size() + "\t" + this.fixElement.getPassingSet().size());
		
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
			if(writer != null){
				writer.println("==============================================================");
				printElement(currentElement, writer);
			}
			return true;
		}
		if(currentElement.getSortedPredictors().isEmpty() && previous1stElement.getSortedPredictors().isEmpty() && previous2ndElement.getSortedPredictors().isEmpty()){
			return true;
		}
		if(currentElement.getSortedPredictors().isEmpty() || previous1stElement.getSortedPredictors().isEmpty() || previous2ndElement.getSortedPredictors().isEmpty()){
			return false;
		}
		if(currentElement.getSortedPredictors().lastEntry().getValue().equals(previous1stElement.getSortedPredictors().lastEntry().getValue()) 
				&& currentElement.getSortedPredictors().lastEntry().getValue().equals(previous2ndElement.getSortedPredictors().lastEntry().getValue())){
			if(writer != null){
				writer.println("==============================================================");
				printElement(currentElement, writer);
				printElement(previous1stElement, writer);
				printElement(previous2ndElement, writer);
			}
			return true;
		}
		return false;
	}


	
	private void increasePartialSamples(Set<Integer> failingSet, Set<Integer> passingSet, double percent, Set<Integer> previousSet) {
		previousSet.addAll(passingSet);
		previousSet.addAll(failingSet);
		
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
		writer.println(String.format("%-40s", "Percentage:") + new DecimalFormat("#.###").format(element.getPercent()));
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
			runIterative(writer);
		}
		return fixElement;
	}

}
