package zuo.processor.functionentry.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.processor.PredicateItem;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.BoundCalculator;
import zuo.processor.functionentry.processor.SelectingProcessor;
import zuo.processor.functionentry.processor.SelectingProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class FunctionClient {
	public static final int TOP_K = 10;

	public static enum Score{
		RANDOM, NEGATIVE, H_1, F_1, H_2, PRECISION, POSITIVE
	}
	
	public static enum Order{
		RANDOM, LESS_FIRST, MORE_FIRST, BEST, WORST, //CLOSER_FIRST 
	}
	
	final int runs;
	
	final File sitesFile;
	final String profilesFolder;
	final String consoleFile;
	
	final SitesInfo sInfo;
	final List<Map.Entry<PredicateItem, Double>> predictors;
	final String method;
	final Map<String, Double> methodsMap;
	
	final double[][][][] result;
	final double [][] wResult;
	final int [] cResult;//{methods, sites, predicates}
	
	private PrintWriter writer;
	final PrintWriter clientWriter;
	
	final String methodsFileDir;
	
	public FunctionClient(int runs, File sitesFile, String profilesFolder, String consoleFile, SitesInfo sInfo, List<Map.Entry<PredicateItem, Double>> predictors, Map<String, Double> methodsM, PrintWriter cWriter, String methodsF) {
		this.runs = runs;
		this.sitesFile = sitesFile;
		this.profilesFolder = profilesFolder;
		this.consoleFile = consoleFile;
		this.sInfo = sInfo;
		this.predictors = predictors;
		this.method = this.predictors.get(0).getKey().getSite().getFunctionName();
		this.methodsMap = methodsM;
		this.result = new double[Score.values().length][Order.values().length][2][5];
		this.wResult = new double[Score.values().length][5];
		this.cResult = new int[3];
		this.clientWriter = cWriter;
		
		this.methodsFileDir = methodsF;
		
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults();
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
	
	public FunctionClient(int runs, String rootDir, String subject, String version, String consoleFolder, SitesInfo sInfo, List<Map.Entry<PredicateItem, Double>> predictors, Map<String, Double> methodsM, PrintWriter cWriter){
		this.runs = runs;
		this.sitesFile = new File(rootDir + subject + "/versions/" + version + "/" + version + "_c.sites");
		this.profilesFolder = rootDir + subject + "/traces/" + version + "/coarse-grained";
		this.consoleFile = consoleFolder + subject + "_" + version + "_function.out"; 
		this.sInfo = sInfo;
		this.predictors = predictors;
		this.method = this.predictors.get(0).getKey().getSite().getFunctionName();
		this.methodsMap = methodsM;
		this.result = new double[Score.values().length][Order.values().length][2][5];
		this.wResult = new double[Score.values().length][5];
		this.cResult = new int[3];
		this.clientWriter = cWriter;
		
		this.methodsFileDir = rootDir + subject + "/versions/" + version + "/adaptive/";
		
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults();
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
	
	public FunctionClient(int runs, String rootDir, String subject, String version, String consoleFolder, PrintWriter cWriter){
		this.runs = runs;
		this.sitesFile = new File(rootDir + subject + "/versions/" + version + "/" + version + "_c.sites");
		this.profilesFolder = rootDir + subject + "/traces/" + version + "/coarse-grained";
		this.consoleFile = consoleFolder + subject + "_" + version + "_function.out";
		
		this.sInfo = new SitesInfo(new InstrumentationSites(new File(rootDir + subject + "/versions/" + version + "/" + version + "_f.sites")));
		CBIClient c = new CBIClient(runs, TOP_K, this.sInfo.getSites().getSitesFile(), 
				rootDir + subject + "/traces/" + version +"/fine-grained", consoleFolder + subject + "_" + version + "_cbi.out");
		this.predictors = c.getPredictorEntryList();
		this.method = this.predictors.get(0).getKey().getSite().getFunctionName();
		this.methodsMap = c.getMethodsMap();
		
		this.result = new double[Score.values().length][Order.values().length][2][5];
		this.wResult = new double[Score.values().length][5];
		this.cResult = new int[3];
		this.clientWriter = cWriter;
		
		this.methodsFileDir = rootDir + subject + "/versions/" + version + "/adaptive/";
		
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(this.consoleFile)));
			printResults();
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

	
	private void printResults(){
		FunctionEntrySites sites = new FunctionEntrySites(sitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(profilesFolder, sites);
		FunctionEntryProfile[] profiles = reader.readFunctionEntryProfiles(runs);
		SelectingProcessor processor = new SelectingProcessor(profiles);
		processor.process();
		
		//print out the general runs information
		assert(processor.getTotalNegative() + processor.getTotalPositive() == runs);
		writer.println("\n");
		writer.println("The general runs information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of runs:") + runs);
		writer.println(String.format("%-50s", "Total number of negative runs:") + processor.getTotalNegative());
		writer.println(String.format("%-50s", "Total number of positive runs:") + processor.getTotalPositive());
		
//		//print out the static instrumentation sites information 
//		writer.println("\n");
//		printSitesInfo(sInfo, writer);
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		writer.println("\n");
		writer.println("The general methods information are as follows:\n==============================================================");
		writer.println(String.format("%-50s", "Total number of methods instrumented:") + sites.getNumFunctionEntrySites());
		this.cResult[0] = sites.getNumFunctionEntrySites();
		this.cResult[1] = sInfo.getNumPredicateSites();
		this.cResult[2] = sInfo.getNumPredicateItems();
		
		//filter out methods within which no predicates are instrumented
		filterFrequencyMap(processor.getFrequencyMap());
		
		//print out entry and percentage information
		System.out.println();
		for(Score score: Score.values()){
			writer.println("\n");
			
			BoundCalculator bc = new BoundCalculator(processor.getTotalNegative(), processor.getTotalPositive());
			for(Order order: Order.values()){
				printEntryAndPercentage(processor.getFrequencyMap(), score, order, bc);
			}
			printWorstCase(processor, score);
		}
		
	}
	
	private void printWorstCase(SelectingProcessor processor, Score score) {
		// TODO Auto-generated method stub
		double threshold = predictors.get(0).getValue();
		assert(threshold == methodsMap.get(method));
		
		BoundCalculator bc = new BoundCalculator(processor.getTotalNegative(), processor.getTotalPositive());
		Map<FunctionEntrySite, FrequencyValue> frequencyMap = processor.getFrequencyMap();
		
		int lb = bc.computeCBIBound(threshold);
		
		int i = 0;
		int nSites = 0, nPredicates = 0;
		double sp = 0, pp = 0;
		double as = 0, ap = 0;
		
		switch (score){
		case NEGATIVE: 
			for(FunctionEntrySite site: frequencyMap.keySet()){
				String method = site.getFunctionName();
				if(frequencyMap.get(site).getNegative() >= lb){
					i++;
					nSites += sInfo.getMap().get(method).getNumSites();
					nPredicates += sInfo.getMap().get(method).getNumPredicates();
				}
			}
			break;
		case H_1:
			if(bc.DH(2, bc.getP()) > 0 && bc.DH(bc.getF(), bc.getP()) < 0){
				int f0 = bc.compute_f0(bc.getP());
				for(FunctionEntrySite site: frequencyMap.keySet()){
					FrequencyValue value = frequencyMap.get(site);
					String method = site.getFunctionName();
					if(value.getH_1() >= threshold || value.getNegative() > f0){
						i++;
						nSites += sInfo.getMap().get(method).getNumSites();
						nPredicates += sInfo.getMap().get(method).getNumPredicates();
					}
				}
			}
			else if(bc.DH(bc.getF(), bc.getP()) >= 0){
				for(FunctionEntrySite site: frequencyMap.keySet()){
					FrequencyValue value = frequencyMap.get(site);
					String method = site.getFunctionName();
					if(value.getH_1() >= threshold){
						i++;
						nSites += sInfo.getMap().get(method).getNumSites();
						nPredicates += sInfo.getMap().get(method).getNumPredicates();
					}
				}
			}
			break;
		case F_1:
			double fs = SelectingProcessor.F_score(lb, bc.getP(), bc.getF());
			for(FunctionEntrySite site: frequencyMap.keySet()){
				String method = site.getFunctionName();
				if(frequencyMap.get(site).getF_score() >= fs){
					i++;
					nSites += sInfo.getMap().get(method).getNumSites();
					nPredicates += sInfo.getMap().get(method).getNumPredicates();
				}
			}
			break;
		case H_2:
			for(FunctionEntrySite site: frequencyMap.keySet()){
				String method = site.getFunctionName();
				FrequencyValue value = frequencyMap.get(site);
				if(value.getPositive() == 0){
					continue;
				}
				if(value.getNegative() < 2){
					continue;
				}
				if(bc.DH(2, value.getPositive()) <= 0){
					i++;
					nSites += sInfo.getMap().get(method).getNumSites();
					nPredicates += sInfo.getMap().get(method).getNumPredicates();
				}
				else if(bc.DH(2, value.getPositive()) > 0 && bc.DH(bc.getF(), value.getPositive()) < 0){
					int f0 = bc.compute_f0(value.getPositive());
					if(value.getH_2() >= threshold || value.getNegative() > f0){
						i++;
						nSites += sInfo.getMap().get(method).getNumSites();
						nPredicates += sInfo.getMap().get(method).getNumPredicates();
					}
				}
				else if(bc.DH(bc.getF(), value.getPositive()) >= 0){
					if(value.getH_2() >= threshold){
						i++;
						nSites += sInfo.getMap().get(method).getNumSites();
						nPredicates += sInfo.getMap().get(method).getNumPredicates();
					}
				}
			}
			break;
		case PRECISION:
			double pr = SelectingProcessor.Precision(lb, bc.getP());
			for(FunctionEntrySite site: frequencyMap.keySet()){
				String method = site.getFunctionName();
				if(frequencyMap.get(site).getPrecision() >= pr){
					i++;
					nSites += sInfo.getMap().get(method).getNumSites();
					nPredicates += sInfo.getMap().get(method).getNumPredicates();
				}
			}
			break;
		case RANDOM:
		case POSITIVE:
			for(FunctionEntrySite site: frequencyMap.keySet()){
				String method = site.getFunctionName();
//				FrequencyValue value = frequencyMap.get(site);
//				if(value.getNegative() < 2){
//					continue;
//				}
				
				i++;
				nSites += sInfo.getMap().get(method).getNumSites();
				nPredicates += sInfo.getMap().get(method).getNumPredicates();
			}
			break;
		default:
			System.err.println("score error");
			System.exit(0);
		}
		
		sp = (double)100 * nSites / sInfo.getNumPredicateSites();
		pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
		assert(i != 0);
		as = (double) nSites / i;
		ap = (double) nPredicates / i;
		
		System.out.println("==============================================================");
		System.out.println(String.format("%-50s", "The worst case by <" + score + ">:")
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
		System.out.println("\n");
		writer.println("==============================================================");
		writer.println(String.format("%-50s", "The worst case by <" + score + ">:") 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
		writer.println("\n");
		clientWriter.println("==============================================================");
		clientWriter.println(String.format("%-50s", "The worst case by <" + score + ">:") 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
		clientWriter.println("\n");
		
		wResult[score.ordinal()][0] = sp;
		wResult[score.ordinal()][1] = pp;
		wResult[score.ordinal()][2] = i;
		wResult[score.ordinal()][3] = as;
		wResult[score.ordinal()][4] = ap;
		
	}

	/**print the methods and the corresponding percentage to be instrumented in the mode <score,order>
	 * @param frequencyMap
	 * @param score
	 * @param order
	 * @param bc 
	 */
	private void printEntryAndPercentage(Map<FunctionEntrySite, FrequencyValue> frequencyMap, final Score score, final Order order, BoundCalculator bc) {
		// TODO Auto-generated method stub
		List list = new ArrayList(frequencyMap.entrySet());
		Collections.sort(list, new Comparator(){

			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return rank(arg0, arg1, score, order);
			}});
		String mode = "<" + score + "," + order + ">";
		writer.println("The methods ordered by " + mode + " are as follows:\n--------------------------------------------------------------");
		printEntry(list);
		writer.println();
		System.out.println("The information of sites and predicates need to be instrumented " + mode + " are as follows:\n--------------------------------------------------------------");
		writer.println("The information of sites and predicates need to be instrumented " + mode + " are as follows:\n--------------------------------------------------------------");
		clientWriter.println("The information of sites and predicates need to be instrumented " + mode + " are as follows:\n--------------------------------------------------------------");
		printPercentage(list, score, order, bc);
//		getMethodsList(list, score, order);
	}
		
	/**get the list of methods to be instrumented
	 * @param list
	 * @param score
	 * @param order
	 */
	private void getMethodsList(List list, Score score, Order order){	
		//get the methods list to be instrumented
		List<String> methods = new ArrayList<String>();
		
		if(order == Order.LESS_FIRST){
			for(int i = 0; i < list.size(); i++){
				Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
				String method = entry.getKey().getFunctionName();
				methods.add(method);
				if(this.method.equals(method)){
					break;
				}
			}
			
			PrintWriter out = null;
			try{
				File fd = new File(this.methodsFileDir);
				if (!fd.exists()) {
					fd.mkdirs();
				}
				//write the passing inputs
				out = new PrintWriter(new BufferedWriter(new FileWriter(this.methodsFileDir + score)));
				for(String method: methods){
					out.println(method);
				}
				out.close();
				
			}
			catch(IOException e){
				e.printStackTrace();
			}
			finally{
				out.close();
			}
		}
	}

	/**rank the methods in the mode <score,order>
	 * @param arg0
	 * @param arg1
	 * @param score
	 * @param order
	 * @return
	 */
	private int rank(Object arg0, Object arg1, Score score, Order order) {
		// TODO Auto-generated method stub
		int r = 0;
		switch (score){
		case RANDOM:
			break;
		case NEGATIVE:
			r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getNegative())
					.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getNegative()));
			if(r == 0){
				r = order(arg0, arg1, order);
			}
			break;
		case H_1:
			r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getH_1())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getH_1()));
			if(r == 0){
//				r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getH_2())
//						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getH_2()));
//				if(r == 0){
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
		case F_1:
			r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
			if (r == 0) {
//				r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getH_1())
//						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getH_1()));
//				if (r == 0) {
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
		case H_2:
			r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getH_2())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getH_2()));
			if(r == 0){
//				r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getH_1())
//						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getH_1()));
//				if(r == 0){
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
		case PRECISION:
			r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getPrecision())
					.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getPrecision()));
			if (r == 0) {
//				r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
//						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
//				if(r == 0){
//					r = order(arg0, arg1, order);
//				}
				r = order(arg0, arg1, order);
			}
			break;
		case POSITIVE:
			r = new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getPositive())
					.compareTo(new Integer(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getPositive()));
			if(r == 0){
//				r = new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getValue().getF_score())
//						.compareTo(new Double(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getValue().getF_score()));
//				if(r == 0){
//					r = order(arg0, arg1, order);
//				}
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
	private int order(Object arg0, Object arg1, Order order) {
		int rr = 0;
		String method0 = null, method1 = null;
		switch (order) {
		case RANDOM:
			//random order
			break;
		case LESS_FIRST:
			//less first
			method0 = ((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getKey().getFunctionName();
			method1 = ((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getKey().getFunctionName();
			rr = new Integer(sInfo.getMap().get(method0).getNumSites())
				.compareTo(new Integer(sInfo.getMap().get(method1).getNumSites()));
			if(rr == 0){
				rr = new Integer(sInfo.getMap().get(method0).getNumPredicates())
					.compareTo(new Integer(sInfo.getMap().get(method1).getNumPredicates()));
			}
			break;
		case MORE_FIRST:
			//more first
			method0 = ((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getKey().getFunctionName();
			method1 = ((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getKey().getFunctionName();
			rr = new Integer(sInfo.getMap().get(method1).getNumSites())
				.compareTo(new Integer(sInfo.getMap().get(method0).getNumSites()));
			if(rr == 0){
				rr = new Integer(sInfo.getMap().get(method1).getNumPredicates())
					.compareTo(new Integer(sInfo.getMap().get(method0).getNumPredicates()));
			}
			break;
//		case CLOSER_FIRST:
//			//closer first
//			break;
		case BEST:
			//best order
			if(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getKey().getFunctionName().equals(method)){
				rr = 1;
			}
			if(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getKey().getFunctionName().equals(method)){
				rr = -1;
			}
			break;
		case WORST:
			//worst order
			if(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg1).getKey().getFunctionName().equals(method)){
				rr = -1;
			}
			if(((Map.Entry<FunctionEntrySite, FrequencyValue>) arg0).getKey().getFunctionName().equals(method)){
				rr = 1;
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
	 */
	private void printEntry(List list) {
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				writer.println(String.format("%-45s", method) + entry.getValue().toString() + "\t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				throw new RuntimeException("filtering error");
//				writer.println(String.format("%-45s", method) + entry.getValue().toString());
			}
		}
	}

	/**print out the percentage instrumented before reaching the top predictor
	 * @param list
	 * @param score
	 * @param order
	 * @param bc 
	 */
	private void printPercentage(List list, Score score, Order order, BoundCalculator bc) {
		double threshold = 0;
		int i = 0;
		
		int nSites = 0, nPredicates = 0;
		double sp = 0, pp = 0;
		double as = 0, ap = 0;
		for(int j = 0; j < list.size(); j++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(j);
			String method = entry.getKey().getFunctionName();
			FrequencyValue value = entry.getValue();
			
			//percentage information of instrumented sites and predicates
			if(this.method.equals(method)){
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
				System.out.println(String.format("%-50s", "Excluding " + method) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				writer.println(String.format("%-50s", "Excluding " + method) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + i) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				clientWriter.println(String.format("%-50s", "Excluding " + method) 
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
				
				nSites += sInfo.getMap().get(method).getNumSites();
				nPredicates += sInfo.getMap().get(method).getNumPredicates();

				sp = (double)100 * nSites / sInfo.getNumPredicateSites();
				pp = (double)100 * nPredicates / sInfo.getNumPredicateItems();
				as = (double)nSites / (i + 1);
				ap = (double)nPredicates / (i + 1);
				
				System.out.println(String.format("%-50s", "Including " + method) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + (i + 1)) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				System.out.println();
				writer.println(String.format("%-50s", "Including " + method) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + (i + 1)) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				writer.println();
				clientWriter.println(String.format("%-50s", "Including " + method) 
						+ String.format("%-15s", "s:" + nSites) 
						+ String.format("%-15s", "s%:" + new DecimalFormat("##.###").format(sp))
						+ String.format("%-15s", "p:" + nPredicates) 
						+ String.format("%-15s", "p%:" + new DecimalFormat("##.###").format(pp))
						+ String.format("%-15s", "i:" + (i + 1)) 
						+ String.format("%-15s", "as:" + new DecimalFormat("#.#").format(as)) 
						+ String.format("%-15s", "ap:" + new DecimalFormat("#.#").format(ap)));
				clientWriter.println();
				
				result[score.ordinal()][order.ordinal()][1][0] = sp;
				result[score.ordinal()][order.ordinal()][1][1] = pp;
				result[score.ordinal()][order.ordinal()][1][2] = i + 1;
				result[score.ordinal()][order.ordinal()][1][3] = as;
				result[score.ordinal()][order.ordinal()][1][4] = ap;
				
				break;
			}
			else{
				boolean skip = skip(score, bc, threshold, value);
//				System.out.println(skip);
//				System.out.println(threshold);
				if(!skip){
					i++;
					nSites += sInfo.getMap().get(method).getNumSites();
					nPredicates += sInfo.getMap().get(method).getNumPredicates();
					double im = methodsMap.get(method);
					if(im > threshold){
						threshold = im;
					}
				}
			}
		}
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
		case F_1:
			double fs = SelectingProcessor.F_score(lb, bc.getP(), bc.getF());
			if(value.getF_score() >= fs){
				return false;
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
		case PRECISION:
			double pr = SelectingProcessor.Precision(lb, bc.getP());
			if(value.getPrecision() >= pr){
				return false;
			}
			break;
		case RANDOM:
		case POSITIVE:
//			if(value.getNegative() < 2){
//				return true;
//			}
			return false;
		default:
			System.err.println("score error");
			System.exit(0);
		}
		
		return true;
	}
	
	

	public int getRuns() {
		return runs;
	}

	public File getSitesFile() {
		return sitesFile;
	}

	public String getProfilesFolder() {
		return profilesFolder;
	}

	public String getConsoleFile() {
		return consoleFile;
	}

	public SitesInfo getsInfo() {
		return sInfo;
	}

	public List<Map.Entry<PredicateItem, Double>> getPredictors() {
		return predictors;
	}

	public double[][][][] getResult() {
		return result;
	}

	public double[][] getwResult() {
		return wResult;
	}

	public int[] getcResult() {
		return cResult;
	}
	

}
