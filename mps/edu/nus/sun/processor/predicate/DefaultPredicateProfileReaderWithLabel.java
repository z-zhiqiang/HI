package edu.nus.sun.processor.predicate;

import java.io.File;

public class DefaultPredicateProfileReaderWithLabel extends
    AbstractPredicateProfileReaderWithLabel {

  public DefaultPredicateProfileReaderWithLabel(File profileFolder, File sitesPath) {
    super(profileFolder, sitesPath);
  }

  @Override
  protected String profileFilterPattern() {
    return "o[0-9]+\\.[fp]profile";
  }

}
