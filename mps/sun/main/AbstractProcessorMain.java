package sun.main;

import java.io.File;
import java.util.List;

import sun.processor.core.IProfileProcessor;
import sun.processor.core.IProfileReader;
import sun.processor.core.Processor;
import sun.processor.core.Processor.BackEnd;

public abstract class AbstractProcessorMain {

	private final File resultOutputFolder;
	private final String profileFolder;

	protected AbstractProcessorMain(String profileFolder,
			File resultOutputFolder) {
		this.resultOutputFolder = resultOutputFolder;
		this.profileFolder = profileFolder;
	}

	public void run() {
		final IProfileReader profileReader = this.createProfileReader(profileFolder);
		final List<BackEnd> backends = this.createBackends(resultOutputFolder);
		final List<IProfileProcessor> profileProcessors = this
				.createProfileProcessors(resultOutputFolder);
		new Processor(profileReader, backends, profileProcessors).process();
	}

	protected abstract IProfileReader createProfileReader(String profileFolder);

	protected abstract List<BackEnd> createBackends(File resultOutputFolder);

	protected abstract List<IProfileProcessor> createProfileProcessors(
			File resultOutputFolder);

}
