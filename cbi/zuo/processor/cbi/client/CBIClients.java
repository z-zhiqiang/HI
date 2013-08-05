package zuo.processor.cbi.client;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import zuo.processor.cbi.site.SitesInfo;

public class CBIClients {
	private final File profilesFolder;
	private final File consoleFolder;
	private final SitesInfo sitesInfo;
	
	private String targetFunction;
	private final Map<String, CBIClient> clientsMap;
	

	public CBIClients(SitesInfo sInfo, File profiles, File consoleFolder){
		this.profilesFolder = profiles;
		this.consoleFolder = consoleFolder;
		this.sitesInfo = sInfo;
		
		clientsMap = new HashMap<String, CBIClient>();
		
		run();
	}


	private void run() {
		// TODO Auto-generated method stub
		
	}


	public String getTargetFunction() {
		return targetFunction;
	}

	public Map<String, CBIClient> getClientsMap() {
		return clientsMap;
	}
	
	
}
