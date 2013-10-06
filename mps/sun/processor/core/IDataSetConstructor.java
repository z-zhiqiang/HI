package sun.processor.core;

import java.io.PrintWriter;
import java.util.List;


public interface IDataSetConstructor {

	IDataSet createDataSet(IProfile[] profiles, List<Object> resultsList, PrintWriter writer);

}
