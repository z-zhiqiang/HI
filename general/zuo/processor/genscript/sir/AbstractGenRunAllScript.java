package zuo.processor.genscript.sir;

public abstract class AbstractGenRunAllScript {
	final String version;
	final String subject;
	final int testnum;
	final String scriptDir;
	final String outName;
	
	public AbstractGenRunAllScript(String version, String subject, int testnum, String scriptDir, String outN) {
		super();
		this.version = version;
		this.subject = subject;
		this.testnum = testnum;
		this.scriptDir = scriptDir;
		this.outName = outN;
	}
	
	public abstract void genRunAllScript();
	

}
