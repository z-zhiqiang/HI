package zuo.processor.cbi.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import zuo.processor.cbi.processor.PredicateItem.BranchPredicateType;
import zuo.processor.cbi.processor.PredicateItem.FloatKindPredicateType;
import zuo.processor.cbi.processor.PredicateItem.ReturnScalarPairPredicateType;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.profile.predicatesite.AbstractPredicateSite;
import zuo.processor.cbi.profile.predicatesite.BranchPredicateSite;
import zuo.processor.cbi.profile.predicatesite.FloatKindPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ReturnPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ScalarPairPredicateSite;
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.FloatKindSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;

public class Processor {
	private final PredicateProfile[] profiles; // profiles
	private Map<PredicateItem, Double> predictors; // the results
	
	private int totalPositive; // number of passing runs
	private int totalNegative; // number of failing runs
	
	public Processor(PredicateProfile[] predicateProfiles){
		this.profiles = predicateProfiles;
		predictors = new HashMap<PredicateItem, Double>();
		totalPositive = totalNegative = 0;
	}
	
	public void process(){
		computeTotalPositiveNegative();
		processReturnPredicates();
		processBranchPredicates();
		processScalarPairsPredicates();
		processFloatKindsPredicates();
	}

	/**compute the number of failing runs and passing runs
	 * 
	 */
	private void computeTotalPositiveNegative() {
		// TODO Auto-generated method stub
		for (int i = 0; i < profiles.length; i++) {
			if(profiles[i].isCorrect()){
				this.totalPositive++;
			}
			else{
				this.totalNegative++;
			}
		}
		assert(totalPositive + totalNegative == profiles.length);
	}

	private void processFloatKindsPredicates() {
		for (int i = 0; i < profiles[0].getFloatKindPredicateSites().size(); i++) {
			int[][] statistics = new int[9][4];//{-Inf, Neg_Nor, Neg_Denor, -0, Nan, +0, Pos_Denor, Pos_Nor, +Inf}{F(p), F(p observed), S(p), S(p observed)}
			calculateFloatKindsStatisticsForEachPredicateSite(i, statistics);
			computeImportance(profiles[0].getFloatKindPredicateSites().get(i), statistics);
		}
	}

