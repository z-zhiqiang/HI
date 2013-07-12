package edu.nus.sun.processor.predicate.siemens;

import java.lang.reflect.InvocationTargetException;

import edu.nus.sun.processor.Projects;
import edu.nus.sun.processor.predicate.DefaultPredicateProcessorWithLabel;

public class SpaceMain {

  public static void main(String[] args) throws SecurityException,
      IllegalArgumentException, NoSuchMethodException, InstantiationException,
      IllegalAccessException, InvocationTargetException {
    DefaultPredicateProcessorWithLabel.construct(Projects.SPACE,
        PredicateSpaceProcessorWithLabel.class);
  }

}
