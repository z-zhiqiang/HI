package zuo.processor.cbi.processor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.profile.predicatesite.AbstractPredicateSite;
import zuo.processor.cbi.profile.predicatesite.BranchPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ReturnPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ScalarPairPredicateSite;
import zuo.processor.cbi.site.InstrumentationSites;

public class Processor {
	private final PredicateProfile[] profiles; // profiles
	private List<PredicateItemWithImportance> predictorsList; // the results
	
	private final int totalPositive; // number of passing runs
	private final int totalNegative; // number of failing runs
	
	public Processor(PredicateProfile[] predicateProfiles){
		int passing = 0, failing = 0;
		for(int i = 0; i < predicateProfiles.length; i++){
			if(predicateProfiles[i].isCorrect()){
				passing++;
			}
			else{
				failing++;
			}
		}
		totalNegative = failing;
		totalPositive = passing;
		
		profiles = predicateProfiles;
		predictorsList = new ArrayList<PredicateItemWithImportance>();
	}
	
	public void process(){
		processReturnPredicates();
		processBranchPredicates();
		processScalarPairsPredicates();
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
			assert(sPredicate.getId() == profiles[0].getScalarPredicateSites().get(index).getId());
			assert(sPredicate.getSite() == profiles[0].getScalarPredicateSites().get(index).getSite());
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
			assert(bPredicate.getId() == profiles[0].getBranchPredicateSites().get(index).getId());
			assert(bPredicate.getSite() == profiles[0].getBranchPredicateSites().get(index).getSite());
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
			assert(rPredicate.getId() == profiles[0].getReturnPredicateSites().get(index).getId());
			assert(rPredicate.getSite() == profiles[0].getReturnPredicateSites().get(index).getSite());
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
		
//		//for debugging
//		if(predicateSite.getSite().getFunctionName().equals("nodedef")){
//			System.out.println();
//			System.out.println(predicateSite.toString());
//		}		
		
		for (int i = 0; i < statistics.length; i++) {
			PredicateItem predicate = new PredicateItem(predicateSite, i);
			double importance = Importance(statistics[i]);
//			//for debugging
//			if(predicateSite.getSite().getFunctionName().equals("nodedef")){
//				for(int j = 0; j < statistics[i].length; j++){
//					System.out.print(statistics[i][j] + "\t");
//				}
//				System.out.println(importance);
//			}
			
			if(importance == 0){
				continue;
			}
			PredicateItemWithImportance pItemWI = new PredicateItemWithImportance(predicate, importance);
			predictorsList.add(pItemWI);
			
//			if(predictors.containsKey(importance)){
//				predictors.get(importance).add(predicate);
//			}
//			else{
//				Set<PredicateItem> set = new HashSet<PredicateItem>();
//				set.add(predicate);
//				predictors.put(importance, set);
//			}
		}
	}

	/**calculate importance for each predicate
	 * @param statisticData: {F(p), F(p observed), S(p), S(p observed)} -> {neg_t, neg, pos_t, pos}
	 * @return
	 */
	private double Importance(int[] statisticData) {
		return importance(statisticData[0], statisticData[2], statisticData[1], statisticData[3], this.totalNegative, this.totalPositive);
	}

	/**compute the importance value for each predicate
	 * @param neg_t
	 * @param pos_t
	 * @param neg
	 * @param pos
	 * @param totalNeg
	 * @param totalPos
	 * @return
	 */
	public static double importance(int neg_t, int pos_t, int neg, int pos, int totalNeg, int totalPos){
		assert(neg_t <= neg && pos_t <= pos);
		if(neg_t <= 1 || pos_t + neg_t == 0){
			return 0;
		}
		double increase = (double) neg_t/(pos_t + neg_t) - (double) neg/(pos + neg);
		if(increase < 0 || Math.abs(increase - 0) < 0.0000001){
			return 0;
		}
		return (double) 2/(1/increase + Math.log(totalNeg)/Math.log(neg_t));
	}
	
	public static void main(String[] args) {
		InstrumentationSites sites = new InstrumentationSites(new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Subjects\\space\\versions\\v23\\v23_f.sites"));
//		SitesInfo sInfo = new SitesInfo(sites);
		PredicateProfileReader reader = new PredicateProfileReader(new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Subjects\\space\\traces\\v23\\fine-grained"), sites);
		PredicateProfile[] profiles = reader.readProfiles();
		Processor p = new Processor(profiles);
		p.process();
	}
	
	
	public int getTotalPositive() {
		return totalPositive;
	}

	public int getTotalNegative() {
		return totalNegative;
	}

	public PredicateProfile[] getProfiles() {
		return profiles;
	}

	public List<PredicateItemWithImportance> getPredictorsList() {
		return predictorsList;
	}


}
