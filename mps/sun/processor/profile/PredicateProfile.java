package sun.processor.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sun.processor.profile.predicate.BranchPredicate;
import sun.processor.profile.predicate.FloatKindPredicate;
import sun.processor.profile.predicate.ReturnPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate;

public class PredicateProfile extends AbstractProfile implements
		IPredicateProfile {

	private final List<ScalarPairPredicate> scalarPairs;

	private final List<ReturnPredicate> returns;

	private final List<FloatKindPredicate> floats;

	private final List<BranchPredicate> branchPredicates;

	private final InstrumentationSites sites;
	
	@Override
	public void dispose() {
		this.scalarPairs.clear();
		this.returns.clear();
		// this.functionEntries.clear();
		this.branchPredicates.clear();
	}

	public PredicateProfile(File profilePath, InstrumentationSites sites,
			boolean isCorrect) {
		super(profilePath, isCorrect);
		this.sites = sites;

		List<ScalarPairPredicate> scalarPredicates = new ArrayList<ScalarPairPredicate>();
		List<ReturnPredicate> returnPredicates = new ArrayList<ReturnPredicate>();
		List<BranchPredicate> branchPredicates = new ArrayList<BranchPredicate>();
		List<FloatKindPredicate> floatPredicates = new ArrayList<FloatKindPredicate>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(profilePath));
			PredicateIDAllocator allo = new PredicateIDAllocator();
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.contains("<report id=\"samples\">")) {
					// read the report.
					this.readReport(reader, scalarPredicates, returnPredicates,
							branchPredicates, floatPredicates, allo);
//					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.scalarPairs = Collections.unmodifiableList(scalarPredicates);
		this.returns = Collections.unmodifiableList(returnPredicates);
		this.branchPredicates = Collections.unmodifiableList(branchPredicates);
		this.floats = Collections.unmodifiableList(floatPredicates);
	}

	private void readReport(BufferedReader reader,
			List<ScalarPairPredicate> scalarPredicates,
			List<ReturnPredicate> returnPredicates,
			List<BranchPredicate> branchPredicates,
			List<FloatKindPredicate> floatPredicates, PredicateIDAllocator allo) throws IOException {
		
		
		for (String line = reader.readLine(); line != null && !line.equals("</report>"); line = reader
				.readLine()) {
			if (line.startsWith("<samples")) {
				String unit = InstrumentationSites.getUnitID(line);
				if (line.contains("scheme=\"returns\"")) {
					readReturns(reader, returnPredicates, unit, allo);
				} else if (line.contains("scheme=\"branches\"")) {
					readBranches(reader, branchPredicates, unit, allo);
				} else if (line.contains("scheme=\"scalar-pairs\"")) {
					readScalarPairs(reader, scalarPredicates, unit, allo);
				} else if (line.contains("scheme=\"float-kinds\"")) {
					this.readFloats(reader, floatPredicates, unit, allo);
				} else {
					throw new RuntimeException();
				}
			}
		}
	}

	private void readScalarPairs(BufferedReader reader,
			List<ScalarPairPredicate> predicates, String unit, PredicateIDAllocator allo) {
		int sequence = -1;
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.contains("</samples>"))
					return;
				line = line.trim();
				if (line.length() == 0)
					continue;
				String[] counters = line.split("\\s+");
				if (counters.length != 3) {
					throw new RuntimeException(line);
				}
				final int lessThanCounter = Integer.parseInt(counters[0]);
				final int equalCounter = Integer.parseInt(counters[1]);
				final int greaterThanCounter = Integer.parseInt(counters[2]);
				
				predicates.add(new ScalarPairPredicate(allo.allocateID(),
						this.sites.getScalarSite(unit, ++sequence), lessThanCounter, equalCounter,
						greaterThanCounter));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void readFloats(BufferedReader reader,
			List<FloatKindPredicate> predicates, String unit, PredicateIDAllocator allo) {
		int sequence = -1;
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.contains("</samples>"))
					return;
				line = line.trim();
				if (line.length() == 0)
					continue;
				String[] counters = line.split("\\s+");
				if (counters.length != 9) {
					throw new RuntimeException();
				}

				int negativeInfinite = Integer.parseInt(counters[0]);
				int negativeNormalized = Integer.parseInt(counters[1]);
				int negativeDenormalized = Integer.parseInt(counters[2]);
				int negativeZero = Integer.parseInt(counters[3]);
				int nan = Integer.parseInt(counters[4]);
				int positiveZero = Integer.parseInt(counters[5]);
				int positiveDenormalized = Integer.parseInt(counters[6]);
				int positiveNormalized = Integer.parseInt(counters[7]);
				int positiveInfinite = Integer.parseInt(counters[8]);

				predicates.add(new FloatKindPredicate(allo.allocateID(), sites.getFloatKindSite(unit, ++sequence),
						negativeInfinite, negativeNormalized, negativeDenormalized,
						negativeZero, nan, positiveZero, positiveDenormalized,
						positiveNormalized, positiveInfinite));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void readBranches(BufferedReader reader,
			List<BranchPredicate> predicates, String unit, PredicateIDAllocator allo) {
		int sequence = -1;
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.contains("</samples>"))
					return;
				line = line.trim();
				if (line.length() == 0)
					continue;
				String[] counters = line.split("\\s+");
				if (counters.length != 2) {
					throw new RuntimeException();
				}
				final int falseCounter = Integer.parseInt(counters[0]);
				final int trueCounter = Integer.parseInt(counters[1]);
				
				predicates.add(new BranchPredicate(allo.allocateID(), sites.getBranchSite(unit, ++sequence),
						trueCounter, falseCounter));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void readReturns(BufferedReader reader,
			List<ReturnPredicate> predicates, String unit, PredicateIDAllocator allo) {
		int sequence = -1;
		// ArrayList<ReturnPredicate> predicates = new ArrayList<ReturnPredicate>();
		try {
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.contains("</samples>"))
					return;
				line = line.trim();
				if (line.length() == 0)
					continue;
				String[] counters = line.split("\\s+");
				if (counters.length != 3) {
					throw new RuntimeException();
				}
				final int negativeCounter = Integer.parseInt(counters[0]);
				final int zeroCounter = Integer.parseInt(counters[1]);
				final int positiveCounter = Integer.parseInt(counters[2]);
				
				predicates.add(new ReturnPredicate(allo.allocateID(), this.sites.getReturnSite(unit, ++sequence),
						negativeCounter, zeroCounter, positiveCounter));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void skip(BufferedReader reader) throws IOException {
		for (String line = reader.readLine(); line != null
				&& !line.contains("</samples>"); line = reader.readLine()) {
		}
	}

	@Override
	public List<ScalarPairPredicate> getScalarPredicates() {
		return this.scalarPairs;
	}

	@Override
	public List<BranchPredicate> getBranchPredicates() {
		return this.branchPredicates;
	}

	@Override
	public List<ReturnPredicate> getReturnPredicates() {
		return this.returns;
	}

	@Override
	public List<FloatKindPredicate> getFloatKindPredicates() {
		return floats;
	}
	
	public static class PredicateIDAllocator{
		private int id;
		public PredicateIDAllocator(){
			this.id = 0;
		}
		public int allocateID(){
			return id++;
		}
	}

}
