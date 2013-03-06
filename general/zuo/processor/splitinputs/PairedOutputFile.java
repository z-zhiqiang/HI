package zuo.processor.splitinputs;

import java.io.File;

public class PairedOutputFile implements Comparable<PairedOutputFile>{
	private final File oracle;
	private final File test;
	private final int index;
	
	public PairedOutputFile(File oracle, File test){
		this.oracle = oracle;
		this.test = test;
		this.validate();
		index = Integer.parseInt(this.oracle.getName().substring(1, this.oracle.getName().lastIndexOf('.')));
//		index = Integer.parseInt(this.oracle.getName().substring(1, this.oracle.getName().length()));
	}
	
	private void validate(){
		if(!this.oracle.getName().equals(this.test.getName())){
			throw new RuntimeException();
		}
		if(this.oracle.equals(this.test)){
			throw new RuntimeException();
		}
	}

	public File getOracle() {
		return oracle;
	}

	public File getTest() {
		return test;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public int compareTo(PairedOutputFile arg0) {
		// TODO Auto-generated method stub
		return (index < arg0.index ? -1 : (index == arg0.index ? 0 : 1));
	}

}
