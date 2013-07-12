package sun.processor.predicate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sun.processor.core.IProfile;
import sun.processor.graph.IDataSet;
import sun.processor.predicate.PredicateItem.PredicateKey;
import sun.processor.profile.IPredicateProfile;

public class PredicateDataSet implements IDataSet {

  // public static Map<String, PredicateDataSet> split(PredicateDataSet ds) {
  // Map<String, PredicateDataSet> map = new HashMap<String,
  // PredicateDataSet>();
  // for (String methodName: ds.getFunctionNames()) {
  // map.put(methodName, new PredicateDataSet(ds));
  // }
  // return map;
  // }

  public static class Run {

    private final int id;

    private final boolean label;

    private final File profile;

    private final List<PredicateItem> predicateItems;

    public boolean getLabel() {
      return this.label;
    }

    public File getProfile() {
      return profile;
    }

    public int getId() {
      return id;
    }

    public Run(final int id, final boolean label, final File profilePath) {
      this.id = id;
      this.label = label;
      this.profile = profilePath;
      this.predicateItems = new ArrayList<PredicateItem>();
    }

    private void add(PredicateItem item) {
      this.predicateItems.add(item);
    }

    public List<PredicateItem> getAllItems() {
      return this.predicateItems;
    }

    public Run project(String methodName) {
      Run run = new Run(this.id, this.label, this.profile);
      for (PredicateItem i : this.predicateItems) {
        if (i.getKey().getMethodName().equals(methodName)) {
          run.predicateItems.add(i);
        }
      }
      return run;
    }

  }

  // private final ArrayList<Run> runs;
  private final Run[] runs;

  private final Set<PredicateKey> keys;

  private final int positive;

  private final int negative;

  public int getPositive() {
    return positive;
  }

  public int getNegative() {
    return negative;
  }

  // private PredicateDataSet(PredicateDataSet ds) {
  // this.runs = new Run[ds.runs.length];
  // for (int i = 0; i < ds.runs.length;++i) {
  // this.runs[i] = new Run(ds.runs[i].label, ds.runs[i].getProfile());
  // }
  // this.numberOfPredicates = -1;
  // this.keys = new HashSet<PredicateItem.PredicateKey>();
  // }

  public PredicateDataSet(IPredicateProfile[] profiles) {
    // this.runs = new ArrayList<PredicateDataSet.Run>();
    int positive = 0;
    int negative = 0;
    this.runs = new Run[profiles.length];
    for (int i = 0; i < profiles.length; ++i) {
      final IProfile p = profiles[i];
      boolean correct = p.isCorrect();
      if (correct)
        ++positive;
      else
        ++negative;
      this.runs[i] = new Run(i, correct, p.getPath());
    }
    this.keys = new HashSet<PredicateItem.PredicateKey>();
    this.positive = positive;
    this.negative = negative;
  }

  // public void addOneRun(IPredicateProfile profile, PredicateItemFactory
  // factory) {
  // this.runs.add(new Run(profile, factory));
  // if (this.numberOfPredicates <= 0) {
  // this.numberOfPredicates = factory.getNumberOfPredicates();
  // } else if (this.numberOfPredicates != factory.getNumberOfPredicates()) {
  // throw new RuntimeException();
  // }
  // }
  public void addOnePredicateToEachRun(PredicateItem[][] items,
      int predicateIndex) {
    final int numberOfProfiles = items.length;
    assert (numberOfProfiles == this.runs.length);
    assert (numberOfProfiles > 0);
    PredicateKey key = items[0][predicateIndex].getKey();
    this.keys.add(key);
    for (int i = 0; i < numberOfProfiles; ++i) {
      assert (key == items[i][predicateIndex].getKey());
      this.runs[i].add(items[i][predicateIndex]);
    }
  }

  public String[] getFunctionNames() {
    Set<String> s = new HashSet<String>();
    for (PredicateKey key : this.keys) {
      s.add(key.getMethodName());
    }
    return s.toArray(new String[s.size()]);
  }

  public PredicateKey[] getKeys() {
    return this.keys.toArray(new PredicateKey[this.keys.size()]);
  }

  public Run[] getRuns() {
    return this.runs;
  }

  public Run[] project(String methodName) {
    Run[] result = new Run[this.runs.length];
    for (int i = 0; i < result.length; ++i) {
      result[i] = this.runs[i].project(methodName);
    }
    int size = result[0].predicateItems.size();
    for (int i = 1; i < result.length; ++i)
      assert (size == result[i].predicateItems.size());
    return result;
  }
}
