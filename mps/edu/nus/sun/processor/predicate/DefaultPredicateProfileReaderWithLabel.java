package edu.nus.sun.processor.predicate;

import java.io.File;
import java.util.Set;

public class DefaultPredicateProfileReaderWithLabel extends
    AbstractPredicateProfileReaderWithLabel {

  public DefaultPredicateProfileReaderWithLabel(File profileFolder, File sitesPath, Set<String> functionSet) {
    super(profileFolder, sitesPath, functionSet);
  }

  @Override
  protected String profileFilterPattern() {
    return "o[0-9]+\\.[fp]profile";
  }

}
