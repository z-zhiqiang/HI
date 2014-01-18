import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zuo.processor.functionentry.client.twopass.Client;
import zuo.processor.functionentry.processor.BoundCalculator;


public class Test {

	public static enum Score{
		RANDOM, NEGATIVE, H_1, F_1, H_2, PRECISION, POSITIVE
	}
	
	public static void main(String[] args) throws IOException {
		List<Object> olist = new ArrayList<Object>();
		olist.add((double) 0);
		System.out.println(olist.get(0));
		System.out.println((Double) olist.get(0) == 0.0D);
		System.out.println(Math.abs((Double) olist.get(0) - 0) < 0.00000001);
		
		
		System.out.println(4 * 0.2D);
		List<Double> dlist = new ArrayList<Double>();
		dlist.add(2 * 1.0D);
		int a = 20, b = 3;
		System.out.println(a / b * b);
		
		System.out.println(2 * Math.log(3));
		
		Set<String> set0 = new LinkedHashSet<String>();
		set0.add("a");
		set0.add("b");
		System.out.println(set0);
		Set<String> set1 = new LinkedHashSet<String>(set0);
		System.out.println(set1);
		set0.add("c");
		set1.add("d");
		System.out.println(set0);
		System.out.println(set1);
		set0.removeAll(set1);
		System.out.println(set0);
		
		String line = "TOP-(1) SUP=[  20=(+   0/-  20)] Metric=0.281028";
		System.out.println(Integer.parseInt(line.substring(line.lastIndexOf('-') + 1, line.lastIndexOf(')')).trim()));
		System.out.println(Integer.parseInt(line.substring(line.lastIndexOf('+') + 1, line.lastIndexOf('/')).trim()));
		BoundCalculator bc = new BoundCalculator(22, 341);
		double threshold = bc.IG(Integer.parseInt(line.substring(line.lastIndexOf('-') + 1, line.lastIndexOf(')')).trim()), 
				Integer.parseInt(line.substring(line.lastIndexOf('+') + 1, line.lastIndexOf('/')).trim()));
		System.out.println(threshold);
		System.out.println(bc.computeIGBound(threshold));
		
		
		Map<String, List<Object>> resultsDS = new LinkedHashMap<String, List<Object>>();
		List<Object> arrayA = new ArrayList<Object>();
		arrayA.add(3.8D);
		arrayA.add(2.8D);
		resultsDS.put("A", arrayA);
		List<Object> arrayB = new ArrayList<Object>();
		arrayB.add(0D);
		arrayB.add(0D);
		resultsDS.put("B", arrayB);
		List<Object> arrayC = new ArrayList<Object>();
		arrayC.add(4.0D);
		arrayC.add(3.5D);
		resultsDS.put("C", arrayC);
		List<Object> arrayD = new ArrayList<Object>();
		arrayD.add(0D);
		arrayD.add(0D);
		resultsDS.put("D", arrayD);
		List<Object> arrayE = new ArrayList<Object>();
		arrayE.add(3.3D);
		arrayE.add(2.5D);
		resultsDS.put("E", arrayE);
		
//		System.out.println(Client.computeCorrelationCoefficient(resultsDS, 0, 1));
		
////		System.out.println("Hello world!");
////		Map<String, int[]> map = new HashMap<String, int[]>();
////		map.put("1", new int[2]);
////		MakeTestScript.main(args);
//		double a = 0.234;
//		System.out.println(new DecimalFormat(".#").format(a));
////		assert(2 == 3);
//		
//		Map<String, Integer> map = new HashMap<String, Integer>();
//		map.put("2", 2);
//		map.put("1", 1);
//		
//		File file = new File("/home/sunzzq/adaptive/");
//		System.out.println(file.getAbsolutePath() + "/a");
//		System.out.println(String.valueOf(Score.F_1));
//		System.out.println(file.getParentFile());
//		
//		
//		Set<Integer> set = new TreeSet<Integer>();
//		Random ran = new Random();
//		for(; set.size() < 100;){
//			int s = ran.nextInt(100);
//			set.add(s);
//		}
//		System.out.println(set.toString());
//		System.out.println();
//		System.out.println(CBIClient.compressNumbers(set));	
//		
//		
//		TreeMap<Integer, String> tMap = new TreeMap<Integer, String>();
//		tMap.put(1, "1");
//		tMap.put(2, "2");
//		tMap.put(3, "3");
//		System.out.println(tMap);
//		System.out.println(tMap.lastKey());
//		
//		Set<String> lSet = new LinkedHashSet<String>();
//		lSet.add("1");
//		lSet.add("2");
//		lSet.add("3");
//		lSet.add("1");
//		lSet.add("");
//		System.out.println(lSet);
//		
//		SortedSet<Integer> tSet = new TreeSet<Integer>();
//		tSet.add(1);
//		tSet.add(4);
//		tSet.add(2);
//		System.out.println(tSet);
//		
//		Set<Integer> hSet = new HashSet<Integer>();
//		hSet.add(2);
//		hSet.add(4);
//		hSet.add(1);
//		System.out.println(hSet);
//		
//		assert(tSet.equals(hSet));
//		
//		double d1 = 0;
//		double d2 = 3.335557038025388E-4;
//		System.out.println(d2 <= d1);
//			
//		Map<Integer, String> map2 = new HashMap<Integer, String>();
//		for(int i: map2.keySet()){
//			System.out.println("yes");
//		}
//		System.out.println("over");
//		System.out.println(map2.equals(null));
//		System.out.println(map2.containsKey(0));
//		
//		Set[] setArray = new Set[2];
//		System.out.println(setArray[0]);
//		setArray[0] = new HashSet<Integer>();
//		setArray[0].add(2);
//		int[] intArray = new int[2];
//		System.out.println(intArray[0]);
//		
//		for(int i = 1; i <= 10; i++){
//			System.out.println(new DecimalFormat("#.#").format(i * 0.1));
//			System.out.println(1.0 == 0.1 * 10);
//		}
//		double percent = 0;
//		while(percent <= 1.0){
//			System.out.println(percent);
//			percent += 0.1;
//		}
//		
//		System.out.println(String.format("%-15s", "best:" + null));
//		
//		String sites = "<samples unit=\"35d4c4c11279d3c0bd91e9fd9534573d\" scheme=\"function-entries\">";
//		System.out.println(sites.matches("<sites\\sunit=\".*\"\\sscheme=\".*\">"));
//		
//		System.out.println(zuo.split.PredicateSplittingSiteProfile.extractUnitScheme(sites));
//		
//		String sitesFile = "v2_subv3_c.sites";
//		System.out.println(sitesFile.replace('c', 'p'));
//		
//		Map<String, List<Object>> mapO = new HashMap<String, List<Object>>();
//		List<Object> listO = new ArrayList<Object>();
//		listO.add(1);
//		listO.add(0.2);
//		listO.add(0.3f);
//		mapO.put("D", listO);
//		System.out.println(mapO.toString());
	}
}
