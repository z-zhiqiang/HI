package zuo.processor.functionentry.processor;

public class BoundCalculator {
	private final int F;
	private final int P;
	
	public BoundCalculator(int fail, int pass){
		this.F = fail;
		this.P = pass;
	}
	
	/**compute the frequency bound corresponding to the given information gain threshold
	 * @param threshold
	 * @return
	 */
	public int computeIGBound(double threshold){
		if(threshold < 0 || threshold > IG(F)){
			throw new RuntimeException("The threshold should be in the following range: [" + IG(0) + ", " + IG(F) + "]");
		}
			
		int start, end, midPt;
		start = 0;
		end = F;
		while(start < end){
			midPt = (start + end) / 2;
			double ig = IG(midPt);
			if(ig > threshold){
				end = midPt;
			}
			else if(ig < threshold){
				start = midPt;
			}
			else{
				return midPt;
			}
			
			if(end - start == 1){
				return end;
			}
		}
		return -1;
	}
	private double IG(int f){
		if(f == F){
			return ((P + F) * Math.log(P + F) - F * Math.log(F) - P * Math.log(P)) / (Math.log(2) * (P + F));
		}
		else{
			return ((P + F) * Math.log(P + F) - F * Math.log(F) + (F - f) * Math.log(F - f) - (P + F - f) * Math.log(P + F - f)) / (Math.log(2) * (P + F));
		}
	}
	
	public static void main(String[] args) {
		BoundCalculator bc = new BoundCalculator(156, 13429);
		System.out.println(bc.computeIGBound(0.086813));
		for(int i = 0; i < bc.F + 1; i++)
			System.out.println(i + ": \t" + bc.IG(i));
//		System.out.println(new Double(bc.IG(1)).toString());
//		System.out.println(Math.log(1));
//		System.out.println(Double.compare(bc.IG(1), 0.918296));
	}

}
