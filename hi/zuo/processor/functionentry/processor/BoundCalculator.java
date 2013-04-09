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
			return (double) ((P + F) * Math.log(P + F) - F * Math.log(F) - P * Math.log(P)) / (Math.log(2) * (P + F));
		}
		else{
			return (double) ((P + F) * Math.log(P + F) - F * Math.log(F) + (F - f) * Math.log(F - f) - (P + F - f) * Math.log(P + F - f)) / (Math.log(2) * (P + F));
		}
	}
	
	
	public void computeCBIBound(double theta){
		double threshold = 2 / theta - 1;
		System.out.println(theta);
		System.out.println(threshold);
		
		if(P * Math.log(F) / Math.log(2) <= 2 * Math.log(2)){
			throw new RuntimeException("abnormal case 1");
		}
		else if(P < F * Math.log(F)){
			System.out.println("case 2");
			int mini = computeMinima();
			System.out.println(mini);
			
			if(threshold < G(mini)){
				System.out.println(G(mini));
				throw new RuntimeException("Error case 2");
			}
			int lb = 2, ub = F;
			if(threshold < G(2)){
				lb = calculateGBoundDe(2, mini, threshold);
			}
			if(threshold < G(F)){
				ub = calculateGBoundIn(mini, F, threshold);
			}
			System.out.println("[" + lb + "," + ub + "]");
		}
		else{
			System.out.println("case 3");
			if(threshold < G(F)){
				throw new RuntimeException("Error case 3");
			}
			if(threshold >= G(2)){
				System.out.println(G(2));
				System.out.println("[2, F]");
				return;
			}
			int bound = calculateGBoundDe(2, F, threshold);
			System.out.println("[" + bound + ",F]");
		}
	}
	
	/**compute the minimum value, i.e., DG(mini) = 0
	 * @return
	 */
	private int computeMinima() {
		int start, end, mid;
		start = 2;
		end = F;
		while(start < end){
			mid = (start + end) / 2;
			double dg = DG(mid);
			System.out.println(mid + ": " + dg);
			if(dg > 0){
				end = mid;
			}
			else if(dg < 0){
				start = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return (G(start) < G(end) ? start : end);
			}
		}
		
		return -1;
	}
	private double DG(int mid) {
		return (double) 1 / P - Math.log(F) / (mid * Math.log(mid) * Math.log(mid));
	}
	
	private int calculateGBoundIn(int s, int e, double threshold){
		if(threshold < G(s) || threshold > G(e)){
			throw new RuntimeException("The threshold should be in the following range: [" + IG(s) + ", " + IG(e) + "]");
		}
		
		int start, end, mid;
		start = s;
		end = e;
		while(start < end){
			mid = (start + end) / 2;
			double g = G(mid);
			System.out.println(mid + ": " + g);
			if(g > threshold){
				end = mid;
			}
			else if(g < threshold){
				start = mid;
			}
			else{
				System.out.println(mid);
				return mid;
			}
			
			if(end - start == 1){
				System.out.println(end);
				return end;
			}
		}
		return -1;
	}

	private int calculateGBoundDe(int s, int e, double threshold){
		if(threshold < G(e) || threshold > G(s)){
			throw new RuntimeException("The threshold should be in the following range: [" + IG(e) + ", " + IG(s) + "]");
		}
		
		int start, end, mid;
		start = s;
		end = e;
		while(start < end){
			mid = (start + end) / 2;
			double g = G(mid);
			System.out.println(mid + ": " + g);
			if(g > threshold){
				start = mid;
			}
			else if(g < threshold){
				end = mid;
			}
			else{
				System.out.println(mid);
				return mid;
			}
			
			if(end - start == 1){
				System.out.println(start);
				return start;
			}
		}
		return -1;
	}
	private double G(int mid) {
		return (double) mid / P + Math.log(F) / Math.log(mid);
	}

	public static void main(String[] args) {
//		BoundCalculator bc = new BoundCalculator(156, 13429);
//		System.out.println(bc.computeIGBound(0.086813));
//		for(int i = 0; i < bc.F + 1; i++)
//			System.out.println(i + ": \t" + bc.IG(i));
//		System.out.println(new Double(bc.IG(1)).toString());
//		System.out.println(Math.log(1));
//		System.out.println(Double.compare(bc.IG(1), 0.918296));
		
		BoundCalculator bc = new BoundCalculator(243, 120);
		bc.computeCBIBound(0.5836264823531641);
	}

}
