package zuo.processor.functionentry.site;

public class FunctionEntrySite {
	final int id;
	
	final String fileName;
	final int lineNumber;
	final String functionName;
	final int cfgNumber;
	
	public FunctionEntrySite(int id, String fileName, int lineNum, String funcName, int cfgNum){
		this.id = id;
		this.fileName = fileName;
		this.lineNumber = lineNum;
		this.functionName = funcName;
		this.cfgNumber = cfgNum;
	}

	public String getFileName() {
		return fileName;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public String getFunctionName() {
		return functionName;
	}

	public int getCfgNumber() {
		return cfgNumber;
	}
	
	public int getId() {
		return id;
	}

	public String toString(){
		return id + "\t" + fileName + "\t" + lineNumber + "\t" + functionName + "\t" + cfgNumber;  
	}
	

}
