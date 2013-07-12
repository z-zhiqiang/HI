package edu.nus.sun.processor.predicate.sed;

import java.lang.reflect.InvocationTargetException;

import edu.nus.sun.processor.Projects;
import edu.nus.sun.processor.predicate.DefaultPredicateProcessorWithLabel;

public class PredicateSedCilMain {

  public static void main(String[] args) throws SecurityException,
      IllegalArgumentException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    DefaultPredicateProcessorWithLabel.construct(Projects.SED_CIL);
  }

}
