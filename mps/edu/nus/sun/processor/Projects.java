package edu.nus.sun.processor;

import java.util.ArrayList;
import java.util.List;

public class Projects {

  public static final IProject SPACE = new IProject() {
    @Override
    public String getProjectName() {
      return "space";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//      for (int i = 1; i < 39; ++i) {
//        // version 11 has many incomplete profiles.
//        if (i == 1 || i == 2 || i == 11 || i == 13 || i == 24 || i == 32
//            || i == 34 || i == 35)
//          continue;
//        versions.add(i);
//      }
//      // versions.add(8);
//      // versions.add(3);
//      return versions;
//    }

  };

  public static final IProject PRINT_TOKENS = new IProject() {
    @Override
    public String getProjectName() {
      return "print_tokens";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//      for (int i = 1; i < 8; ++i)
//        versions.add(i);
//      return versions;
//    }

  };

  public static final IProject PRINT_TOKENS_CIL = new IProject() {
    @Override
    public String getProjectName() {
      return "print_tokens-cil";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//      for (int i = 1; i < 8; ++i)
//        versions.add(i);
//      return versions;
//    }

  };

  public static final IProject GZIP = new IProject() {

    @Override
    public String getProjectName() {
      return "gzip";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//
//      // versions.add(2);
//      versions.add(4);
//      versions.add(5);
//      versions.add(13);
//      versions.add(14);
//      versions.add(15);
//      versions.add(16);
//      versions.add(17);
//      versions.add(19);
//      versions.add(22);
//      versions.add(34);
//      versions.add(39);
//      versions.add(43);
//      versions.add(46);
//      versions.add(51);
//      versions.add(52);
//      versions.add(54);
//      return versions;
//    }
  };

  public static final IProject GREP = new IProject() {
    @Override
    public String getProjectName() {
      return "grep";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//
//      versions.add(3);
//      versions.add(11);
//      versions.add(14);
//      versions.add(19);
//      versions.add(27);
//      versions.add(28);
//      versions.add(29);
//      versions.add(34);
//      versions.add(38);
//      versions.add(42);
//      versions.add(54);
//      versions.add(56);
//      return versions;
//    }

  };

  public static final IProject SED = new IProject() {
    @Override
    public String getProjectName() {
      return "sed";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//      for (int i = 4; i <= 6; ++i)
//        versions.add(i);
//      for (int i = 9; i <= 11; ++i)
//        versions.add(i);
//
//      versions.add(14);
//      versions.add(16);
//      versions.add(19);
//      return versions;
//    }
  };

  public static final IProject SED_CIL = new IProject() {
    @Override
    public String getProjectName() {
      return "sed-cil";
    }

//    @Override
//    public List<Integer> getVersions() {
//      List<Integer> versions = new ArrayList<Integer>();
//      for (int i = 4; i <= 6; ++i)
//        versions.add(i);
//      for (int i = 9; i <= 11; ++i)
//        versions.add(i);
//
//      versions.add(14);
//      versions.add(16);
//      versions.add(19);
//      return versions;
//    }
  };
}
