package zuo.processor.functionentry.datastructure;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;

public final class Statistic{
	private final FlagStatistic lCFlagStatistics;
	private final FlagStatistic gCFlagStatistics;
	private final Map<Integer, FlagStatistic> pFlagStatisticsMap;
	
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
		builder.append(String.format("%-25s", "LC: ")).append(this.lCFlagStatistics.toString()).append("\n");
		builder.append(String.format("%-25s", "GC: ")).append(this.gCFlagStatistics.toString()).append("\n");
		for(int k: this.pFlagStatisticsMap.keySet()){
			builder.append(String.format("%-25s", "Top " + k + ": ")).append(this.pFlagStatisticsMap.get(k).toString()).append("\n");
		}
		return builder.toString();
	}

	public void incertOneStatisticToExcel(Row row){
		this.lCFlagStatistics.incertOneFlagStatisticToExcel(row);
		this.gCFlagStatistics.incertOneFlagStatisticToExcel(row);
		for(int k: this.pFlagStatisticsMap.keySet()){
			this.pFlagStatisticsMap.get(k).incertOneFlagStatisticToExcel(row);
		}
	}
	
	public FlagStatistic getlCFlagStatistics() {
		return lCFlagStatistics;
	}

	public FlagStatistic getgCFlagStatistics() {
		return gCFlagStatistics;
	}

	public Map<Integer, FlagStatistic> getpFlagStatisticsMap() {
		return pFlagStatisticsMap;
	}
	
	
}