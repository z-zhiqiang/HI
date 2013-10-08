package sun.processor.predicate.constructor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import sun.processor.core.IDataSet;
import sun.processor.core.IDataSetConstructor;
import sun.processor.core.IProfile;
import sun.processor.predicate.PredicateDataSet;
import sun.processor.predicate.PredicateItem;
import sun.processor.predicate.PredicateItem.PredicateKey;
import sun.processor.predicate.PredicateItem.PredicateType;
import sun.processor.predicate.PredicateItemFactory;
import sun.processor.profile.IPredicateProfile;
import sun.processor.profile.predicate.BranchPredicate;
import sun.processor.profile.predicate.FloatKindPredicate;
import sun.processor.profile.predicate.ReturnPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate;

public class PredicateDataSetConstructor implements IDataSetConstructor {

  private int numberOfPredicateItemsGenerated;

  private int numberOfPredicateItemsFilteredByIncrease;

  private int numberOfPredicateItemsFilteredByLocal;

  private final boolean filtering;

  /**
   * by default, we filter the predicates.
   */
  public PredicateDataSetConstructor() {
    this(true);
  }

  public PredicateDataSetConstructor(boolean filtering) {
    this.filtering = filtering;
  }

  private static int getNumberOfReturnPredicates(
      IPredicateProfile[] predicateProfiles) {
    return ((IPredicateProfile) predicateProfiles[0]).getReturnPredicates()
        .size();
  }

  private static int getNumberOfBranchPredicates(
      IPredicateProfile[] predicateProfiles) {
    return predicateProfiles[0].getBranchPredicates().size();
  }

  private static int getNumberOfScalarPredicates(
      IPredicateProfile[] predicateProfiles) {
    return predicateProfiles[0].getScalarPredicates().size();
  }

  private static int getNumberOfFloatKindsPredicates(
      IPredicateProfile[] predicateProfiles) {
    return predicateProfiles[0].getFloatKindPredicates().size();
  }

  private static void getBranchPredicates(IPredicateProfile[] profiles,
      int index, BranchPredicate[] verticalReturnPredicates) {
    // BranchPredicate[] branchPredicates = new
    // BranchPredicate[profiles.length];
    final int length = profiles.length;
    assert verticalReturnPredicates.length == length;
    for (int i = 0; i < length; ++i) {
      verticalReturnPredicates[i] = profiles[i].getBranchPredicates()
          .get(index);
    }
    // return branchPredicates;
  }

  private static ScalarPairPredicate[] getMetaScalarPredicates(
      IPredicateProfile[] profiles, int index,
      ScalarPairPredicate[] scalarPredicates) {
    final int length = profiles.length;
    // ScalarPairPredicate[] scalarPredicates = new ScalarPairPredicate[length];
    assert scalarPredicates.length == length;
    for (int i = 0; i < length; ++i) {
      scalarPredicates[i] = profiles[i].getScalarPredicates().get(index);
    }
    return scalarPredicates;
  }

  private static void getReturnPredicates(IPredicateProfile[] profiles,
      int index, final ReturnPredicate[] returnPredicatesCollector) {
    // ReturnPredicate[] returnPredicates = new
    // ReturnPredicate[profiles.length];
    final int numberOfProfiles = profiles.length;
    assert returnPredicatesCollector.length == numberOfProfiles;
    for (int i = 0; i < numberOfProfiles; ++i) {
      returnPredicatesCollector[i] = profiles[i].getReturnPredicates().get(
          index);
    }
    // return returnPredicates;
  }

  private static IPredicateProfile[] cast(IProfile[] profiles) {
    IPredicateProfile[] casted = new IPredicateProfile[profiles.length];
    for (int i = 0; i < profiles.length; ++i) {
      casted[i] = (IPredicateProfile) profiles[i];
    }
    return casted;
  }

  private static void convert(PredicateItemFactory factory,
      FloatKindPredicate[] verticalPredicates,
      final PredicateItem[][] itemsCollector) {
    // PredicateItem[][] items = new PredicateItem[verticalPredicates.length][];
    final int length = verticalPredicates.length;
    assert itemsCollector.length == length;
    for (int i = 0; i < length; ++i) {
      // items[i] = factory.createPredicateItems(verticalPredicates[i]);
      factory.createPredicateItems(verticalPredicates[i], itemsCollector[i]);
    }
  }

