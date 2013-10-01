package edu.nus.sun.processor.mps.client;

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

  public void run(List<Object> resultsList) {
	int[] statistics = new int[4];
	
    final long start = System.currentTimeMillis();
    
    final IProfileReader profileReader = this.createProfileReader(profileFolder);
    final List<BackEnd> backends = this.createBackends(resultOutputFolder);
    final List<IProfileProcessor> profileProcessors = this.createProfileProcessors(resultOutputFolder);
    new Processor(profileReader, backends, profileProcessors).process(statistics);
    
    final long end = System.currentTimeMillis();
    long time = (end - start) / 1000;
    System.out.println("preprocessing time = " + time);
    
    for(int statistic: statistics){
    	resultsList.add(statistic);
    }
    resultsList.add(time);
  }

  protected abstract IProfileReader createProfileReader(File profileFolder);

  protected abstract List<BackEnd> createBackends(File resultOutputFolder);

  protected abstract List<IProfileProcessor> createProfileProcessors(File resultOutputFolder);

}