	private void calculateFloatKindsStatisticsForEachPredicateSite(int index, int[][] statistics) {
		if(statistics.length != 9){
			throw new RuntimeException();
		}
		
		for(int j = 0; j < profiles.length; j++){
			FloatKindPredicateSite fPredicate = profiles[j].getFloatKindPredicateSites().get(index);
			if(profiles[j].isCorrect()){// passing run
				if(fPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][3]++;
					}
				}
				else{
					continue;
				}
				
				if(fPredicate.getNegativeInfinite() != 0){
					statistics[0][2]++;
				}
				if(fPredicate.getNegativeNormalized() != 0){
					statistics[1][2]++;
				}
				if(fPredicate.getNegativeDenormalized() != 0){
					statistics[2][2]++;
				}
				if(fPredicate.getNegativeZero() != 0){
					statistics[3][2]++;
				}
				if(fPredicate.getNan() != 0){
					statistics[4][2]++;
				}
				if(fPredicate.getPositiveZero() != 0){
					statistics[5][2]++;
				}
				if(fPredicate.getPositiveDenormalized() != 0){
					statistics[6][2]++;
				}
				if(fPredicate.getPositiveNormalized() != 0){
					statistics[7][2]++;
				}
				if(fPredicate.getPositiveInfinite() != 0){
					statistics[8][2]++;
				}
			}
			else{// failing run
				if(fPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][1]++;
					}
				}
				else{
					continue;
				}
				
				if(fPredicate.getNegativeInfinite() != 0){
					statistics[0][0]++;
				}
				if(fPredicate.getNegativeNormalized() != 0){
					statistics[1][0]++;
				}
				if(fPredicate.getNegativeDenormalized() != 0){
					statistics[2][0]++;
				}
				if(fPredicate.getNegativeZero() != 0){
					statistics[3][0]++;
				}
				if(fPredicate.getNan() != 0){
					statistics[4][0]++;
				}
				if(fPredicate.getPositiveZero() != 0){
					statistics[5][0]++;
				}
				if(fPredicate.getPositiveDenormalized() != 0){
					statistics[6][0]++;
				}
				if(fPredicate.getPositiveNormalized() != 0){
					statistics[7][0]++;
				}
				if(fPredicate.getPositiveInfinite() != 0){
					statistics[8][0]++;
				}
			
			}
		}
	}

	/**process scalar-pair predicates
	 * 
	 */
	private void processScalarPairsPredicates() {
		for (int i = 0; i < profiles[0].getScalarPredicateSites().size(); i++) {
			//handle each predicate site
			int[][] statistics = new int[6][4];
			calculateScalarPairStatisticsForEachPredicateSite(i, statistics);
//			if(i == 0){
//				for (int m = 0; m < statistics.length; m++) {
//					for (int n = 0; n < statistics[m].length; n++) {
//						System.out.print(statistics[m][n] + "\t");
//					}
//					System.out.println();
//				}
//				System.out.println(profiles[0].getScalarPredicateSites().get(i).toString());
//			}
			computeImportance(profiles[0].getScalarPredicateSites().get(i), statistics);
		}
		
	}

	private void calculateScalarPairStatisticsForEachPredicateSite(int index, int[][] statistics) {
		assert(statistics.length == 6 && statistics[0].length == 4);
		for (int j = 0; j < profiles.length; j++){
			ScalarPairPredicateSite sPredicate = profiles[j].getScalarPredicateSites().get(index);
			if(profiles[j].isCorrect()){// passing run
				if(sPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][3]++;
					}
				}
				else{
					continue;
				}
				
				if(sPredicate.getLessCount() != 0){
					statistics[0][2]++;
				}
				if(sPredicate.getLessEqualCount() != 0){
					statistics[1][2]++;
				}
				if(sPredicate.getGreaterCount() != 0){
					statistics[2][2]++;
				}
				if(sPredicate.getGreaterEqualCount() != 0){
					statistics[3][2]++;
				}
				if(sPredicate.getEqualCount() != 0){
					statistics[4][2]++;
				}
				if(sPredicate.getNotEqualCount() != 0){
					statistics[5][2]++;
				}
			}
			else{// failing run
				if(sPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][1]++;
					}
				}
				else{
					continue;
				}
				
				if(sPredicate.getLessCount() != 0){
					statistics[0][0]++;
				}
				if(sPredicate.getLessEqualCount() != 0){
					statistics[1][0]++;
				}
				if(sPredicate.getGreaterCount() != 0){
					statistics[2][0]++;
				}
				if(sPredicate.getGreaterEqualCount() != 0){
					statistics[3][0]++;
				}
				if(sPredicate.getEqualCount() != 0){
					statistics[4][0]++;
				}
				if(sPredicate.getNotEqualCount() != 0){
					statistics[5][0]++;
				}
			}
		}
		
	}

	/**process branch predicates
	 * 
	 */
	private void processBranchPredicates() {
		for (int i = 0; i < profiles[0].getBranchPredicateSites().size(); i++){
			int[][] statistics = new int[2][4];//{true, false}{F(p), F(p observed), S(p), S(p observed)}
			calculateBranchStatisticsForEachPredicateSite(i, statistics);
//			if(i == 32){
//				for (int m = 0; m < statistics.length; m++) {
//					for (int n = 0; n < statistics[m].length; n++) {
//						System.out.print(statistics[m][n] + "\t");
//					}
//					System.out.println();
//				}
//				System.out.println(profiles[0].getBranchPredicateSites().get(i).toString());
//			}
			computeImportance(profiles[0].getBranchPredicateSites().get(i), statistics);
		}
		
	}

	private void calculateBranchStatisticsForEachPredicateSite(int index, int[][] statistics) {
		assert(statistics.length == 2 && statistics[0].length == 4);
		for (int j = 0; j < profiles.length; j++) {
			BranchPredicateSite bPredicate = profiles[j].getBranchPredicateSites().get(index);
			if(profiles[j].isCorrect()){
				if(bPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][3]++;
					}
				}
				else{
					continue;
				}
				
				if(bPredicate.getTrueCount() != 0){
					statistics[0][2]++;
				}
				if(bPredicate.getFalseCount() != 0){
					statistics[1][2]++;
				}
			}
			else{
				if(bPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][1]++;
					}
				}
				else{
					continue;
				}
				
				if(bPredicate.getTrueCount() != 0){
					statistics[0][0]++;
				}
				if(bPredicate.getFalseCount() != 0){
					statistics[1][0]++;
				}
			}
		}
		
	}

	/**process return predicates
	 * 
	 */
	private void processReturnPredicates() {
		for (int i = 0; i < profiles[0].getReturnPredicateSites().size(); i++) {
			int[][] statistics = new int[6][4];//{<, <=, >, >=, ==, !=}{F(p), F(p observed), S(p), S(p observed)}
			calculateReturnStatisticsForEachPredicateSite(i, statistics);
//			if(i == 32){
//				for (int m = 0; m < statistics.length; m++) {
//					for (int n = 0; n < statistics[m].length; n++) {
//						System.out.print(statistics[m][n] + "\t");
//					}
//					System.out.println();
//				}
//				System.out.println(profiles[0].getReturnPredicateSites().get(i).toString());
//				System.out.println(profiles[1].getReturnPredicateSites().get(i).toString());
//			}
			computeImportance(profiles[0].getReturnPredicateSites().get(i), statistics);
		}
	}

	private void calculateReturnStatisticsForEachPredicateSite(int index, int[][] statistics){
//		assert(statistics.length == 6 && statistics[0].length == 4);
		if(statistics.length != 6 || statistics[0].length != 4){
			throw new RuntimeException();
		}
		for (int j = 0; j < profiles.length; j++){
			ReturnPredicateSite rPredicate = profiles[j].getReturnPredicateSites().get(index);
			if(profiles[j].isCorrect()){// passing run
				if(rPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][3]++;
					}
				}
				else{
					continue;
				}
				
				if(rPredicate.getLessCount() != 0){
					statistics[0][2]++;
				}
				if(rPredicate.getLessEqualCount() != 0){
					statistics[1][2]++;
				}
				if(rPredicate.getGreaterCount() != 0){
					statistics[2][2]++;
				}
				if(rPredicate.getGreaterEqualCount() != 0){
					statistics[3][2]++;
				}
				if(rPredicate.getEqualCount() != 0){
					statistics[4][2]++;
				}
				if(rPredicate.getNotEqualCount() != 0){
					statistics[5][2]++;
				}
			}
			else{// failing run
				if(rPredicate.getTotalCount() != 0){
					for (int k = 0; k < statistics.length; k++) {
						statistics[k][1]++;
					}
				}
				else{
					continue;
				}
				
				if(rPredicate.getLessCount() != 0){
					statistics[0][0]++;
				}
				if(rPredicate.getLessEqualCount() != 0){
					statistics[1][0]++;
				}
				if(rPredicate.getGreaterCount() != 0){
					statistics[2][0]++;
				}
				if(rPredicate.getGreaterEqualCount() != 0){
					statistics[3][0]++;
				}
				if(rPredicate.getEqualCount() != 0){
					statistics[4][0]++;
				}
				if(rPredicate.getNotEqualCount() != 0){
					statistics[5][0]++;
				}
			}
		}
	}
	
	
	/**compute the importance scores of predicates corresponding to one instrumentation site
	 * @param predicateSite: one site
	 * @param statistics:
	 */
	private void computeImportance(AbstractPredicateSite predicateSite, int[][] statistics) {
		for (int i = 0; i < statistics.length; i++) {
			if(statistics[i][0] > statistics[i][1] || statistics[i][1] > totalNegative || statistics[i][2] > statistics[i][3] || statistics[i][3] > totalPositive)
				throw new RuntimeException("something wrong with statistics 1");
		}
		switch(predicateSite.getSite().getCategory()) {
		case BRANCH: {
			assert(statistics.length == 2);
			break;
		}
		case FLOAT_KIND: {
			assert(statistics.length == 9);
			break;
		}
		case RETURN: {
			assert(statistics.length == 6);
			break;
		}
		case SCALAR_PAIR: {
			assert(statistics.length == 6);
			break;
		}
		default:
			throw new RuntimeException("Category Error 2");
		}
		
		for (int i = 0; i < statistics.length; i++) {
			PredicateItem predicate = new PredicateItem(predicateSite.getSite(), predicateSite.getId(), i);
			double importance = Importance(statistics[i]);
			if(predictors.containsKey(predicate)){
				throw new RuntimeException("key value wrong");
			}
			predictors.put(predicate, importance);
		}
	}

	/**calculate importance for each predicate
	 * @param statisticData: {F(p), F(p observed), S(p), S(p observed)}
	 * @return
	 */
	private double Importance(int[] statisticData) {
		if(statisticData[0] <= 1 || (statisticData[2] + statisticData[0] == 0) || (statisticData[3] + statisticData[1] == 0)){
//			System.out.println("pre");
			return 0;
		}
		double increase = (double) statisticData[0]/(statisticData[2] + statisticData[0]) - (double) statisticData[1]/(statisticData[3] + statisticData[1]);
//		System.out.println(increase);
		if(increase < 0 || Math.abs(increase - 0) < 0.0000001){
//			System.out.println("mid");
			return 0;
		}
//		System.out.println("post");
		return 2/(1/increase + Math.log(totalNegative)/Math.log(statisticData[0]));
	}
	
	public void printTopKPredictors(int k){
		List list = new ArrayList(predictors.entrySet());
		Collections.sort(list, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return ((Map.Entry<PredicateItem, Double>) arg1).getValue()
						.compareTo(((Entry<PredicateItem, Double>) arg0).getValue());
			}
			});
		for (int i = 0; i < k && i < list.size(); i++) {
			Entry<PredicateItem, Double> entry = (Entry<PredicateItem, Double>) list.get(i);
			System.out.println(entry.getKey().toString() + ":  " + entry.getValue());
		}
	}

	
	public static void main(String[] args) {
		PredicateProfileReader reader = new PredicateProfileReader("/home/sunzzq/Research/grep/traces/v1/fine-grained", "/home/sunzzq/Research/grep/versions/v1/v1_f.sites");
		PredicateProfile[] profiles = reader.readProfiles(4000);
		Processor p = new Processor(profiles);
		p.process();
		System.out.println(p.getTotalNegative());
		System.out.println(p.getTotalPositive());
		System.out.println(p.getPredictors().size());
//		System.out.println(p.getPredictors().toString());
		p.printTopKPredictors(10);
		
		int num = profiles[0].getBranchPredicateSites().size() * 2 + profiles[0].getFloatKindPredicateSites().size() * 9 
				+ profiles[0].getScalarPredicateSites().size() * 6 + profiles[0].getReturnPredicateSites().size() * 6;
		System.out.println(num);
		
		int num2 = 0;
		for(Entry<String, List<BranchSite>> entry: reader.getSites().getBranchSites().entrySet()){
			num2 += entry.getValue().size() * 2;
		}
		for(Entry<String, List<FloatKindSite>> entry: reader.getSites().getFloatSites().entrySet()){
			num2 += entry.getValue().size() * 9;
		}
		for(Entry<String, List<ReturnSite>> entry: reader.getSites().getReturnSites().entrySet()){
			num2 += entry.getValue().size() * 6;
		}
		for(Entry<String, List<ScalarSite>> entry: reader.getSites().getScalarSites().entrySet()){
			num2 += entry.getValue().size() * 6;
		}
		System.out.println(num2);
		
		
	}
	
	
	public int getTotalPositive() {
		return totalPositive;
	}

	public void setTotalPositive(int totalPositive) {
		this.totalPositive = totalPositive;
	}

	public int getTotalNegative() {
		return totalNegative;
	}

	public void setTotalNegative(int totalNegative) {
		this.totalNegative = totalNegative;
	}

	public Map<PredicateItem, Double> getPredictors() {
		return predictors;
	}

	public void setPredictors(Map<PredicateItem, Double> predictors) {
		this.predictors = predictors;
	}


	public PredicateProfile[] getProfiles() {
		return profiles;
	}

}