  private static void convert(PredicateItemFactory factory,
      BranchPredicate[] verticalBranchPredicates, PredicateItem[][] items) {
    // PredicateItem[][] items = new
    // PredicateItem[verticalBranchPredicates.length][];
    assert items.length == verticalBranchPredicates.length;
    for (int i = 0; i < verticalBranchPredicates.length; ++i) {
      // items[i] = factory.createPredicateItems(verticalBranchPredicates[i]);
      factory.createPredicateItems(verticalBranchPredicates[i], items[i]);
    }
    // return items;
  }

  private static void convert(PredicateItemFactory factory,
      ScalarPairPredicate[] verticalScalarPairPredicates,
      final PredicateItem[][] itemsCollector) {
    final int length = verticalScalarPairPredicates.length;
    // PredicateItem[][] items = new PredicateItem[length][];
    assert itemsCollector.length == length;
    for (int i = 0; i < length; ++i) {
      // items[i] =
      // factory.createPredicateItems(verticalScalarPairPredicates[i]);
      // factory.createPredicateItems(verticalScalarPairPredicates[i],
      // itemsCollector[i]);
      itemsCollector[i] = verticalScalarPairPredicates[i]
          .getPredicateItems(factory);
    }
    // return items;
  }

  private static PredicateItem[][] convert(PredicateItemFactory factory,
      ReturnPredicate[] verticalReturnPredicates, final PredicateItem[][] items) {
    int length = verticalReturnPredicates.length;
    assert items.length == length;
    // PredicateItem[][] items = new
    // PredicateItem[verticalReturnPredicates.length][];
    for (int i = 0; i < length; ++i) {
      // items[i] = factory.createPredicateItems(verticalReturnPredicates[i]);
      factory.createPredicateItems(verticalReturnPredicates[i], items[i]);
    }
    return items;
  }

  @Override
  public IDataSet createDataSet(IProfile[] profiles, Object[] resultsArray, PrintWriter writer) {
    PredicateItemFactory factory = new PredicateItemFactory();
    if (profiles.length == 0) {
      throw new RuntimeException("empty profiles");
    }
    if (!(profiles[0] instanceof IPredicateProfile))
      throw new RuntimeException();
    IPredicateProfile[] predicateProfiles = cast(profiles);
    PredicateDataSet ds = new PredicateDataSet(predicateProfiles);

    System.out.println("constructing return predicates...");
    constructReturnPredicates(factory, predicateProfiles, ds);

    System.out.println("constructing branch predicates...");
    constructBranchPredicates(factory, predicateProfiles, ds);

    System.out.println("constructing scalar pair predicates...");
    constructScalarPairPredicates(factory, predicateProfiles, ds);

    System.out.println("constructing float kinds predicates...");
    constructFloatKindsPredicates(factory, predicateProfiles, ds);

    System.out.printf("Generated %d predicates and filtered away "
        + "%d ones with increase and %d ones with local-pruning.\n",
        this.numberOfPredicateItemsGenerated,
        this.numberOfPredicateItemsFilteredByIncrease,
        this.numberOfPredicateItemsFilteredByLocal);
    
    writer.printf("Generated %d predicates and filtered away "
            + "%d ones with increase and %d ones with local-pruning.\n",
            this.numberOfPredicateItemsGenerated,
            this.numberOfPredicateItemsFilteredByIncrease,
            this.numberOfPredicateItemsFilteredByLocal);

    //added to get the predicates number info
    resultsArray[0] = this.numberOfPredicateItemsGenerated;
    resultsArray[1] = this.numberOfPredicateItemsFilteredByIncrease;
    resultsArray[2] = this.numberOfPredicateItemsFilteredByLocal;
    
    assignPredicateID(ds);

    return ds;
  }

  private void constructFloatKindsPredicates(PredicateItemFactory factory,
      IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
    final int numberOfProfiles = predicateProfiles.length;
    final int numberOfDistinctFloatKindPredicates = getNumberOfFloatKindsPredicates(predicateProfiles);
    final PredicateItem[][] items = new PredicateItem[numberOfProfiles][PredicateItemFactory.NUM_PREDICATES_PER_FLOAT];
    final FloatKindPredicate[] verticalPredicates = new FloatKindPredicate[numberOfProfiles];
    final boolean[] filteringResult = new boolean[PredicateItemFactory.NUM_PREDICATES_PER_FLOAT];
    for (int i = 0; i < numberOfDistinctFloatKindPredicates; ++i) {
      // FloatKindPredicate[] verticalPredicates = getFloatKindPredicates(
      // predicateProfiles, i);
      getFloatKindPredicates(predicateProfiles, i, verticalPredicates);
      // PredicateItem[][] items = convert(factory, verticalPredicates);
      convert(factory, verticalPredicates, items);
      // final boolean[] filteringResult = filter(items, predicateProfiles);
      filter(items, predicateProfiles, filteringResult);

      this.numberOfPredicateItemsGenerated += PredicateItemFactory.NUM_PREDICATES_PER_FLOAT;

      assert (PredicateItemFactory.NUM_PREDICATES_PER_FLOAT == items[0].length);
      for (int j = 0; j < PredicateItemFactory.NUM_PREDICATES_PER_FLOAT; ++j) {
        if (!filteringResult[j]) {
          // PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
          // for (int k = 0; k < oneVerticalItem.length; ++k) {
          // oneVerticalItem[k] = items[k][j];
          // }
          // ds.addOnePredicateToEachRun(oneVerticalItem);
          ds.addOnePredicateToEachRun(items, j);
        } else {
          // this.numberOfPredicateItemsFilteredByIncrease++;
        }
      }
    }
  }

