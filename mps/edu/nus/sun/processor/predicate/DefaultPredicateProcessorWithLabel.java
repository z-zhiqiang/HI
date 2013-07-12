package edu.nus.sun.processor.predicate;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import sun.main.AbstractProcessorWithLabels;
import sun.processor.core.IDataSetConstructor;
import sun.processor.core.IDataSetProcessor;
import sun.processor.core.IProfileProcessor;
import sun.processor.core.IProfileReader;
import sun.processor.core.Processor;
import sun.processor.core.Processor.BackEnd;
import sun.processor.predicate.constructor.PredicateDataSetConstructor;
import sun.processor.predicate.processor.PredicateDataSetMappingOutputter;
import sun.processor.predicate.processor.PredicateDataSetProtoBufOutputter;
import sun.processor.profile.LabelPrinterProfileProcessor;
import edu.nus.sun.processor.IProject;

public class DefaultPredicateProcessorWithLabel extends
		AbstractProcessorWithLabels {

	private static final String DATASET_FOLDER_NAME = "predicate-dataset";

	private static final String EXPERIMENT_ROOT = "/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/";

	private static final String MPS_PB = "mps-ds.pb";

	protected final File sitesInfoPath;

	public DefaultPredicateProcessorWithLabel(File profileFolder, File resultOutputFolder, File sitesInfoPath) {
		super(profileFolder, resultOutputFolder);
		this.sitesInfoPath = sitesInfoPath;
	}

	public static void construct(IProject project) throws SecurityException,
			IllegalArgumentException, NoSuchMethodException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException {
		construct(project, DefaultPredicateProcessorWithLabel.class);
	}

	public static void construct(IProject project,
			Class<? extends DefaultPredicateProcessorWithLabel> constructor)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		final String projectName = project.getProjectName();
		File projectRoot = new File(EXPERIMENT_ROOT, projectName);
		if (!projectRoot.exists())
			throw new RuntimeException("Project " + projectRoot + " does not exist!");

		if (projectName.equals("space")) {
			File[] versions = new File(projectRoot, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 10);
				}});
			Arrays.sort(versions, new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(((File) arg0).getName().substring(1))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(1))));
				}});
			
			for(File version: versions){
				String vi = version.getName();
				File fault = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
				if (!fault.exists()) {
					throw new RuntimeException("Faulty version folder " + fault + " does not exist.");
				}
				final File sitesInfoPath = new File(projectRoot, "versions/" + vi + "/" + vi + "_f.sites");
				final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
				if(!resultOutputFolder.exists()){
					resultOutputFolder.mkdir();
				}
				      
				Constructor<? extends DefaultPredicateProcessorWithLabel> con = constructor
						.getConstructor(File.class, File.class, File.class);
				DefaultPredicateProcessorWithLabel instance = con.newInstance(fault, resultOutputFolder, sitesInfoPath);
				instance.run();
			}
		} 
		else {
			File[] versions = new File(projectRoot, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("v[0-9]*", name);
				}});
			Arrays.sort(versions, new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(((File) arg0).getName().substring(1))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(1))));
				}});
			
			for(File version: versions){
				File[] subversions = version.listFiles(new FilenameFilter(){
					@Override
					public boolean accept(File dir, String name) {
						// TODO Auto-generated method stub
						return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length == 12);
					}});
				Arrays.sort(subversions, new Comparator(){
					@Override
					public int compare(Object arg0, Object arg1) {
						// TODO Auto-generated method stub
						return new Integer(Integer.parseInt(((File) arg0).getName().substring(4))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(4))));
					}});
				
				for(File subversion: subversions){
					String vi = version.getName() + "/" + subversion.getName();
					System.out.println(vi);
					
					File fault = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
					if (!fault.exists()) {
						throw new RuntimeException("Faulty version folder " + fault + " does not exist.");
					}
					final File sitesInfoPath = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_f.sites");
					final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
					if(!resultOutputFolder.exists()){
						resultOutputFolder.mkdir();
					}
					
					Constructor<? extends DefaultPredicateProcessorWithLabel> con = constructor
							.getConstructor(File.class, File.class, File.class);
					DefaultPredicateProcessorWithLabel instance = con.newInstance(fault, resultOutputFolder, sitesInfoPath);
					instance.run();
				}
				
			}
		}
		
	}


	@Override
	protected List<BackEnd> createBackends(final File resultOutputFolder) {
		final ArrayList<BackEnd> list = new ArrayList<Processor.BackEnd>();

		// final File intraResultFolder = new File(resultOutputFolder, "intra");
		final File intraResultFolder = resultOutputFolder;
		if (!intraResultFolder.exists()) {
			intraResultFolder.mkdirs();
		}
		list.add(this.createIntraBackend(intraResultFolder));

		return list;
	}

	private BackEnd createIntraBackend(File resultOutputFolder) {
		String folder = resultOutputFolder.getAbsolutePath();
		IDataSetConstructor constructor = new PredicateDataSetConstructor();

		ArrayList<IDataSetProcessor> processors = new ArrayList<IDataSetProcessor>();

		processors.add(new PredicateDataSetMappingOutputter(new File(folder,
				"profile.mapping"), new File(folder, "predicate.mapping")));
		processors.add(new PredicateDataSetProtoBufOutputter(new File(folder,
				MPS_PB)));

		return new BackEnd(constructor, processors);
	}

	@Override
	protected List<IProfileProcessor> createProfileProcessors(
			final File resultOutputFolder) {
		final List<IProfileProcessor> pps = new ArrayList<IProfileProcessor>();
		pps.add(new LabelPrinterProfileProcessor(new File(resultOutputFolder,
				"incorrect-profiles.txt")));
		return pps;
	}

	@Override
	protected IProfileReader createProfileReader(final File profileFolder) {
		return new DefaultPredicateProfileReaderWithLabel(profileFolder, this.sitesInfoPath);
	}

}
