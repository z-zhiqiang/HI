package zuo.processor.cbi.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import zuo.processor.cbi.profile.predicatesite.AbstractPredicateSite;
import zuo.processor.cbi.profile.predicatesite.BranchPredicateSite;
import zuo.processor.cbi.profile.predicatesite.FloatKindPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ReturnPredicateSite;
import zuo.processor.cbi.profile.predicatesite.ScalarPairPredicateSite;
import zuo.processor.cbi.site.InstrumentationSites;


public class PredicateProfile {
	
	private final File path;
	
	private final boolean isCorrect;

	private final List<ScalarPairPredicateSite> scalarPairs;

	private final List<ReturnPredicateSite> returns;

	private final List<FloatKindPredicateSite> floats;

	private final List<BranchPredicateSite> branchs;

	private final InstrumentationSites sites;
	
	public void dispose() {
		this.scalarPairs.clear();
		this.returns.clear();
		this.floats.clear();
		this.branchs.clear();
	}

	public PredicateProfile(File profilePath, InstrumentationSites sites, boolean isCorrect) {
		this.path = profilePath;
		this.isCorrect = isCorrect;
		this.sites = sites;

		List<ScalarPairPredicateSite> scalarPredicateSites = new ArrayList<ScalarPairPredicateSite>();
		List<ReturnPredicateSite> returnPredicateSites = new ArrayList<ReturnPredicateSite>();
		List<BranchPredicateSite> branchPredicateSites = new ArrayList<BranchPredicateSite>();
		List<FloatKindPredicateSite> floatPredicateSites = new ArrayList<FloatKindPredicateSite>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(profilePath));
			PredicateSiteIDAllocator allo = new PredicateSiteIDAllocator();
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.contains("<report id=\"samples\">")) {
					// read the report.
					this.readReport(reader, scalarPredicateSites, returnPredicateSites,
							branchPredicateSites, floatPredicateSites, allo);
//					break;
				}
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.scalarPairs = Collections.unmodifiableList(scalarPredicateSites);
		this.returns = Collections.unmodifiableList(returnPredicateSites);
		this.branchs = Collections.unmodifiableList(branchPredicateSites);
		this.floats = Collections.unmodifiableList(floatPredicateSites);
	}

	private void readReport(BufferedReader reader,
			List<ScalarPairPredicateSite> scalarPredicateSites,
			List<ReturnPredicateSite> returnPredicateSites,
			List<BranchPredicateSite> branchPredicateSites,
			List<FloatKindPredicateSite> floatPredicateSites, PredicateSiteIDAllocator allo) throws IOException {
		
		
		for (String line = reader.readLine(); line != null && !line.equals("</report>"); line = reader
				.readLine()) {
			if (line.startsWith("<samples")) {
				String unit = InstrumentationSites.getUnitID(line);
				if (line.contains("scheme=\"returns\"")) {
					readReturns(reader, returnPredicateSites, unit, allo);
				} else if (line.contains("scheme=\"branches\"")) {
					readBranches(reader, branchPredicateSites, unit, allo);
				} else if (line.contains("scheme=\"scalar-pairs\"")) {
					readScalarPairs(reader, scalarPredicateSites, unit, allo);
				} else if (line.contains("scheme=\"float-kinds\"")) {
					this.readFloats(reader, floatPredicateSites, unit, allo);
				} else {
					throw new RuntimeException();
				}
			}
		}
	}

	private void readScalarPairs(BufferedReader reader,
			List<ScalarPairPredicateSite> predicateSites, String unit, PredicateSiteIDAllocator allo) {
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
				
				predicateSites.add(new ScalarPairPredicateSite(allo.allocateID(),
						this.sites.getScalarSite(unit, ++sequence), lessThanCounter, equalCounter,
						greaterThanCounter));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void readFloats(BufferedReader reader,
			List<FloatKindPredicateSite> predicateSites, String unit, PredicateSiteIDAllocator allo) {
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

//				int negativeInfinite = Integer.parseInt(counters[0]);
//				int negativeNormalized = Integer.parseInt(counters[1]);
//				int negativeDenormalized = Integer.parseInt(counters[2]);
//				int negativeZero = Integer.parseInt(counters[3]);
//				int nan = Integer.parseInt(counters[4]);
//				int positiveZero = Integer.parseInt(counters[5]);
//				int positiveDenormalized = Integer.parseInt(counters[6]);
//				int positiveNormalized = Integer.parseInt(counters[7]);
//				int positiveInfinite = Integer.parseInt(counters[8]);
//
//				predicateSites.add(new FloatKindPredicateSite(allo.allocateID(), sites.getFloatKindSite(unit, ++sequence),
//						negativeInfinite, negativeNormalized, negativeDenormalized,
//						negativeZero, nan, positiveZero, positiveDenormalized,
//						positiveNormalized, positiveInfinite));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void readBranches(BufferedReader reader,
			List<BranchPredicateSite> predicateSites, String unit, PredicateSiteIDAllocator allo) {
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
				
				predicateSites.add(new BranchPredicateSite(allo.allocateID(), sites.getBranchSite(unit, ++sequence),
						trueCounter, falseCounter));
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}

	private void readReturns(BufferedReader reader,
			List<ReturnPredicateSite> predicateSites, String unit, PredicateSiteIDAllocator allo) {
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
					throw new RuntimeException();
				}
				final int negativeCounter = Integer.parseInt(counters[0]);
				final int zeroCounter = Integer.parseInt(counters[1]);
				final int positiveCounter = Integer.parseInt(counters[2]);
				
				predicateSites.add(new ReturnPredicateSite(allo.allocateID(), this.sites.getReturnSite(unit, ++sequence),
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

	public List<ScalarPairPredicateSite> getScalarPredicateSites() {
		return this.scalarPairs;
	}

	public List<BranchPredicateSite> getBranchPredicateSites() {
		return this.branchs;
	}

	public List<ReturnPredicateSite> getReturnPredicateSites() {
		return this.returns;
	}

	public List<FloatKindPredicateSite> getFloatKindPredicateSites() {
		return floats;
	}
	
	public File getPath() {
		return path;
	}
	
	public boolean isCorrect() {
		return isCorrect;
	}
	
	public static class PredicateSiteIDAllocator{
		private int id;
		public PredicateSiteIDAllocator(){
			this.id = 0;
		}
		public int allocateID(){
			return id++;
		}
	}

	
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.path.getName()).append("\n").append(this.isCorrect).append("\n-----------------------------------------------------------\n");
		
		builder.append(this.branchs.size()).append(" branches").append("\n");
		for (AbstractPredicateSite predicateSite: this.branchs) {
			builder.append(predicateSite.toString()).append("\n");
		}
		builder.append("\n");
		
		builder.append(this.floats.size()).append(" floats").append("\n");
		for (AbstractPredicateSite predicateSite: this.floats) {
			builder.append(predicateSite.toString()).append("\n");
		}
		builder.append("\n");
		
		builder.append(this.returns.size()).append(" returns").append("\n");
		for (AbstractPredicateSite predicateSite: this.returns) {
			builder.append(predicateSite.toString()).append("\n");
		}
		builder.append("\n");
		
		builder.append(this.scalarPairs.size()).append(" scalarPairs").append("\n");
		for (AbstractPredicateSite predicateSite: this.scalarPairs) {
			builder.append(predicateSite.toString()).append("\n");
		}
		builder.append("\n");
		
		return builder.toString();
	}

}
