package sun.processor.profile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;

public class InstrumentationSites {

  public static abstract class AbstractSite {

    final private String fileName;

    final private int lineNumber;

    final private String functionName;

    final private int cfgNumber;

    final private int hashCode;

    @Override
    public int hashCode() {
      return this.hashCode;
    }

    public int computeHashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + cfgNumber;
      result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
      result = prime * result
          + ((functionName == null) ? 0 : functionName.hashCode());
      result = prime * result + lineNumber;
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      AbstractSite other = (AbstractSite) obj;
      if (cfgNumber != other.cfgNumber)
        return false;
      if (fileName == null) {
        if (other.fileName != null)
          return false;
      } else if (!fileName.equals(other.fileName))
        return false;
      if (functionName == null) {
        if (other.functionName != null)
          return false;
      } else if (!functionName.equals(other.functionName))
        return false;
      if (lineNumber != other.lineNumber)
        return false;
      return true;
    }

    public AbstractSite(String fileName, int lineNumber, String functionName,
        int cfgNumber) {
      super();
      this.fileName = fileName.intern();
      this.lineNumber = lineNumber;
      this.functionName = functionName.intern();
      this.cfgNumber = cfgNumber;
      this.hashCode = this.computeHashCode();
    }

    public String getFileName() {
      return fileName;
    }

    public int getLineNumber() {
      return lineNumber;
    }

    public String getFunctionName() {
      return functionName + zuo.processor.functionentry.site.FunctionEntrySite.DELIMITER + fileName;
    }

    public int getCfgNumber() {
      return cfgNumber;
    }

    public String getFileString() {
      return "file = " + this.fileName + ", cfg = " + this.cfgNumber;
    }

    public String toStringWithoutFile() {
      StringBuilder builder = new StringBuilder();

      this.toString(builder);
      builder.append(", line=").append(lineNumber).append(", method=")
          .append(functionName);

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

  }

//  private final ImmutableList<ScalarSite> scalarSites;
//
//  private final ImmutableList<ReturnSite> returnSites;
//
//  private final ImmutableList<BranchSite> branchSites;
//
//  private final ImmutableList<FloatKindSite> floatSites;

  
  private final Map<String, ImmutableList<ScalarSite>> scalarSites;
  
  private final Map<String, ImmutableList<ReturnSite>> returnSites;
  
  private final Map<String, ImmutableList<BranchSite>> branchSites;
  
  private final Map<String, ImmutableList<FloatKindSite>> floatSites;
  
  
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

//  public static InstrumentationSites manuallyCreateInstrumentationSitesForBranches(
//      ImmutableList<BranchSite> branchSites) {
//    return new InstrumentationSites(branchSites);
//  }

//  private InstrumentationSites(ImmutableList<BranchSite> branchSites) {
//    this.scalarSites = ImmutableList.of();
//    this.returnSites = ImmutableList.of();
//    this.branchSites = branchSites;
//    this.floatSites = ImmutableList.of();
//  }

