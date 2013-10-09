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

import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.functionentry.processor.PruningProcessor;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySite;
import zuo.processor.functionentry.site.FunctionEntrySites;

public class TwopassFunctionClient {
	private FunctionEntryProfile[] failingProfiles;
	private final PruningProcessor processor;
	private final SitesInfo sInfo;
	private List<Map.Entry<FunctionEntrySite, Integer>> list;
	
	public TwopassFunctionClient(File csitesFile, File failingProfilesFolder, File fsitesFile){
		FunctionEntrySites sites = new FunctionEntrySites(csitesFile);
		FunctionEntryProfileReader reader = new FunctionEntryProfileReader(failingProfilesFolder, sites);
		failingProfiles = reader.readFailingFunctionEntryProfiles();
		processor = new PruningProcessor(failingProfiles);
		processor.process();
//		assert(processor.getTotalNegative() + processor.getTotalPositive() == profiles.length);
		assert(processor.getNegativeFrequencyMap().size() == sites.getNumFunctionEntrySites());
		
		this.sInfo = new SitesInfo(new InstrumentationSites(fsitesFile));
		filterNegativeFrequencyMap(processor.getNegativeFrequencyMap());
		assert(processor.getNegativeFrequencyMap().size() == this.sInfo.getMap().size());
		// construct a sorted list of negativeFrequencyMap
		constructEntryList();
	}


	private void constructEntryList() {
		List<Map.Entry<FunctionEntrySite, Integer>> list = new ArrayList<Map.Entry<FunctionEntrySite, Integer>>(processor.getNegativeFrequencyMap().entrySet());
		Collections.sort(list, new Comparator<Map.Entry<FunctionEntrySite, Integer>>(){

			@Override
			public int compare(Map.Entry<FunctionEntrySite, Integer> arg0, Map.Entry<FunctionEntrySite, Integer> arg1) {
				// TODO Auto-generated method stub
				return rank(arg0, arg1);
			}

			private int rank(Map.Entry<FunctionEntrySite, Integer> arg0, Map.Entry<FunctionEntrySite, Integer> arg1) {
				// TODO Auto-generated method stub
				int r = 0;
				r = new Integer(arg1.getValue())
					.compareTo(new Integer(arg0.getValue()));
				if(r == 0){
					String method0 = arg0.getKey().getFunctionName();
					String method1 = arg1.getKey().getFunctionName();
					r = new Integer(sInfo.getMap().get(method0).getNumSites())
						.compareTo(new Integer(sInfo.getMap().get(method1).getNumSites()));
					if(r == 0){
						r = new Integer(sInfo.getMap().get(method0).getNumPredicates())
							.compareTo(new Integer(sInfo.getMap().get(method1).getNumPredicates()));
					}
				}
				return r;
			}
			
		});
		
		this.list = Collections.unmodifiableList(list);
	}

	
	private void filterNegativeFrequencyMap(Map<FunctionEntrySite, Integer> negativeFrequencyMap) {
		// TODO Auto-generated method stub
		for(Iterator<FunctionEntrySite> it = negativeFrequencyMap.keySet().iterator(); it.hasNext();){
			String function = it.next().getFunctionName();
			if(!sInfo.getMap().containsKey(function)){
				it.remove();
			}
		}
	}

	public Set<String> getFunctionSet(int bound){
		Set<String> functionSet = new LinkedHashSet<String>();
		for(int i = 0; i < list.size(); i++){
			Entry<FunctionEntrySite, Integer> entry = (Entry<FunctionEntrySite, Integer>) list.get(i);
			if(entry.getValue() >= bound){
				String functionName = entry.getKey().getFunctionName();
				if(functionSet.contains(functionName))
					throw new RuntimeException("multiple functions with the same name");
				functionSet.add(functionName);
			}
		}
		return Collections.unmodifiableSet(functionSet);
	}

	/**
	 * @param mode: 0->F&%; 1->F; 2->%
	 * @param percent
	 * @return
	 */
	public Set<String> getBoostFunctionSet(byte mode, double percent){
		Set<String> functionSet = new LinkedHashSet<String>();
		
		switch(mode){
		case 0: //only functions f(m)==F and the number of functions selected is less than "percent"
			for(int i = 0, j = 0; i < list.size(); i++){
				Entry<FunctionEntrySite, Integer> entry = list.get(i);
				if(entry.getValue() >= this.failingProfiles.length && j < list.size() * percent){
					functionSet.add(entry.getKey().getFunctionName());
					j++;
				}
			}
			break;
		case 1: //all the functions whose negative support is F
			for(int i = 0; i < list.size(); i++){
				Entry<FunctionEntrySite, Integer> entry = (Entry<FunctionEntrySite, Integer>) list.get(i);
				if(entry.getValue() >= this.failingProfiles.length){
					functionSet.add(entry.getKey().getFunctionName());
				}
				else{
					break;
				}

			}
			break;
		case 2: //the number of functions selected is less than "percent"
			for(int i = 0; i < list.size() * percent; i++){
				functionSet.add(((Entry<FunctionEntrySite,Integer>) list.get(i)).getKey().getFunctionName());
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
			Entry<FunctionEntrySite, Integer> entry = (Entry<FunctionEntrySite, Integer>) list.get(i);
			String method = entry.getKey().getFunctionName();
			if(sInfo.getMap().containsKey(method)){
				System.out.println(String.format("%-45s", method) + entry.getValue().toString() + "   \t" + sInfo.getMap().get(method).toStringWithoutSites());
				writer.println(String.format("%-45s", method) + entry.getValue().toString() + "   \t" + sInfo.getMap().get(method).toStringWithoutSites());
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


	public List<Map.Entry<FunctionEntrySite, Integer>> getList() {
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
