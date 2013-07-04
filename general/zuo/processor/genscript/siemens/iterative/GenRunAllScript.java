package zuo.processor.genscript.siemens.iterative;

public class GenRunAllScript extends AbstractGenRunAllScript{
	final int versionnum;
	
	
	public GenRunAllScript(String subject, String scriptDir, int subversionnum) {
		super(subject, scriptDir);
		this.versionnum = subversionnum;
	}

	

	public void genRunAllScript(){
		StringBuffer code = new StringBuffer();
		
		code.append("echo " + subject + "\n");
		code.append("sh " + subject + ".sh > ../outputs/" + subject + "/execution\n");
		code.append("\n");
		code.append("for i in {1.." + versionnum + "}\ndo\n");
		code.append("\techo v$i\n");
		code.append("\tsh v$i.sh > ../outputs/versions/v$i/outputs/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll.sh");
	}
	

}
