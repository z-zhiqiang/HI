package sun.processor.core;

import java.io.File;

public interface IProfile {

	File getPath();

	boolean isCorrect();
	
	void dispose();

}
