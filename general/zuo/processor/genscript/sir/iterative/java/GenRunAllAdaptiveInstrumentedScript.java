package zuo.processor.genscript.sir.iterative.java;

import java.util.Set;


public class GenRunAllAdaptiveInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	
	public GenRunAllAdaptiveInstrumentedScript(String version, String subject, String scriptDir, Set<Integer> subs) {
		super(version, subject, scriptDir);
		this.subs = subs;
	}
	
	public void genRunAllScript(){
        StringBuffer code = new StringBuffer();
        
        StringBuilder builder = new StringBuilder();
		for(int i: subs){
			builder.append(i).append(" ");
		}
        
		code.append("for i in " + builder.toString() + "\ndo\n");
		code.append("\techo subv$i\n");
		code.append("\tsh " + version + "\\_subv$i\\_fg_a.sh > ../outputs.alt/" + version + "/versions/subv$i/fine-grained-adaptive/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_" + version + "_inst_a.sh");
	}

}