  private void getFloatKindPredicates(IPredicateProfile[] profiles, int index,
      final FloatKindPredicate[] predicatesCollector) {
    // FloatKindPredicate[] predicates = new
    // FloatKindPredicate[profiles.length];
    int numberOfProfiles = profiles.length;
    assert predicatesCollector.length == numberOfProfiles;
    for (int i = 0; i < numberOfProfiles; ++i) {
      predicatesCollector[i] = profiles[i].getFloatKindPredicates().get(index);
    }
    // return predicates;
  }

  private void assignPredicateID(PredicateDataSet ds) {
    int sequence = -1;
    for (PredicateKey key : ds.getKeys()) {
      key.setId(++sequence);
    }
  }

  private void constructScalarPairPredicates(PredicateItemFactory factory,
      IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
    final int numberOfDistinctScalarPairPredicates = getNumberOfScalarPredicates(predicateProfiles);
    final int numberOfProfiles = predicateProfiles.length;
    final ScalarPairPredicate[] metaScalarPredicates = new ScalarPairPredicate[numberOfProfiles];
    // final PredicateItem[][] items = new
    // PredicateItem[numberOfProfiles][PredicateItemFactory.NUM_PREDICATES_PER_SCALAR_PAIR];
    final PredicateItem[][] items = new PredicateItem[numberOfProfiles][];
    final boolean[] filteringResult = new boolean[PredicateItemFactory.NUM_PREDICATES_PER_SCALAR_PAIR];
    for (int i = 0; i < numberOfDistinctScalarPairPredicates; ++i) {
//      if (i % 500 == 0)
//        System.out.println("scalar-pairs " + i + "/"
//            + numberOfDistinctScalarPairPredicates);
      getMetaScalarPredicates(predicateProfiles, i, metaScalarPredicates);

      // PredicateItem[][] items = convert(factory, verticalScalarPredicates);
      convert(factory, metaScalarPredicates, items);
      filter(items, predicateProfiles, filteringResult);

      this.numberOfPredicateItemsGenerated += PredicateItemFactory.NUM_PREDICATES_PER_SCALAR_PAIR;

      assert (filteringResult.length == items[0].length);
      for (int predicateIndex = 0; predicateIndex < PredicateItemFactory.NUM_PREDICATES_PER_SCALAR_PAIR; ++predicateIndex) {
        if (!filteringResult[predicateIndex]) {
          // PredicateItem[] oneVerticalItem = new
          // PredicateItem[numberOfProfiles];
          // for (int k = 0; k < numberOfProfiles; ++k) {
          // oneVerticalItem[k] = items[k][j];
          // }
          // ds.addOnePredicateToEachRun(oneVerticalItem);
          ds.addOnePredicateToEachRun(items, predicateIndex);
        } else {
          // this.numberOfPredicateItemsFilteredByIncrease++;
        }
      }
      for (ScalarPairPredicate p : metaScalarPredicates)
        p.clearPredicateItems();
    }
  }

