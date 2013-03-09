package zuo.processor.genscript.sir;

public abstract class AbstractGenRunAllScript {
	final String version;
	final String subject;
//	final int testnum;
	final String scriptDir;
//	final String outName;
	
	public AbstractGenRunAllScript(String version, String subject, String scriptDir) {
		super();
		this.version = version;
		this.subject = subject;
//		this.testnum = testnum;
		this.scriptDir = scriptDir;
	}
	
	public abstract void genRunAllScript();
	

}
