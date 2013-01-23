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

	private final Map<PredicateKey, Integer> predicateKey2IDMap;

	private final Map<PredicateKey, PredicateKey> predicatePool;

//	private int sequence;

	public PredicateItemFactory() {
		this.predicateKey2IDMap = new HashMap<PredicateItem.PredicateKey, Integer>();
//		sequence = -1;
		this.predicatePool = new HashMap<PredicateItem.PredicateKey, PredicateItem.PredicateKey>();
	}

	public int getNumberOfPredicates() {
		return this.predicateKey2IDMap.size();
	}

	public PredicateItem[] createPredicateItems(FloatKindPredicate predicate) {
		PredicateItem[] items = new PredicateItem[9];
		final int id = predicate.getId();
		final PredicateCategory category = PredicateCategory.FLOAT_KIND;
		final AbstractSite site = predicate.getSite();
		if (predicate.getTotalCount() == 0) {
			// no observed
			if (items.length != 9) {
				throw new RuntimeException();
			}
			items[0] = this.createPredicateItem(category, PredicateType.NEGATIVE_INF,
					id, site, PredicateValue.NOT_OBSERVED);
			items[1] = this.createPredicateItem(category,
					PredicateType.NEGATIVE_NORMALIZED, id, site,
					PredicateValue.NOT_OBSERVED);
			items[2] = this.createPredicateItem(category,
					PredicateType.NEGATIVE_DENORMALIZED, id, site,
					PredicateValue.NOT_OBSERVED);
			items[3] = this.createPredicateItem(category,
					PredicateType.NEGATIVE_ZERO, id, site, PredicateValue.NOT_OBSERVED);
			items[4] = this.createPredicateItem(category, PredicateType.NAN, id,
					site, PredicateValue.NOT_OBSERVED);
			items[5] = this.createPredicateItem(category,
					PredicateType.POSITIVE_ZERO, id, site, PredicateValue.NOT_OBSERVED);
			items[6] = this.createPredicateItem(category,
					PredicateType.POSITIVE_DENORMALIZED, id, site,
					PredicateValue.NOT_OBSERVED);
			items[7] = this.createPredicateItem(category,
					PredicateType.POSITIVE_NORMALIZED, id, site,
					PredicateValue.NOT_OBSERVED);
			items[8] = this.createPredicateItem(category, PredicateType.POSITIVE_INF,
					id, site, PredicateValue.NOT_OBSERVED);
		} else {
			items[0] = this.createPredicateItem(category, PredicateType.NEGATIVE_INF,
					id, site, getStatus(predicate.getNegativeInfinite()));
			items[1] = this.createPredicateItem(category,
					PredicateType.NEGATIVE_NORMALIZED, id, site,
					getStatus(predicate.getNegativeNormalized()));
			items[2] = this.createPredicateItem(category,
					PredicateType.NEGATIVE_DENORMALIZED, id, site,
					getStatus(predicate.getNegativeDenormalized()));
			items[3] = this.createPredicateItem(category,
					PredicateType.NEGATIVE_ZERO, id, site,
					getStatus(predicate.getNegativeZero()));
			items[4] = this.createPredicateItem(category, PredicateType.NAN, id,
					site, getStatus(predicate.getNan()));
			items[5] = this.createPredicateItem(category,
					PredicateType.POSITIVE_ZERO, id, site,
					getStatus(predicate.getPositiveZero()));
			items[6] = this.createPredicateItem(category,
					PredicateType.POSITIVE_DENORMALIZED, id, site,
					getStatus(predicate.getPositiveDenormalized()));
			items[7] = this.createPredicateItem(category,
					PredicateType.POSITIVE_NORMALIZED, id, site,
					getStatus(predicate.getPositiveNormalized()));
			items[8] = this.createPredicateItem(category, PredicateType.POSITIVE_INF,
					id, site, getStatus(predicate.getPositiveInfinite()));
		}
		return items;
	}

	public PredicateItem[] createPredicateItems(ScalarPairPredicate predicate) {
		PredicateItem[] items = new PredicateItem[6];
		final int id = predicate.getId();
		final PredicateCategory category = PredicateCategory.SCALAR_PAIR;
		final AbstractSite fName = predicate.getSite();
		if (predicate.getTotalCount() == 0) {
			// not observed
			if (items.length != 6) {
				throw new RuntimeException();
			}
			items[0] = this.createPredicateItem(category,
					PredicateType.GREATER_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
			items[1] = this.createPredicateItem(category, PredicateType.GREATER, id,
					fName, PredicateValue.NOT_OBSERVED);
			items[2] = this.createPredicateItem(category, PredicateType.EQUAL, id,
					fName, PredicateValue.NOT_OBSERVED);
			items[3] = this.createPredicateItem(category, PredicateType.NOT_EQUAL,
					id, fName, PredicateValue.NOT_OBSERVED);
			items[4] = this.createPredicateItem(category, PredicateType.LESS, id,
					fName, PredicateValue.NOT_OBSERVED);
			items[5] = this.createPredicateItem(category, PredicateType.LESS_EQUAL,
					id, fName, PredicateValue.NOT_OBSERVED);

		} else {
			// observed
			items[0] = this.createPredicateItem(category,
					PredicateType.GREATER_EQUAL, id, fName,
					getStatus(predicate.getGreaterEqualCount()));
			items[1] = this.createPredicateItem(category, PredicateType.GREATER, id,
					fName, getStatus(predicate.getGreaterCount()));
			items[2] = this.createPredicateItem(category, PredicateType.EQUAL, id,
					fName, getStatus(predicate.getEqualCount()));
			items[3] = this.createPredicateItem(category, PredicateType.NOT_EQUAL,
					id, fName, getStatus(predicate.getNotEqualCount()));
			items[4] = this.createPredicateItem(category, PredicateType.LESS, id,
					fName, getStatus(predicate.getLessCount()));
			items[5] = this.createPredicateItem(category, PredicateType.LESS_EQUAL,
					id, fName, getStatus(predicate.getLessEqualCount()));
		}
		return items;
	}

	public PredicateItem[] createPredicateItems(ReturnPredicate predicate) {
		PredicateItem[] items = new PredicateItem[6];
		final int id = predicate.getId();
		PredicateCategory categpry = PredicateCategory.RETURN;
		final AbstractSite fName = predicate.getSite();
		if (predicate.getTotalCount() == 0) {
			// not observed
			items[0] = this.createPredicateItem(categpry,
					PredicateType.GREATER_EQUAL, id, fName, PredicateValue.NOT_OBSERVED);
			items[1] = this.createPredicateItem(categpry, PredicateType.GREATER, id,
					fName, PredicateValue.NOT_OBSERVED);
			items[2] = this.createPredicateItem(categpry, PredicateType.EQUAL, id,
					fName, PredicateValue.NOT_OBSERVED);
			items[3] = this.createPredicateItem(categpry, PredicateType.NOT_EQUAL,
					id, fName, PredicateValue.NOT_OBSERVED);
			items[4] = this.createPredicateItem(categpry, PredicateType.LESS, id,
					fName, PredicateValue.NOT_OBSERVED);
			items[5] = this.createPredicateItem(categpry, PredicateType.LESS_EQUAL,
					id, fName, PredicateValue.NOT_OBSERVED);
		} else {
			// observed
			items[0] = this.createPredicateItem(categpry,
					PredicateType.GREATER_EQUAL, id, fName,
					getStatus(predicate.getGreaterEqualCount()));
			items[1] = this.createPredicateItem(categpry, PredicateType.GREATER, id,
					fName, getStatus(predicate.getGreaterCount()));
			items[2] = this.createPredicateItem(categpry, PredicateType.EQUAL, id,
					fName, getStatus(predicate.getEqualCount()));
			items[3] = this.createPredicateItem(categpry, PredicateType.NOT_EQUAL,
					id, fName, getStatus(predicate.getNotEqualCount()));
			items[4] = this.createPredicateItem(categpry, PredicateType.LESS, id,
					fName, getStatus(predicate.getLessCount()));
			items[5] = this.createPredicateItem(categpry, PredicateType.LESS_EQUAL,
					id, fName, getStatus(predicate.getLessEqualCount()));
		}
		return items;
	}

	public static PredicateValue getStatus(int counter) {
		if (counter == 0) {
			return PredicateValue.FALSE;
		} else {
			return PredicateValue.TRUE;
		}
	}

	public PredicateItem[] createPredicateItems(BranchPredicate branchPredicate) {
		PredicateItem[] items = new PredicateItem[2];
		final int id = branchPredicate.getId();
		PredicateCategory category = PredicateCategory.BRANCH;
		final AbstractSite fName = branchPredicate.getSite();
		if (branchPredicate.getTotalCount() == 0) {
			// not observed
			final PredicateValue status = PredicateValue.NOT_OBSERVED;
			items[0] = this.createPredicateItem(category, PredicateType.TRUE_BRANCH,
					id, fName, status);
			items[1] = this.createPredicateItem(category, PredicateType.FALSE_BRANCH,
					id, fName, status);
		} else {
			// observed
			items[0] = this.createPredicateItem(category, PredicateType.TRUE_BRANCH,
					id, fName, getStatus(branchPredicate.getTrueCount()));
			items[1] = this.createPredicateItem(category, PredicateType.FALSE_BRANCH,
					id, fName, getStatus(branchPredicate.getFalseCount()));
		}
		return items;
	}

	private PredicateItem createPredicateItem(PredicateCategory category,
			PredicateType predicateType, int originalId, AbstractSite site,
			PredicateValue status) {
		final PredicateKey key = getKey(category, predicateType, originalId, site);
		// final int itemId = this.getItemID(key);
		return new PredicateItem(key, status);
	}

	private PredicateKey getKey(PredicateCategory category,
			PredicateType predicateType, int originalId, AbstractSite site) {
		final PredicateKey key = new PredicateKey(category, predicateType,
				originalId, site);
		if (!this.predicatePool.containsKey(key)) {
			this.predicatePool.put(key, key);
		}
		return this.predicatePool.get(key);
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
