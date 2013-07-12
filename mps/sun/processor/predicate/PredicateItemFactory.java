package sun.processor.predicate;

import java.util.HashMap;
import java.util.Map;

import sun.processor.predicate.PredicateItem.PredicateCategory;
import sun.processor.predicate.PredicateItem.PredicateKey;
import sun.processor.predicate.PredicateItem.PredicateType;
import sun.processor.predicate.PredicateItem.PredicateValue;
import sun.processor.profile.InstrumentationSites.AbstractSite;
import sun.processor.profile.predicate.BranchPredicate;
import sun.processor.profile.predicate.FloatKindPredicate;
import sun.processor.profile.predicate.ReturnPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate;

public class PredicateItemFactory {

  public static final int NUM_PREDICATES_PER_BRANCH = 2;

  public static final int NUM_PREDICATES_PER_RETURN = 6;

  public static final int NUM_PREDICATES_PER_FLOAT = 9;

  public static final int NUM_PREDICATES_PER_SCALAR_PAIR = 6;

  private final Map<PredicateKey, Integer> predicateKey2IDMap;

  private final Map<PredicateKey, PredicateKey> predicatePool;

  public PredicateItemFactory() {
    this.predicateKey2IDMap = new HashMap<PredicateItem.PredicateKey, Integer>();
    // sequence = -1;
    this.predicatePool = new HashMap<PredicateItem.PredicateKey, PredicateItem.PredicateKey>();
  }

  public int getNumberOfPredicates() {
    return this.predicateKey2IDMap.size();
  }

