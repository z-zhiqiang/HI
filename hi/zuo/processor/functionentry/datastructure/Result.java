package zuo.processor.functionentry.datastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Result {
	private final double[] iResult;
	private boolean lCFlag;
	private boolean gCFlag;
	private final Set<String> methods;
	private final Map<Integer, PruneResult> pFlagMap;

	public Result(int[] ks) {
		this.iResult = new double[6];
		this.lCFlag = true;
		this.gCFlag = true;
		this.methods = new HashSet<String>();
		this.pFlagMap = new HashMap<Integer, PruneResult>();
		for (int k : ks) {
			this.pFlagMap.put(k, new PruneResult());
		}
	}

	public boolean islCFlag() {
		return lCFlag;
	}

	public void setlCFlag(boolean lCFlag) {
		this.lCFlag = lCFlag;
	}

	public boolean isgCFlag() {
		return gCFlag;
	}

	public void setgCFlag(boolean gCFlag) {
		this.gCFlag = gCFlag;
	}

	public double[] getiResult() {
		return iResult;
	}

	public Set<String> getMethods() {
		return methods;
	}

	public Map<Integer, PruneResult> getpFlagMap() {
		return pFlagMap;
	}


}
