package zuo.processor.functionentry.datastructure;

import java.util.HashSet;
import java.util.Set;

public final class PruneResult{
	private final double[] pResult;
	private boolean pFlag;
	private final Set<String> pruneMethods;
	
	public PruneResult(){
		this.pResult = new double[6];
		this.pFlag = true;
		this.pruneMethods = new HashSet<String>();
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

	
}
