package edu.nus.sun.processor.predicated.siemense;

import java.io.File;

import sun.processor.core.IProfile;
import sun.processor.profile.InstrumentationSites;
import sun.processor.profile.PredicateProfile;
import sun.processor.util.FileUtil;
import edu.nus.sun.processor.sir.AbstractSIRProfileReader;

public class SIRPredicateProfileReader extends AbstractSIRProfileReader {

	private final InstrumentationSites sites;

	public SIRPredicateProfileReader(String profileFolder, String sitesPath) {
		super(profileFolder);
		if (sitesPath == null) {
			this.sites = null;
		} else {
			this.sites = new InstrumentationSites(new File(sitesPath));
		}
	}

	// @Override
	// protected String canonicalizeTestName(String testOutputName) {
	// return testOutputName;
	// }
	//
	// @Override
	// protected String canonicalizeProfileName(String profileName) {
	// return profileName.substring(0, profileName.lastIndexOf('.'));
	// }
	//
	// @Override
	// protected String profileFilterPattern() {
	// return "t[0-9]+\\.profile$";
	// }
	//
	// @Override
	// protected String testOutputFilterPattern() {
	// return "t[0-9]+";
	// }

	@Override
	protected IProfile createProfile(File profileFile) {
		String filename = profileFile.getName();
		if(!filename.matches(profileFilterPattern())){
			throw new RuntimeException("wrong profile name");
		}
		boolean isCorrect = true;
		if(filename.endsWith(".fprofile")){
			isCorrect = false;
		}
		return new PredicateProfile(profileFile, sites, isCorrect);
	}

}
