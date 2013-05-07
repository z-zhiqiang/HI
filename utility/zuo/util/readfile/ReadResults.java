package zuo.util.readfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class ReadResults {
	final String rootDir;
	final String subject;
	final static String[] files = {"outputs", "fine-grained-sampled-1", "fine-grained-sampled-100", "fine-grained-sampled-10000", 
		"coarse-grained", "fine-grained", "fine-grained-adaptive-H_1", "fine-grained-adaptive-H_2"};
    
	public ReadResults(String rootD, String sub) {
		// TODO Auto-generated constructor stub
		this.rootDir = rootD;
		this.subject = sub;
	}

	public void readSir(){
		File[] versions = new File(rootDir + subject + "/outputs.alt/").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 2);
			}});
		Arrays.sort(versions, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(((File) arg0).getName().substring(1))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(1))));
			}});
		
		for(File version: versions){
			File[] subversions = new File(version, "versions").listFiles(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					// TODO Auto-generated method stub
					return Pattern.matches("subv[0-9]*", name) && (new File(dir, name).listFiles().length == 9);
				}});
			Arrays.sort(subversions, new Comparator(){
				@Override
				public int compare(Object arg0, Object arg1) {
					// TODO Auto-generated method stub
					return new Integer(Integer.parseInt(((File) arg0).getName().substring(4))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(4))));
				}});
			
			for(File subversion: subversions){
				String vi = version.getName() + "_" + subversion.getName();
				System.out.print(vi + "\t");
				for(int i = 0; i < files.length; i++){
					System.out.print(readFile(new File(subversion, files[i] + "/time")) + "\t");
				}
				System.out.println();
			}
		}
	}
	
	public void readSiemens(){
		File[] versions = new File(rootDir + subject + "/outputs/versions").listFiles(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				// TODO Auto-generated method stub
				return Pattern.matches("v[0-9]*", name) && (new File(dir, name).listFiles().length == 8);
			}});
		Arrays.sort(versions, new Comparator(){
			@Override
			public int compare(Object arg0, Object arg1) {
				// TODO Auto-generated method stub
				return new Integer(Integer.parseInt(((File) arg0).getName().substring(1))).compareTo(new Integer(Integer.parseInt(((File) arg1).getName().substring(1))));
			}});
		List<String> versionsList = new ArrayList<String>();
		for(File version: versions){
			String vi = version.getName();
			System.out.print(vi + "\t");
			for(int i = 0; i < files.length; i++){
				System.out.print(readFile(new File(version, files[i] + "/time")) + "\t");
			}
			System.out.println();
		}
	}
	
	private int readFile(File file){
		int time = 0;
		BufferedReader reader = null;
		String line;
		try {
			reader = new BufferedReader(new FileReader(file));
			while((line = reader.readLine()) != null){
				String[] ss = line.split(" ");
				time = Integer.parseInt(ss[ss.length - 1].trim());
			}
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			if(reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return time;
	}
	
	public static void main(String[] args) {
		ReadResults rr;
		String[][] argvs = {
				{"809", "grep"},
				{"213", "gzip"},
				{"363", "sed"},
				{"13585", "space"},
				{"1608", "tcas"},
				{"1052", "totinfo"},
				{"5542", "replace"},
				{"4130", "printtokens"},
				{"4115", "printtokens2"},
				{"2650", "schedule"},
				{"2710", "schedule2"}
		};
		
		for(int i = 0; i < argvs.length; i++){
			String subject = argvs[i][1];
			System.out.println(subject);
			if(i > 3){
				rr = new ReadResults("/home/sunzzq/Research/Automated_Debugging/Subjects/Siemens/", subject);
				rr.readSiemens();
			}
			else if(i == 3){
				rr = new ReadResults("/home/sunzzq/Research/Automated_Debugging/Subjects/", subject);
				rr.readSiemens();
			}
			else{
				rr = new ReadResults("/home/sunzzq/Research/Automated_Debugging/Subjects/", subject);
				rr.readSir();
			}
		}
		
	}
}