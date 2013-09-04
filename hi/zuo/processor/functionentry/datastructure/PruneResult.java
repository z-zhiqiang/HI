package zuo.processor.functionentry.datastructure;

import java.util.HashSet;
import java.util.Set;

public final class PruneResult{
	private final double[] pResult;
	private boolean pFlag;
	private final Set<String> pruneMethods;
	
	private boolean pFlagCI0;
	private boolean pFlagCI2;
	
	public PruneResult(){
		this.pResult = new double[6];
		this.pFlag = true;
		this.pruneMethods = new HashSet<String>();
		
		pFlagCI0 = true;
		pFlagCI2 = true;
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

	public boolean ispFlagCI0() {
		return pFlagCI0;
	}

	public boolean ispFlagCI2() {
		return pFlagCI2;
	}

	public void setpFlagCI0(boolean pFlagCI0) {
		this.pFlagCI0 = pFlagCI0;
	}

	public void setpFlagCI2(boolean pFlagCI2) {
		this.pFlagCI2 = pFlagCI2;
	}

	
	
}
