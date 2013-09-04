package zuo.processor.functionentry.datastructure;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import zuo.processor.cbi.client.CBIClient;

public class FlagStatistic{
	private int numberOfRounds;
	private final Set<Integer> rounds;
	
	private String best;
	private double[] bestResult;
	private Set<String> bMethods;
	
	private final double[] averageResult;
	private final Set<String> aMethods;
	
	public FlagStatistic(){
		this.numberOfRounds = 0;
		this.rounds = new TreeSet<Integer>();
		
		this.best = null;
		this.bestResult = new double[6];
		this.bMethods = new HashSet<String>();
		
		this.averageResult = new double[6];
		this.aMethods = new HashSet<String>();
	}
	
	public void solveOneResult(double[] result, Set<String> methods, int round){
		if(this.numberOfRounds == 0 || result[0] < this.bestResult[0]){
			this.best = String.valueOf(round);
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
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%-10s", "r#:" + numberOfRounds) + String.format("%-15s", "best:" + best));
		
		if(this.numberOfRounds != 0){
			builder.append(String.format("%-15s", "bs%:" + new DecimalFormat("##.##").format(bestResult[0]))
					+ String.format("%-15s", "bp%:" + new DecimalFormat("##.##").format(bestResult[1]))
					+ String.format("%-15s", "bi:" + new DecimalFormat("##.##").format(bestResult[2])) 
					+ String.format("%-15s", "bas:" + new DecimalFormat("##.##").format(bestResult[3])) 
					+ String.format("%-15s", "bap:" + new DecimalFormat("##.##").format(bestResult[4]))
					+ String.format("%-25s", "bafp:" + new DecimalFormat("##.##").format(bestResult[5]))
					);
			
			builder.append(String.format("%-15s", "as%:" + new DecimalFormat("##.##").format(averageResult[0]))
					+ String.format("%-15s", "ap%:" + new DecimalFormat("##.##").format(averageResult[1]))
					+ String.format("%-15s", "ai:" + new DecimalFormat("##.##").format(averageResult[2])) 
					+ String.format("%-15s", "aas:" + new DecimalFormat("##.##").format(averageResult[3])) 
					+ String.format("%-15s", "aap:" + new DecimalFormat("##.##").format(averageResult[4]))
					+ String.format("%-15s", "aafp:" + new DecimalFormat("##.##").format(averageResult[5]))
					);
			
			builder.append("\n");
			builder.append(String.format("%-25s", ""));
			builder.append(CBIClient.compressNumbers(rounds));
		}
		
		return builder.toString();
	}

	public void incertOneFlagStatisticToExcel(Row row){
		int cellnum = row.getPhysicalNumberOfCells();
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(numberOfRounds);
		cell = row.createCell(cellnum++);
		cell.setCellValue(best);
		
		if(this.numberOfRounds != 0){
			for(int index = 0; index < bestResult.length; index++){
				cell = row.createCell(cellnum++);
				cell.setCellValue(doubleFormat(bestResult[index]));
			}
			for(int index = 0; index < averageResult.length; index++){
				cell = row.createCell(cellnum++);
				cell.setCellValue(doubleFormat(averageResult[index]));
			}
		}
		else{
			for(int index = 0; index < bestResult.length + averageResult.length; index++){
				cell = row.createCell(cellnum++);
				cell.setCellValue(" ");
			}
		}
	}
	
	public static double doubleFormat(double value){
		return Double.parseDouble(new DecimalFormat("##.##").format(value));
	}
	
	public int getNumberOfRounds() {
		return numberOfRounds;
	}

	public Set<Integer> getRounds() {
		return rounds;
	}

	public String getBest() {
		return best;
	}

	public double[] getBestResult() {
		return bestResult;
	}

	public Set<String> getbMethods() {
		return bMethods;
	}

	public double[] getAverageResult() {
		return averageResult;
	}

	public Set<String> getaMethods() {
		return aMethods;
	}

	
}
