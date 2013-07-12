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

//  protected final Map<String, Boolean> executionLabels;

  public AbstractProfileReaderWithLabel(File profileFolder) {
    System.out.println("profile folder: " + profileFolder);
//    System.out.println("execution label folder: " + executionLabelFile);
    this.profileFolder = profileFolder;
//    this.executionLabels = this.readExecutionLabels(executionLabelFile);
  }

//  private static Boolean parseLabel(String label) {
//    if (label.equals("pass"))
//      return Boolean.TRUE;
//    else if (label.equals("fail"))
//      return Boolean.FALSE;
//    else
//      throw new RuntimeException(
//          "Cannot reach here. Unhandled execution label " + label);
//  }

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

//  private Map<String, Boolean> readExecutionLabels(File executionLabelFile) {
//    BufferedReader reader = null;
//    try {
//      ImmutableMap.Builder<String, Boolean> builder = ImmutableMap.builder();
//      reader = new BufferedReader(new FileReader(executionLabelFile));
//      for (String line = reader.readLine(); line != null; line = reader
//          .readLine()) {
//        line = line.trim();
//        if (line.isEmpty())
//          continue;
//        String[] elements = line.split("\\s+");
//        if (elements.length != 2)
//          throw new RuntimeException("Ill-formatted execution label file. "
//              + line);
//        builder.put(elements[0], parseLabel(elements[1]));
//      }
//      return builder.build();
//    } catch (IOException e) {
//      throw new RuntimeException("Error in reading execution label file "
//          + executionLabelFile.getAbsolutePath(), e);
//    } finally {
//      if (reader != null)
//        try {
//          reader.close();
//        } catch (IOException e) {
//          e.printStackTrace();
//        }
//    }
//  }

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
//      final Boolean label = this.executionLabels.get(this
//          .mapToTestOutput(profileFile.getName()));
//      if (label == null) {
//        throw new RuntimeException("Cannot find execution labell for profile "
//            + profileFile.getName() + " with the key = "
//            + this.mapToTestOutput(profileFile.getName()));
//      }
      profiles[i] = this.createProfile(label, profileFile);
    }
    System.out.println();
    return profiles;
  }

  protected abstract String mapToTestOutput(String profileName);
}
