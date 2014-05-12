package zuo.processor.cbi.site;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import zuo.processor.functionentry.site.FunctionEntrySite;

public class InstrumentationSites {
	
	public static enum SiteCategory {
		 BRANCH, FLOAT_KIND, RETURN, SCALAR_PAIR
	}

	public static abstract class AbstractSite {
		final private String fileName;
		final private int lineNumber;
		final private String functionName;
		final private int cfgNumber;

		public AbstractSite(String fileName, int lineNumber, String functionName,
				int cfgNumber) {
			super();
			this.fileName = fileName.intern();
			this.lineNumber = lineNumber;
			this.functionName = functionName.intern();
			this.cfgNumber = cfgNumber;
		}

		public String getFileName() {
			return fileName;
		}

		public int getLineNumber() {
			return lineNumber;
		}

		public String getFunctionName() {
			return FunctionEntrySite.getUniqueFunctionName(this.functionName, this.fileName);
		}

		public int getCfgNumber() {
			return cfgNumber;
		}

		public String getFileString() {
			return "file = " + this.fileName;
		}

		public String toStringWithoutFile() {
			StringBuilder builder = new StringBuilder();

			this.toString(builder);
			builder.append(", line=").append(lineNumber).append(", method=")
					.append(functionName).append(", cfg=").append(this.cfgNumber);

			return builder.toString();
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			this.toString(builder);
			builder.append(", line=").append(lineNumber).append(", method=")
					.append(functionName).append(", file=").append(fileName)
					.append(", cfg=").append(cfgNumber);

			return builder.toString();
		}

		protected abstract void toString(StringBuilder builder);
		public abstract SiteCategory getCategory();
	}

	public static class BranchSite extends AbstractSite {
		final private String predicate;

		public BranchSite(String fileName, int lineNumber, String functionName,
				int cfgNumber, String predicate) {
			super(fileName, lineNumber, functionName, cfgNumber);
			this.predicate = predicate.intern();
		}

		public String getPredicate() {
			return predicate;
		}

		@Override
		protected void toString(StringBuilder builder) {
			builder.append("{predicate=").append(predicate).append('}');
		}

		@Override
		public SiteCategory getCategory() {
			// TODO Auto-generated method stub
			return SiteCategory.BRANCH;
		}
	}

	public static class ReturnSite extends AbstractSite {

		private final String callee;

		public ReturnSite(String fileName, int lineNumber, String functionName,
				int cfgNumber, String callee) {
			super(fileName, lineNumber, functionName, cfgNumber);
			this.callee = callee.intern();
		}

		public String getCallee() {
			return callee;
		}

		@Override
		protected void toString(StringBuilder builder) {
			builder.append("{callee=").append(callee).append('}');
		}

		@Override
		public SiteCategory getCategory() {
			// TODO Auto-generated method stub
			return SiteCategory.RETURN;
		}
	}

	public static class ScalarSite extends AbstractSite {

		// 5
		private final String left;
		// 6
		private final String leftType;
		// 7
		private final String containerType;
		// 8
		private final String right;
		// 9
		private final String rightType;

		public ScalarSite(String fileName, int lineNumber, String functionName,
				int cfgNumber, String left, String leftType, String containerType,
				String right, String rightType) {
			super(fileName, lineNumber, functionName, cfgNumber);
			this.left = left.intern();
			this.leftType = leftType.intern();
			this.containerType = containerType.intern();
			this.right = right.intern();
			this.rightType = rightType.intern();
		}

		@Override
		protected void toString(StringBuilder builder) {
			builder.append("{").append(this.left).append("[").append(this.leftType)
					.append(",").append(this.containerType).append("]").append(", ")
					.append(this.right).append("[").append(rightType).append("]")
					.append("}");
		}

		@Override
		public SiteCategory getCategory() {
			// TODO Auto-generated method stub
			return SiteCategory.SCALAR_PAIR;
		}

	}

	public static class FloatKindSite extends AbstractSite {

		// 5
		private final String left;

		// 6
		private final String leftType;

		// 7
		private final String containerType;

