package zuo.processor.functionentry.client.twopass;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.regex.Pattern;

import edu.nus.sun.processor.mps.client.DefaultPredicateProcessorWithLabel;

public class Client {
	private static final String DATASET_FOLDER_NAME = "predicate-dataset";

	private static final String EXPERIMENT_ROOT = "/home/sunzzq/Research/Automated_Bug_Isolation/Twopass/Subjects/";


	public static void main(String[] args) {
		construct("gzip");
	}
	
	public static void construct(String projectName){
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
				File profiles = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
				if (!profiles.exists()) {
					throw new RuntimeException("Faulty version folder " + profiles + " does not exist.");
				}
				final File sitesInfoPath = new File(projectRoot, "versions/" + vi + "/" + vi + "_f.sites");
				final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
				if(!resultOutputFolder.exists()){
					resultOutputFolder.mkdir();
				}
				      
				DefaultPredicateProcessorWithLabel instance = new DefaultPredicateProcessorWithLabel(profiles, resultOutputFolder, sitesInfoPath);
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
						return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 11);
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
					
					//profiles folders
					File fProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/fine-grained/");
					if (!fProfilesFolder.exists()) {
						throw new RuntimeException("Fine-grained faulty profiles folder " + fProfilesFolder + " does not exist.");
					}
					File cProfilesFolder = new File(new File(projectRoot, "traces"), vi + "/coarse-grained/");
					if(!cProfilesFolder.exists()){
						throw new RuntimeException("Coarse-grained faulty profiles folder " + cProfilesFolder + " does not exist.");
					}
					
					//instrumentation sites files
					final File fSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_f.sites");
					final File cSitesFile = new File(projectRoot, "versions/" + vi + "/" + version.getName() + "_" + subversion.getName() + "_c.sites");
					
					//dataset output folder
					final File resultOutputFolder = new File(projectRoot, "versions/" + vi + "/" + DATASET_FOLDER_NAME);
					if(!resultOutputFolder.exists()){
						resultOutputFolder.mkdir();
					}
					
//					TwopassFunctionClient funClient = new TwopassFunctionClient(cSitesFile, cProfilesFolder, fSitesFile);
//					funClient.printEntry();
//					Set<String> functionSet = funClient.getBoostFunctionSet((byte)0, 0.1f);
					
					DefaultPredicateProcessorWithLabel instance = new DefaultPredicateProcessorWithLabel(fProfilesFolder, resultOutputFolder, fSitesFile);
					instance.run();
				}
				
			}
		}
		
	}

}