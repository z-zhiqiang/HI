package sun.processor.predicate.processor;

import java.io.PrintWriter;
import java.util.List;

import sun.processor.core.IDataSet;
import sun.processor.core.IDataSetProcessor;
import sun.processor.predicate.PredicateDataSet;

public abstract class AbstractPredicateDataSetProcessor implements
		IDataSetProcessor {

	@Override
	public void process(IDataSet dataset, List<Object> resultsList, PrintWriter writer) {
		if (dataset instanceof PredicateDataSet) {
			this.processPredicateDataSet((PredicateDataSet) dataset, resultsList, writer);
		} else {
			throw new RuntimeException(
					"You use a predicate dataset processor to process "
							+ "a non-predicate dataset. This class is ".concat(this.getClass()
									.getCanonicalName()));
		}
	}

	protected abstract void processPredicateDataSet(PredicateDataSet dataset, List<Object> resultsList, PrintWriter writer);

}
