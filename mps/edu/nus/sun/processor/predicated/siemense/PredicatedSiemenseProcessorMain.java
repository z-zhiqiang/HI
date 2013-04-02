package edu.nus.sun.processor.predicated.siemense;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import sun.main.AbstractProcessorMain;
import sun.processor.core.IDataSetConstructor;
import sun.processor.core.IDataSetProcessor;
import sun.processor.core.IProfileProcessor;
import sun.processor.core.IProfileReader;
import sun.processor.core.Processor.BackEnd;
import sun.processor.predicate.constructor.PredicateDataSetConstructor;
import sun.processor.predicate.processor.PredicateDataSetMappingOutputter;
import sun.processor.predicate.processor.PredicateDataSetProtoBufOutputter;
import sun.processor.profile.LabelPrinterProfileProcessor;

public class PredicatedSiemenseProcessorMain extends AbstractProcessorMain {

	protected final File sitesInfoPath;

	protected PredicatedSiemenseProcessorMain(String profileFolder,
			File resultOutputFolder, File sitesInfoPath) {
		super(profileFolder, resultOutputFolder);
		this.sitesInfoPath = sitesInfoPath;
	}

	@Override
	protected IProfileReader createProfileReader(String profileFolder) {
		return new SIRPredicateProfileReader(profileFolder,
				this.sitesInfoPath.getAbsolutePath());
	}

	@Override
	protected List<BackEnd> createBackends(File resultOutputFolder) {
		ArrayList<BackEnd> list = new ArrayList<BackEnd>();
		list.add(this.createIntraBackend(resultOutputFolder));
		return list;
	}

	private BackEnd createIntraBackend(File resultOutputFolder) {
		String folder = resultOutputFolder.getAbsolutePath();
		IDataSetConstructor constructor = new PredicateDataSetConstructor();

		ArrayList<IDataSetProcessor> processors = new ArrayList<IDataSetProcessor>();

		// processors.add(new MinusGraphOutputter(folder, "minus.pb"));
		// processors.add(new RedundantPredicateFilter());

		processors.add(new PredicateDataSetMappingOutputter(new File(folder,
				"profile.mapping"), new File(folder, "predicate.mapping")));
		processors.add(new PredicateDataSetProtoBufOutputter(new File(folder,
				"mps-ds.pb")));

		return new BackEnd(constructor, processors);
	}

	// public static void main(String[] args) {
	// String projectName = "print_tokens";
	// startProcessing(projectName);
	// }

	public static void startProcessing(String dir, String projectName) {
		File projectRoot = new File(dir + "/" + projectName + "/");
		if (!projectRoot.exists()) {
			throw new RuntimeException("project does not exist.");
		}

		File versionsFolder = new File(projectRoot, "versions");
		if (!versionsFolder.exists()) {
			throw new RuntimeException("versions folder does not exist.");
		}
		File tracesFolder = new File(projectRoot, "traces");
		if(!tracesFolder.exists()){
			throw new RuntimeException("traces folder does not exist.");
		}

		// File[] correctVersions = correctFolder.listFiles(new FilenameFilter()
		// {
		// @Override
		// public boolean accept(File arg0, String arg1) {
		// return arg1.matches("v[0-9]+");
		// }
		// });

		File[] versions = versionsFolder.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.matches("v7");
			}
		});

		for (File fault : versions) {
			new PredicatedSiemenseProcessorMain(tracesFolder.getAbsolutePath() + "/" + fault.getName() + "/fine-grained", 
					new File(projectRoot.getAbsolutePath() + "/predicate-dataset/", fault.getName()), 
					new File(versionsFolder, fault.getName() + "/sites.txt")).run();
		}
	}

	@Override
	protected List<IProfileProcessor> createProfileProcessors(File resultOutputFolder) {
		List<IProfileProcessor> pps = new ArrayList<IProfileProcessor>();
		pps.add(new LabelPrinterProfileProcessor(new File(resultOutputFolder, "incorrect-profiles.txt")));
		return pps;
	}

	public static void main(String[] args) {
//		if (args.length < 2) {
//			System.err.println("Usage: command projectRootDirectory projectName");
//		} 
//		else
//			startProcessing(args[0], args[1]);
		new PredicatedSiemenseProcessorMain("/home/sunzzq/Downloads/debug", 
				new File("/home/sunzzq/Downloads/debug/ms.out"), 
				new File("/home/sunzzq/Downloads/debug/matrix.sites")).run();
	}
}
