import java.text.DecimalFormat;


public class Test {

	public static void main(String[] args) {
//		System.out.println("Hello world!");
//		Map<String, int[]> map = new HashMap<String, int[]>();
//		map.put("1", new int[2]);
//		MakeTestScript.main(args);
		double a = 0.234;
		System.out.println(new DecimalFormat(".#").format(a));
		assert(2 == 3);
	}
}
