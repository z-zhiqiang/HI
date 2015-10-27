package zuo.processor.genscript.sir.iterative.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import zuo.processor.genscript.client.iterative.java.AntGenSirScriptClient;
import zuo.processor.genscript.client.iterative.java.NanoxmlGenSirScriptClient;
import zuo.util.file.FileUtility;

public abstract class AbstractGenRunScript {
	public static final int ROUNDS = 1;
	
	final String subVersion;
	final String version;
	final String subject;
	final String srcName;
	final String compileCommand;
	
	final String sourceDir;
	final String executeDir;
	final String outputDir;
	
	final String scriptDir;
	
	public final static String runinfo = "echo \">>>>>>> running test ";
	public static final String EXE = "EXE";
	
	protected final Map<Integer, String> inputsMap;
	protected final Map<Integer, String> inputsCompMap;
	
	protected final String startTimeCommand = "stime=\"$(date +%s%N)\"";
	protected final String endTimeCommand;
	
	protected final long sleepTime;
	
	
	public AbstractGenRunScript(String sub, String srcN, String ver, String subV, String cc, String source, String execute, String output, String script){
		this.subject = sub;
		this.srcName = srcN;
		this.version = ver;
		this.subVersion = subV;
		this.compileCommand = cc;
		this.sourceDir = source;
		this.executeDir = execute;
		this.outputDir = output;
		this.scriptDir = script;
		
		inputsMap = FileUtility.readInputsMap(AntGenSirScriptClient.rootDir + subject + "/testplans.alt/" + "inputs.map");
		inputsCompMap = FileUtility.readInputsMap(AntGenSirScriptClient.rootDir + subject + "/testplans.alt/" + "inputsComp.map");
		
		this.sleepTime = sleepTime();
		
		endTimeCommand = "time=\"$(($(date +%s%N)-stime-" + sleepTime * ROUNDS * 1000000000 + "))\"\n" +
				"echo \"Time in seconds: $((time/1000000000)) \nTime in milliseconds: $((time/1000000))\"";
	}
	
	private long sleepTime(){
		long time = 0;
		for (Iterator<Integer> it = inputsMap.keySet().iterator(); it.hasNext();) {
			int index = it.next();
			String input = inputsMap.get(index);
			String[] lines = input.split("\n");
			for(String line: lines){
				if(line.startsWith("sleep")){
					System.out.println(line);
					time += Long.parseLong(line.split(" ")[1].trim());
				}
			}
		}
			
		return time;
	}
	
	public String addTimingCode(String command){
		StringBuilder builder = new StringBuilder();
		String[] lines = command.split("\n");
		for(String line: lines){
			if(line.startsWith("java")){
				builder.append("stime=\"$(date +%s%N)\"").append("\n");
				builder.append(line).append("\n");
				builder.append("time=\"$(($(date +%s%N)-stime))\"").append("\n");
				builder.append("tTime=$((tTime+time))").append("\n");
			}
			else{
				builder.append(line).append("\n");
			}
		}
		return builder.toString();
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
			pout = new PrintWriter(new BufferedWriter(new FileWriter(folder + "/" + file)));
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
