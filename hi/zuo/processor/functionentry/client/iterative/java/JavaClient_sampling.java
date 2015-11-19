package zuo.processor.functionentry.client.iterative.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;


import zuo.processor.cbi.client.CBIClient;
import zuo.processor.cbi.client.CBIClient_sampling;
import zuo.processor.cbi.datastructure.FixPointStructure;
import zuo.processor.cbi.profile.PredicateProfile;
import zuo.processor.cbi.profile.PredicateProfileReader;
import zuo.processor.cbi.site.InstrumentationSites;
import zuo.processor.cbi.site.InstrumentationSites.BranchSite;
import zuo.processor.cbi.site.InstrumentationSites.FloatKindSite;
import zuo.processor.cbi.site.InstrumentationSites.ReturnSite;
import zuo.processor.cbi.site.SitesInfo;
import zuo.processor.cbi.site.InstrumentationSites.ScalarSite;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Order;
import zuo.processor.functionentry.client.iterative.IterativeFunctionClient.Score;
import zuo.processor.functionentry.datastructure.PruneResult;
import zuo.processor.functionentry.datastructure.Result;
import zuo.processor.functionentry.datastructure.Statistic;
import zuo.processor.functionentry.profile.FunctionEntryProfile;
import zuo.processor.functionentry.profile.FunctionEntryProfileReader;
import zuo.processor.functionentry.site.FunctionEntrySites;
import zuo.processor.split.PredicateSplittingSiteProfile;
import zuo.util.file.FileCollection;
import zuo.util.file.FileUtil;

public class JavaClient_sampling {
	private static final String D_Simu = "<<<";
	final File rootDir;
	final String subject;
	final File consoleFolder;
	
	final int round;
	final int startVersion;
	final int endVersion;
	
	final int startSubversion;
	final int endSubversion;
	
	final int[] ks;
	final int start;
	final int offset;
	
	
	public JavaClient_sampling(int[] ks, File rootDir, String subject, File consoleFolder, int round, final int start, int offset, int startV, int endV, int startsubV, int endsubV) {
		this.ks = ks;

		this.rootDir = rootDir;
		this.subject = subject;
		this.consoleFolder = consoleFolder;
		
		this.round = round;
		this.start = start;
		this.offset = offset;

		this.startVersion = startV;
		this.endVersion = endV;
		
		this.startSubversion = startsubV;
		this.endSubversion = endsubV;
		
	}



