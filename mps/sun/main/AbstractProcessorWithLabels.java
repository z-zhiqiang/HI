package sun.main;

import java.io.File;
import java.util.List;

import sun.processor.core.IProfileProcessor;
import sun.processor.core.IProfileReader;
import sun.processor.core.Processor;
import sun.processor.core.Processor.BackEnd;

public abstract class AbstractProcessorWithLabels {

  private final File profileFolder;

  private final File resultOutputFolder;

  public AbstractProcessorWithLabels(File profileFolder, File resultOutputFolder) {
    super();
    this.profileFolder = profileFolder;
    this.resultOutputFolder = resultOutputFolder;
  }

  public void run() {
    final long start = System.currentTimeMillis();
    final IProfileReader profileReader = this.createProfileReader(profileFolder);
    final List<BackEnd> backends = this.createBackends(resultOutputFolder);
    final List<IProfileProcessor> profileProcessors = this
        .createProfileProcessors(resultOutputFolder);
    new Processor(profileReader, backends, profileProcessors).process();
    final long end = System.currentTimeMillis();
    System.out.println("time = " + (end - start) / 1000);
  }

  protected abstract IProfileReader createProfileReader(File profileFolder);

  protected abstract List<BackEnd> createBackends(File resultOutputFolder);

  protected abstract List<IProfileProcessor> createProfileProcessors(
      File resultOutputFolder);

}
