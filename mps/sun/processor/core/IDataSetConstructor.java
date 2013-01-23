package sun.processor.core;

import sun.processor.graph.IDataSet;

public interface IDataSetConstructor {

	IDataSet createDataSet(IProfile[] profiles);

}
