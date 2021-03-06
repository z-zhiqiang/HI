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
import zuo.processor.cbi.client.CBIClients;
import zuo.processor.cbi.datastructure.FixPointStructure;
import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.cbi.site.SitesInfo.InfoValue;
import zuo.processor.functionentry.datastructure.PruneResult;
import zuo.processor.functionentry.datastructure.Result;
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.processor.SelectingProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class IterativeFunctionClient {
	public static enum Score{
//		RANDOM, 
//		NEGATIVE, 
		H_1, H_2, F_1, C
	}
	
	public static enum Order{
		RANDOM, 
		LESS_FIRST, MORE_FIRST, 
		WORST, BEST, //CLOSER_FIRST 
	}
	
	final FunctionEntrySites sites;
	final FunctionEntryProfile[] selectedFunctionEntryProfiles;
    final TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors;//for comparison
	
	final SitesInfo sInfo;
	final String targetFunction;
	final FixPointStructure fullFixElement;
	
	final Map<String, CBIClient> clientsMap;
	
	final Result[][] results;
	
	final double[][] C_matrix;
	
	public IterativeFunctionClient(FunctionEntrySites sites, FunctionEntryProfile[] profiles, File cbiconsoleFile, File functionconsoleFile, SitesInfo sInfo, CBIClient fullICBIClient, Map<String, CBIClient> map, int[] ks, final double[][] C_matrix) {
		this.sites = sites;
		this.sInfo = sInfo;
		this.targetFunction = getTargetFunction(fullICBIClient);
		this.fullFixElement = fullICBIClient.getFullFixElement();
		this.clientsMap = map;
		
		this.selectedFunctionEntryProfiles = constructSelectedFunctionEntryProfiles(profiles);
		this.sortedPredictors = fullICBIClient.getFullFixElement().getSortedPredictors();
		
		this.results = new Result[Score.values().length][Order.values().length];
		for(int i = 0; i < this.results.length; i++){
			for(int j = 0; j < this.results[i].length; j++){
				this.results[i][j] = new Result(ks);
			}
		}
		
		this.C_matrix = C_matrix;
		
		PrintWriter cbiWriter = null, functionWriter = null;
		try {
			if(!functionconsoleFile.getParentFile().exists()){
				functionconsoleFile.getParentFile().mkdirs();
			}
			cbiWriter = new PrintWriter(new BufferedWriter(new FileWriter(cbiconsoleFile)));
			functionWriter = new PrintWriter(new BufferedWriter(new FileWriter(functionconsoleFile)));
			printOutFullFixElementInfo(fullICBIClient, cbiWriter);
			run(cbiWriter, functionWriter, ks);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(functionWriter != null){
				functionWriter.close();
			}
			if(cbiWriter != null){
				cbiWriter.close();
			}
		}
	}
	
	
	private void printOutFullFixElementInfo(CBIClient fullICBIClient, PrintWriter cbiWriter) {
		// TODO Auto-generated method stub
		cbiWriter.println("FULLY INSTRUMENTED");
		cbiWriter.println("==============================================================");
		fullICBIClient.printSelectedPredicateProfilesInformation(fullICBIClient.getFullFixElement(), cbiWriter);
		CBIClient.printTopK(fullICBIClient.getFullFixElement().getSortedPredictors(), CBIClients.fK, cbiWriter);
	}


	/**get the target function within which the top predictor is.
	 * @param fullICBIClient
	 * @return
	 */
	public static String getTargetFunction(CBIClient fullICBIClient) {
		Set<String> functionSet = new LinkedHashSet<String>();
		for(PredicateItem item: fullICBIClient.getFullFixElement().getSortedPredictors().lastEntry().getValue()){
			functionSet.add(item.getPredicateSite().getSite().getFunctionName());
		}
		if(functionSet.size() != 1){
			System.out.println(functionSet);
		}
		
		return getOneFunction(functionSet);
	}

	private static String getOneFunction(Set<String> functionSet) {
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
		FunctionEntryProfile[] fProfiles = new FunctionEntryProfile[this.fullFixElement.getFailingSet().size() + this.fullFixElement.getPassingSet().size()];
		int j = 0;
		for(int k: this.fullFixElement.getFailingSet()){
			fProfiles[j++] = profiles[k];
		}
		for(int k: this.fullFixElement.getPassingSet()){
			fProfiles[j++] = profiles[k];
		}
		return fProfiles;
	}


	private void run(PrintWriter cbiWriter, PrintWriter functionWriter, int[] ks){
		SelectingProcessor processor = new SelectingProcessor(selectedFunctionEntryProfiles, this.C_matrix);
		processor.process();
		System.out.println("s");
		
		//print out the general runs information
		printSelectedFunctionEntryProfilesInformation(functionWriter);
		System.out.println("a");
		
		//print out the static instrumentation sites information 
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		printSitesInformation(functionWriter);
		System.out.println("b");
		
		//filter out methods within which no predicates are instrumented
		filterFrequencyMap(processor.getFrequencyMap());
//		printout(processor.getFrequencyMap(), this.sInfo.getMap());
		assert(processor.getFrequencyMap().size() == this.sInfo.getMap().size());
		System.out.println("c");
		
		//print out entry and percentage information
		for(Score score: Score.values()){
			for(Order order: Order.values()){
				if(!(score == Score.C && order == Order.LESS_FIRST)){
					continue;
				}
				List<Entry<FunctionEntrySite, FrequencyValue>> list = sortFunctionEntrySiteMap(processor.getFrequencyMap(), score, order);
				System.out.println("d");
//				printEntryAndPercentage(list, score, order, cbiWriter, functionWriter, processor);
				for(int k: ks){
					printPruneCase(list, score, order, cbiWriter, functionWriter, k, processor);
				}
			}
		}
	}


	private void printout(Map<FunctionEntrySite, FrequencyValue> frequencyMap, Map<String, InfoValue> map) {
		// TODO Auto-generated method stub
		System.out.println();
		System.out.println(frequencyMap.size());
		System.out.println(map.size());
		
		Set<String> functions = new HashSet<String>();
		for(FunctionEntrySite functionSite: frequencyMap.keySet()){
			String function = functionSite.getFunctionName();
			if(!functions.add(function)){
				System.out.println(function);
			}
		}
		System.out.println(functions.size());
		
		Set<String> functionsSet = new HashSet<String>(map.keySet());
		System.out.println(functionsSet.size());
		functionsSet.removeAll(functions);
		System.out.println(functionsSet);
	}


	private void printSitesInformation(PrintWriter writer) {
		writer.println("The general methods information are as follows:");
		writer.println("==============================================================");
		writer.println(String.format("%-50s", "Total number of methods instrumented:") + sites.getNumFunctionEntrySites());
		writer.println("");
	}


	private void printSelectedFunctionEntryProfilesInformation(PrintWriter writer) {
		Set<Integer> neg = this.fullFixElement.getFailingSet();
		Set<Integer> pos = this.fullFixElement.getPassingSet();
		assert(pos.size() + neg.size() == selectedFunctionEntryProfiles.length);
		writer.println("The general runs information are as follows:");
		writer.println("==============================================================");
		writer.println(String.format("%-40s", "Total number of runs:") + selectedFunctionEntryProfiles.length);
		writer.println(String.format("%-40s", "Total number of negative runs:") + neg.size());
		writer.println(CBIClient.compressNumbers(neg));
		writer.println(String.format("%-40s", "Total number of positive runs:") + pos.size());
		writer.println(CBIClient.compressNumbers(pos));
		writer.println();
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
	

	/**print the methods and the corresponding percentage to be instrumented in the mode <score,order>
	 * @param frequencyMap
	 * @param score
	 * @param order
	 * @param bc 
	 * @param functionWriter 
	 * @param processor 
	 * @param writer2 
	 */
	private void printEntryAndPercentage(List<Entry<FunctionEntrySite, FrequencyValue>> list, final Score score, final Order order, PrintWriter cbiWriter, PrintWriter functionWriter, SelectingProcessor processor) {
		// TODO Auto-generated method stub
		String mode = "<" + score + "," + order + ">";
		functionWriter.println("\n\n");
		functionWriter.println("The methods ordered by " + mode + " are as follows:");
		functionWriter.println("--------------------------------------------------------------");
		printEntry(list, functionWriter);
		functionWriter.println("The information of sites and predicates need to be instrumented " + mode + " are as follows:");
		functionWriter.println("--------------------------------------------------------------");
		printPercentage(list, score, order, cbiWriter, functionWriter, processor);
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
		int r = 0;
		switch (score){
//		case RANDOM:
//			break;
//		case NEGATIVE:
//			r = new Integer(arg1.getValue().getNegative()).compareTo(new Integer(arg0.getValue().getNegative()));
//			if(r == 0){
//				r = order(arg0, arg1, order);
//			}
//			break;
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
		case C:
			r = new Double(arg1.getValue().getC_score()).compareTo(new Double(arg0.getValue().getC_score()));
			if(r == 0){
				r = order(arg0, arg1, order);
			}
			break;
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
				writer.println(String.format("%-80s", method) + entry.getValue().toString() + "\t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				throw new RuntimeException("filtering error");
			}
		}
		writer.println();
	}
	
	private void printPruneCase(List<Entry<FunctionEntrySite, FrequencyValue>> list, Score score, Order order, PrintWriter cbiWriter, PrintWriter writer, int k, SelectingProcessor processor){
		PruneResult pruneResult = this.results[score.ordinal()][order.ordinal()].getpFlagMap().get(k);
		
		double threshold = 0;
		boolean skip = false;
		int i = 0;
		
		int nSites = 0, nPredicates = 0;
		double sp = 0, pp = 0;
		double as = 0, ap = 0;
		double sumFPercent = 0;
		double afp = 0;
		
		TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors = new TreeMap<Double, SortedSet<PredicateItem>>();
		
		
		for(int j = 0; j < list.size(); j++){
			Entry<FunctionEntrySite, FrequencyValue> entry = list.get(j);
			String function = entry.getKey().getFunctionName();
			FrequencyValue value = entry.getValue();
			
			skip = skip(score, threshold, value, processor);
//			if(isDifferentScoreValue(list, j, score)){
//			}
			
			if(!skip){
				pruneResult.getPruneMethods().add(function);
				FixPointStructure fixElement = clientsMap.get(function).getFixElement(cbiWriter);
				System.out.println(i);
				
				i++;
				nSites += sInfo.getMap().get(function).getNumSites();
				nPredicates += sInfo.getMap().get(function).getNumPredicates();
				sumFPercent += fixElement.getPercent();
				
				appendPredictors(sortedPrunedPredictors, fixElement.getSortedPredictors());
				double im = getKth(sortedPrunedPredictors, k);
				if(im > threshold){
					threshold = im;
				}
			}
		}
		
		//---------------------------------------------------------------------------------
		sp = (double)100 * nSites / sInfo.getNumPredicateSites();
		pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
		if (i != 0) {
			as = (double) nSites / i;
			ap = (double) nPredicates / i;
			afp = sumFPercent / i;
		}
		else{
			as = 0;
			ap = 0;
			afp = 0;
		}
		
		
		writer.println("--------------------------------------------------------------");
		writer.println(String.format("%-80s", "Pruning top " + k + " by <" + score + "," + order + ">") 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.##").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.##").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("##.##").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("##.##").format(ap))
						+ String.format("%-15s", "afp:" + new DecimalFormat("##.##").format(afp))
						);
//		CBIClient.printTopK(sortedPrunedPredictors, k, writer);
		
		pruneResult.getpResult()[0] = sp;
		pruneResult.getpResult()[1] = pp;
		pruneResult.getpResult()[2] = i;
		pruneResult.getpResult()[3] = as;
		pruneResult.getpResult()[4] = ap;
		pruneResult.getpResult()[5] = afp;
		
		//---------------------------------------------------------------------------------
		// check whether the pruned top k predictors are the same as the original predictors
		checkPruneConsistency(k, score, order, sortedPrunedPredictors);
		
	}

	/**print out the percentage instrumented before reaching the top predictor
	 * @param list
	 * @param score
	 * @param order
	 * @param bc 
	 * @param functionWriter 
	 * @param processor 
	 * @param writer2 
	 */
	private void printPercentage(List<Entry<FunctionEntrySite, FrequencyValue>> list, Score score, Order order, PrintWriter cbiWriter, PrintWriter functionWriter, SelectingProcessor processor) {
		Result result = this.results[score.ordinal()][order.ordinal()];
		
		double threshold = 0;
		boolean skip = false;
		
		int i = 0;
		int nSites = 0, nPredicates = 0;
		double sp = 0, pp = 0;
		double as = 0, ap = 0;
		double sumFPercent = 0;
		double afp = 0;
		
		Set<PredicateItem> pruneTopPredicates = null;
		
		for(int j = 0; j < list.size(); j++){
			Entry<FunctionEntrySite, FrequencyValue> entry = list.get(j);
			String function = entry.getKey().getFunctionName();
			FrequencyValue value = entry.getValue();
			
			skip = skip(score, threshold, value, processor);
//			if(isDifferentScoreValue(list, j, score)){
//			}
			
			//percentage information of instrumented sites and predicates
			if(this.targetFunction.equals(function)){
				sp = (double)100 * nSites / sInfo.getNumPredicateSites();
				pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
				if (i != 0) {
					as = (double) nSites / i;
					ap = (double) nPredicates / i;
					afp = sumFPercent / i;
				}
				else{
					as = 0;
					ap = 0;
					afp = 0;
				}
				
				functionWriter.println(String.format("%-80s", "Excluding " + function) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.##").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.##").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("##.##").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("##.##").format(ap))
						+ String.format("%-15s", "afp:" + new DecimalFormat("##.##").format(afp)) 
						);
				
				//---------------------------------------------------------------------------------
				if(skip){
					System.out.println("lCFlag==false(Skip): " + score + "_" + order);
					result.setlCFlag(false);
				}
				else{
					result.getMethods().add(function);
					FixPointStructure fixElement = clientsMap.get(function).getFixElement(cbiWriter);
					
					i++;
					nSites += sInfo.getMap().get(function).getNumSites();
					nPredicates += sInfo.getMap().get(function).getNumPredicates();
					sumFPercent += fixElement.getPercent();
					
					TreeMap<Double, SortedSet<PredicateItem>> predictors = fixElement.getSortedPredictors();
					if(!predictors.isEmpty()){
						double im = predictors.lastKey();
						if(im >= threshold){
							threshold = im;
							pruneTopPredicates = predictors.lastEntry().getValue();
						}
					}
					
					//check whether iterative instrumentation gets the same locally top predictors as the full instrumentation
					checkLocalConsistency(score, order, cbiWriter);
				}
				checkGlobalConsistency(score, order, pruneTopPredicates);
				
				//---------------------------------------------------------------------------------
				sp = (double)100 * nSites / sInfo.getNumPredicateSites();
				pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
				if (i != 0) {
					as = (double) nSites / i;
					ap = (double) nPredicates / i;
					afp = sumFPercent / i;
				}
				else{
					as = 0;
					ap = 0;
					afp = 0;
				}	
				
				functionWriter.println(String.format("%-80s", "Including " + function) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.##").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.##").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("##.##").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("##.##").format(ap))
						+ String.format("%-15s", "afp:" + new DecimalFormat("##.##").format(afp))
						);
			
				result.getiResult()[0] = sp;
				result.getiResult()[1] = pp;
				result.getiResult()[2] = i;
				result.getiResult()[3] = as;
				result.getiResult()[4] = ap;
				result.getiResult()[5] = afp;
				
				break;
			}
			else{
				if(!skip){
					result.getMethods().add(function);
					FixPointStructure fixElement = clientsMap.get(function).getFixElement(cbiWriter);
					
					i++;
					nSites += sInfo.getMap().get(function).getNumSites();
					nPredicates += sInfo.getMap().get(function).getNumPredicates();
					sumFPercent += fixElement.getPercent();
					
					TreeMap<Double, SortedSet<PredicateItem>> predictors = fixElement.getSortedPredictors();
					if(!predictors.isEmpty()){
						double im = predictors.lastKey();
						if(im >= threshold){
							threshold = im;
							pruneTopPredicates = predictors.lastEntry().getValue();
						}
					}
				}
			}
		}
		
	}

	/**check whether iterative instrumentation gets the same top predictors as the full instrumentation
	 * @param cbiWriter 
	 * 
	 */
	private void checkLocalConsistency(Score score, Order order, PrintWriter cbiWriter) {
		// TODO Auto-generated method stub
		Set<PredicateItem> set = new LinkedHashSet<PredicateItem>();
		for(PredicateItem item: this.sortedPredictors.lastEntry().getValue()){
			if(item.getPredicateSite().getSite().getFunctionName().equals(targetFunction)){
				set.add(item);
			}
		}
		CBIClient tc = clientsMap.get(targetFunction);
		if(tc.getFixElement(cbiWriter).getSortedPredictors().isEmpty() || !tc.getFixElement(cbiWriter).getSortedPredictors().lastEntry().getValue().equals(set)){
//			System.out.println(targetFunction);
			System.out.println("lCFlag==false(Consistency): " + score + "_" + order);
			this.results[score.ordinal()][order.ordinal()].setlCFlag(false);
		}
	}
	
	private void checkGlobalConsistency(Score score, Order order, Set<PredicateItem> pruneTopPredicates) {
		Set<PredicateItem> set = new LinkedHashSet<PredicateItem>();
		for(PredicateItem item: this.sortedPredictors.lastEntry().getValue()){
			if(item.getPredicateSite().getSite().getFunctionName().equals(targetFunction)){
				set.add(item);
			}
		}
		if(!set.equals(pruneTopPredicates)){
//			System.out.println(targetFunction);
			System.out.println("gCFlag==false: " + score + "_" + order);
			this.results[score.ordinal()][order.ordinal()].setgCFlag(false);
		}
	}
	
	private void checkPruneConsistency(int k, Score score, Order order, TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors) {
		Set<PredicateItem> pruneSet = new LinkedHashSet<PredicateItem>();
//		getTopKPredictors(pruneSet, this.sortedPrunedPredictors, k);
		getTopKImportances(pruneSet, sortedPrunedPredictors, k);
		
		Set<PredicateItem> originalSetTopK = new LinkedHashSet<PredicateItem>();
//		getTopKPredictors(originalSet, this.sortedPredictors, k);
		getTopKImportances(originalSetTopK, this.sortedPredictors, k);
		
		if(!originalSetTopK.equals(pruneSet)){
//			System.out.println("Original: \t" + originalSet.toString());
//			System.out.println("Pruned: \t" + pruneSet.toString());
			System.out.println("pFlag==false(" + k + "): " + score + "_" + order);
			this.results[score.ordinal()][order.ordinal()].getpFlagMap().get(k).setpFlag(false);	
		}
		
		if(!originalSetTopK.containsAll(pruneSet)){
			System.out.println("pFlagCI0==false(" + k + "): " + score + "_" + order);
			this.results[score.ordinal()][order.ordinal()].getpFlagMap().get(k).setpFlagCI0(false);
		}
		
		Set<PredicateItem> originalSetTopK2 = new LinkedHashSet<PredicateItem>();
		getTopKImportances(originalSetTopK2, this.sortedPredictors, k + 2);
		if(!originalSetTopK2.containsAll(pruneSet)){
			System.out.println("pFlagCI2==false(" + k + "): " + score + "_" + order);
			this.results[score.ordinal()][order.ordinal()].getpFlagMap().get(k).setpFlagCI2(false);
		}
		
	}
	private void getTopKImportances(Set<PredicateItem> set, TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k) {
		// TODO Auto-generated method stub
		int i = 1;
		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
			double im = it.next();
			assert(im != 0);
			if(i <= k){
				set.addAll(sortedPredictors.get(im));
				i++;
			}
			else{
				break;
			}
		}
	}

//	private void getTopKPredictors(Set<PredicateItem> set, TreeMap<Double, SortedSet<PredicateItem>> sortedPredictors, int k) {
//		// TODO Auto-generated method stub
//		for(Iterator<Double> it = sortedPredictors.descendingKeySet().iterator(); it.hasNext();){
//			double im = it.next();
//			if(im == 0){
//				break;
//			}
//			if(set.size() < k){
//				set.addAll(sortedPredictors.get(im));
//			}
//			else{
//				break;
//			}
//		}
//	}

	private double getKth(TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors, int k){
		return getKthImportance(sortedPrunedPredictors, k);
	}

	/**get the kth importance value
	 * @param sortedPrunedPredictors
	 * @param k
	 * @return
	 */
	private double getKthImportance(TreeMap<Double, SortedSet<PredicateItem>> sortedPrunedPredictors, int k){
		int j = 1;
		double im = 0;
		for(Iterator<Double> it = sortedPrunedPredictors.descendingKeySet().iterator(); it.hasNext(); j++){
			im = it.next();
			if(j == k){
				return im;
			}
		}
		return 0;
	}
	
	
	private boolean isDifferentScoreValue(List<Entry<FunctionEntrySite, FrequencyValue>> list, int j, Score score) {
		// TODO Auto-generated method stub
		if(j == 0){
			return true;
		}
		
		FrequencyValue value = list.get(j).getValue();
		FrequencyValue preValue = list.get(j - 1).getValue();
		
		switch (score){
//		case NEGATIVE:
//			return value.getNegative() != preValue.getNegative();
		case H_1:
			return value.getH_1() != preValue.getH_1();
		case H_2:
			return value.getH_2() != preValue.getH_2();
		case F_1:
			return value.getF_score() != preValue.getF_score();
		case C:
			return value.getC_score() != preValue.getC_score();
		default:
			System.err.println("No such score");
			System.exit(0);
		}
		
		return false;
	}

	
	private boolean skip(Score score, double threshold, FrequencyValue value, SelectingProcessor processor) {
		int lb = processor.computeCBIBound(threshold);
		switch (score){
//		case NEGATIVE: 
//			if(value.getNegative() >= lb){
//				return false;
//			}
//			break;
		case H_1:
			if(processor.DH(2, processor.getTotalPositive()) > 0 && processor.DH(processor.getTotalNegative(), processor.getTotalPositive()) < 0){
				int f0 = processor.compute_f0(processor.getTotalPositive());
				if(value.getH_1() >= threshold || value.getNegative() > f0){
					return false;
				}
			}
			else if(processor.DH(processor.getTotalNegative(), processor.getTotalPositive()) >= 0){
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
			if(processor.DH(2, value.getPositive()) <= 0){
				return false;
			}
			else if(processor.DH(2, value.getPositive()) > 0 && processor.DH(processor.getTotalNegative(), value.getPositive()) < 0){
				int f0 = processor.compute_f0(value.getPositive());
				if(value.getH_2() >= threshold || value.getNegative() > f0){
					return false;
				}
			}
			else if(processor.DH(processor.getTotalNegative(), value.getPositive()) >= 0){
				if(value.getH_2() >= threshold){
					return false;
				}
			}
			break;
		case F_1:
			double fs = SelectingProcessor.F_score(lb, processor.getTotalPositive(), processor.getTotalNegative());
			if(value.getF_score() >= fs){
				return false;
			}
			break;
		case C:
			if(value.getC_score() >= threshold){
				return false;
			}
			break;
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

	public Result[][] getResults() {
		return results;
	}

}
