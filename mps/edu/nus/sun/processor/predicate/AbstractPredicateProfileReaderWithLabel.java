package edu.nus.sun.processor.predicate;

import java.io.File;
import java.util.Set;

import sun.processor.core.IProfile;
import sun.processor.profile.InstrumentationSites;
import sun.processor.profile.PredicateProfile;
import sun.processor.profile.predicate.BranchPredicate;
import sun.processor.profile.predicate.ReturnPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate.Factory;
import sun.processor.profile.reader.AbstractProfileReaderWithLabel;

public abstract class AbstractPredicateProfileReaderWithLabel extends
    AbstractProfileReaderWithLabel {

  private final InstrumentationSites sites;
  
  private final Set<String> functionSet;

  private final ScalarPairPredicate.Factory scalarFactory;

  private final BranchPredicate.Factory branchFactory;

  private final ReturnPredicate.Factory returnFactory;

  public AbstractPredicateProfileReaderWithLabel(final File profileFolder, final File sitesPath, final Set<String> functionSet) {
    super(profileFolder);
    this.sites = new InstrumentationSites(sitesPath);
    this.functionSet = functionSet;
    this.scalarFactory = new Factory();
    this.branchFactory = new BranchPredicate.Factory();
    this.returnFactory = new ReturnPredicate.Factory();
  }

  @Override
  protected IProfile createProfile(final boolean executionLabel, final File profileFile) {
    return new PredicateProfile(profileFile, this.sites, this.functionSet, executionLabel,
        this.scalarFactory, this.branchFactory, this.returnFactory);
  }

}
