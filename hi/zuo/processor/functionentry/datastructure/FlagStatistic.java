package zuo.processor.functionentry.datastructure;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public final class FlagStatistic{
	private int numberOfRounds;
	private final Set<Integer> rounds;
	private int best;
	private double[] bestResult;
	private Set<String> bMethods;
	private final double[] averageResult;
	private final Set<String> aMethods;
	
	public FlagStatistic(){
		this.numberOfRounds = 0;
		this.rounds = new HashSet<Integer>();
		this.best = -1;
		this.bestResult = null;
		this.bMethods = null;
		this.averageResult = new double[5];
		this.aMethods = new HashSet<String>();
	}
	
	public void solveOneResult(double[] result, Set<String> methods, int round){
		if(this.bestResult == null || result[0] < this.bestResult[0]){
			this.best = round;
			this.bestResult = result;
			this.bMethods = methods;
		}
		
		assert(result.length == this.averageResult.length);
		for(int i = 0; i < this.averageResult.length; i++){
			this.averageResult[i] = (this.averageResult[i] * this.numberOfRounds + result[i]) / (this.numberOfRounds + 1);
		}
		this.aMethods.addAll(methods);
		
		this.numberOfRounds++;
		assert(!this.rounds.contains(round));
		this.rounds.add(round);
	}
	
	public String toSting(){
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%-15s", "r#:" + numberOfRounds) + String.format("%-15s", "best:" + best));
		
		builder.append(String.format("%-15s", "bs%:" + new DecimalFormat("##.###").format(bestResult[0]))
						+ String.format("%-15s", "bp%:" + new DecimalFormat("##.###").format(bestResult[1]))
						+ String.format("%-15s", "bi:" + bestResult[2]) 
						+ String.format("%-15s", "bas:" + new DecimalFormat("#.#").format(bestResult[3])) 
						+ String.format("%-15s", "bap:" + new DecimalFormat("#.#").format(bestResult[4])));
		
		builder.append(String.format("%-15s", "as%:" + new DecimalFormat("##.###").format(averageResult[0]))
				+ String.format("%-15s", "ap%:" + new DecimalFormat("##.###").format(averageResult[1]))
				+ String.format("%-15s", "ai:" + new DecimalFormat("#.#").format(averageResult[2])) 
				+ String.format("%-15s", "aas:" + new DecimalFormat("#.#").format(averageResult[3])) 
				+ String.format("%-15s", "aap:" + new DecimalFormat("#.#").format(averageResult[4])));
		
		return builder.toString();
		
	}
}
