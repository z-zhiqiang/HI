import java.io.File;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;


public class Test {

	public static enum Score{
		RANDOM, NEGATIVE, H_1, F_1, H_2, PRECISION, POSITIVE
	}
	
	public static void main(String[] args) {
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
	}
}
