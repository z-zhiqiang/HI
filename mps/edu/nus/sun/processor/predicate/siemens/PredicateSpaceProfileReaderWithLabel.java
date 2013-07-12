package edu.nus.sun.processor.predicate.siemens;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.regex.Pattern;

import edu.nus.sun.processor.predicate.DefaultPredicateProfileReaderWithLabel;

public class PredicateSpaceProfileReaderWithLabel extends
    DefaultPredicateProfileReaderWithLabel {

  private final HashSet<String> excludedProfiles;

  public PredicateSpaceProfileReaderWithLabel(File profileFolder, File sitesPath) {
    super(profileFolder, sitesPath);
    this.excludedProfiles = new HashSet<String>();

//    this.excludedProfiles.add("t7033.profile");
//    this.excludedProfiles.add("t4168.profile");
  }

  @Override
  protected FilenameFilter createProfileFilter() {
    return new FilenameFilter() {

      @Override
      public boolean accept(File arg0, String name) {
        return Pattern.matches(profileFilterPattern(), name)
//            && !(excludedProfiles.contains(name) && arg0.getName().equals("v11"))
                ;
      }
    };
  }

}
