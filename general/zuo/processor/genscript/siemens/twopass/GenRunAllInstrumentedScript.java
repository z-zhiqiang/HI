package zuo.processor.genscript.siemens.twopass;

import java.util.HashSet;
import java.util.Set;

public class GenRunAllInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	
	public GenRunAllInstrumentedScript(String subject, String scriptDir, Set<Integer> subs) {
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
		code.append("\tsh v$i\\_cg.sh > ../outputs/versions/v$i/coarse-grained/execution\n");
		code.append("\tsh v$i\\_fg.sh > ../outputs/versions/v$i/fine-grained/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_inst.sh");
	}

}