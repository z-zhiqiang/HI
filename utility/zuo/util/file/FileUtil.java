package zuo.util.file;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Comparator;
import java.util.regex.Pattern;

public class FileUtil {
	
	public static class FileComparator implements Comparator<File> {
		@Override
		public int compare(File o1, File o2) {
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
	
	private static String canonicalizeProfileName(String profileName) {
		return profileName.substring(0, profileName.lastIndexOf('.'));
	}

	
	public static FilenameFilter createProfileFilter() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File arg0, String name) {
				return Pattern.matches(profileFilterPattern(), name);
			}
		};
	}

	public static String profileFilterPattern() {
		return "o[0-9]+\\.[fp]profile";
	}

}
