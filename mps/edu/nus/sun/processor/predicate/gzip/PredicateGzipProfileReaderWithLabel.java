package edu.nus.sun.processor.predicate.gzip;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import edu.nus.sun.processor.predicate.DefaultPredicateProfileReaderWithLabel;

public class PredicateGzipProfileReaderWithLabel extends
    DefaultPredicateProfileReaderWithLabel {

  public PredicateGzipProfileReaderWithLabel(File profileFolder, File sitesPath) {
    super(profileFolder, sitesPath);
  }

  @Override
  protected FilenameFilter createProfileFilter() {
    return new FilenameFilter() {

      @Override
      public boolean accept(File arg0, String name) {
        return Pattern.matches(profileFilterPattern(), name)
//            && !name.equals("test52.profile")
            ;
      }
    };
  }

}
