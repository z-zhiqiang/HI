package zuo.processor.functionentry.client.iterative;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.BoundCalculator;
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.processor.SelectingProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class IterativeFunctionClient {
	private boolean pTFlag, lCFlag, gCFlag, gPFlag;
	public static enum Score{
//		RANDOM, 
		NEGATIVE, H_1, H_2, F_1, 
//		PRECISION, POSITIVE
	}
	
	public static enum Order{
		RANDOM, LESS_FIRST, MORE_FIRST, WORST, BEST, //CLOSER_FIRST 
	}
	
	final FunctionEntrySites sites;
	final FunctionEntryProfile[] selectedFunctionEntryProfiles;
    final TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors;
    final TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors;//for comparison
	
	final SitesInfo sInfo;
	final String targetFunction;
	final Set<Integer> samples;
	final Map<String, CBIClient> clientsMap;
	
	final double[][][][] result;
	final double[][][] pResult;
	final int[] cResult;//{methods, sites, predicates}
	
	final Map<Score, List<String>> prunedMethodsList;
	final Map<Score, List<String>> methodsList;
	
	public IterativeFunctionClient(FunctionEntrySites sites, FunctionEntryProfile[] profiles, File consoleFile, SitesInfo sInfo, CBIClient fullICBIClient, Map<String, CBIClient> map) {
		this.pTFlag = true;
		this.lCFlag = true;
		this.gCFlag = true;
		this.gPFlag = true;
		
		this.sites = sites;
		this.sInfo = sInfo;
		this.targetFunction = getTargetFunction(fullICBIClient);
		this.samples = fullICBIClient.getSamples();
		this.clientsMap = map;
		this.result = new double[Score.values().length][Order.values().length][2][5];
		this.pResult = new double[Score.values().length][Order.values().length][5];
		this.cResult = new int[3];
		
		this.prunedMethodsList = new HashMap<Score, List<String>>();
		this.methodsList = new HashMap<Score, List<String>>();
		
		this.selectedFunctionEntryProfiles = constructSelectedFunctionEntryProfiles(profiles);
		
		this.sortedPrunedPredictors = new TreeMap<Double, SortedSet<PredicateItem>>();
		this.sortedPredictors = fullICBIClient.getSortedPredictors();
		
		PrintWriter writer = null;
		try {
			if(!consoleFile.getParentFile().exists()){
				consoleFile.getParentFile().mkdirs();
			}
			writer = new PrintWriter(new BufferedWriter(new FileWriter(consoleFile)));
			run(writer, profiles);
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
	
	
	/**get the target function within which the top predictor is.
	 * @param fullICBIClient
	 * @return
	 */
	public static String getTargetFunction(CBIClient fullICBIClient) {
		// TODO Auto-generated method stub
		Set<String> functionSet = new LinkedHashSet<String>();
		for(PredicateItem item: fullICBIClient.getSortedPredictors().lastEntry().getValue()){
			functionSet.add(item.getPredicateSite().getSite().getFunctionName());
		}
		if(functionSet.size() != 1){
			System.out.println(functionSet);
		}
		
		return getOneFunction(functionSet);
	}

	private static String getOneFunction(Set<String> functionSet) {
		// TODO Auto-generated method stub
		Iterator<String> it = functionSet.iterator();
		String function = it.next();
//		while(it.hasNext()){
//			String fun = it.next();
//			if(this.sInfo.getMap().get(fun).getNumSites() < this.sInfo.getMap().get(function).getNumSites()){
//				function = fun;
//			}
//			else if(this.sInfo.getMap().get(fun).getNumSites() == this.sInfo.getMap().get(function).getNumSites() 
//					&& this.sInfo.getMap().get(fun).getNumPredicates() < this.sInfo.getMap().get(function).getNumPredicates()){
//				function = fun;
//			}
//		}
		return function;
	}


	private FunctionEntryProfile[] constructSelectedFunctionEntryProfiles(FunctionEntryProfile[] profiles) {
		// TODO Auto-generated method stub
		FunctionEntryProfile[] fProfiles = new FunctionEntryProfile[samples.size()];
		int j = 0;
		for(int k: samples){
			fProfiles[j++] = profiles[k];
		}
		assert(j == samples.size());
		return fProfiles;
	}


	private void run(PrintWriter writer, FunctionEntryProfile[] profiles){
		SelectingProcessor processor = new SelectingProcessor(selectedFunctionEntryProfiles);
		processor.process();
		
		//print out the general runs information
		assert(processor.getTotalNegative() + processor.getTotalPositive() == selectedFunctionEntryProfiles.length);
		printSelectedFunctionEntryProfilesInformation(writer, profiles);
		
		//print out the static instrumentation sites information 
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		printSitesInformation(writer);
		
		this.cResult[0] = sites.getNumFunctionEntrySites();
		this.cResult[1] = sInfo.getNumPredicateSites();
		this.cResult[2] = sInfo.getNumPredicateItems();
		
		//filter out methods within which no predicates are instrumented
		filterFrequencyMap(processor.getFrequencyMap());
		assert(processor.getFrequencyMap().size() == this.sInfo.getMap().size());
		
		//print out entry and percentage information
		for(Score score: Score.values()){
			BoundCalculator bc = new BoundCalculator(processor.getTotalNegative(), processor.getTotalPositive());
			for(Order order: Order.values()){
				List<Entry<FunctionEntrySite, FrequencyValue>> list = sortFunctionEntrySiteMap(processor.getFrequencyMap(), score, order);
				printEntryAndPercentage(list, score, order, bc, writer);
			}
		}
	}


	private void printSitesInformation(PrintWriter writer) {
		writer.println("The general methods information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of methods instrumented:") + sites.getNumFunctionEntrySites());
		writer.println("\n");
	}


	private void printSelectedFunctionEntryProfilesInformation(PrintWriter writer, FunctionEntryProfile[] profiles) {
		Set<Integer> neg = new TreeSet<Integer>();
		Set<Integer> pos = new TreeSet<Integer>();
		for(int s: samples){
			FunctionEntryProfile profile = profiles[s];
			if(profile.isCorrect()){
				pos.add(s);
			}
			else{
				neg.add(s);
			}
		}
		assert(pos.size() + neg.size() == selectedFunctionEntryProfiles.length);
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-40s", "Total number of runs:") + selectedFunctionEntryProfiles.length);
		writer.println(String.format("%-40s", "Total number of negative runs:") + neg.size());
		writer.println(CBIClient.compressNumbers(neg));
		writer.println(String.format("%-40s", "Total number of positive runs:") + pos.size());
		writer.println(CBIClient.compressNumbers(pos));
		writer.println("\n");
	}
	
	public static void appendPredictors(TreeMap<Double, SortedSet<PredicateItem>> fullSortedPredictors, TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors) {
		// TODO Auto-generated method stub
		for(double im: sortedPredictors.keySet()){
			if(fullSortedPredictors.containsKey(im)){
				fullSortedPredictors.get(im).addAll(sortedPredictors.get(im));
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
				set.addAll(sortedPredictors.get(im));
				fullSortedPredictors.put(im, set);
			}
		}
	}
	
	private void checkGlobalConsistency(int k, Score score, Order order, int mode) {
		// TODO Auto-generated method stub
		Set<PredicateItem> pruneSet = new HashSet<PredicateItem>();
//		getTopKPredictors(pruneSet, this.sortedPrunedPredictors, k);
		getTopKImportances(pruneSet, this.sortedPrunedPredictors, k);
		
		Set<PredicateItem> originalSet = new HashSet<PredicateItem>();
//		getTopKPredictors(originalSet, this.sortedPredictors, k);
		getTopKImportances(originalSet, this.sortedPredictors, k);
		
		if(!originalSet.equals(pruneSet)){
//			System.out.println("Original: \t" + originalSet.toString());
//			System.out.println("Pruned: \t" + pruneSet.toString());
			if(mode == 0){
				System.out.println("gCFlag==false: " + score + "_" + order);
				gCFlag = false;				
			}
			else if(mode == 1){
				System.out.println("pFlag==false: " + score + "_" + order);
				gPFlag = false;	
			}
		}
	}


	private void getTopKImportances(Set<PredicateItem> set, TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k) {
		// TODO Auto-generated method stub
		int i = 1;
		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
			double im = it.next();
			if(im == 0){
				break;
			}
			if(i <= k){
				set.addAll(sortedPredictors.get(im));
				i++;
			}
			else{
				break;
			}
		}
	}


	private void getTopKPredictors(Set<PredicateItem> set, TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k) {
		// TODO Auto-generated method stub
		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
			double im = it.next();
			if(im == 0){
				break;
			}
			if(set.size() < k){
				set.addAll(sortedPredictors.get(im));
			}
			else{
				break;
			}
		}
	}

	private double getKth(TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors, int k){
//		return getKthPredictor(sortedPrunedPredictors, k);
		return getKthImportance(sortedPrunedPredictors, k);
	}

	/**get the kth predictor's importance value
	 * @param sortedPrunedPredictors
	 * @param k
	 * @return
	 */
	private double getKthPredictor(TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors, int k) {
		// TODO Auto-generated method stub
		int j = 1;
		double pim, im = 0;
		for(Iterator<Double> it = sortedPrunedPredictors.descendingKeySet().iterator(); it.hasNext();){
			pim = im;
			im = it.next();
			if(im == 0){
				return pim;
			}
			if(j < k){
				j += sortedPrunedPredictors.get(im).size();
			}
			else if(j == k){
				return im;
			}
			else{
				return pim;
			}
		}
		return 0;
	}

	/**get the kth importance value
	 * @param sortedPrunedPredictors
	 * @param k
	 * @return
	 */
	private double getKthImportance(TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors, int k){
		int j = 1;
		double pim, im = 0;
		for(Iterator<Double> it = sortedPrunedPredictors.descendingKeySet().iterator(); it.hasNext(); j++){
			pim = im;
			im = it.next();
			if(im == 0){
				return pim;
			}
			if(j == k){
				return im;
			}
		}
		return 0;
	}
	

	/**print the methods and the corresponding percentage to be instrumented in the mode <score,order>
	 * @param frequencyMap
	 * @param score
	 * @param order
	 * @param bc 
	 * @param writer 
	 */
	private void printEntryAndPercentage(List<Entry<FunctionEntrySite, FrequencyValue>> list, final Score score, final Order order, BoundCalculator bc, PrintWriter writer) {
		// TODO Auto-generated method stub
		String mode = "<" + score + "," + order + ">";
		writer.println("The methods ordered by " + mode + " are as follows:\n--------------------------------------------------------------");
		printEntry(list, writer);
		writer.println("The information of sites and predicates need to be instrumented " + mode + " are as follows:\n--------------------------------------------------------------");
		printPercentage(list, score, order, bc, writer);
	}


	private List<Entry<FunctionEntrySite, FrequencyValue>> sortFunctionEntrySiteMap(Map<FunctionEntrySite, FrequencyValue> frequencyMap, final Score score, final Order order) {
		List<Entry<FunctionEntrySite, FrequencyValue>> list = new ArrayList<Entry<FunctionEntrySite, FrequencyValue>>(frequencyMap.entrySet());
		Collections.sort(list, new Comparator<Entry<FunctionEntrySite, FrequencyValue>>(){

			@Override
			public int compare(Entry<FunctionEntrySite, FrequencyValue> o1,
					Entry<FunctionEntrySite, FrequencyValue> o2) {
				// TODO Auto-generated method stub
				return rank(o1, o2, score, order);
			}});
		return list;
	}
		
	/**rank the methods in the mode <score,order>
	 * @param arg0
	 * @param arg1
	 * @param score
	 * @param order
	 * @return
	 */
	private int rank(Entry<FunctionEntrySite, FrequencyValue> arg0, Entry<FunctionEntrySite, FrequencyValue> arg1, Score score, Order order) {
		// TODO Auto-generated method stub
		int r = 0;
		switch (score){
//		case RANDOM:
//			break;
		case NEGATIVE:
			r = new Integer(arg1.getValue().getNegative()).compareTo(new Integer(arg0.getValue().getNegative()));
			if(r == 0){
				r = order(arg0, arg1, order);
			}
			break;
		case H_1:
			r = new Double(arg1.getValue().getH_1()).compareTo(new Double(arg0.getValue().getH_1()));
			if(r == 0){
//				r = new Double(arg1.getValue().getH_2()).compareTo(new Double(arg0.getValue().getH_2()));
//				if(r == 0){
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
		case H_2:
			r = new Double(arg1.getValue().getH_2()).compareTo(new Double(arg0.getValue().getH_2()));
			if(r == 0){
//				r = new Double(arg1.getValue().getH_1()).compareTo(new Double(arg0.getValue().getH_1()));
//				if(r == 0){
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
		case F_1:
			r = new Double(arg1.getValue().getF_score()).compareTo(new Double(arg0.getValue().getF_score()));
			if (r == 0) {
//				r = new Double(arg1.getValue().getH_1()).compareTo(new Double(arg0.getValue().getH_1()));
//				if (r == 0) {
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
//		case PRECISION:
//			r = new Double(arg1.getValue().getPrecision()).compareTo(new Double(arg0.getValue().getPrecision()));
//			if (r == 0) {
////				r = new Double(arg1.getValue().getF_score()).compareTo(new Double(arg0.getValue().getF_score()));
////				if(r == 0){
////					r = order(arg0, arg1, order);
////				}
//				r = order(arg0, arg1, order);
//			}
//			break;
//		case POSITIVE:
//			r = new Integer(arg0.getValue().getPositive()).compareTo(new Integer(arg1.getValue().getPositive()));
//			if(r == 0){
////				r = new Double(arg1.getValue().getF_score()).compareTo(new Double(arg0.getValue().getF_score()));
////				if(r == 0){
////					r = order(arg0, arg1, order);
////				}
//				r = order(arg0, arg1, order);
//			}
//			break;
		default:{
			System.err.println("score error");
			System.exit(0);
		}
		}
		return r;
	}
	
	/**rank the methods with same score in different orders
	 * @param arg0
	 * @param arg1
	 * @param order
	 * @return
	 */
	private int order(Entry<FunctionEntrySite, FrequencyValue> arg0, Entry<FunctionEntrySite, FrequencyValue> arg1, Order order) {
		int rr = 0;
		String method0 = null, method1 = null;
		switch (order) {
		case RANDOM:
			//random order
			break;
		case LESS_FIRST:
			//less first
			method0 = arg0.getKey().getFunctionName();
			method1 = arg1.getKey().getFunctionName();
			rr = new Integer(sInfo.getMap().get(method0).getNumSites()).compareTo(new Integer(sInfo.getMap().get(method1).getNumSites()));
			if(rr == 0){
				rr = new Integer(sInfo.getMap().get(method0).getNumPredicates()).compareTo(new Integer(sInfo.getMap().get(method1).getNumPredicates()));
			}
			break;
		case MORE_FIRST:
			//more first
			method0 = arg0.getKey().getFunctionName();
			method1 = arg1.getKey().getFunctionName();
			rr = new Integer(sInfo.getMap().get(method1).getNumSites()).compareTo(new Integer(sInfo.getMap().get(method0).getNumSites()));
			if(rr == 0){
				rr = new Integer(sInfo.getMap().get(method1).getNumPredicates()).compareTo(new Integer(sInfo.getMap().get(method0).getNumPredicates()));
			}
			break;
		case WORST:
			//worst order
			if(arg1.getKey().getFunctionName().equals(targetFunction)){
				rr = -1;
			}
			if(arg0.getKey().getFunctionName().equals(targetFunction)){
				rr = 1;
			}
			break;
		case BEST:
			//best order
			if(arg1.getKey().getFunctionName().equals(targetFunction)){
				rr = 1;
			}
			if(arg0.getKey().getFunctionName().equals(targetFunction)){
				rr = -1;
			}
			break;
		default:
			System.err.println("ordering mode error");
			System.exit(0);
		}
		
		return rr;
	}

	/**filter out the methods having no instrumented predicates
	 * @param frequencyMap
	 */
	private void filterFrequencyMap(Map<FunctionEntrySite, FrequencyValue> frequencyMap) {
		// TODO Auto-generated method stub
		for(Iterator<FunctionEntrySite> it = frequencyMap.keySet().iterator(); it.hasNext();){
			String function = it.next().getFunctionName();
			if(!sInfo.getMap().containsKey(function)){
				it.remove();
			}
		}
	}
	
	/**print out the method entries
	 * @param list
	 * @param writer 
	 */
	private void printEntry(List<Entry<FunctionEntrySite, FrequencyValue>> list, PrintWriter writer) {
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				writer.println(String.format("%-45s", method) + entry.getValue().toString() + "\t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				throw new RuntimeException("filtering error");
			}
		}
		writer.println();
	}

	/**print out the percentage instrumented before reaching the top predictor
	 * @param list
	 * @param score
	 * @param order
	 * @param bc 
	 * @param writer 
	 */
	private void printPercentage(List<Entry<FunctionEntrySite, FrequencyValue>> list, Score score, Order order, BoundCalculator bc, PrintWriter writer) {
		double threshold = 0;
		boolean skip = false;
		int i = 0;
		
		int nSites = 0, nPredicates = 0;
		double sp = 0, pp = 0;
		double as = 0, ap = 0;
		
		this.sortedPrunedPredictors.clear();
		
		List<String> pfunctions = new ArrayList<String>();
		List<String> functions = new ArrayList<String>();
		
		for(int j = 0; j < list.size(); j++){
			Entry<FunctionEntrySite, FrequencyValue> entry = list.get(j);
			String function = entry.getKey().getFunctionName();
			FrequencyValue value = entry.getValue();
			
			skip = skip(score, bc, threshold, value);
			
			//percentage information of instrumented sites and predicates
			if(this.targetFunction.equals(function)){
				sp = (double)100 * nSites / sInfo.getNumPredicateSites();
				pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
				if (i != 0) {
					as = (double) nSites / i;
					ap = (double) nPredicates / i;
				}
				else{
					as = 0;
					ap = 0;
				}
				
				writer.println(String.format("%-50s", "Excluding " + function) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				
				result[score.ordinal()][order.ordinal()][0][0] = sp;
				result[score.ordinal()][order.ordinal()][0][1] = pp;
				result[score.ordinal()][order.ordinal()][0][2] = i;
				result[score.ordinal()][order.ordinal()][0][3] = as;
				result[score.ordinal()][order.ordinal()][0][4] = ap;
				
				
				//---------------------------------------------------------------------------------
				if(skip){
					System.out.println("pTFlag==false: " + score + "_" + order);
					pTFlag = false;
				}
				else{
					functions.add(function);
					pfunctions.add(function);
					
					i++;
					nSites += sInfo.getMap().get(function).getNumSites();
					nPredicates += sInfo.getMap().get(function).getNumPredicates();
					
					CBIClient c = clientsMap.get(function);
					appendPredictors(this.sortedPrunedPredictors, c.getSortedPredictors());
					double im = getKth(this.sortedPrunedPredictors, Client.k);
					if(im > threshold){
						threshold = im;
					}
				}
				
				//---------------------------------------------------------------------------------
				sp = (double)100 * nSites / sInfo.getNumPredicateSites();
				pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
				if (i != 0) {
					as = (double) nSites / i;
					ap = (double) nPredicates / i;
				}
				else{
					as = 0;
					ap = 0;
				}	
				
				writer.println(String.format("%-50s", "Including " + function) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				writer.println();
				
				result[score.ordinal()][order.ordinal()][1][0] = sp;
				result[score.ordinal()][order.ordinal()][1][1] = pp;
				result[score.ordinal()][order.ordinal()][1][2] = i;
				result[score.ordinal()][order.ordinal()][1][3] = as;
				result[score.ordinal()][order.ordinal()][1][4] = ap;
				
				//---------------------------------------------------------------------------------
				//check whether the pruned top k predictors are the same as the original predictors
				checkGlobalConsistency(Client.k, score, order, 0);
				
				if(order == Order.LESS_FIRST){
					methodsList.put(score, new ArrayList<String>(functions));
				}
			}
			else{
				if(!skip){
					functions.add(function);
					pfunctions.add(function);
					
					i++;
					nSites += sInfo.getMap().get(function).getNumSites();
					nPredicates += sInfo.getMap().get(function).getNumPredicates();
					
					CBIClient c = clientsMap.get(function);
					appendPredictors(this.sortedPrunedPredictors, c.getSortedPredictors());
					double im = getKth(this.sortedPrunedPredictors, Client.k);
					if(im > threshold){
						threshold = im;
					}
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		sp = (double)100 * nSites / sInfo.getNumPredicateSites();
		pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
		if (i != 0) {
			as = (double) nSites / i;
			ap = (double) nPredicates / i;
		}
		else{
			as = 0;
			ap = 0;
		}
		
		writer.println();
		writer.println("==============================================================");
		writer.println(String.format("%-50s", "Pruning by <" + score + "," + order + ">") 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
		writer.println();
		CBIClient.printTopK(sortedPrunedPredictors, Client.k, writer);
		
		pResult[score.ordinal()][order.ordinal()][0] = sp;
		pResult[score.ordinal()][order.ordinal()][1] = pp;
		pResult[score.ordinal()][order.ordinal()][2] = i;
		pResult[score.ordinal()][order.ordinal()][3] = as;
		pResult[score.ordinal()][order.ordinal()][4] = ap;
		
		//---------------------------------------------------------------------------------
		// check whether the pruned top k predictors are the same as the original predictors
		checkGlobalConsistency(Client.k, score, order, 1);
		
		if (order == Order.LESS_FIRST) {
			prunedMethodsList.put(score, pfunctions);
		}
		
		//---------------------------------------------------------------------------------
		//check whether iterative instrumentation gets the same locally top predictors as the full instrumentation
		checkLocalConsistency();
		
	}

	/**check whether iterative instrumentation gets the same top predictors as the full instrumentation
	 * 
	 */
	private void checkLocalConsistency() {
		// TODO Auto-generated method stub
		Set<PredicateItem> set = new LinkedHashSet<PredicateItem>();
		for(PredicateItem item: this.sortedPredictors.lastEntry().getValue()){
			if(item.getPredicateSite().getSite().getFunctionName().equals(targetFunction)){
				set.add(item);
			}
		}
		CBIClient tc = clientsMap.get(targetFunction);
		SortedSet<PredicateItem> sSet = tc.getSortedPredictors().lastEntry().getValue();
		if(!set.equals(sSet)){
//			System.out.println(targetFunction);
//			System.out.println("Full:\n" + set.toString());
//			System.out.println("Iterative:\n" + sSet.toString());
			System.out.println("lCFlag==false");
			lCFlag = false;
		}
	}
	
	private boolean isDifferentScoreValue(List<Entry<FunctionEntrySite, FrequencyValue>> list, int j, Score score) {
		// TODO Auto-generated method stub
		if(j == 0){
			return true;
		}
		
		FrequencyValue value = list.get(j).getValue();
		FrequencyValue preValue = list.get(j - 1).getValue();
		
		switch (score){
		case NEGATIVE:
			return value.getNegative() != preValue.getNegative();
		case H_1:
			return value.getH_1() != preValue.getH_1();
		case H_2:
			return value.getH_2() != preValue.getH_2();
		case F_1:
			return value.getF_score() != preValue.getF_score();
		default:
			System.err.println("No such score");
			System.exit(0);
		}
		
		return false;
	}


	private boolean skip(Score score, BoundCalculator bc, double threshold, FrequencyValue value) {
		int lb = bc.computeCBIBound(threshold);
		switch (score){
		case NEGATIVE: 
			if(value.getNegative() >= lb){
				return false;
			}
			break;
		case H_1:
			if(bc.DH(2, bc.getP()) > 0 && bc.DH(bc.getF(), bc.getP()) < 0){
				int f0 = bc.compute_f0(bc.getP());
				if(value.getH_1() >= threshold || value.getNegative() > f0){
					return false;
				}
			}
			else if(bc.DH(bc.getF(), bc.getP()) >= 0){
				if(value.getH_1() >= threshold){
					return false;
				}
			}
			break;
		case H_2:
			if(value.getPositive() == 0){
				return true;
			}
			if(value.getNegative() < 2){
				return true;
			}
			if(bc.DH(2, value.getPositive()) <= 0){
				return false;
			}
			else if(bc.DH(2, value.getPositive()) > 0 && bc.DH(bc.getF(), value.getPositive()) < 0){
				int f0 = bc.compute_f0(value.getPositive());
				if(value.getH_2() >= threshold || value.getNegative() > f0){
					return false;
				}
			}
			else if(bc.DH(bc.getF(), value.getPositive()) >= 0){
				if(value.getH_2() >= threshold){
					return false;
				}
			}
			break;
		case F_1:
			double fs = SelectingProcessor.F_score(lb, bc.getP(), bc.getF());
			if(value.getF_score() >= fs){
				return false;
			}
			break;
//		case PRECISION:
//			double pr = SelectingProcessor.Precision(lb, bc.getP());
//			if(value.getPrecision() >= pr){
//				return false;
//			}
//			break;
//		case POSITIVE:
//		case RANDOM:
////			if(value.getNegative() < 2){
////				return true;
////			}
//			return false;
		default:
			System.err.println("score error");
			System.exit(0);
		}
		
		return true;
	}
	

	public SitesInfo getsInfo() {
		return sInfo;
	}

	public double[][][][] getResult() {
		return result;
	}

	public double[][][] getpResult() {
		return pResult;
	}

	public int[] getcResult() {
		return cResult;
	}

	public Map<Score, List<String>> getPrunedMethodsList() {
		return prunedMethodsList;
	}

	public Map<Score, List<String>> getMethodsList() {
		return methodsList;
	}


	public boolean ispTFlag() {
		return pTFlag;
	}


	public boolean islCFlag() {
		return lCFlag;
	}


	public boolean isgCFlag() {
		return gCFlag;
	}


	public boolean isgPFlag() {
		return gPFlag;
	}
	
}
