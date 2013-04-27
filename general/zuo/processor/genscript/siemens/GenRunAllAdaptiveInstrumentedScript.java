package zuo.processor.genscript.siemens;

import java.util.HashSet;
import java.util.Set;

import zuo.processor.functionentry.client.FunctionClient.Score;

public class GenRunAllAdaptiveInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	final Score score;
	
	public GenRunAllAdaptiveInstrumentedScript(String subject, String scriptDir, Set<Integer> subs, Score score) {
		super(subject, scriptDir);
		this.subs = subs;
		this.score = score;
	}
	
	public void genRunAllScript(){
        StringBuffer code = new StringBuffer();
        
        StringBuilder builder = new StringBuilder();
		for(int i: subs){
			builder.append(i).append(" ");
		}
        
		code.append("for i in " + builder.toString() + "\ndo\n");
		code.append("\techo v$i\n");
		code.append("\tsh v$i\\_fg_a" + score + ".sh > ../outputs/versions/v$i/fine-grained-adaptive-" + score + "/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_inst_a" + score + ".sh");
	}

}
