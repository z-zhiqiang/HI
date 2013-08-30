package zuo.processor.functionentry.datastructure;

import java.util.HashMap;
import java.util.Map;

public final class Statistic{
	final FlagStatistic lCFlagStatistics;
	final FlagStatistic gCFlagStatistics;
	final Map<Integer, FlagStatistic> pFlagStatisticsMap;
	
	public Statistic(int[] ks){
		this.lCFlagStatistics = new FlagStatistic();
		this.gCFlagStatistics = new FlagStatistic();
		this.pFlagStatisticsMap = new HashMap<Integer, FlagStatistic>();
		for(int k: ks){
			this.pFlagStatisticsMap.put(k, new FlagStatistic());
		}
	}
	
	public void solveOneResult(Result result, int round){
		if(result.islCFlag()){
			this.lCFlagStatistics.solveOneResult(result.getiResult(), result.getMethods(), round);
		}
		if(result.isgCFlag()){
			assert(result.islCFlag());
			this.gCFlagStatistics.solveOneResult(result.getiResult(), result.getMethods(), round);
		}
		for(int k: result.getpFlagMap().keySet()){
			PruneResult pruneResult = result.getpFlagMap().get(k);
			if(pruneResult.ispFlag()){
				assert(k != 1 || result.isgCFlag());
				this.pFlagStatisticsMap.get(k).solveOneResult(pruneResult.getpResult(), pruneResult.getPruneMethods(), round);
			}
			
		}
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%-10s", "LC: ")).append(this.lCFlagStatistics.toSting()).append("\n");
		builder.append(String.format("%-10s", "GC: ")).append(this.gCFlagStatistics.toSting()).append("\n");
		for(int k: this.pFlagStatisticsMap.keySet()){
			builder.append(String.format("%-10s", "Top " + k + ": ")).append(this.pFlagStatisticsMap.get(k).toSting()).append("\n");
		}
		return builder.toString();
	}
}
