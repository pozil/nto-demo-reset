package com.pozil.df17;

public class Config {
	private final static String ENV_LOGIN_HOST		= "login_host";
	private final static String ENV_USERNAME		= "username";
	private final static String ENV_PASSWORD		= "password";
	private final static String ENV_CLIENT_ID		= "client_id";
	private final static String ENV_CLIENT_SECRET	= "client_secret";
	private final static String ENV_API_VERSION		= "api_version";
	
	private final static String LOGIN_HOST;
	private final static String USERNAME;
	private final static String PASSWORD;
	private final static String CLIENT_ID;
	private final static String CLIENT_SECRET;
	private final static double API_VERSION;
	
	private static Config config;
	
	static {
		LOGIN_HOST		= getEnvValue(ENV_LOGIN_HOST);
		USERNAME		= getEnvValue(ENV_USERNAME);
		PASSWORD		= getEnvValue(ENV_PASSWORD);
		CLIENT_ID		= getEnvValue(ENV_CLIENT_ID);
		CLIENT_SECRET	= getEnvValue(ENV_CLIENT_SECRET);
		API_VERSION		= Double.valueOf(getEnvValue(ENV_API_VERSION));
	}

	public static Config getInstance() {
		if (config == null)
			config = new Config();
		return config;
	}
	
	private Config() {}

	private static String getEnvValue(String key) {
		String value = System.getenv(key);
		if (value == null)
			throw new IllegalArgumentException("Missing env variable: "+ key);
		return value;
	}
	
	public String getLoginHost() {
		return LOGIN_HOST;
	}

	public String getUsername() {
		return USERNAME;
	}

	public String getPassword() {
		return PASSWORD;
	}

	public String getClientId() {
		return CLIENT_ID;
	}

	public String getClientSecret() {
		return CLIENT_SECRET;
	}

	public double getApiVersion() {
		return API_VERSION;
	}
}
