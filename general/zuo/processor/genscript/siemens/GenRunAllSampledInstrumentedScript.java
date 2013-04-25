package zuo.processor.genscript.siemens;

import java.util.HashSet;
import java.util.Set;

public class GenRunAllSampledInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	final int sample;
	
	public GenRunAllSampledInstrumentedScript(String subject, String scriptDir, Set<Integer> subs, int sample) {
		super(subject, scriptDir);
		this.subs = subs;
		this.sample = sample;
	}
	
	public void genRunAllScript(){
        StringBuffer code = new StringBuffer();
        
        StringBuilder builder = new StringBuilder();
		for(int i: subs){
			builder.append(i).append(" ");
		}
        
		code.append("for i in " + builder.toString() + "\ndo\n");
		code.append("\techo v$i\n");
		code.append("\tsh v$i\\_fg_s" + sample + ".sh > ../outputs/versions/v$i/fine-grained-sampled-" + sample + "/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_inst_s" + sample + ".sh");
	}

}
