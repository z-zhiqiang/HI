package edu.nus.sun.processor.mps.client;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import sun.processor.core.IProfileProcessor;
import sun.processor.core.IProfileReader;
import sun.processor.core.Processor;
import sun.processor.core.Processor.BackEnd;

public abstract class AbstractProcessorWithLabels {

  private final File profileFolder;

  private final File resultOutputFolder;
  
//  private final static com.sun.management.OperatingSystemMXBean mxbean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  

  public AbstractProcessorWithLabels(File profileFolder, File resultOutputFolder) {
    super();
    this.profileFolder = profileFolder;
    this.resultOutputFolder = resultOutputFolder;
  }

  public void run(List<Object> resultsList, PrintWriter writer) {
//	Object[] statistics = new Object[5];
	
    final long start = System.currentTimeMillis();
    
    final IProfileReader profileReader = this.createProfileReader(profileFolder);
    final List<BackEnd> backends = this.createBackends(resultOutputFolder);
    final List<IProfileProcessor> profileProcessors = this.createProfileProcessors(resultOutputFolder);
    new Processor(profileReader, backends, profileProcessors).process(resultsList, writer);
    
    final long end = System.currentTimeMillis();
    double time = (double) (end - start) / 1000;
    System.out.println("preprocessing time = " + time);
    
//    for(Object statistic: statistics){
//    	resultsList.add(statistic);
//    }
    resultsList.add(time);
  }

  protected abstract IProfileReader createProfileReader(File profileFolder);

  protected abstract List<BackEnd> createBackends(File resultOutputFolder);

  protected abstract List<IProfileProcessor> createProfileProcessors(File resultOutputFolder);
  
  public static long printMemoryUsage(PrintWriter writer){
	  Runtime runtime = Runtime.getRuntime();
	  runtime.gc();
	  runtime.gc();
	  long memory = runtime.totalMemory() - runtime.freeMemory();
	  
	  System.out.println();
	  System.out.println("Used memory is bytes: " + memory);
	  System.out.println("Used memory is kilobytes: " + memory / (1024L));
	  
	  writer.println();
	  writer.println("Used memory is bytes: " + memory);
	  writer.println("Used memory is kilobytes: " + memory / (1024L));
	  
	  return memory / 1024L;
  }

}