  private void constructBranchPredicates(PredicateItemFactory factory,
      IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
    final int numberOfDistinctReturnPredicates = getNumberOfBranchPredicates(predicateProfiles);
    final int numberOfProfiles = predicateProfiles.length;

    final BranchPredicate[] verticalReturnPredicates = new BranchPredicate[numberOfProfiles];
    final PredicateItem[][] items = new PredicateItem[numberOfProfiles][PredicateItemFactory.NUM_PREDICATES_PER_BRANCH];
    final boolean[] filteringResult = new boolean[PredicateItemFactory.NUM_PREDICATES_PER_BRANCH];
    for (int i = 0; i < numberOfDistinctReturnPredicates; ++i) {
      // BranchPredicate[] verticalReturnPredicates = getBranchPredicates(
      // predicateProfiles, i);
      getBranchPredicates(predicateProfiles, i, verticalReturnPredicates);

      // PredicateItem[][] items = convert(factory, verticalReturnPredicates);
      convert(factory, verticalReturnPredicates, items);
      // final boolean[] filteringResult = filter(items, predicateProfiles);
      filter(items, predicateProfiles, filteringResult);

      this.numberOfPredicateItemsGenerated += PredicateItemFactory.NUM_PREDICATES_PER_BRANCH;

      assert (filteringResult.length == items[0].length);
      for (int j = 0; j < PredicateItemFactory.NUM_PREDICATES_PER_BRANCH; ++j) {
        if (!filteringResult[j]) {
          // PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
          // for (int k = 0; k < oneVerticalItem.length; ++k) {
          // oneVerticalItem[k] = items[k][j];
          // }
          // ds.addOnePredicateToEachRun(oneVerticalItem);
          ds.addOnePredicateToEachRun(items, j);
        } else {
          // this.numberOfPredicateItemsFilteredByIncrease++;
        }
      }
    }
  }

  private void constructReturnPredicates(PredicateItemFactory factory,
      IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
    final int numberOfDistinctReturnPredicates = getNumberOfReturnPredicates(predicateProfiles);
    final int numberOfProfiles = predicateProfiles.length;

    final PredicateItem[][] items = new PredicateItem[numberOfProfiles][PredicateItemFactory.NUM_PREDICATES_PER_RETURN];
    final ReturnPredicate[] verticalReturnPredicates = new ReturnPredicate[numberOfProfiles];
    final boolean[] filteringResult = new boolean[PredicateItemFactory.NUM_PREDICATES_PER_RETURN];

    for (int i = 0; i < numberOfDistinctReturnPredicates; ++i) {
      // ReturnPredicate[] verticalReturnPredicates = getReturnPredicates(
      // predicateProfiles, i);
      getReturnPredicates(predicateProfiles, i, verticalReturnPredicates);

      // PredicateItem[][] items = convert(factory, verticalReturnPredicates);
      convert(factory, verticalReturnPredicates, items);
      // final boolean[] filteringResult = filter(items, predicateProfiles);
      filter(items, predicateProfiles, filteringResult);

      this.numberOfPredicateItemsGenerated += filteringResult.length;

      assert (filteringResult.length == items[0].length);
      for (int predicateIndex = 0; predicateIndex < PredicateItemFactory.NUM_PREDICATES_PER_RETURN; ++predicateIndex) {
        if (!filteringResult[predicateIndex]) {
          // PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
          // for (int k = 0; k < oneVerticalItem.length; ++k) {
          // oneVerticalItem[k] = items[k][j];
          // }
          // ds.addOnePredicateToEachRun(oneVerticalItem);
          ds.addOnePredicateToEachRun(items, predicateIndex);
        }
      }
    }
  }

  private void filter(PredicateItem[][] items, IProfile[] profiles,
      boolean[] resultCollector) {
    if (!this.filtering) {
      return;
    }
    assert (items.length == profiles.length);
    final int numberOfPredicates = items[0].length;
    assert resultCollector.length == numberOfPredicates;
    // boolean[] result = new boolean[numberOfPredicates];
    FilteringStat[] stats = new FilteringStat[numberOfPredicates];
    ArrayList<FilteringStat> falseStats = new ArrayList<FilteringStat>();
    for (int i = 0; i < numberOfPredicates; ++i) {
      FilteringStat stat = filter(items, profiles, i);
      if (stat.filteringResult) {
        this.numberOfPredicateItemsFilteredByIncrease++;
      } else {
        falseStats.add(stat);
      }
      stats[i] = stat;
    }
    if (falseStats.size() != 0 && resultCollector.length == 6) {
      FilteringStructure struct = new FilteringStructure();
      for (FilteringStat stat : stats) {
        switch (stat.predicateType) {
        case GREATER_EQUAL:
          struct.ge = stat;
          break;
        case GREATER:
          struct.g = stat;
          break;
        case EQUAL:
          struct.e = stat;
          break;
        case NOT_EQUAL:
          struct.ne = stat;
          break;
        case LESS:
          struct.l = stat;
          break;
        case LESS_EQUAL:
          struct.le = stat;
          break;
        default:
          throw new RuntimeException("Cannot reach here." + stat.predicateType);
        }
      }
      if (!struct.ge.filteringResult) {
        if (struct.ge.inSameRuns(struct.g)) {
          struct.ge.filteringResult = true;
          ++this.numberOfPredicateItemsFilteredByLocal;
        } else if (struct.ge.inSameRuns(struct.e)) {
          struct.ge.filteringResult = true;
          ++this.numberOfPredicateItemsFilteredByLocal;
        }
      }
      if (!struct.ne.filteringResult) {
        if (struct.ne.inSameRuns(struct.g)) {
          struct.ne.filteringResult = true;
          ++this.numberOfPredicateItemsFilteredByLocal;
        } else if (struct.ne.inSameRuns(struct.l)) {
          struct.ne.filteringResult = true;
          ++this.numberOfPredicateItemsFilteredByLocal;
        }
      }
      if (!struct.le.filteringResult) {
        if (struct.le.inSameRuns(struct.l)) {
          struct.le.filteringResult = true;
          ++this.numberOfPredicateItemsFilteredByLocal;
        } else if (struct.le.inSameRuns(struct.e)) {
          struct.le.filteringResult = true;
          ++this.numberOfPredicateItemsFilteredByLocal;
        }
      }
    }
    for (int i = 0; i < numberOfPredicates; ++i) {
      resultCollector[i] = stats[i].filteringResult;
    }
    // return result;
  }

