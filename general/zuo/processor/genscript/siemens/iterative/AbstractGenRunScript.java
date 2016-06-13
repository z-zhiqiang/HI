package zuo.processor.genscript.siemens.iterative;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import zuo.processor.genscript.client.iterative.GenSiemensScriptsClient;
import zuo.util.file.FileUtility;

public abstract class AbstractGenRunScript {
	public static final int ROUNDS = 3;
	public static final int ROUNDS_FG = 1;

	final String version;
	final String subject;
	final String compileCommand;
	
	final String sourceDir;
	final String executeDir;
	final String outputDir;
	
	final String scriptDir;
	
	public final static String runinfo = "echo \">>>>>>> running test ";
	
	protected final Map<Integer, String> inputsMap;
	
	protected final String startTimeCommand = "stime=\"$(date +%s%N)\"";
	protected final String endTimeCommand = "time=\"$(($(date +%s%N)-stime))\"\n" +
			"echo \"Time in seconds: $((time/1000000000)) \nTime in milliseconds: $((time/1000000))\"";
	
	
	public AbstractGenRunScript(String sub, String ver, String cc, String source, String execute, String output, String script){
		this.subject = sub;
		this.version = ver;
		this.compileCommand = cc;
		this.sourceDir = source;
		this.executeDir = execute;
		this.outputDir = output;
		this.scriptDir = script;
		
		inputsMap = FileUtility.readInputsMap(GenSiemensScriptsClient.rootDir + subject + "/testplans.alt/" + "inputs.map");
	}
	
    public abstract void genRunScript() throws IOException;
    protected abstract void mkOutDir();
    
    public static void printToFile(String scr, String folder, String file){
		PrintWriter pout = null;
		try{
			File fd = new File(folder);
			if(!fd.exists()){
				fd.mkdir();
			}
			pout = new PrintWriter(new BufferedWriter(new FileWriter(folder + "/" + file, false)));
			pout.print(scr);
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			pout.close();
		}
	}
	

}
