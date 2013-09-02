package zuo.processor.functionentry.datastructure;

import java.util.HashSet;
import java.util.Set;

public final class PruneResult{
	private final double[] pResult;
	private boolean pFlag;
	private final Set<String> pruneMethods;
	private double percent;
	
	public PruneResult(){
		this.pResult = new double[5];
		this.pFlag = true;
		this.pruneMethods = new HashSet<String>();
		this.percent = 0;
	}

	public boolean ispFlag() {
		return pFlag;
	}

	public void setpFlag(boolean pFlag) {
		this.pFlag = pFlag;
	}

	public double[] getpResult() {
		return pResult;
	}

	public Set<String> getPruneMethods() {
		return pruneMethods;
	}

	public double getPercent() {
		return percent;
	}

	public void setPercent(double percent) {
		this.percent = percent;
	}
	
}
