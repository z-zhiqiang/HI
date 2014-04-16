package zuo.processor.genscript.bash.iterative;

import java.util.Set;

public class GenRunAllInstrumentedScript extends AbstractGenRunAllScript {
	final Set<Integer> subs;
	
	public GenRunAllInstrumentedScript(String version, String subject, String scriptDir, Set<Integer> subs) {
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
		code.append("\tsh " + version + "\\_subv$i\\_cg.sh > ../outputs.alt/" + version + "/versions/subv$i/coarse-grained/execution\n");
		code.append("\tsh " + version + "\\_subv$i\\_fg.sh > ../outputs.alt/" + version + "/versions/subv$i/fine-grained/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_" + version + "_inst.sh");
	}

}
