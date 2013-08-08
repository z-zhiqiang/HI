package zuo.processor.genscript.sir.iterative;

import java.util.Set;

public class GenRunAllSampledInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	final int sample;
	
	public GenRunAllSampledInstrumentedScript(String version, String subject, String scriptDir, Set<Integer> subs, int sample) {
		super(version, subject, scriptDir);
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
		code.append("\techo subv$i\n");
		code.append("\tsh " + version + "\\_subv$i\\_fg_s" + sample + ".sh > ../outputs.alt/" + version + "/versions/subv$i/fine-grained-sampled-" + sample + "/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_" + version + "_inst_s" + sample + ".sh");
	}

}
