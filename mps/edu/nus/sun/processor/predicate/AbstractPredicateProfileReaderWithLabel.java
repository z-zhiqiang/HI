package edu.nus.sun.processor.predicate;

import java.io.File;

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

  private final ScalarPairPredicate.Factory scalarFactory;

  private final BranchPredicate.Factory branchFactory;

  private final ReturnPredicate.Factory returnFactory;

  public AbstractPredicateProfileReaderWithLabel(final File profileFolder, final File sitesPath) {
    super(profileFolder);
    this.sites = new InstrumentationSites(sitesPath);
    this.scalarFactory = new Factory();
    this.branchFactory = new BranchPredicate.Factory();
    this.returnFactory = new ReturnPredicate.Factory();
  }

  @Override
  protected IProfile createProfile(final boolean executionLabel,
      final File profileFile) {
    return new PredicateProfile(profileFile, this.sites, executionLabel,
        this.scalarFactory, this.branchFactory, this.returnFactory);
  }

  @Override
  protected String mapToTestOutput(final String profileName) {
    final int index = profileName.indexOf(".profile");
    if (index < 0) {
      throw new RuntimeException();
    }
    return profileName.substring(0, index);
  }

}
