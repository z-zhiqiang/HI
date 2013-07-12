package edu.nus.sun.processor.predicate.siemens;

import java.io.File;

import sun.processor.core.IProfileReader;

import edu.nus.sun.processor.predicate.DefaultPredicateProcessorWithLabel;

public class PredicateSpaceProcessorWithLabel extends
    DefaultPredicateProcessorWithLabel {

  public PredicateSpaceProcessorWithLabel(File profileFolder, File resultOutputFolder, File sitesInfoPath) {
    super(profileFolder, resultOutputFolder, sitesInfoPath);
  }

  @Override
  protected IProfileReader createProfileReader(File profileFolder) {
    return new PredicateSpaceProfileReaderWithLabel(profileFolder, this.sitesInfoPath);
  }

}
