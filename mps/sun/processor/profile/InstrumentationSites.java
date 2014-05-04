package sun.processor.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import zuo.processor.functionentry.site.FunctionEntrySite;

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
      return functionName + FunctionEntrySite.DELIMITER + fileName;
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

  private final ImmutableList<ScalarSite> scalarSites;

  private final ImmutableList<ReturnSite> returnSites;

  private final ImmutableList<BranchSite> branchSites;

  private final ImmutableList<FloatKindSite> floatSites;

  public FloatKindSite getFloatKindSite(int index) {
    return this.floatSites.get(index);
  }

  public ScalarSite getScalarSite(int index) {
    return this.scalarSites.get(index);
  }

  public ReturnSite getReturnSite(int index) {
    return this.returnSites.get(index);
  }

  public BranchSite getBranchSite(int index) {
    return this.branchSites.get(index);
  }

  public static InstrumentationSites manuallyCreateInstrumentationSitesForBranches(
      ImmutableList<BranchSite> branchSites) {
    return new InstrumentationSites(branchSites);
  }

  private InstrumentationSites(ImmutableList<BranchSite> branchSites) {
    this.scalarSites = ImmutableList.of();
    this.returnSites = ImmutableList.of();
    this.branchSites = branchSites;
    this.floatSites = ImmutableList.of();
  }

  public InstrumentationSites(File sitesPath) {
    ImmutableList.Builder<ScalarSite> scalarSites = ImmutableList.builder();
    ImmutableList.Builder<ReturnSite> returnSites = ImmutableList.builder();
    ImmutableList.Builder<BranchSite> branchSites = ImmutableList.builder();
    ImmutableList.Builder<FloatKindSite> floats = ImmutableList.builder();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(sitesPath));
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.startsWith("<sites")) {
          if (line.contains("scheme=\"branches\"")) {
            this.readBranches(reader, branchSites);
          } else if (line.contains("scheme=\"returns\"")) {
            this.readReturns(reader, returnSites);
          } else if (line.contains("scheme=\"scalar-pairs\"")) {
            this.readScalarPairs(reader, scalarSites);
          } else if (line.contains("scheme=\"float-kinds\"")) {
            this.readFloatKinds(reader, floats);
          } else {
            throw new RuntimeException();
          }
        }
      }
      reader.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.scalarSites = scalarSites.build();
    this.returnSites = returnSites.build();
    this.branchSites = branchSites.build();
    this.floatSites = floats.build();
  }

  public void print() {
    for (BranchSite s : this.branchSites) {
      System.out.println(s);
    }
    for (ScalarSite s : this.scalarSites) {
      System.out.println(s);
    }
    for (ReturnSite s : this.returnSites) {
      System.out.println(s);
    }
    for (FloatKindSite s : this.floatSites)
      System.out.println(s);
  }

  private void readFloatKinds(BufferedReader reader,
      ImmutableList.Builder<FloatKindSite> builder)
      throws NumberFormatException, IOException {
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
  }

  private void readBranches(BufferedReader reader,
      ImmutableList.Builder<BranchSite> builder) throws NumberFormatException,
      IOException {
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
  }

  private void skip(BufferedReader reader) throws IOException {
    for (String line = reader.readLine(); line != null
        && !line.contains("</sites>"); line = reader.readLine()) {
    }
  }

  private void readReturns(BufferedReader reader,
      ImmutableList.Builder<ReturnSite> builder) throws NumberFormatException,
      IOException {
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
  }

  private void readScalarPairs(BufferedReader reader,
      ImmutableList.Builder<ScalarSite> builder) throws IOException {
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
  }

  public static void main(String[] args) {

    File file = new File(
        "/home/neo/experiments/predicate-debug/print_tokens/versions/v1/sites.txt");
    new InstrumentationSites(file).print();
  }

}
