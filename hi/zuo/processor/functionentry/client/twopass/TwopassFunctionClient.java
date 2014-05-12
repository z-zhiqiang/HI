package zuo.processor.functionentry.client.twopass;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.nus.sun.processor.mps.client.AbstractProcessorWithLabels;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.PruningProcessor;
import zuo.processor.functionentry.processor.PruningProcessor.FrequencyValue;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class TwopassFunctionClient {
	private FunctionEntryProfile[] cgProfiles;
	private final PruningProcessor processor;
	private final SitesInfo sInfo;
	private List<Map.Entry<FunctionEntrySite, FrequencyValue>> list;
	
	public TwopassFunctionClient(File csitesFile, File cgProfilesFolder, File fsitesFile, Object[] resultsCG, PrintWriter writer){
		final long start = System.currentTimeMillis();
		
		FunctionEntrySites sites = new FunctionEntrySites(csitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(cgProfilesFolder, sites);
		cgProfiles = reader.readFunctionEntryProfiles();
		processor = new PruningProcessor(cgProfiles);
		processor.process();
		assert(processor.getFrequencyMap().size() == sites.getNumFunctionEntrySites());
		
		this.sInfo = new SitesInfo(new InstrumentationSites(fsitesFile));
		filterNegativeFrequencyMap(processor.getFrequencyMap());
		assert(processor.getFrequencyMap().size() == this.sInfo.getMap().size());
		// construct a sorted list of negativeFrequencyMap
		constructEntryList();
		
		final long end = System.currentTimeMillis();
		double time = (double) (end - start) / 1000;
		
		resultsCG[0] = list.size();
		resultsCG[1] = processor.getNumberofTFFunctions();
		resultsCG[2] = AbstractProcessorWithLabels.printMemoryUsage(writer);
		resultsCG[3] = time;
		System.out.println("coarse-grained analysis time = " + time);
		System.out.println();
	}


	private void constructEntryList() {
		List<Map.Entry<FunctionEntrySite, FrequencyValue>> list = new ArrayList<Map.Entry<FunctionEntrySite, FrequencyValue>>(processor.getFrequencyMap().entrySet());
		Collections.sort(list, new Comparator<Map.Entry<FunctionEntrySite, FrequencyValue>>(){

			@Override
			public int compare(Map.Entry<FunctionEntrySite, FrequencyValue> arg0, Map.Entry<FunctionEntrySite, FrequencyValue> arg1) {
				// TODO Auto-generated method stub
				return rank(arg0, arg1);
			}

			private int rank(Map.Entry<FunctionEntrySite, FrequencyValue> arg0, Map.Entry<FunctionEntrySite, FrequencyValue> arg1) {
				// TODO Auto-generated method stub
				int r = 0;
				r = new Double(arg1.getValue().getDS()).compareTo(new Double(arg0.getValue().getDS()));
				if(r == 0){
					r = new Integer(arg1.getValue().getNegative()).compareTo(new Integer(arg0.getValue().getNegative()));
					if(r == 0){
						r = new Integer(arg0.getValue().getPositive()).compareTo(new Integer(arg1.getValue().getPositive()));
						if(r == 0){
							String method0 = arg0.getKey().getFunctionName();
							String method1 = arg1.getKey().getFunctionName();
							r = new Integer(sInfo.getMap().get(method0).getNumSites()).compareTo(new Integer(sInfo.getMap().get(method1).getNumSites()));
							if(r == 0){
								r = new Integer(sInfo.getMap().get(method0).getNumPredicates()).compareTo(new Integer(sInfo.getMap().get(method1).getNumPredicates()));
							}
						}
					}
				}
				return r;
			}
			
		});
		
		this.list = Collections.unmodifiableList(list);
	}

	private void filterNegativeFrequencyMap(Map<FunctionEntrySite, FrequencyValue> map) {
		// TODO Auto-generated method stub
		for(Iterator<FunctionEntrySite> it = map.keySet().iterator(); it.hasNext();){
			String function = it.next().getFunctionName();
			if(!sInfo.getMap().containsKey(function)){
				it.remove();
			}
		}
	}

	public Set<String> getFunctionSet(int bound){
		Set<String> functionSet = new LinkedHashSet<String>();
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			if(entry.getValue().getNegative() >= bound){
				String functionName = entry.getKey().getFunctionName();
				if(functionSet.contains(functionName))
					throw new RuntimeException("multiple functions with the same name");
				functionSet.add(functionName);
			}
		}
		return Collections.unmodifiableSet(functionSet);
	}

	/**
	 * @param mode: 0->%*f; 1->%*S; 2->%*P; 
	 * @param percent
	 * @return
	 */
	public Set<String> getBoostFunctionSet(byte mode, double percent){
		Set<String> functionSet = new LinkedHashSet<String>();
		
		switch(mode){
		case 0: //the number of functions selected is equal to "percent" * the total number of functions
			for(int i = 0; i < list.size() * percent; i++){
				if(list.get(i).getValue().getNegative() == 0){
					break;
				}
				functionSet.add(list.get(i).getKey().getFunctionName());
			}
			break;
		case 1: //functions within which the number of sites are equal to "percent" * the total number of sites
			int numberofSites = (int) (sInfo.getNumPredicateSites() * percent);
			for(int i = 0, j = 0; i < list.size() && j < numberofSites; i++){
				if(list.get(i).getValue().getNegative() == 0){
					break;
				}
				String function = list.get(i).getKey().getFunctionName();
				functionSet.add(function);
				j += sInfo.getMap().get(function).getNumSites();
			}
			break;
		case 2: //functions within which the number of predicates are equal to "percent" * the total number of predicates
			int numberofPredicates = (int) (sInfo.getNumPredicateItems() * percent);
			for(int i = 0, j = 0; i < list.size() && j < numberofPredicates; i++){
				if(list.get(i).getValue().getNegative() == 0){
					break;
				}
				String function = list.get(i).getKey().getFunctionName();
				functionSet.add(function);
				j += sInfo.getMap().get(function).getNumPredicates();
			}
			break;
		default:
			throw new RuntimeException("Option Error");
		}
		
		return Collections.unmodifiableSet(functionSet);
	}
	
	/**print out each function and the corresponding information
	 * @param writer 
	 * 
	 */
	public void printEntry(PrintWriter writer){
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, FrequencyValue> entry = (Entry<FunctionEntrySite, FrequencyValue>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				System.out.println(String.format("%-70s", method) + entry.getValue().toString() + "   \t" + sInfo.getMap().get(method).toStringWithoutSites());
				writer.println(String.format("%-70s", method) + entry.getValue().toString() + "   \t" + sInfo.getMap().get(method).toStringWithoutSites());
			}
			else{
				throw new RuntimeException("filtering error");
			}
		}
		System.out.println();
		writer.println();
	}
	
	
	
	public SitesInfo getsInfo() {
		return sInfo;
	}


	public List<Map.Entry<FunctionEntrySite, FrequencyValue>> getList() {
		return list;
	}


	public PruningProcessor getProcessor() {
		return processor;
	}


	public static void main(String[] args) {
//		TwopassFunctionClient client = new TwopassFunctionClient(new File("/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/grep/versions/v1/subv3/v1_subv3_c.sites"), 
//				new File("/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/grep/traces/v1/subv3/coarse-grained"),
//				new File("/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/grep/versions/v1/subv3/v1_subv3_f.sites"));
//		System.out.println();
//		System.out.println(client.processor.getTotalNegative() + "\t" + client.processor.getTotalPositive());
//		System.out.println(client.getFunctionSet(client.processor.getTotalNegative()).size());
//		System.out.println(client.processor.getNegativeFrequencyMap().size());
//		System.out.println(client.list.size());
//		System.out.println("\n");
//		
//		client.printEntry();
//		System.out.println("\n");
//		for(String function: client.getFunctionSet(client.processor.getTotalNegative())){
//			System.out.println(function);
//		}
//		System.out.println("\n");
//		for(String function: client.getBoostFunctionSet((byte) 0, 0.1f)){
//			System.out.println(function);
//		}
//		System.out.println();
//		for(String function: client.getBoostFunctionSet((byte) 1, 0.1f)){
//			System.out.println(function);
//		}
//		System.out.println();
//		for(String function: client.getBoostFunctionSet((byte) 2, 0.5f)){
//			System.out.println(function);
//		}
	}

}
