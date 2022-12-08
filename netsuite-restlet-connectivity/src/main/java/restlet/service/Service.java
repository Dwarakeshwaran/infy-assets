package restlet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import restlet.config.RestApiOauthConfig;

public class Service {

	private static final Logger logger = LoggerFactory.getLogger(Service.class);
	private static RestApiOauthOperations restApiOauthOperations = new RestApiOauthOperations();

	public boolean netsuiteRestletService(String realm, String consumerKey, String consumerSecret, String tokenKey,
			String tokenSecret, String restUrl, String restPath) {

		boolean sentStatus = false;

		RestApiOauthConfig restApiOauthConfig = new RestApiOauthConfig(realm, consumerKey, consumerSecret, tokenKey,
				tokenSecret);

		sentStatus = restApiOauthOperations.postDataToRestApi(restApiOauthConfig, restUrl, restPath);

		logger.info("Sent Status {}", sentStatus);

		return sentStatus;

	}

}
