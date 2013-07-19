package sun.processor.core;

import java.util.ArrayList;
import java.util.List;

public class Processor {

	public static class BackEnd {

		final IDataSetConstructor graphConstructor;

		final IDataSetProcessor[] graphProcessors;

		public BackEnd(IDataSetConstructor graphConstructor,
				ArrayList<IDataSetProcessor> graphProcessors) {
			super();
			this.graphConstructor = graphConstructor;
			this.graphProcessors = graphProcessors
					.toArray(new IDataSetProcessor[graphProcessors.size()]);
		}

		private void process(IProfile[] profiles) {
			IDataSet dataset = this.graphConstructor.createDataSet(profiles);
//			if (!(dataset instanceof GraphDataSet)) {
//				throw new RuntimeException("Your dataset constructor creates a non-graph dataset.");
//			}
//			GraphDataSet graphs = (GraphDataSet) dataset;
			Runtime.getRuntime().gc();
			Runtime.getRuntime().gc();
			for (IDataSetProcessor processor : this.graphProcessors) {
				processor.process(dataset);
			}
		}
	}

	private final IProfileReader profileReader;

	private final BackEnd[] backends;

	final IProfileProcessor[] profileProcessores;

	public Processor(IProfileReader profileReader, List<BackEnd> backends,
			List<IProfileProcessor> profileProcessors) {
		super();
		this.profileReader = profileReader;
		// this.graphConstructor = graphConstructor;
		// this.graphProcessors = new ArrayList<IGraphDSProcessor>(graphProcessors);
		this.backends = backends.toArray(new BackEnd[backends.size()]);
		this.profileProcessores = profileProcessors
				.toArray(new IProfileProcessor[profileProcessors.size()]);
	}

	public void process() {
		System.out.println("###############################################");
		System.out.println("################# STARTING... #################");
		System.out.println("###############################################");
		IProfile[] profiles = this.profileReader.readProfiles();

		for (IProfileProcessor pp : this.profileProcessores) {
			pp.init();
			for (IProfile profile : profiles) {
				pp.process(profile);
			}
			pp.dispose();
		}

		for (BackEnd be : this.backends) {
			be.process(profiles);
		}
		System.out.println("###############################################");
		System.out.println("################## ...END... ##################");
		System.out.println("###############################################");
		System.out.println();
		System.out.println();
	}

}
