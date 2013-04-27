package zuo.processor.genscript.sir;

import java.util.HashSet;
import java.util.Set;

import zuo.processor.functionentry.client.FunctionClient.Score;

public class GenRunAllAdaptiveInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	Score score;
	
	public GenRunAllAdaptiveInstrumentedScript(String version, String subject, String scriptDir, Set<Integer> subs, Score score) {
		super(version, subject, scriptDir);
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
		code.append("\techo subv$i\n");
		code.append("\tsh " + version + "\\_subv$i\\_fg_a" + score + ".sh > ../outputs.alt/" + version + "/versions/subv$i/fine-grained-adaptive-" + score + "/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_" + version + "_inst_a" + score + ".sh");
	}

}
