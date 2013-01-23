package sun.processor.core;


public interface IProfileProcessor {

	void init();
	
	void dispose();
	
	void process(IProfile profile);

}