		public FloatKindSite(String fileName, int lineNumber, String functionName,
				int cfgNumber, String left, String leftType, String containerType) {
			super(fileName, lineNumber, functionName, cfgNumber);
			this.left = left;
			this.leftType = leftType;
			this.containerType = containerType;
		}

		@Override
		protected void toString(StringBuilder builder) {
			builder.append("{").append(this.left).append("[").append(this.leftType)
					.append(",").append(this.containerType).append("]").append("}");
		}

		@Override
		public SiteCategory getCategory() {
			// TODO Auto-generated method stub
			return SiteCategory.FLOAT_KIND;
		}

	}

	final File sitesFile;
	
	private final Map<String, List<ScalarSite>> scalarSites;

	private final Map<String, List<ReturnSite>> returnSites;

	private final Map<String, List<BranchSite>> branchSites;

	private final Map<String, List<FloatKindSite>> floatSites;

	
	public Map<String, List<ScalarSite>> getScalarSites() {
		return scalarSites;
	}

	public Map<String, List<ReturnSite>> getReturnSites() {
		return returnSites;
	}

	public Map<String, List<BranchSite>> getBranchSites() {
		return branchSites;
	}

	public Map<String, List<FloatKindSite>> getFloatSites() {
		return floatSites;
	}

	public FloatKindSite getFloatKindSite(String unit, int index) {
		return this.floatSites.get(unit).get(index);
	}

	public ScalarSite getScalarSite(String unit, int index) {
		return this.scalarSites.get(unit).get(index);
	}

	public ReturnSite getReturnSite(String unit, int index) {
		return this.returnSites.get(unit).get(index);
	}

	public BranchSite getBranchSite(String unit, int index) {
		return this.branchSites.get(unit).get(index);
	}

	public InstrumentationSites(File sitesFile) {
		Map<String, List<ScalarSite>> scalarSites = new HashMap<String, List<ScalarSite>>();
		Map<String, List<ReturnSite>> returnSites = new HashMap<String, List<ReturnSite>>();
		Map<String, List<BranchSite>> branchSites = new HashMap<String, List<BranchSite>>();
		Map<String, List<FloatKindSite>> floatSites = new HashMap<String, List<FloatKindSite>>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sitesFile));
			for (String line = reader.readLine(); line != null; line = reader
					.readLine()) {
				if (line.startsWith("<sites")) {
					String unit = getUnitID(line);
					if (line.contains("scheme=\"branches\"")) {
						this.readBranches(reader, branchSites, unit);
					} else if (line.contains("scheme=\"returns\"")) {
						this.readReturns(reader, returnSites, unit);
					} else if (line.contains("scheme=\"scalar-pairs\"")) {
						this.readScalarPairs(reader, scalarSites, unit);
					} else if (line.contains("scheme=\"float-kinds\"")) {
						this.readFloatKinds(reader, floatSites, unit);
					} else {
						throw new RuntimeException();
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.scalarSites = Collections.unmodifiableMap(scalarSites);
		this.returnSites = Collections.unmodifiableMap(returnSites);
		this.branchSites = Collections.unmodifiableMap(branchSites);
		this.floatSites = Collections.unmodifiableMap(floatSites);
		this.sitesFile = sitesFile;
	}
	
	public static String getUnitID(String line){
		String[] segs = line.split("\\s+");
		String unit = segs[1];
		if(!unit.contains("unit=")){
			throw new RuntimeException("wrong unit extraction");
		}
		String unitid = unit.substring(unit.indexOf("\"") + 1, unit.lastIndexOf("\"")); 
	    assert(unitid.length() == 32);
		return unitid;
	}

	public void print() {
		printEachKindSites(this.branchSites);
		printEachKindSites(this.floatSites);
		printEachKindSites(this.returnSites);
		printEachKindSites(this.scalarSites);
	}
	private void printEachKindSites(Map sites){
		for (Iterator<Map.Entry<String, List<AbstractSite>>> iterator = sites.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, List<AbstractSite>> entry = iterator.next();
			System.out.println(entry.getKey() + "\n-------------------------------------------------");
			List<AbstractSite> list = entry.getValue();
			for (AbstractSite site : list) {
				System.out.println(site.toString());
			}
		}
		System.out.println();
	}

	private void readFloatKinds(BufferedReader reader, Map<String, List<FloatKindSite>> floatSites, String unit)
			throws NumberFormatException, IOException {
		ArrayList<FloatKindSite> sites = new ArrayList<InstrumentationSites.FloatKindSite>();
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			if (line.contains("</sites>")) {
				break;
			} else {
				String[] s = line.split("\t");
				if (s.length != 7) {
					throw new RuntimeException();
				}
				String fileName = s[0];
				int lineNumber = Integer.parseInt(s[1]);
				String functionName = s[2];
				int cfgNumber = Integer.parseInt(s[3]);

				String left = s[4];
				String leftType = s[5];
				String containerType = s[6];
				sites.add(new FloatKindSite(fileName, lineNumber, functionName,
						cfgNumber, left, leftType, containerType));
			}
		}
		if(floatSites.containsKey(unit)){
			throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
		}
		floatSites.put(unit, sites);
	}

	private void readBranches(BufferedReader reader, Map<String, List<BranchSite>> branchSites, String unit)
			throws NumberFormatException, IOException {
		ArrayList<BranchSite> sites = new ArrayList<InstrumentationSites.BranchSite>();
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			if (line.contains("</sites>")) {
				break;
			} else {
				String[] s = line.split("\t");
				if (s.length != 5) {
					throw new RuntimeException();
				}
				String fileName = s[0];
				int lineNumber = Integer.parseInt(s[1]);
				String functionName = s[2];
				int cfgNumber = Integer.parseInt(s[3]);
				String predicate = s[4];
				sites.add(new BranchSite(fileName, lineNumber, functionName, cfgNumber,
						predicate));
			}
		}
		if(branchSites.containsKey(unit)){
			throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
		}
		branchSites.put(unit, sites);
	}