	/**compute and print out the results for Sir subject excluding Siemens and space
	 * 
	 */
	public void runSir(){
		File[] versions = new File(rootDir, subject + "/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches("v[0-9]*", name) 
						&& Integer.parseInt(name.substring(1)) >= startVersion && Integer.parseInt(name.substring(1)) <= endVersion;
			}});
		Arrays.sort(versions, new Comparator<File>(){

			@Override
			public int compare(File o1, File o2) {
				return new Integer(Integer.parseInt(o1.getName().substring(1))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(1))));
			}});
		
		for(File version: versions){
			File[] subversions = version.listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length >= 9)
							&& Integer.parseInt(name.substring(4)) >= startSubversion && Integer.parseInt(name.substring(4)) <= endSubversion
									&& !(subject.equals("siena") && dir.getName().equals("v5") && name.equals("subv2"))
									&& !(subject.equals("siena") && dir.getName().equals("v5") && name.equals("subv2"));
				}});
			Arrays.sort(subversions, new Comparator<File>(){

				@Override
				public int compare(File o1, File o2) {
					return new Integer(Integer.parseInt(o1.getName().substring(4))).compareTo(new Integer(Integer.parseInt(o2.getName().substring(4))));
				}});
			
			for(File subversion: subversions){
//				FileUtility.clearFiles(new File(subversion, "adaptive"));
//				FileUtility.removeDirectory(new File(subversion, "adaptive"));
				
				String vi = version.getName() + "_" + subversion.getName();
				if(subject.equals("derby") && !(subversion.getName().equals("subv8") || subversion.getName().equals("subv30") || subversion.getName().equals("subv61"))){
					continue;
				}
				System.out.println(vi);
				
				FunctionEntrySites cSites = new FunctionEntrySites(new File(subversion, "coarse-grained/output.sites"));
				FunctionEntryProfileReader functionEntryProfileReader = new FunctionEntryProfileReader(new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/coarse-grained"), cSites);
//				FunctionEntryProfile[] cProfiles = functionEntryProfileReader.readFunctionEntryProfiles();
				
				File fgSitesFile = new File(subversion, "fine-grained/output.sites");
				InstrumentationSites fSites = new InstrumentationSites(fgSitesFile);
				File fgProfilesFolder = new File(rootDir, subject + "/traces/" + version.getName() + "/" + subversion.getName() + "/fine-grained");
				PredicateProfileReader predicateProfileReader = null;
				
				if(needRefine(fSites, cSites.getFunctions())){
					File refineProfilesFolder = new File(fgProfilesFolder.getParentFile(), "refine");
					File refineSitesFile = new File(fgSitesFile.getParentFile(), fgSitesFile.getName().replace('f', 'r'));
					PredicateSplittingSiteProfile refineSplit = new PredicateSplittingSiteProfile(fgSitesFile, fgProfilesFolder, refineSitesFile, refineProfilesFolder, cSites.getFunctions());
					refineSplit.split();
					
					fSites = new InstrumentationSites(refineSitesFile);
					predicateProfileReader = new PredicateProfileReader(refineProfilesFolder, fSites);
				}
				else{
					predicateProfileReader = new PredicateProfileReader(fgProfilesFolder, fSites);
				}
				PredicateProfile[] fProfiles = predicateProfileReader.readProfiles();
				
				//write out methods list
//				FileCollection.writeCollection(sInfo.getMap().keySet(), new File(new File(subversion, "adaptive"), "full"));
				
				//check profiles consistency and compute totalPositive & totalNegative
				//-------------------------------------------------------------------------------------------------------------
				int totalNeg = 0;
				int totalPos = 0;
				
				File[] fgProfiles = predicateProfileReader.getProfileFolder().listFiles(FileUtil.createProfileFilter());
				Arrays.sort(fgProfiles, new FileUtil.FileComparator());
				File[] cgProfiles = functionEntryProfileReader.getProfileFolder().listFiles(FileUtil.createProfileFilter());
				Arrays.sort(cgProfiles, new FileUtil.FileComparator());
				if(fgProfiles.length != cgProfiles.length){
					throw new RuntimeException("unequal number of profiles: " + fgProfiles.length + " vs " + cgProfiles.length);
				}
				for(int i = 0; i < fgProfiles.length; i++){
					String fgName = fgProfiles[i].getName();
					String cgName = cgProfiles[i].getName();
					if(!fgName.equals(cgName)){
						throw new RuntimeException("wrong file mapping: " + fgName + " vs " + cgName);
					}
					if(fgName.matches(FileUtil.failingProfileFilterPattern())){
						totalNeg++;
					}
					else{
						totalPos++;
					}
				}
				
				int totalPositive = 0;
				int totalNegative = 0;
				
				for(PredicateProfile fgProfile: fProfiles){
					if(fgProfile.isCorrect()){
						totalPositive++;
					}
					else{
						totalNegative++;
					}
				}
				assert(totalPositive == totalPos && totalNegative == totalNeg);
				
				//-------------------------------------------------------------------------------------------------------------
				
				//simulate for multiple rounds
				for(int i = 0; i < round; i++){
					
					File cbiconsoleFile = new File(new File(consoleFolder, String.valueOf(i)), subject + "_" + vi + "_cbi.out");
					PrintWriter cbiWriter = null;
					CBIClient_sampling client;
					
					try {
						if(!cbiconsoleFile.getParentFile().exists()){
							cbiconsoleFile.getParentFile().mkdirs();
						}
						
						cbiWriter = new PrintWriter(new BufferedWriter(new FileWriter(cbiconsoleFile)));
						client = new CBIClient_sampling(fProfiles, this.start);
						FixPointStructure fixpoint = client.getFixElement(cbiWriter);
						exportPruneInfoEachRound(fixpoint, vi, i);
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally{
						if(cbiWriter != null){
							cbiWriter.close();
						}
					}
					
					
				}
				
			}
		}
		
	}
	

	/**
	 * export the tests and methods for prune case in each round, which will be used to simulate reality in our experiments;
	 * here we only consider <C, LESS_FIRST> for Top-1
	 * 
	 * @param fullInstrumentedCBIClient
	 * @param clientsMap
	 * @param results
	 * @param ks
	 */
	private void exportPruneInfoEachRound(FixPointStructure fixpoint, String versionName, int roundNum) {
		//get prune result for <C, LESS_FIRST>
		PrintWriter out = null;
		try{
			File file = new File(new File(new File(this.consoleFolder, "SimuInfo_sample"), versionName), "R" + roundNum + "T" + 1); 
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			//write the passing inputs
			out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			
			out.println("Full_Sample_100" + D_Simu + fixpoint.getPassingSet().toString() + D_Simu + fixpoint.getFailingSet().toString());
			
			out.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		finally{
			out.close();
		}
	}


	public static boolean needRefine(InstrumentationSites fSites, Set<String> functions) {
		// TODO Auto-generated method stub
//		for(String unit: fSites.getBranchSites().keySet()){
//			for(BranchSite site: fSites.getBranchSites().get(unit)){
//				if(!functions.contains(site.getFunctionName())){
//					return true;
//				}
//			}
//		}
//		
//		for(String unit: fSites.getReturnSites().keySet()){
//			for(ReturnSite site: fSites.getReturnSites().get(unit)){
//				if(!functions.contains(site.getFunctionName())){
//					return true;
//				}
//			}
//		}
//		
//		for(String unit: fSites.getFloatSites().keySet()){
//			for(FloatKindSite site: fSites.getFloatSites().get(unit)){
//				if(!functions.contains(site.getFunctionName())){
//					return true;
//				}
//			}
//		}
//		
//		for(String unit: fSites.getScalarSites().keySet()){
//			for(ScalarSite site: fSites.getScalarSites().get(unit)){
//				if(!functions.contains(site.getFunctionName())){
//					return true;
//				}
//			}
//		}
		
		return false;
	}

	
	public static void main(String[] args) {
		if(args.length != 11){
			System.err.println("\nUsage: subjectMode(0:Siemens; 1:Sir) rootDir subject consoleDir round start([1, 10]) offset([0, 10]) startVersion endVersion startSubVersion endSubVersion\n");
			return;
		}
		int[] ks = {1, 3, 5, 10};
		long time0 = System.currentTimeMillis();

		JavaClient_sampling c = new JavaClient_sampling(ks, new File(args[1]), args[2], new File(new File(args[3]), args[2] + "_" + args[4] + "_" + args[5] + "_" + args[6] + "_v" + args[7] + "-v" + args[8] + "_subv" + args[9] + "-subv" + args[10]), 
				Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]), Integer.parseInt(args[8]), Integer.parseInt(args[9]), Integer.parseInt(args[10]));
		c.runSir();
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");

	}
	
}