  public void createPredicateItems(FloatKindPredicate predicate,
      PredicateItem[] itemsCollector) {
    // PredicateItem[] items = new PredicateItem[NUM_PREDICATES_PER_FLOAT];
    assert itemsCollector.length == NUM_PREDICATES_PER_FLOAT;
    final int id = predicate.getId();
    final PredicateCategory category = PredicateCategory.FLOAT_KIND;
    final AbstractSite site = predicate.getSite();
    if (itemsCollector.length != NUM_PREDICATES_PER_FLOAT) {
      throw new RuntimeException();
    }
    if (predicate.getTotalCount() == 0) {
      // no observed
      itemsCollector[0] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_INF, id, site, PredicateValue.NOT_OBSERVED);
      itemsCollector[1] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_NORMALIZED, id, site,
          PredicateValue.NOT_OBSERVED);
      itemsCollector[2] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_DENORMALIZED, id, site,
          PredicateValue.NOT_OBSERVED);
      itemsCollector[3] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_ZERO, id, site, PredicateValue.NOT_OBSERVED);
      itemsCollector[4] = this.createPredicateItem(category, PredicateType.NAN,
          id, site, PredicateValue.NOT_OBSERVED);
      itemsCollector[5] = this.createPredicateItem(category,
          PredicateType.POSITIVE_ZERO, id, site, PredicateValue.NOT_OBSERVED);
      itemsCollector[6] = this.createPredicateItem(category,
          PredicateType.POSITIVE_DENORMALIZED, id, site,
          PredicateValue.NOT_OBSERVED);
      itemsCollector[7] = this.createPredicateItem(category,
          PredicateType.POSITIVE_NORMALIZED, id, site,
          PredicateValue.NOT_OBSERVED);
      itemsCollector[8] = this.createPredicateItem(category,
          PredicateType.POSITIVE_INF, id, site, PredicateValue.NOT_OBSERVED);
    } else {
      itemsCollector[0] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_INF, id, site,
          getStatus(predicate.getNegativeInfinite()));
      itemsCollector[1] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_NORMALIZED, id, site,
          getStatus(predicate.getNegativeNormalized()));
      itemsCollector[2] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_DENORMALIZED, id, site,
          getStatus(predicate.getNegativeDenormalized()));
      itemsCollector[3] = this.createPredicateItem(category,
          PredicateType.NEGATIVE_ZERO, id, site,
          getStatus(predicate.getNegativeZero()));
      itemsCollector[4] = this.createPredicateItem(category, PredicateType.NAN,
          id, site, getStatus(predicate.getNan()));
      itemsCollector[5] = this.createPredicateItem(category,
          PredicateType.POSITIVE_ZERO, id, site,
          getStatus(predicate.getPositiveZero()));
      itemsCollector[6] = this.createPredicateItem(category,
          PredicateType.POSITIVE_DENORMALIZED, id, site,
          getStatus(predicate.getPositiveDenormalized()));
      itemsCollector[7] = this.createPredicateItem(category,
          PredicateType.POSITIVE_NORMALIZED, id, site,
          getStatus(predicate.getPositiveNormalized()));
      itemsCollector[8] = this.createPredicateItem(category,
          PredicateType.POSITIVE_INF, id, site,
          getStatus(predicate.getPositiveInfinite()));
    }
  }

  public void createPredicateItems(ScalarPairPredicate predicate,
      final PredicateItem[] itemsCollector) {
    // PredicateItem[] items = new PredicateItem[6];
    final int id = predicate.getId();
    final PredicateCategory category = PredicateCategory.SCALAR_PAIR;
    final AbstractSite fName = predicate.getSite();
    if (itemsCollector.length != NUM_PREDICATES_PER_SCALAR_PAIR) {
      throw new RuntimeException();
    }
    if (predicate.getTotalCount() == 0) {
      // not observed
      itemsCollector[0] = this.createPredicateItem(category,
          PredicateType.GREATER_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[1] = this.createPredicateItem(category,
          PredicateType.GREATER, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[2] = this.createPredicateItem(category,
          PredicateType.EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[3] = this.createPredicateItem(category,
          PredicateType.NOT_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[4] = this.createPredicateItem(category,
          PredicateType.LESS, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[5] = this.createPredicateItem(category,
          PredicateType.LESS_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);

    } else {
      // observed
      itemsCollector[0] = this.createPredicateItem(category,
          PredicateType.GREATER_EQUAL, id, fName,
          getStatus(predicate.getGreaterEqualCount()));
      itemsCollector[1] = this.createPredicateItem(category,
          PredicateType.GREATER, id, fName,
          getStatus(predicate.getGreaterCount()));
      itemsCollector[2] = this.createPredicateItem(category,
          PredicateType.EQUAL, id, fName, getStatus(predicate.getEqualCount()));
      itemsCollector[3] = this.createPredicateItem(category,
          PredicateType.NOT_EQUAL, id, fName,
          getStatus(predicate.getNotEqualCount()));
      itemsCollector[4] = this.createPredicateItem(category,
          PredicateType.LESS, id, fName, getStatus(predicate.getLessCount()));
      itemsCollector[5] = this.createPredicateItem(category,
          PredicateType.LESS_EQUAL, id, fName,
          getStatus(predicate.getLessEqualCount()));
    }
    // return items;
  }

  public void createPredicateItems(ReturnPredicate predicate,
      final PredicateItem[] itemsCollector) {
    // PredicateItem[] items = new PredicateItem[NUM_PREDICATES_PER_RETURN];
    assert itemsCollector.length == NUM_PREDICATES_PER_RETURN;
    final int id = predicate.getId();
    PredicateCategory categpry = PredicateCategory.RETURN;
    final AbstractSite fName = predicate.getSite();
    if (predicate.getTotalCount() == 0) {
      // not observed
      itemsCollector[0] = this.createPredicateItem(categpry,
          PredicateType.GREATER_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[1] = this.createPredicateItem(categpry,
          PredicateType.GREATER, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[2] = this.createPredicateItem(categpry,
          PredicateType.EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[3] = this.createPredicateItem(categpry,
          PredicateType.NOT_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[4] = this.createPredicateItem(categpry,
          PredicateType.LESS, id, fName, PredicateValue.NOT_OBSERVED);
      itemsCollector[5] = this.createPredicateItem(categpry,
          PredicateType.LESS_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
    } else {
      // observed
      itemsCollector[0] = this.createPredicateItem(categpry,
          PredicateType.GREATER_EQUAL, id, fName,
          getStatus(predicate.getGreaterEqualCount()));
      itemsCollector[1] = this.createPredicateItem(categpry,
          PredicateType.GREATER, id, fName,
          getStatus(predicate.getGreaterCount()));
      itemsCollector[2] = this.createPredicateItem(categpry,
          PredicateType.EQUAL, id, fName, getStatus(predicate.getEqualCount()));
      itemsCollector[3] = this.createPredicateItem(categpry,
          PredicateType.NOT_EQUAL, id, fName,
          getStatus(predicate.getNotEqualCount()));
      itemsCollector[4] = this.createPredicateItem(categpry,
          PredicateType.LESS, id, fName, getStatus(predicate.getLessCount()));
      itemsCollector[5] = this.createPredicateItem(categpry,
          PredicateType.LESS_EQUAL, id, fName,
          getStatus(predicate.getLessEqualCount()));
    }
    // return items;
  }

  public static PredicateValue getStatus(int counter) {
    if (counter == 0) {
      return PredicateValue.FALSE;
    } else {
      return PredicateValue.TRUE;
    }
  }

  public void createPredicateItems(BranchPredicate branchPredicate,
      final PredicateItem[] itemsCollector) {
    // PredicateItem[] items = new PredicateItem[2];
    assert itemsCollector.length == NUM_PREDICATES_PER_BRANCH;
    final int id = branchPredicate.getId();
    PredicateCategory category = PredicateCategory.BRANCH;
    final AbstractSite fName = branchPredicate.getSite();
    if (branchPredicate.getTotalCount() == 0) {
      // not observed
      final PredicateValue status = PredicateValue.NOT_OBSERVED;
      itemsCollector[0] = this.createPredicateItem(category,
          PredicateType.TRUE_BRANCH, id, fName, status);
      itemsCollector[1] = this.createPredicateItem(category,
          PredicateType.FALSE_BRANCH, id, fName, status);
    } else {
      // observed
      itemsCollector[0] = this.createPredicateItem(category,
          PredicateType.TRUE_BRANCH, id, fName,
          getStatus(branchPredicate.getTrueCount()));
      itemsCollector[1] = this.createPredicateItem(category,
          PredicateType.FALSE_BRANCH, id, fName,
          getStatus(branchPredicate.getFalseCount()));
    }
    // return items;
  }

  private PredicateItem createPredicateItem(PredicateCategory category,
      PredicateType predicateType, int originalId, AbstractSite site,
      PredicateValue status) {
    final PredicateKey key = getKey(category, predicateType, originalId, site);
    // return new PredicateItem(key, status);
    return key.getInstance(status);
  }

  private PredicateKey getKey(PredicateCategory category,
      PredicateType predicateType, int originalId, AbstractSite site) {
    final PredicateKey key = new PredicateKey(category, predicateType,
        originalId, site);
    final PredicateKey cache = this.predicatePool.get(key);
    if (cache == null) {
      this.predicatePool.put(key, key);
      return key;
    } else {
      return cache;
    }
    // if (!this.predicatePool.containsKey(key)) {
    // this.predicatePool.put(key, key);
    // }
    // return this.predicatePool.get(key);
  }
  //
  // private int getItemID(PredicateKey key) {
  // // final PredicateKey key = new PredicateKey(predicateType, originalId);
  // if (predicateKey2IDMap.containsKey(key)) {
  // return predicateKey2IDMap.get(key).intValue();
  // } else {
  // Integer id = new Integer(++sequence);
  // this.predicateKey2IDMap.put(key, id);
  // return sequence;
  // }
  // }
}
