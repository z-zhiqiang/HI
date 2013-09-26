import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import zuo.processor.cbi.client.CBIClient;


public class Test {

	public static enum Score{
		RANDOM, NEGATIVE, H_1, F_1, H_2, PRECISION, POSITIVE
	}
	
	public static void main(String[] args) throws IOException {
//		System.out.println("Hello world!");
//		Map<String, int[]> map = new HashMap<String, int[]>();
//		map.put("1", new int[2]);
//		MakeTestScript.main(args);
		double a = 0.234;
		System.out.println(new DecimalFormat(".#").format(a));
//		assert(2 == 3);
		
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("2", 2);
		map.put("1", 1);
		
		File file = new File("/home/sunzzq/adaptive/");
		System.out.println(file.getAbsolutePath() + "/a");
		System.out.println(String.valueOf(Score.F_1));
		
		
		Set<Integer> set = new TreeSet<Integer>();
		Random ran = new Random();
		for(; set.size() < 100;){
			int s = ran.nextInt(100);
			set.add(s);
		}
		System.out.println(set.toString());
		System.out.println();
		System.out.println(CBIClient.compressNumbers(set));	
		
		
		TreeMap<Integer, String> tMap = new TreeMap<Integer, String>();
		tMap.put(1, "1");
		tMap.put(2, "2");
		tMap.put(3, "3");
		System.out.println(tMap);
		System.out.println(tMap.lastKey());
		
		Set<String> lSet = new LinkedHashSet<String>();
		lSet.add("1");
		lSet.add("2");
		lSet.add("3");
		lSet.add("1");
		lSet.add("3");
		System.out.println(lSet);
		
		SortedSet<Integer> tSet = new TreeSet<Integer>();
		tSet.add(1);
		tSet.add(4);
		tSet.add(2);
		System.out.println(tSet);
		
		Set<Integer> hSet = new HashSet<Integer>();
		hSet.add(2);
		hSet.add(4);
		hSet.add(1);
		System.out.println(hSet);
		
		assert(tSet.equals(hSet));
		
		double d1 = 0;
		double d2 = 3.335557038025388E-4;
		System.out.println(d2 <= d1);
			
		Map<Integer, String> map2 = new HashMap<Integer, String>();
		for(int i: map2.keySet()){
			System.out.println("yes");
		}
		System.out.println("over");
		System.out.println(map2.equals(null));
		System.out.println(map2.containsKey(0));
		
		Set[] setArray = new Set[2];
		System.out.println(setArray[0]);
		setArray[0] = new HashSet<Integer>();
		setArray[0].add(2);
		int[] intArray = new int[2];
		System.out.println(intArray[0]);
		
		for(int i = 1; i <= 10; i++){
			System.out.println(new DecimalFormat("#.#").format(i * 0.1));
			System.out.println(1.0 == 0.1 * 10);
		}
		double percent = 0;
		while(percent <= 1.0){
			System.out.println(percent);
			percent += 0.1;
		}
		
		System.out.println(String.format("%-15s", "best:" + null));
		
//		Runtime.getRuntime().exec("sleep 100");
	}
}
