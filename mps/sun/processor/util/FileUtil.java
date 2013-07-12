package sun.processor.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;

public class FileUtil {

  public static boolean isEmpty(File file) {
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));
      String line = reader.readLine();
      if (line == null) {
        return true;
      }
      int length = 0;
      for (; line != null; line = reader.readLine()) {
        line = line.trim();
        length += line.length();
      }
      return length == 0;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (reader != null)
        try {
          reader.close();
        } catch (IOException e) {
        }
    }
  }

  public static void copyToFolder(File source, File targetFolder) {
    copy(source, new File(targetFolder, source.getName()));
  }

  public static void copy(File source, File target) {
    System.out.printf("copying %s to %s...\n", source, target);
    try {
      BufferedInputStream is = new BufferedInputStream(new FileInputStream(
          source));
      BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(
          target));
      byte[] buffer = new byte[4096];
      int length;
      while ((length = is.read(buffer)) >= 0) {
        os.write(buffer, 0, length);
      }
      is.close();
      os.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static ImmutableList<String> readToList(File file) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        builder.add(line.intern());
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
  }

  public static boolean contentEqual(File f1, File f2) {
    if (!f1.getName().equals(f2.getName())) {
      throw new RuntimeException("file name should be the same.");
    }
    return testEquality(f1, f2);
  }

  private static boolean testEquality(File first, File second) {
    BufferedInputStream stream1 = null;
    BufferedInputStream stream2 = null;
    try {
      stream1 = new BufferedInputStream(new FileInputStream(first));
      stream2 = new BufferedInputStream(new FileInputStream(second));
      byte[] buffer1 = new byte[1024];
      byte[] buffer2 = new byte[1024];
      int eof1 = -5;
      int eof2 = -5;
      while (true) {
        eof1 = stream1.read(buffer1);
        eof2 = stream2.read(buffer2);
        if (!Arrays.equals(buffer1, buffer2)) {
          return false;
        }
        if (eof1 == -1 || eof2 == -1) {
          break;
        }
      }
      if (eof1 != eof2) {
        return false;
      } else {
        return Arrays.equals(buffer1, buffer2);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (stream1 != null) {
        try {
          stream1.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (stream2 != null) {
        try {
          stream2.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }
}
