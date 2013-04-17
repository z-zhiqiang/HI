package zuo.processor.genscript.siemens;

public abstract class AbstractGenRunAllScript {
	final String subject;
	final String scriptDir;
	
	public AbstractGenRunAllScript(String subject, String scriptDir) {
		super();
		this.subject = subject;
		this.scriptDir = scriptDir;
	}
	
	public abstract void genRunAllScript();
	

}
