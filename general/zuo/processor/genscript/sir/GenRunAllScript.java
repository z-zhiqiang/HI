package zuo.processor.genscript.sir;

public class GenRunAllScript extends AbstractGenRunAllScript{
	final int subversionnum;
	
	
	public GenRunAllScript(String version, String subject, String scriptDir, int subversionnum) {
		super(version, subject, scriptDir);
		this.subversionnum = subversionnum;
	}

	

	public void genRunAllScript(){
		StringBuffer code = new StringBuffer();
		
		code.append("echo " + version + "\n");
		code.append("sh " + version + ".sh > ../outputs.alt/" + version + "/" + subject + "/execution\n");
		code.append("\n");
		code.append("for i in {1.." + subversionnum + "}\ndo\n");
		code.append("\techo subv$i\n");
		code.append("\tsh " + version + "\\_subv$i.sh > ../outputs.alt/" + version + "/versions/subv$i/outputs/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_" + version + ".sh");
	}
	

	public static void main(String[] args) {
		new GenRunAllScript("v1", "grep", "", 18).genRunAllScript();
	}
}
