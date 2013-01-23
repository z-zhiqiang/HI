package edu.nus.sun.processor.sir;

import sun.processor.profile.reader.AbstractProfileReader;

/**
 * this is the base class profile reader for benchmark subjects from SIR.
 * (Software Infrastructure Repository)
 * 
 * Basically, this class assumes that
 * 
 * <li>all test outputs have the file name pattern "t[0-9]+"</li>
 * 
 * <li>and all corresponding profiles have the file name pattern
 * "t[0-9]+.profile"</li>
 * 
 * @author Chengnian Sun.
 * 
 */
public abstract class AbstractSIRProfileReader extends AbstractProfileReader {

	public AbstractSIRProfileReader(String profileFolder) {
		super(profileFolder);
	}

//	@Override
//	protected String canonicalizeTestName(String testOutputName) {
//		return testOutputName.substring(0, testOutputName.lastIndexOf('.'));
//	}

	@Override
	protected String canonicalizeProfileName(String profileName) {
		return profileName.substring(0, profileName.lastIndexOf('.'));
	}

	@Override
	protected String profileFilterPattern() {
		return "o[0-9]+\\.[fp]profile";
	}

//	@Override
//	protected String testOutputFilterPattern() {
//		return "o[0-9]+\\.[fp]?out";
//	}

	// @Override
	// protected IProfile createProfile(IPairedOutput pairedOutput, File
	// profileFile) {
	// if (!(pairedOutput instanceof DefaultFilePairOutput)) {
	// throw new RuntimeException("pairedOutput must be of class "
	// + DefaultFilePairOutput.class);
	// }
	// DefaultFilePairOutput pairOuput = (DefaultFilePairOutput) pairedOutput;
	// return new PredicateProfile(profileFile, sites, FileUtil.contentEqual(
	// pairOuput.getOutput(), pairOuput.getOracle()));
	// }

}
