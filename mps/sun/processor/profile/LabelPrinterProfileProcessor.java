package sun.processor.profile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import sun.processor.core.IProfile;
import sun.processor.core.IProfileProcessor;

public class LabelPrinterProfileProcessor implements IProfileProcessor {

	private final File resultFile;

	private BufferedWriter writer;

	public LabelPrinterProfileProcessor(File resultFile) {
		this.resultFile = resultFile;
	}

	@Override
	public void init() {
		try {
			if (!this.resultFile.getParentFile().exists()) {
				this.resultFile.getParentFile().mkdirs();
			}
			this.writer = new BufferedWriter(new FileWriter(this.resultFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void dispose() {
		if (this.writer != null)
			try {
				this.writer.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	@Override
	public void process(IProfile profile) {
		if (!profile.isCorrect()){
			try {
				writer.write("Incorrect: ");
				writer.write(profile.getPath().getName());
				writer.newLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
