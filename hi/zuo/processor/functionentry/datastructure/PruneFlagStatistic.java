package zuo.processor.functionentry.datastructure;


import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;


public class PruneFlagStatistic extends FlagStatistic {
	private int numberOfRoundsCI2;
	private int numberOfRoundsCI0;
	
	public PruneFlagStatistic(){
		super();
		this.numberOfRoundsCI2 = 0;
		this.numberOfRoundsCI0 = 0;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("%-10s", "rCI2#:" + numberOfRoundsCI2)).append(String.format("%-10s", "rCI0#:" + numberOfRoundsCI0));
		builder.append(super.toString());
		return builder.toString();
	}

	public void incertOneFlagStatisticToExcel(Row row){
		int cellnum = row.getPhysicalNumberOfCells();
		Cell cell = row.createCell(cellnum++);
		cell.setCellValue(numberOfRoundsCI2);
		cell = row.createCell(cellnum++);
		cell.setCellValue(numberOfRoundsCI0);
		
		super.incertOneFlagStatisticToExcel(row);
	}

	public void increaseNumberOfRoundsCI0(){
		this.numberOfRoundsCI0++;
	}
	
	public void increaseNumberOfRoundsCI2(){
		this.numberOfRoundsCI2++;
	}
}
