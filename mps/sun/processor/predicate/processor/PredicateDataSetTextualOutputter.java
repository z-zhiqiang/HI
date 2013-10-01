package sun.processor.predicate.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import sun.processor.predicate.PredicateDataSet;
import sun.processor.predicate.PredicateDataSet.Run;
import sun.processor.predicate.PredicateItem;
import sun.processor.predicate.PredicateItem.PredicateValue;

public class PredicateDataSetTextualOutputter extends
    AbstractPredicateDataSetProcessor {

  private final File outputFile;

  public PredicateDataSetTextualOutputter(File outputFile) {
    this.outputFile = outputFile;
  }

  static class MetaData {
    int positive;
    int negative;
    int maxItemId;
  }

  private MetaData computeMetaData(PredicateDataSet dataset) {
    int positive = 0;
    int negative = 0;
    int maxItemId = 0;
    for (Run run : dataset.getRuns()) {
      if (run.getLabel()) {
        ++positive;
      } else {
        ++negative;
      }
      for (PredicateItem item : run.getAllItems()) {
        if (item.getPredicateStatus().equals(PredicateValue.TRUE)) {
          int id = item.getKey().getId();
          if (id > maxItemId)
            maxItemId = id;
        }
      }
    }
    MetaData metaData = new MetaData();
    metaData.positive = positive;
    metaData.negative = negative;
    metaData.maxItemId = maxItemId;
    return metaData;
  }

  @Override
  protected void processPredicateDataSet(PredicateDataSet dataset, int[] statistics) {

    BufferedWriter outputWriter = null;
    try {
      outputWriter = new BufferedWriter(new FileWriter(this.outputFile));

      MetaData metaData = this.computeMetaData(dataset);

      try {
        outputWriter.write("positive = ");
        outputWriter.write(String.valueOf(metaData.positive));
        outputWriter.newLine();

        outputWriter.write("negative = ");
        outputWriter.write(String.valueOf(metaData.negative));
        outputWriter.newLine();

        outputWriter.write("max_item = ");
        outputWriter.write(String.valueOf(metaData.maxItemId));
        outputWriter.newLine();

        outputWriter.write("tx_count = ");
        outputWriter.write(String
            .valueOf(metaData.positive + metaData.negative));
        outputWriter.newLine();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      int runIDSequence = -1;
      for (Run run : dataset.getRuns()) {
        int runId = ++runIDSequence;
        outputWriter.write(run.getLabel() ? '+' : '-');
        outputWriter.write('[');
        outputWriter.write(String.valueOf(runId));
        outputWriter.write("] ");

        for (PredicateItem item : run.getAllItems()) {
          if (item.getPredicateStatus().equals(PredicateValue.TRUE)) {
            // int id = this.getNewID(item.getKey());
            int id = item.getKey().getId();
            outputWriter.write(String.valueOf(id));
            outputWriter.write(' ');
          }
        }
        outputWriter.newLine();
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (outputWriter != null) {
        try {
          outputWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
