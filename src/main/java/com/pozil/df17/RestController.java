package com.pozil.df17;

import java.io.InputStream;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.poz.salesforce.mdapi.MetadataApiUtil;
import com.poz.salesforce.mdapi.MetadataLoginUtil;
import com.poz.salesforce.rest.SalesforceRestClient;
import com.poz.util.FileUtil;
import com.sforce.soap.metadata.DeployDetails;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;

@org.springframework.web.bind.annotation.RestController
public class RestController {
	
	private final static String DEPLOY_DIR = "deploy/";
	private final static String DEPLOY_RESET_ZIP = DEPLOY_DIR + "reset.zip";
	private final static String DEPLOY_COMPLETED_ZIP = DEPLOY_DIR + "completed.zip";
	
	@RequestMapping(path = "/reset", method = RequestMethod.POST)
	void resetDemo() {
		deployMetadata(DEPLOY_RESET_ZIP);
		resetMixStatuses();
	}
	
	@RequestMapping(path = "/complete", method = RequestMethod.POST)
	void completeDemo() {
		deployMetadata(DEPLOY_COMPLETED_ZIP);
	}
	
	private void resetMixStatuses() {
		SalesforceRestClient client = new SalesforceRestClient();
		try {
			client.authenticate(Config.getInstance());
			JSONObject jsonResponse = client.query("SELECT Id, Status__c FROM Merchandising_Mix__c WHERE Status__c != 'Draft'");
			JSONArray records = (JSONArray) jsonResponse.get("records");
			for (int i=0; i<records.size(); i++) {
				JSONObject record = (JSONObject) records.get(i);
				String recordId = (String) record.get("Id");
				client.update("Merchandising_Mix__c", recordId, "{\"Status__c\": \"Draft\"}");
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Failed to reset mix statuses: "+ e.getMessage(), e);
		}
		finally {
			client.disconnect();
		}
	}
	
	private void deployMetadata(String deployFilePath) {
		Config config = Config.getInstance();
		MetadataConnection connection;
		MetadataApiUtil mdapiUtil;
		try {
			connection = MetadataLoginUtil.login(config.getUsername(), config.getPassword(), config.getLoginHost(), config.getApiVersion());
			mdapiUtil = new MetadataApiUtil(connection, config.getApiVersion());
		}
		catch (ConnectionException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("Failed to connect to org: "+ e.getMessage(), e);
		}
		
		DeployOptions deployOptions = new DeployOptions();
        deployOptions.setIgnoreWarnings(false);
        deployOptions.setPerformRetrieve(false);
        deployOptions.setRollbackOnError(true);
        deployOptions.setPurgeOnDelete(true); // Skip recycle bin
        
        try (InputStream deployZipStream = new ClassPathResource(deployFilePath).getInputStream()) {
        	byte[] deployZipBytes = FileUtil.readBytes(deployZipStream);
        	DeployResult result = mdapiUtil.deploy(deployZipBytes, deployOptions);
        	printDeploySuccess(result);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("Failed to deploy changes: "+ e.getMessage(), e);
		}
	}
	
	private void printDeploySuccess(DeployResult result) {
		System.out.println("---");
		DeployDetails details = result.getDetails();
		for (DeployMessage message : details.getComponentSuccesses()) {
			// Ignore manifest file
			if (!"".equals(message.getComponentType())) {
				String status;
				if (message.isCreated())
					status = "created";
				else if (message.isDeleted())
					status = "deleted";
				else if (message.isChanged())
					status = "changed";
				else
					status = "unchanged";
				
				printKeyValue(message.getComponentType() +" "+ message.getFullName(), status, 40);
			}
		}
		System.out.println("---");
	}
	
	private void printKeyValue(String key, String value, int keyLength) {
		String output = key;
		for (int i=key.length(); i<keyLength; i++)
			output += " ";
		output += value;
		System.out.println(output);
	}
}
