package zuo.processor.genscript.siemens.iterative;

import java.util.Set;

public class GenRunAllAdaptiveInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	
	public GenRunAllAdaptiveInstrumentedScript(String subject, String scriptDir, Set<Integer> subs) {
		super(subject, scriptDir);
		this.subs = subs;
	}
	
	public void genRunAllScript(){
        StringBuffer code = new StringBuffer();
        
        StringBuilder builder = new StringBuilder();
		for(int i: subs){
			builder.append(i).append(" ");
		}
        
		code.append("for i in " + builder.toString() + "\ndo\n");
		code.append("\techo v$i\n");
		code.append("\tsh v$i\\_fg_a.sh > ../outputs/versions/v$i/fine-grained-adaptive/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_inst_a.sh");
	}

}
