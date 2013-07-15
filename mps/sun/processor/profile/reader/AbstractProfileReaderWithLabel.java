package sun.processor.profile.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import sun.processor.core.IProfile;
import sun.processor.core.IProfileReader;

import com.google.common.collect.ImmutableMap;

/**
 * 
 * read profiles, but different from the class AbstractProfileReader, this class
 * reads the execution labels from a file, instead of computing the labels by
 * comparing the oracle test output and the real output
 * 
 * @author Chengnian Sun
 * 
 */
public abstract class AbstractProfileReaderWithLabel implements
    IProfileReader {

  protected final File profileFolder;

  public AbstractProfileReaderWithLabel(File profileFolder) {
    System.out.println("profile folder: " + profileFolder);
    this.profileFolder = profileFolder;
  }

  protected abstract String profileFilterPattern();

  protected FilenameFilter createProfileFilter() {
    return new FilenameFilter() {

      @Override
      public boolean accept(File arg0, String name) {
        return Pattern.matches(profileFilterPattern(), name);
      }
    };
  }

  protected File[] collectProfileFiles() {
    File[] profileFiles = this.profileFolder.listFiles(this
        .createProfileFilter());
    if (profileFiles.length == 0)
      throw new RuntimeException("No profiles in folder " + this.profileFolder);
    return profileFiles;
  }

  protected abstract IProfile createProfile(boolean executionLabel,
      File profileFile);

  @Override
  public IProfile[] readProfiles() {
    File[] profileFiles = this.collectProfileFiles();
    IProfile[] profiles = new IProfile[profileFiles.length];
    System.out.println("reading profiles...");

    for (int i = 0; i < profiles.length; ++i) {
      if ((i + 1) % 5 == 0)
        System.out.print(".");
      if ((i + 1) % 600 == 0)
        System.out.println();
      final File profileFile = profileFiles[i];
      
      boolean label = true;
      if(profileFile.getName().endsWith(".fprofile")){
    	  label = false;
      }
      profiles[i] = this.createProfile(label, profileFile);
    }
    System.out.println();
    return profiles;
  }

}
