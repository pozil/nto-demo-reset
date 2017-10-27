package com.poz.salesforce.rest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.pozil.df17.Config;

public class SalesforceRestClient {

	private final static String UTF8 = StandardCharsets.UTF_8.name();
	private final static String SOBJECT_API = "sobjects/";
	
	private String accessToken;
	private String restBaseUrl;

	private CloseableHttpClient httpClient;
	private JSONParser parser;


	public SalesforceRestClient() {
		httpClient = HttpClientBuilder.create().build();
		parser = new JSONParser();
	}

	public void authenticate(Config config)
			throws AuthenticationException, HttpException, IOException {

		try {			
			// Set the SID
			System.out.println("Logging in as " + config.getUsername() + " in environment " + config.getLoginHost());
			String baseUrl = "https://" + config.getLoginHost() + "/services/oauth2/token";
			// Send a post request to the OAuth URL
			HttpPost oAuthPost = new HttpPost(baseUrl);
			// The request body must contain these 5 values
			List<BasicNameValuePair> parametersBody = new ArrayList<BasicNameValuePair>();
			parametersBody.add(new BasicNameValuePair("grant_type", "password"));
			parametersBody.add(new BasicNameValuePair("username", config.getUsername()));
			parametersBody.add(new BasicNameValuePair("password", config.getPassword()));
			parametersBody.add(new BasicNameValuePair("client_id", config.getClientId()));
			parametersBody.add(new BasicNameValuePair("client_secret", config.getClientSecret()));
			oAuthPost.setEntity(new UrlEncodedFormEntity(parametersBody, UTF8));

			// Execute the request
			System.out.println("POST " + baseUrl + "...\n");
			HttpResponse response = httpClient.execute(oAuthPost);

			int httpStatusCode = response.getStatusLine().getStatusCode();
			String rawResponseBody = EntityUtils.toString(response.getEntity());
			System.out.println("OAuth login response: HTTP "+ httpStatusCode);
			System.out.println(rawResponseBody);
			System.out.println();

			// Parse response & save access token
			JSONObject oAuthResponse = (JSONObject) parser.parse(rawResponseBody);
			if (httpStatusCode != 200) {
				throw new AuthenticationException("OAuth authentication '"+ oAuthResponse.get("error") +"' error : "+ oAuthResponse.get("error_description"));
			}
			accessToken = (String) oAuthResponse.get("access_token");

			// Query for user info.
			String userIdEndpoint = (String) oAuthResponse.get("id");
			HttpGet userInfoRequest = new HttpGet(userIdEndpoint);
			authorizeRequest(userInfoRequest);
			HttpResponse userInfoResponse = httpClient.execute(userInfoRequest);

			// Parse response & save base REST URL
			httpStatusCode = userInfoResponse.getStatusLine().getStatusCode();
			rawResponseBody = EntityUtils.toString(userInfoResponse.getEntity());
			System.out.println("User info response: HTTP "+ httpStatusCode);
			System.out.println(rawResponseBody);
			System.out.println("");
			JSONObject userInfo = (JSONObject) parser.parse(rawResponseBody);
			Map<String, String> urls = (Map<String, String>) userInfo.get("urls");
			restBaseUrl = urls.get("rest").replace("{version}", String.valueOf(config.getApiVersion()));
			System.out.println("REST API url is " + restBaseUrl);	
		}
		catch (ParseException pe) {
			System.out.println("JSON parse error at position: " + pe.getPosition());
			throw new RuntimeException(pe);
		}
	}

	public void authorizeRequest(HttpRequest request) {
		request.addHeader("Authorization", "Bearer "+ accessToken);
	}

	public void disconnect() {
		try {
			httpClient.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed to close REST client: "+ e.getMessage(), e);
		}
	}

	public JSONObject query(String query) throws ParseException, IOException {
		String requestUrl = restBaseUrl + "query?q=" + query.replaceAll(" ", "+");
		HttpGet request = new HttpGet(requestUrl);
		authorizeRequest(request);
		HttpResponse response = httpClient.execute(request);

		int httpStatusCode = response.getStatusLine().getStatusCode();
		String rawResponseBody = EntityUtils.toString(response.getEntity());
		System.out.println("Query response: HTTP "+ httpStatusCode);
		System.out.println(rawResponseBody);
		System.out.println("");

		// Parse response
		return (JSONObject) parser.parse(rawResponseBody);
	}
	
	public void update(String sObjectName, String id, String body) throws IOException {
		String requestUrl = restBaseUrl + SOBJECT_API + sObjectName +"/"+ id;
		HttpPatch request = new HttpPatch(requestUrl);
		request.addHeader("Content-Type", "application/json");
		authorizeRequest(request);
		HttpEntity entity = new ByteArrayEntity(body.getBytes(UTF8));
		request.setEntity(entity);
		HttpResponse response = httpClient.execute(request);
		
		int httpStatusCode = response.getStatusLine().getStatusCode();
		if (httpStatusCode < 200 || httpStatusCode > 299) {
			HttpEntity responseEntity = response.getEntity();
			if (responseEntity != null) {
				String rawResponseBody = EntityUtils.toString(responseEntity);
				System.out.println(rawResponseBody);
			}
			throw new RuntimeException("Updated failed with status "+ httpStatusCode);
		}
			
	}
}
