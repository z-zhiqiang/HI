package zuo.processor.genscript.sir.twopass;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import zuo.util.file.FileCollection;
import zuo.util.file.FileUtility;


public class GenRunBoostFineGrainedInstrumentScript extends AbstractGenRunScript implements GenRunInstrumentScript {
	final String traceDir;
	private final List<Integer> failingTests;
	private final List<Integer> passingTests;
	private String srcName;
	private final File boostFunctions;
	
	
	public GenRunBoostFineGrainedInstrumentScript(String sub, String ver, String subV, String cc, String sD, String eD, String oD, String scD, String tD, String failing, String passing, String srcN, File boost) {
		super(sub, ver, subV, cc, sD, eD, oD, scD);
		this.traceDir = tD;
		this.mkOutDir();
		this.failingTests = FileUtility.readInputsArray(failing);
		this.passingTests = FileUtility.readInputsArray(passing);
		
		this.srcName = srcN;
		this.boostFunctions = boost;
	}


	@Override
	public void genRunScript() {
		String includeC = "";
		String paraC = "";
		if(subject.equals("gzip")){
			paraC = " -DSTDC_HEADERS=1 -DHAVE_UNISTD_H=1 -DDIRENT=1 -DHAVE_ALLOCA_H=1";
		}
		if(subject.equals("grep")){
			includeC = " -I" + sourceDir;
		}
		String instrumentCommand = compileCommand
				+ "sampler-cc -fsampler-scheme=function-entries -fsampler-scheme=branches -fsampler-scheme=returns -fsampler-scheme=scalar-pairs -fcompare-constants -fsampler-scheme=float-kinds -fno-sample "
				+ functionFiltering()
				+ sourceDir + srcName + ".c" 
				+ " $COMPILE_PARAMETERS"
				+ paraC
				+ " -o " + executeDir + subVersion + "_binst.exe"
				+ includeC
				;
		
		StringBuffer code = new StringBuffer();
		code.append(instrumentCommand + "\n");
		code.append("echo script: " + subVersion + "\n");
		code.append("export VERSIONSDIR=" + executeDir + "\n");
		code.append("export TRACESDIR=" + traceDir + "\n");
		
		
		stmts(code);
		code.append(startTimeCommand + "\n");
		for(int j = 0; j < ROUNDS; j++){
			stmts(code);
		}
		code.append(endTimeCommand + " >& " + outputDir + "time\n");
		
		code.append("rm ../outputs/*\n");
//		code.append("rm $TRACESDIR/o*profile\n");
		
		printToFile(code.toString(), scriptDir, version + "_" + subVersion + "_boost.sh");
	}


	private String functionFiltering() {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		for(String function: FileCollection.readSet(boostFunctions)){
			builder.append("-finclude-function=").append(function).append(" ");
		}
		builder.append("-fexclude-function=* ");
		return builder.toString();
	}


	private void stmts(StringBuffer code) {
		for (Iterator<Integer> it = failingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".fprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_binst.exe "));
			code.append("\n");
		}
		
		for (Iterator<Integer> it = passingTests.iterator(); it.hasNext();) {
			int index = it.next();
			code.append(runinfo + index + "\"\n");// running info
			code.append("export SAMPLER_FILE=$TRACESDIR/o" + index + ".pprofile\n");
			code.append(inputsMap.get(index).replace(EXE, "$VERSIONSDIR/" + subVersion + "_binst.exe "));
			code.append("\n");
		}
	}


	@Override
	protected void mkOutDir() {
		//make directory for outputs
		File fo = new File(outputDir);
		if(!fo.exists()){
			fo.mkdirs();
		}
		
		//make directory for traces
		File ft = new File(traceDir);
		if(!ft.exists()){
			ft.mkdirs();
		}
	}
	
	
}