  public InstrumentationSites(File sitesPath) {
//    ImmutableList.Builder<ScalarSite> scalarSites = ImmutableList.builder();
//    ImmutableList.Builder<ReturnSite> returnSites = ImmutableList.builder();
//    ImmutableList.Builder<BranchSite> branchSites = ImmutableList.builder();
//    ImmutableList.Builder<FloatKindSite> floats = ImmutableList.builder();
    
    Map<String, ImmutableList<ScalarSite>> scalarSites = new HashMap<String, ImmutableList<ScalarSite>>();
    
    Map<String, ImmutableList<ReturnSite>> returnSites = new HashMap<String, ImmutableList<ReturnSite>>();
    
    Map<String, ImmutableList<BranchSite>> branchSites = new HashMap<String, ImmutableList<BranchSite>>();
    
    Map<String, ImmutableList<FloatKindSite>> floatSites = new HashMap<String, ImmutableList<FloatKindSite>>();
    
    try {
      BufferedReader reader = new BufferedReader(new FileReader(sitesPath));
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.startsWith("<sites")) {
        	String unit = zuo.processor.cbi.site.InstrumentationSites.getUnitID(line);
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

//    this.scalarSites = scalarSites.build();
//    this.returnSites = returnSites.build();
//    this.branchSites = branchSites.build();
//    this.floatSites = floats.build();
    
    this.scalarSites = Collections.unmodifiableMap(scalarSites);
	this.returnSites = Collections.unmodifiableMap(returnSites);
	this.branchSites = Collections.unmodifiableMap(branchSites);
	this.floatSites = Collections.unmodifiableMap(floatSites);
  }

  public void print() {
	try {
		PrintWriter out =  new PrintWriter(new BufferedWriter(new FileWriter(new File("test.out"))));
		for (String unit: this.branchSites.keySet()) {
			for(BranchSite branch: this.branchSites.get(unit)){
				out.println(branch);
			}
		}
		for (String unit: this.scalarSites.keySet()) {
			for(ScalarSite scalar: this.scalarSites.get(unit)){
				out.println(scalar);
			}
		}
		for(String unit: this.returnSites.keySet()){
			for (ReturnSite s: this.returnSites.get(unit)) {
				out.println(s);
			}
		}
		for(String unit: this.floatSites.keySet()){
			for (FloatKindSite s: this.floatSites.get(unit))
				out.println(s);
		}
		
		out.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }

  private void readFloatKinds(BufferedReader reader, Map<String, ImmutableList<FloatKindSite>> floatSites, String unit)
      throws NumberFormatException, IOException {
	  ImmutableList.Builder<FloatKindSite> builder = new ImmutableList.Builder<FloatKindSite>();
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
        builder.add(new FloatKindSite(fileName, lineNumber, functionName,
            cfgNumber, left, leftType, containerType));
      }
    }
    
    ImmutableList<FloatKindSite> floats = builder.build();
    if(floatSites.containsKey(unit)){
		throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
	}
	floatSites.put(unit, floats);
  }

  private void readBranches(BufferedReader reader, Map<String, ImmutableList<BranchSite>> branchSites, String unit) throws NumberFormatException,
      IOException {
	  ImmutableList.Builder<BranchSite> builder = new ImmutableList.Builder<BranchSite>();
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
        builder.add(new BranchSite(fileName, lineNumber, functionName,
            cfgNumber, predicate));
      }
    }
    
    ImmutableList<BranchSite> branches = builder.build();
    if(branchSites.containsKey(unit)){
		throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
	}
	branchSites.put(unit, branches);
  }

  private void skip(BufferedReader reader) throws IOException {
    for (String line = reader.readLine(); line != null
        && !line.contains("</sites>"); line = reader.readLine()) {
    }
  }

  private void readReturns(BufferedReader reader, Map<String, ImmutableList<ReturnSite>> returnSites, String unit) throws NumberFormatException,
      IOException {
      ImmutableList.Builder<ReturnSite> builder = new ImmutableList.Builder<ReturnSite>();
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
        builder.add(new ReturnSite(fileName, lineNumber, functionName,
            cfgNumber, callee));
      }
    }

    ImmutableList<ReturnSite> returns = builder.build();
    if(returnSites.containsKey(unit)){
		throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
	}
	returnSites.put(unit, returns);
  }

  private void readScalarPairs(BufferedReader reader, Map<String, ImmutableList<ScalarSite>> scalarSites, String unit) throws IOException {
	  ImmutableList.Builder<ScalarSite> builder = new ImmutableList.Builder<ScalarSite>();
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
        builder.add(new ScalarSite(fileName, lineNumber, functionName,
            cfgNumber, left, leftType, containerType, right, rightType));
      }
    }
    
    ImmutableList<ScalarSite> scalars = builder.build();
    if(scalarSites.containsKey(unit)){
		throw new RuntimeException("Wrong sites information: <unit, scheme> is not unique!!");
	}
	scalarSites.put(unit, scalars);
  }

//  public static void main(String[] args) {
//    File file = new File("E:\\Research\\IResearch\\Automated_Bug_Isolation\\Iterative\\Subjects\\bash\\versions\\v1\\subv1\\v1_subv1_f.sites");
//    new InstrumentationSites(file).print();
//  }

}
