package sun.processor.core;

import java.io.PrintWriter;
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

		private IDataSet process(IProfile[] profiles, Object[] resultsArray, PrintWriter writer) {
			IDataSet dataset = this.graphConstructor.createDataSet(profiles, resultsArray, writer);
			
			for (IDataSetProcessor processor : this.graphProcessors) {
				processor.process(dataset, resultsArray, writer);
			}
			
			return dataset;
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

	public IDataSet process(Object[] resultsArray, PrintWriter writer) {
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

		IDataSet dataset = null;
		for (BackEnd be : this.backends) {
			dataset = be.process(profiles, resultsArray, writer);
		}
		System.out.println("###############################################");
		System.out.println("################## ...END... ##################");
		System.out.println("###############################################");
		
		return dataset;
	}

}
