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
		if(threshold < IG(0, 0) || threshold > IG(F, 0)){
			throw new RuntimeException("The threshold should be in the following range: [" + IG(0, 0) + ", " + IG(F, 0) + "]");
		}
		System.out.println("The threshold should be in the following range: [" + IG(0, 0) + ", " + IG(F, 0) + "]");
		
		int start, end, midPt;
		start = 0;
		end = F;
		while(start < end){
			midPt = (start + end) / 2;
			double ig = IG(midPt, 0);
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
	
	public double IG(int neg, int pos) {
		// TODO Auto-generated method stub
		int total = F + P;
		return H(F, P) - (neg + pos) * H(neg, pos) / total - (total - neg - pos) * H(F - neg, P - pos) / total;
		
	}

	private static double H(int neg, int pos) {
		// TODO Auto-generated method stub
		if(neg * pos == 0){
			return 0;
		}
		else{
			return ((neg + pos) * Math.log(neg + pos) - neg * Math.log(neg) - pos * Math.log(pos)) / (Math.log(2) * (neg + pos));
		}
	}
	
	public int computeCBIBound(double threshold){
		if(DH(2, P) <= 0){
			throw new RuntimeException("abnormal case 1");
//			return 2;
		} 
		else {
			double h_1_2 = SelectingProcessor.H_1(2, P, F);
			double h_1_F = SelectingProcessor.H_1(F, P, F);
			if(DH(F, P) < 0){
				int f0 = compute_f0(P);
				
				double h_1_f0 = SelectingProcessor.H_1(f0, P, F);
				double h_1_f1 = SelectingProcessor.H_1(f0 + 1, P, F);
				double max = (h_1_f0 > h_1_f1 ? h_1_f0 : h_1_f1);
				double min = (h_1_f0 < h_1_f1 ? h_1_f0 : h_1_f1);
				if(threshold > max){
//					throw new RuntimeException("OutOfRange Error case 2: " + threshold + ">" + max);
					System.out.println("OutOfRange case 2: " + threshold + ">" + max);
					return F + 1;
				}
				int lb = 2, ub = F;
				if(threshold > h_1_f0){
					lb = f0 + 1;
				}
				else if(threshold >= h_1_2 && threshold <= h_1_f0){
					lb = calculateHBoundIn(2, f0, threshold);
				}
				if(threshold > h_1_f1){
					ub = f0;
				}
				else if(threshold >= h_1_F && threshold <= h_1_f1){
					ub = calculateHBoundDe(f0 + 1, F, threshold);
				}
//				System.out.println("[" + lb + "," + ub + "]");
				return lb;
			}
			else{
				int lb = 2;
				if(threshold > h_1_F){
//					throw new RuntimeException("OutOfRange Error case 3");
					System.out.println("OutOfRange case 3");
					return F + 1;
				}
				if(threshold > h_1_2){
					lb = calculateHBoundIn(2, F, threshold);
				}
//				System.out.println("[" + lb + ",F]");
				return lb;
			}
		}
	}
	
	/** derivative of H score
	 * @param f
	 * @param p: for H_1, p==P
	 * @return
	 */
	public double DH(int f, int p){
		if(f <= 1 || p == 0){
			return 0;
		}
		return Math.log(F) / (Math.log(f) * f * Math.log(f)) - (double) 1 / p;
	}
	
	public int compute_f0(int p) {
		int start, end, mid;
		start = 2;
		end = F;
		while(start < end){
			mid = (start + end) / 2;
			double dh = DH(mid, p);
//			System.out.println(mid + ": " + dh);
			if(dh > 0){
				start = mid;
			}
			else if(dh < 0){
				end = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return start;
			}
		}
		
		return -1;
	}
	
	private int calculateHBoundIn(int s, int e, double threshold){
		if(threshold < SelectingProcessor.H_1(s, P, F) || threshold > SelectingProcessor.H_1(e, P, F)){
			throw new RuntimeException("The threshold should be in the following range: [" + SelectingProcessor.H_1(s, P, F) + ", " + SelectingProcessor.H_1(e, P, F) + "]");
		}
		
		int start, end, mid;
		start = s;
		end = e;
		if(SelectingProcessor.H_1(start, P, F) == threshold){
			return start;
		}
		if(SelectingProcessor.H_1(end, P, F) == threshold){
			return end;
		}
		while(start < end){
			mid = (start + end) / 2;
			double g = SelectingProcessor.H_1(mid, P, F);
//			System.out.println(mid + ": " + g);
			if(g > threshold){
				end = mid;
			}
			else if(g < threshold){
				start = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return end;
			}
		}
		return -1;
	}

	private int calculateHBoundDe(int s, int e, double threshold){
		if(threshold < SelectingProcessor.H_1(e, P, F) || threshold > SelectingProcessor.H_1(s, P, F)){
			throw new RuntimeException("The threshold should be in the following range: [" + SelectingProcessor.H_1(e, P, F) + ", " + SelectingProcessor.H_1(s, P, F) + "]");
		}
		
		int start, end, mid;
		start = s;
		end = e;
		if(SelectingProcessor.H_1(start, P, F) == threshold){
			return start;
		}
		if(SelectingProcessor.H_1(end, P, F) == threshold){
			return end;
		}
		while(start < end){
			mid = (start + end) / 2;
			double h = SelectingProcessor.H_1(mid, P, F);
//			System.out.println(mid + ": " + h);
			if(h > threshold){
				start = mid;
			}
			else if(h < threshold){
				end = mid;
			}
			else{
				return mid;
			}
			
			if(end - start == 1){
				return start;
			}
		}
		return -1;
	}

	public static void main(String[] args) {
//		BoundCalculator bc = new BoundCalculator(156, 13429);
//		System.out.println(bc.computeIGBound(0.086813));
//		for(int i = 0; i < bc.F + 1; i++)
//			System.out.println(i + ": \t" + bc.IG(i));
//		System.out.println(new Double(bc.IG(1)).toString());
//		System.out.println(Math.log(1));
//		System.out.println(Double.compare(bc.IG(1), 0.918296));
		
		BoundCalculator bc = new BoundCalculator(205, 158);
		System.out.println(bc.IG(203, 0));
		System.out.println(bc.computeIGBound(0.9451427749638099));
		System.out.println(bc.computeIGBound(0));
	}

	public int getF() {
		return F;
	}

	public int getP() {
		return P;
	}
	

}
