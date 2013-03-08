package zuo.processor.genscript.sir;

public class GenRunAllScript extends AbstractGenRunAllScript{
	final int subversionnum;
	
	
	public GenRunAllScript(String version, String subject, int testnum, String scriptDir, String outN, int subversionnum) {
		super(version, subject, testnum, scriptDir, outN);
		this.subversionnum = subversionnum;
	}


	public void genRunAllScript(){
		StringBuffer code = new StringBuffer();
		
		code.append("echo " + version + "\n");
		code.append("sh " + version + ".sh > ../outputs/" + version + "/" + subject + "/execution\n");
		code.append("for t in {1.." + testnum + "}\ndo\n\tmv ../outputs/" + outName + "$t ../outputs/" + version + "/" + subject + "/o$t.out\ndone\n");
		code.append("\n");
		code.append("for i in {1.." + subversionnum + "}\ndo\n");
		code.append("\techo subv$i\n");
		code.append("\tsh " + version + "\\_subv$i.sh > ../outputs/" + version + "/versions/subv$i/outputs/execution\n");
		code.append("\tfor j in {1.." + testnum + "}\n");
		code.append("\tdo\n\t\tmv ../outputs/" + outName + "$j ../outputs/" + version + "/versions/subv$i/outputs/o$j.out\n");
		code.append("\tdone\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_" + version + ".sh");
	}

	public static void main(String[] args) {
//		new GenRunAllScript("v1", "grep", 809, 18, "").genRunAllScript();
	}
}
