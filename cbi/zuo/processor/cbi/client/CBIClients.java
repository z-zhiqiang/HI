package zuo.processor.cbi.client;

import java.util.HashMap;
import java.util.Map;

public class CBIClients {
//	private final 
	private String targetFunction;
	private final Map<String, CBIClient> clientsMap;
	

	public CBIClients(){
		clientsMap = new HashMap<String, CBIClient>();
	}


	public String getTargetFunction() {
		return targetFunction;
	}

	public Map<String, CBIClient> getClientsMap() {
		return clientsMap;
	}
	
	
}
