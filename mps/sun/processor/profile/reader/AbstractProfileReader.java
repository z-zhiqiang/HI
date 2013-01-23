package sun.processor.profile.reader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import sun.processor.core.IProfile;
import sun.processor.core.IProfileReader;

public abstract class AbstractProfileReader implements IProfileReader {

	protected final File profileFolder;

	public AbstractProfileReader(String profileFolder) {
		this.profileFolder = new File(profileFolder);
	}

	private class FileComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
			// return mapTestToInteger(o1.getName()) - mapTestToInteger(o2.getName());
//			return canonicalizeTestName(o1.getName()).compareTo(canonicalizeTestName(o2.getName()));
			int i1 = Integer.parseInt(canonicalizeProfileName(o1.getName()).substring(1));
			int i2 = Integer.parseInt(canonicalizeProfileName(o2.getName()).substring(1));
			if(i1 < i2)
				return -1;
			else if(i1 > i2)
				return 1;
			else
				return 0;
			
		}
	}


	protected abstract String canonicalizeProfileName(String profileName);

	protected FilenameFilter createProfileFilter() {
		return new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String name) {
				return Pattern.matches(profileFilterPattern(), name);
			}
		};
	}

	protected abstract String profileFilterPattern();

	protected File[] collectProfileFiles() {
		File[] profileFiles = this.profileFolder.listFiles(this
				.createProfileFilter());
		Arrays.sort(profileFiles, new FileComparator());
		if (profileFiles.length == 0)
			throw new RuntimeException("No profiles in folder " + this.profileFolder);
		return profileFiles;
	}

	@Override
	public final IProfile[] readProfiles() {
		System.out.println("reading profiles in folder " + this.profileFolder);

		File[] profileFiles = this.collectProfileFiles();

		IProfile[] profiles = new IProfile[profileFiles.length];
		System.out.println("reading profiles...");
		for (int i = 0; i < profiles.length; ++i) {
			if ((i + 1) % 5 == 0)
				System.out.print(".");
			if ((i + 1) % 600 == 0)
				System.out.println();
			profiles[i] = this.createProfile(profileFiles[i]);
		}
		System.out.println();
		return profiles;
	}

	protected abstract IProfile createProfile(File profileFile);

}
