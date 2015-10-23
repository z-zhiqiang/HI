
public class AntWrapper {

	public static void main(String[] args) {
		try{
			junit.textui.TestRunner.main(args);
		}
		catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
}
