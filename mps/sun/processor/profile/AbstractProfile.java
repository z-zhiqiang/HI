package sun.processor.profile;

import java.io.File;

import sun.processor.core.IProfile;

public abstract class AbstractProfile implements IProfile {

	private final File path;
	
	private final boolean isCorrect;

	public AbstractProfile(File path, boolean isCorrect) {
		this.path = path;
		this.isCorrect = isCorrect;
	}

	@Override
	public File getPath() {
		return this.path;
	}

	@Override
	public boolean isCorrect() {
		return this.isCorrect;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<Profile ").append(this.path).append(".: ").append(this.isCorrect).append(">");
		return builder.toString();
	}

}
