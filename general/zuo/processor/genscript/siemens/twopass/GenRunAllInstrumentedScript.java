package zuo.processor.genscript.siemens.twopass;

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
		code.append("\tsh v$i\\_cfg.sh > ../outputs/versions/v$i/coarse-fine-grained/execution\n");
		code.append("\tsh v$i\\_boost.sh > ../outputs/versions/v$i/boost/execution\n");
		code.append("\tsh v$i\\_pruneMinusBoost.sh > ../outputs/versions/v$i/prune-minus-boost/execution\n");
		code.append("\tsh v$i\\_prune.sh > ../outputs/versions/v$i/prune/execution\n");
		code.append("done");
		
		System.out.println(code.toString());
		AbstractGenRunScript.printToFile(code.toString(), scriptDir, "runAll_inst.sh");
	}

}
