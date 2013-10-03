package sun.processor.core;

import java.util.ArrayList;
import java.util.List;

import edu.nus.sun.processor.mps.client.AbstractProcessorWithLabels;

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

		private void process(IProfile[] profiles, Object[] statistics) {
			IDataSet dataset = this.graphConstructor.createDataSet(profiles, statistics);
			
//			AbstractProcessorWithLabels.printMemoryUsage(1);
			
//			Runtime.getRuntime().gc();
//			Runtime.getRuntime().gc();
			for (IDataSetProcessor processor : this.graphProcessors) {
				processor.process(dataset, statistics);
			}
			
//			AbstractProcessorWithLabels.printMemoryUsage(2);
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

	public void process(Object[] statistics) {
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
			be.process(profiles, statistics);
		}
		System.out.println("###############################################");
		System.out.println("################## ...END... ##################");
		System.out.println("###############################################");
	}

}