  private static class FilteringStructure {
    FilteringStat g; // greater than
    FilteringStat ge; // greater equal
    FilteringStat e; // equal
    FilteringStat ne; // not equal
    FilteringStat l; // less than
    FilteringStat le; // less equal
  }

  private static class FilteringStat {
    PredicateType predicateType;
    boolean filteringResult;
    int positive;
    int negative;
    int numberOfFailuresWherePisTrue = 0;
    int numberOfSuccessWherePisTrue = 0;
    int numberOfFailuresWherePisObserved = 0;
    int numberOfSuccessWherePisObserved = 0;

    public boolean inSameRuns(FilteringStat s) {
      return s.numberOfSuccessWherePisObserved == this.numberOfSuccessWherePisObserved
          && s.numberOfFailuresWherePisObserved == this.numberOfFailuresWherePisObserved
          && s.numberOfSuccessWherePisTrue == this.numberOfSuccessWherePisTrue
          && s.numberOfFailuresWherePisTrue == this.numberOfFailuresWherePisTrue;
    }
  }

  private FilteringStat filter(PredicateItem[][] items, IProfile[] profiles,
      int predicateIndex) {
    FilteringStat stat = new FilteringStat();
    stat.predicateType = items[0][predicateIndex].getKey().getPredicateType();
    final int length = items.length;
    for (int i = 0; i < length; ++i) {
      final PredicateItem item = items[i][predicateIndex];
      final boolean label = profiles[i].isCorrect();
      if (label) {
        ++stat.positive;
        if (item.isObserved()) {
          ++stat.numberOfSuccessWherePisObserved;
        }
        if (item.isTrue()) {
          ++stat.numberOfSuccessWherePisTrue;
        }
      } else {
        ++stat.negative;
        if (item.isObserved()) {
          ++stat.numberOfFailuresWherePisObserved;
        }
        if (item.isTrue()) {
          ++stat.numberOfFailuresWherePisTrue;
        }
      }
    }
    if (stat.numberOfSuccessWherePisObserved
        + stat.numberOfFailuresWherePisObserved == 0) {
      stat.filteringResult = true;
      return stat;
    }
    if (stat.numberOfSuccessWherePisTrue + stat.numberOfFailuresWherePisTrue == 0) {
      stat.filteringResult = true;
      return stat;
    }
    assert (stat.numberOfSuccessWherePisTrue
        + stat.numberOfFailuresWherePisTrue > 0);
    assert (stat.numberOfSuccessWherePisObserved
        + stat.numberOfFailuresWherePisObserved > 0);
    final double failure = ((double) stat.numberOfFailuresWherePisTrue)
        / (stat.numberOfSuccessWherePisTrue + stat.numberOfFailuresWherePisTrue);
    final double context = ((double) stat.numberOfFailuresWherePisObserved)
        / (stat.numberOfSuccessWherePisObserved + stat.numberOfFailuresWherePisObserved);
    boolean increase = (failure - context) <= 0;
    boolean scnFiltering = ((double) stat.numberOfFailuresWherePisTrue)
        / stat.negative <= ((double) stat.numberOfSuccessWherePisTrue)
        / stat.positive;
    stat.filteringResult = increase || scnFiltering;
//    stat.filteringResult = scnFiltering;
    return stat;
  }

}
