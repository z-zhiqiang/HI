package edu.nus.sun.processor.predicate.gzip;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import sun.processor.core.IProfileReader;

import edu.nus.sun.processor.Projects;
import edu.nus.sun.processor.predicate.DefaultPredicateProcessorWithLabel;

public class PredicateGzipProcessorWithLabel extends
    DefaultPredicateProcessorWithLabel {

  public PredicateGzipProcessorWithLabel(File profileFolder,
      File executionLabelFile, File resultOutputFolder, File sitesInfoPath) {
    super(profileFolder, resultOutputFolder, sitesInfoPath);
  }

  public static void main(String[] args) throws SecurityException,
      IllegalArgumentException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    construct(Projects.GZIP, PredicateGzipProcessorWithLabel.class);
  }

  @Override
  protected IProfileReader createProfileReader(File profileFolder) {
    return new PredicateGzipProfileReaderWithLabel(profileFolder, this.sitesInfoPath);
  }

}
