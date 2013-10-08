package sun.processor.core;

import java.io.PrintWriter;
import java.util.List;


public interface IDataSetProcessor {

	void process(IDataSet dataset, Object[] resultsArray, PrintWriter writer);

}
