package zuo.processor.genscript.client.iterative.java;

public abstract class AbstractGenSirScriptClient {
	public final static String rootPath = "/home/paper_60/";
	public final static String rootDir = rootPath + "oopsla_artifacts/single/Subjects/Java/";
	
	public final static String jsampler = rootPath + "bin/JSampler.jar";
	public final static String seeder = "java -cp " + rootPath + "bin/ EqualizeLineNumbers ";
	
	public final static String outCompFile = "comp.out";

}
