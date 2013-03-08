package zuo.processor.genscript.sir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import zuo.processor.genscript.client.GenSiemensScriptsClient;
import zuo.processor.genscript.client.GenSirScriptClient;
import zuo.util.file.FileUtility;

public abstract class AbstractGenRunScript {
	final String subVersion;
	final String version;
	final String subject;
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
	protected final String endTimeCommand = "time=\"$(($(date +%s%N)-stime))\"\n" +
			"echo \"Time in seconds: $((time/1000000000)) \nTime in milliseconds: $((time/1000000))\"";
	
	
	public AbstractGenRunScript(String sub, String ver, String subV, String cc, String source, String execute, String output, String script){
		this.subject = sub;
		this.version = ver;
		this.subVersion = subV;
		this.compileCommand = cc;
		this.sourceDir = source;
		this.executeDir = execute;
		this.outputDir = output;
		this.scriptDir = script;
		
		inputsMap = FileUtility.readInputsMap(GenSirScriptClient.inputsMapFile);
		inputsCompMap = FileUtility.readInputsMap(GenSirScriptClient.inputsCompMapFile);
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
	
//    protected class FoldernameComparator implements Comparator<String>{
//		@Override
//		public int compare(String fn0, String fn1) {
//			// TODO Auto-generated method stub
//			int i0 = Integer.parseInt(fn0.substring(1));
//			int i1 = Integer.parseInt(fn1.substring(1));
//			if (i0 < i1){
//				return -1;
//			}
//			else if (i0 > i1){
//				return 1;
//			}
//			else{
//				return 0;
//			}
//		}
//		
//	}
//    protected class ShFilenameComparator implements Comparator<String>{
//
//		@Override
//		public int compare(String arg0, String arg1) {
//			// TODO Auto-generated method stub
//			int i0 = Integer.parseInt(arg0.substring(1, arg0.lastIndexOf('.')));
//			int i1 = Integer.parseInt(arg1.substring(1, arg1.lastIndexOf('.')));
//			return (i0 < i1 ? -1 : (i0 == i1 ? 0: 1));
//		}
//    	
//    }
//	
//	protected class VersionFoldernameFilter implements FilenameFilter{
//		@Override
//		public boolean accept(File arg0, String name) {
//			// TODO Auto-generated method stub
//			return name.matches("v[0-9]+");
//		}
//	}
//	protected class VersionShFilenameFilter implements FilenameFilter{
//
//		@Override
//		public boolean accept(File arg0, String arg1) {
//			// TODO Auto-generated method stub
//			return arg1.matches("v[0-9]+\\.sh");
//		}
//		
//	}

}
