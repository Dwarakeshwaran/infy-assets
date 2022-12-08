package restlet.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth10aService;

import restlet.config.RestApiOauthConfig;
import restlet.utils.ErrorLog;

public class RestApiOauthOperations {

	private static final Logger logger = LoggerFactory.getLogger(RestApiOauthOperations.class);

	public boolean postDataToRestApi(RestApiOauthConfig restOauthConfig, String restUrl, String restPath) {

		logger.info("Inside postDataToRestApi method ...");

		OAuth10aService ouathService = restOauthConfig.getOuathService();
		OAuth1AccessToken oauthAccessToken = restOauthConfig.getOauthAccessToken();
		String path = restUrl + restPath;
		String realm = restOauthConfig.getRealm();
		String message = "";

		OAuthRequest request = new OAuthRequest(Verb.POST, path);

		request.addHeader("Content-Type", "application/json");
		request.setRealm(realm);

		request.setPayload(message);

		ouathService.signRequest(oauthAccessToken, request);

		try (Response response = ouathService.execute(request)) {

			logger.info("Status Code {}", response.getCode());
			logger.info("Response Body {}", response.getBody());

		} catch (Exception e) {

			ErrorLog errorLog = new ErrorLog(e);
			logger.error("Error Log {}", errorLog);

		}

		return false;

	}

}
