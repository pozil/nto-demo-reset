package com.poz.salesforce.mdapi;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;


public abstract class MetadataLoginUtil {

	private final static String SOAP_API_URL = "/services/Soap/c/";
	
    public static MetadataConnection login(String username, String password, String loginHost, double apiVersion) throws ConnectionException {
        String authUrl = "https://"+ loginHost + SOAP_API_URL + Double.toString(apiVersion);
    	final LoginResult loginResult = loginToSalesforce(username, password, authUrl);
        return createMetadataConnection(loginResult);
    }

    private static MetadataConnection createMetadataConnection(final LoginResult loginResult) throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setServiceEndpoint(loginResult.getMetadataServerUrl());
        config.setSessionId(loginResult.getSessionId());
        return new MetadataConnection(config);
    }

    private static LoginResult loginToSalesforce(final String username, final String password, final String loginUrl) throws ConnectionException {
        final ConnectorConfig config = new ConnectorConfig();
        config.setAuthEndpoint(loginUrl);
        config.setServiceEndpoint(loginUrl);
        config.setManualLogin(true);
        return (new EnterpriseConnection(config)).login(username, password);
    }
}