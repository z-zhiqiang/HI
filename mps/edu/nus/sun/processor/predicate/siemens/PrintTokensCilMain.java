package edu.nus.sun.processor.predicate.siemens;

import java.lang.reflect.InvocationTargetException;

import edu.nus.sun.processor.Projects;
import edu.nus.sun.processor.predicate.DefaultPredicateProcessorWithLabel;

public class PrintTokensCilMain {

  public static void main(String[] args) throws SecurityException,
      IllegalArgumentException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    DefaultPredicateProcessorWithLabel.construct(Projects.PRINT_TOKENS_CIL);
  }

}
