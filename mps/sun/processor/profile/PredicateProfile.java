package sun.processor.profile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import sun.processor.profile.InstrumentationSites.BranchSite;
import sun.processor.profile.InstrumentationSites.FloatKindSite;
import sun.processor.profile.InstrumentationSites.ReturnSite;
import sun.processor.profile.InstrumentationSites.ScalarSite;
import sun.processor.profile.predicate.BranchPredicate;
import sun.processor.profile.predicate.FloatKindPredicate;
import sun.processor.profile.predicate.ReturnPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate;
import sun.processor.profile.predicate.ScalarPairPredicate.Factory;

import com.google.common.collect.ImmutableList;

public class PredicateProfile extends AbstractProfile implements
    IPredicateProfile {

  private ImmutableList<ScalarPairPredicate> scalarPairs;

  private ImmutableList<ReturnPredicate> returns;

  private ImmutableList<FloatKindPredicate> floats;

  private ImmutableList<BranchPredicate> branchPredicates;

  private InstrumentationSites sites;
  
  @Override
  public void dispose() {
    this.scalarPairs = null;
    this.returns = null;
    this.floats = null;
    this.branchPredicates = null;
    this.sites = null;
  }

  public PredicateProfile(File profilePath, InstrumentationSites sites,
      boolean isCorrect, ScalarPairPredicate.Factory scalarFactory,
      BranchPredicate.Factory branchFactory,
      sun.processor.profile.predicate.ReturnPredicate.Factory returnFactory) {
    super(profilePath, isCorrect);
    this.sites = sites;

    ImmutableList.Builder<ScalarPairPredicate> scalarPredicates = ImmutableList
        .builder();
    ImmutableList.Builder<ReturnPredicate> returnPredicates = ImmutableList
        .builder();
    ImmutableList.Builder<BranchPredicate> branchPredicates = ImmutableList
        .builder();
    ImmutableList.Builder<FloatKindPredicate> floatPredicates = ImmutableList
        .builder();
    try {
      BufferedReader reader = new BufferedReader(new FileReader(profilePath));
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.contains("<report id=\"samples\">")) {
          // read the report.
          this.readReport(reader, scalarPredicates, returnPredicates,
              branchPredicates, floatPredicates, scalarFactory, branchFactory,
              returnFactory);
          break;
        }
      }
      reader.close();
    } catch (Exception e) {
      throw new RuntimeException("cannot parse profile " + this.getPath(), e);
    }

    this.scalarPairs = (scalarPredicates.build());
    this.returns = (returnPredicates.build());
    this.branchPredicates = (branchPredicates.build());
    this.floats = (floatPredicates.build());
  }

  private void readReport(BufferedReader reader,
      ImmutableList.Builder<ScalarPairPredicate> scalarPredicates,
      ImmutableList.Builder<ReturnPredicate> returnPredicates,
      ImmutableList.Builder<BranchPredicate> branchPredicates,
      ImmutableList.Builder<FloatKindPredicate> floatPredicates,
      Factory factory,
      sun.processor.profile.predicate.BranchPredicate.Factory branchFactory,
      ReturnPredicate.Factory returnFactory) throws IOException {
    for (String line = reader.readLine(); line != null; line = reader
        .readLine()) {
      if (line.startsWith("<samples")) {
        if (line.contains("scheme=\"returns\"")) {
          readReturns(reader, returnPredicates, returnFactory);
        } else if (line.contains("scheme=\"branches\"")) {
          readBranches(reader, branchPredicates, branchFactory);
        } else if (line.contains("scheme=\"scalar-pairs\"")) {
          readScalarPairs(reader, scalarPredicates, factory);
        } else if (line.contains("scheme=\"float-kinds\"")) {
          this.readFloats(reader, floatPredicates);
        } else {
          throw new RuntimeException();
        }
      }
    }
  }

  private void readScalarPairs(BufferedReader reader,
      ImmutableList.Builder<ScalarPairPredicate> predicates,
      ScalarPairPredicate.Factory factory) {
    int sequence = -1;
    try {
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.contains("</samples>"))
          return;
        line = line.trim();
        if (line.length() == 0)
          continue;
        String[] counters = split(line, 3); // line.split("\\s+");
        if (counters.length != 3) {
          throw new RuntimeException(line);
        }
        final int lessThanCounter = Integer.parseInt(counters[0]);
        final int equalCounter = Integer.parseInt(counters[1]);
        final int greaterThanCounter = Integer.parseInt(counters[2]);
        
        final int id = ++sequence;
        
        ScalarSite site = this.sites.getScalarSite(id);
//        if(this.functionSet.contains(site.getFunctionName())){
        	predicates.add(factory.create(id, site, lessThanCounter, equalCounter, greaterThanCounter));
//        }
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  private void readFloats(BufferedReader reader,
      ImmutableList.Builder<FloatKindPredicate> predicates) {
    int sequence = -1;
    try {
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.contains("</samples>"))
          return;
        line = line.trim();
        if (line.length() == 0)
          continue;
        String[] counters = split(line, 9); // line.split("\\s+");
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

        int id = ++sequence;
        
        FloatKindSite site = this.sites.getFloatKindSite(id);
//        if (this.functionSet.contains(site.getFunctionName())) {
			predicates.add(new FloatKindPredicate(id, site, negativeInfinite, negativeNormalized, negativeDenormalized, negativeZero,
					nan, positiveZero, positiveDenormalized, positiveNormalized, positiveInfinite));
//		}
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  private void readBranches(BufferedReader reader,
      ImmutableList.Builder<BranchPredicate> predicates,
      sun.processor.profile.predicate.BranchPredicate.Factory branchFactory) {
    int sequence = -1;
    try {
      for (String line = reader.readLine(); line != null; line = reader
          .readLine()) {
        if (line.contains("</samples>"))
          return;
        line = line.trim();
        if (line.length() == 0)
          continue;
        String[] counters = split(line, 2); // line.split("\\s+");
        if (counters.length != 2) {
          throw new RuntimeException();
        }
        final int falseCounter = Integer.parseInt(counters[0]);
        final int trueCounter = Integer.parseInt(counters[1]);
        
        int id = ++sequence;
        
        BranchSite site = this.sites.getBranchSite(id);
//        if (this.functionSet.contains(site.getFunctionName())) {
			predicates.add(branchFactory.create(id, site, trueCounter, falseCounter));
//		}
      }
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }

  private static String[] split(String line, final int expectedLength) {
    int start = 0;
    // List<String> list = new ArrayList<String>();
    String[] list = new String[expectedLength];
    final int length = line.length();
    int i = 0;
    while (start < length) {
      int end = line.indexOf('\t', start);
      if (end < 0) {
        end = length;
      }
      // list.add(line.substring(start, end));
      list[i++] = line.substring(start, end);
      start = end + 1;
    }
    if (i != expectedLength)
      throw new RuntimeException(i + ", " + expectedLength + " in profile ");
    return list;
  }

  private void readReturns(BufferedReader reader,
      ImmutableList.Builder<ReturnPredicate> predicates,
      ReturnPredicate.Factory factory) {
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
        String[] counters = split(line, 3); // line.split("\\s+");
        if (counters.length != 3) {
          throw new RuntimeException();
        }
        final int negativeCounter = Integer.parseInt(counters[0]);
        final int zeroCounter = Integer.parseInt(counters[1]);
        final int positiveCounter = Integer.parseInt(counters[2]);
        
        final int id = ++sequence;
        
        ReturnSite site = this.sites.getReturnSite(id);
//        if (this.functionSet.contains(site.getFunctionName())) {
			predicates.add(factory.create(id, site, negativeCounter, zeroCounter, positiveCounter));
//		}
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
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

}
