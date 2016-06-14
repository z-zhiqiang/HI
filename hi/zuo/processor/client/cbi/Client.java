package zuo.processor.client.cbi;

import java.io.File;

import zuo.processor.functionentry.client.iterative.CClient;
import zuo.processor.functionentry.client.iterative.java.JavaClient;

public class Client {
	
	public static void main(String[] args) {
		if(args.length != 3){
			System.err.println("Usage: rootDir subject consoleDir\n");
			return;
		}
		int[] ks = {1, 3, 5, 10};
		long time0 = System.currentTimeMillis();

		if(args[1].equals("nanoxml") || args[1].equals("siena") || args[1].equals("apache-ant") || args[1].equals("derby")){
			JavaClient c = new JavaClient(ks, new File(args[0]), args[1], new File(new File(args[2]), args[1]));
			c.runSir();
		}
		else if(args[1].equals("sed") || args[1].equals("gzip") || args[1].equals("grep") || args[1].equals("bash")){
			CClient c = new CClient(ks, new File(args[0]), args[1], new File(new File(args[2]), args[1]));
			c.runSir();
		}
		else if(args[1].equals("space")){
			CClient c = new CClient(ks, new File(args[0]), args[1], new File(new File(args[2]), args[1]));
			c.runSiemens();
		}
		
		long time1 = System.currentTimeMillis();
		long s = (time1 - time0) / 1000;
		System.out.println("time: \t" + s + "s\t" + (s / 60) + "m\t" + (s / 3600) + "h");
	}

}
