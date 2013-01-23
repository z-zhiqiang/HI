package sun.processor.predicate.constructor;

import java.util.ArrayList;

import sun.processor.core.IDataSetConstructor;
import sun.processor.core.IProfile;
import sun.processor.graph.IDataSet;
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

	private static BranchPredicate[] getBranchPredicates(
			IPredicateProfile[] profiles, int index) {
		BranchPredicate[] branchPredicates = new BranchPredicate[profiles.length];
		for (int i = 0; i < profiles.length; ++i) {
			branchPredicates[i] = profiles[i].getBranchPredicates().get(index);
		}
		return branchPredicates;
	}

	private static ScalarPairPredicate[] getScalarPredicates(
			IPredicateProfile[] profiles, int index) {
		ScalarPairPredicate[] scalarPredicates = new ScalarPairPredicate[profiles.length];
		for (int i = 0; i < profiles.length; ++i) {
			scalarPredicates[i] = profiles[i].getScalarPredicates().get(index);
		}
		return scalarPredicates;
	}

	private static ReturnPredicate[] getReturnPredicates(
			IPredicateProfile[] profiles, int index) {
		ReturnPredicate[] returnPredicates = new ReturnPredicate[profiles.length];
		for (int i = 0; i < profiles.length; ++i) {
			returnPredicates[i] = profiles[i].getReturnPredicates().get(index);
		}
		return returnPredicates;
	}

	private static IPredicateProfile[] cast(IProfile[] profiles) {
		IPredicateProfile[] casted = new IPredicateProfile[profiles.length];
		for (int i = 0; i < profiles.length; ++i) {
			casted[i] = (IPredicateProfile) profiles[i];
		}
		return casted;
	}
	
	private static PredicateItem[][] convert(PredicateItemFactory factory,
			FloatKindPredicate[] verticalPredicates) {
		PredicateItem[][] items = new PredicateItem[verticalPredicates.length][];
		for (int i = 0; i < verticalPredicates.length; ++i) {
			items[i] = factory.createPredicateItems(verticalPredicates[i]);
		}
		return items;
	}

	private static PredicateItem[][] convert(PredicateItemFactory factory,
			BranchPredicate[] verticalBranchPredicates) {
		PredicateItem[][] items = new PredicateItem[verticalBranchPredicates.length][];
		for (int i = 0; i < verticalBranchPredicates.length; ++i) {
			items[i] = factory.createPredicateItems(verticalBranchPredicates[i]);
		}
		return items;
	}

	private static PredicateItem[][] convert(PredicateItemFactory factory,
			ScalarPairPredicate[] verticalScalarPairPredicates) {
		PredicateItem[][] items = new PredicateItem[verticalScalarPairPredicates.length][];
		for (int i = 0; i < verticalScalarPairPredicates.length; ++i) {
			items[i] = factory.createPredicateItems(verticalScalarPairPredicates[i]);
		}
		return items;
	}

	private static PredicateItem[][] convert(PredicateItemFactory factory,
			ReturnPredicate[] verticalReturnPredicates) {
		PredicateItem[][] items = new PredicateItem[verticalReturnPredicates.length][];
		for (int i = 0; i < verticalReturnPredicates.length; ++i) {
			items[i] = factory.createPredicateItems(verticalReturnPredicates[i]);
		}
		return items;
	}

	@Override
	public IDataSet createDataSet(IProfile[] profiles) {
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

		System.out.println("construcinting branch predicates...");
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

		assignPredicateID(ds);

		return ds;
	}

	private void constructFloatKindsPredicates(PredicateItemFactory factory,
			IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
		final int numberOfDistinctFloatKindPredicates = getNumberOfFloatKindsPredicates(predicateProfiles);
		for (int i = 0; i < numberOfDistinctFloatKindPredicates; ++i) {
			FloatKindPredicate[] verticalPredicates = getFloatKindPredicates(
					predicateProfiles, i);
			PredicateItem[][] items = convert(factory, verticalPredicates);
			final boolean[] filteringResult = filter(items, predicateProfiles);

			this.numberOfPredicateItemsGenerated += filteringResult.length;

			assert (filteringResult.length == items[0].length);
			for (int j = 0; j < filteringResult.length; ++j) {
				if (!filteringResult[j]) {
					PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
					for (int k = 0; k < oneVerticalItem.length; ++k) {
						oneVerticalItem[k] = items[k][j];
					}
					ds.addOnePredicateToEachRun(oneVerticalItem);
				} else {
					// this.numberOfPredicateItemsFilteredByIncrease++;
				}
			}
		}
	}

	private FloatKindPredicate[] getFloatKindPredicates(
			IPredicateProfile[] profiles, int index) {
		FloatKindPredicate[] predicates = new FloatKindPredicate[profiles.length];
		for (int i = 0; i < profiles.length; ++i) {
			predicates[i] = profiles[i].getFloatKindPredicates().get(index);
		}
		return predicates;
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
		for (int i = 0; i < numberOfDistinctScalarPairPredicates; ++i) {
			ScalarPairPredicate[] verticalScalarPredicates = getScalarPredicates(
					predicateProfiles, i);

			PredicateItem[][] items = convert(factory, verticalScalarPredicates);
			final boolean[] filteringResult = filter(items, predicateProfiles);

			this.numberOfPredicateItemsGenerated += filteringResult.length;

			assert (filteringResult.length == items[0].length);
			for (int j = 0; j < filteringResult.length; ++j) {
				if (!filteringResult[j]) {
					PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
					for (int k = 0; k < oneVerticalItem.length; ++k) {
						oneVerticalItem[k] = items[k][j];
					}
					ds.addOnePredicateToEachRun(oneVerticalItem);
				} else {
					// this.numberOfPredicateItemsFilteredByIncrease++;
				}
			}
		}
	}

	private void constructBranchPredicates(PredicateItemFactory factory,
			IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
		final int numberOfDistinctReturnPredicates = getNumberOfBranchPredicates(predicateProfiles);
		for (int i = 0; i < numberOfDistinctReturnPredicates; ++i) {
			BranchPredicate[] verticalReturnPredicates = getBranchPredicates(
					predicateProfiles, i);

			PredicateItem[][] items = convert(factory, verticalReturnPredicates);
			final boolean[] filteringResult = filter(items, predicateProfiles);

			this.numberOfPredicateItemsGenerated += filteringResult.length;

			assert (filteringResult.length == items[0].length);
			for (int j = 0; j < filteringResult.length; ++j) {
				if (!filteringResult[j]) {
					PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
					for (int k = 0; k < oneVerticalItem.length; ++k) {
						oneVerticalItem[k] = items[k][j];
					}
					ds.addOnePredicateToEachRun(oneVerticalItem);
				} else {
					// this.numberOfPredicateItemsFilteredByIncrease++;
				}
			}
		}
	}

	private void constructReturnPredicates(PredicateItemFactory factory,
			IPredicateProfile[] predicateProfiles, PredicateDataSet ds) {
		final int numberOfDistinctReturnPredicates = getNumberOfReturnPredicates(predicateProfiles);
		for (int i = 0; i < numberOfDistinctReturnPredicates; ++i) {
			ReturnPredicate[] verticalReturnPredicates = getReturnPredicates(
					predicateProfiles, i);

			PredicateItem[][] items = convert(factory, verticalReturnPredicates);
			final boolean[] filteringResult = filter(items, predicateProfiles);

			this.numberOfPredicateItemsGenerated += filteringResult.length;

			assert (filteringResult.length == items[0].length);
			for (int j = 0; j < filteringResult.length; ++j) {
				if (!filteringResult[j]) {
					PredicateItem[] oneVerticalItem = new PredicateItem[items.length];
					for (int k = 0; k < oneVerticalItem.length; ++k) {
						oneVerticalItem[k] = items[k][j];
					}
					ds.addOnePredicateToEachRun(oneVerticalItem);
				} else {

				}
			}
		}
	}

	private boolean[] filter(PredicateItem[][] items, IProfile[] profiles) {
		assert (items.length == profiles.length);
		boolean[] result = new boolean[items[0].length];
		FilteringStat[] stats = new FilteringStat[result.length];
		ArrayList<FilteringStat> falseStats = new ArrayList<PredicateDataSetConstructor.FilteringStat>();
		for (int i = 0; i < items[0].length; ++i) {
			FilteringStat stat = filter(items, profiles, i);
			if (stat.filteringResult) {
				this.numberOfPredicateItemsFilteredByIncrease++;
			} else {
				falseStats.add(stat);
			}
			stats[i] = stat;
		}
		if (falseStats.size() != 0 && result.length == 6) {
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
		for (int i = 0; i < result.length; ++i) {
			result[i] = stats[i].filteringResult;
		}
		return result;
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
		for (int i = 0; i < items.length; ++i) {
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
//		stat.filteringResult = scnFiltering;
		return stat;
	}

}