//	private void skip(BufferedReader reader) throws IOException {
//		for (String line = reader.readLine(); line != null
//				&& !line.contains("</sites>"); line = reader.readLine()) {
//		}
//	}

	private void readReturns(BufferedReader reader, Map<String, List<ReturnSite>> returnSites, String unit)
			throws NumberFormatException, IOException {
		ArrayList<ReturnSite> sites = new ArrayList<InstrumentationSites.ReturnSite>();
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			if (line.contains("</sites>")) {
				break;
			} else {
				String[] s = line.split("\t");
				if (s.length != 5) {
					throw new RuntimeException();
				}
				String fileName = s[0];
				int lineNumber = Integer.parseInt(s[1]);
				String functionName = s[2];
				int cfgNumber = Integer.parseInt(s[3]);
				String callee = s[4];
				sites.add(new ReturnSite(fileName, lineNumber, functionName, cfgNumber,
						callee));
			}
		}
		if(returnSites.containsKey(unit)){
			throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
		}
		returnSites.put(unit, sites);
	}

	private void readScalarPairs(BufferedReader reader, Map<String, List<ScalarSite>> scalarSites, String unit)
			throws IOException {
		ArrayList<ScalarSite> sites = new ArrayList<InstrumentationSites.ScalarSite>();
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			if (line.contains("</sites>")) {
				break;
			} else {
				String[] s = line.split("\t");
				if (s.length != 9) {
					throw new RuntimeException();
				}
				String fileName = s[0];
				int lineNumber = Integer.parseInt(s[1]);
				String functionName = s[2];
				int cfgNumber = Integer.parseInt(s[3]);
				String left = s[4];
				String leftType = s[5];
				String containerType = s[6];
				String right = s[7];
				String rightType = s[8];
				sites.add(new ScalarSite(fileName, lineNumber, functionName, cfgNumber,
						left, leftType, containerType, right, rightType));
			}
		}
		if(scalarSites.containsKey(unit)){
			throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
		}
		scalarSites.put(unit, sites);
	}
	
	

	public File getSitesFile() {
		return sitesFile;
	}

	public static void main(String[] args) {

		File file = new File("/home/sunzzq/Research/tcas/versions/v1/v1_f.sites");
		new InstrumentationSites(file).print();
	}

}
