package edu.nus.sun.processor.predicate;

import sun.processor.predicate.PredicateItem.PredicateCategory;

public class PredicateInfo {

  private final String variableNameInAssignment;

  private final PredicateCategory category;

  private final int lineNumber;

  public PredicateInfo(int lineNumber, PredicateCategory category) {
    this(lineNumber, category, null);
  }

  public PredicateInfo(int lineNumber, PredicateCategory category,
      String variableNameInAssignment) {
    this.lineNumber = lineNumber;
    this.category = category;
    this.variableNameInAssignment = variableNameInAssignment;
  }

  public String getVariableNameInAssignment() {
    return variableNameInAssignment;
  }

  public PredicateCategory getCategory() {
    return category;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public String toString() {
    return "" + lineNumber;
  }

}
