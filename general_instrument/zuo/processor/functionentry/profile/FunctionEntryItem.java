package zuo.processor.functionentry.profile;

import zuo.processor.functionentry.site.FunctionEntrySite;

public class FunctionEntryItem {
	private final byte counter;
	private final FunctionEntrySite site;

	public FunctionEntryItem(int count, FunctionEntrySite site){
		this.counter = (byte) (count > 0 ? 1 : 0);
		this.site = site;
	}
	
	public String toString(){
		return counter + "\t" + site;
	}

	public byte getCounter() {
		return counter;
	}

	public FunctionEntrySite getSite() {
		return site;
	}
	
}
