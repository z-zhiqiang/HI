package edu.nus.sun.processor.predicate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import sun.processor.predicate.PredicateItem.PredicateCategory;

public class PredicateMapping {

  public static void main(final String[] args) {
    final PredicateMapping mapping = PredicateMapping.read(new File(
        "predicate-dataset/sed/v10/predicate.mapping"));
    mapping.print();
  }

  private static PredicateCategory parsePredicateCategory(String line) {
    // RETURN, SCALAR_PAIR, BRANCH, FLOAT_KIND
    final int start = line.indexOf(',');
    final int end = line.lastIndexOf(',');
    if (start < 0 || end < start) {
      throw new RuntimeException("invalid indices, start=" + start + ", end="
          + end + ", line=" + line);
    }
    final String type = line.substring(start + 1, end).trim();
    if ("RETURN".equals(type)) {
      return PredicateCategory.RETURN;
    } else if ("SCALAR_PAIR".equals(type)) {
      return PredicateCategory.SCALAR_PAIR;
    } else if ("BRANCH".equals(type)) {
      return PredicateCategory.BRANCH;
    } else if ("FLOAT_KIND".equals(type)) {
      return PredicateCategory.FLOAT_KIND;
    } else {
      throw new RuntimeException("invalid type: [" + type + "]");
    }
  }

  public static PredicateMapping read(final File file) {
    final PredicateMapping mapping = new PredicateMapping();

    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new FileReader(file));

      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.startsWith("Predicate ")) {
          final int predicateId = Integer.parseInt(line.substring(
              line.indexOf(' ') + 1, line.indexOf(':')));
          // predicate type
          line = reader.readLine();
          final PredicateCategory category = parsePredicateCategory(line);
          // predicate info.
          line = reader.readLine();
          final int lineIndex = line.indexOf("line=") + "line=".length();
          final int lineIndexEnd = line.indexOf(',', lineIndex);
          final int lineNumber = Integer.parseInt(line.substring(lineIndex,
              lineIndexEnd));
          if (category.equals(PredicateCategory.SCALAR_PAIR)) {
            final int varIndex = line.indexOf('{');
            final int varEnd = line.indexOf('[');
            final String varName = line.substring(varIndex + 1, varEnd);
            mapping.add(predicateId, new PredicateInfo(lineNumber, category, varName));
          } else {
            mapping.add(predicateId, new PredicateInfo(lineNumber, category));
          }
          // file info
          line = reader.readLine();
          // empty line
          line = reader.readLine();
        }
      }

    } catch (final IOException e) {
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          e.printStackTrace();
        }
      }
    }
    return mapping;
  }

  private final Map<Integer, PredicateInfo> map;

  private PredicateMapping() {
    this.map = new HashMap<Integer, PredicateInfo>();
  }

  public PredicateInfo getPredicateInfo(int predicateId) {
    return this.map.get(predicateId);
  }

  private void add(final int predicateKey, final PredicateInfo predicateInfo) {
    if (this.map.containsKey(predicateKey)) {
      throw new RuntimeException("existing key " + predicateKey);
    }
    this.map.put(predicateKey, predicateInfo);
  }

  public void print() {
    for (final Map.Entry<Integer, PredicateInfo> entry : this.map.entrySet()) {
      System.out.println(entry.getKey() + ": " + entry.getValue());
    }
  }

}
