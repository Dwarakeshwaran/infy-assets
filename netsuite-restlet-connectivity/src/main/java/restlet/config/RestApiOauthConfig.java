package restlet.config;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.core.base64.Base64;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.exceptions.OAuthSignatureException;
import com.github.scribejava.core.model.OAuth1AccessToken;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.services.SignatureService;
import com.github.scribejava.core.utils.OAuthEncoder;
import com.github.scribejava.core.utils.Preconditions;

import restlet.utils.ErrorLog;

public class RestApiOauthConfig {

	private static final Logger logger = LoggerFactory.getLogger(RestApiOauthConfig.class);

	private String realm;
	private String consumerKey;
	private String consumerSecret;
	private String tokenKey;
	private String tokenSecret;

	private OAuth10aService oauthService;

	private OAuth1AccessToken oauthAccessToken;

	public RestApiOauthConfig(String realm, String consumerKey, String consumerSecret, String tokenKey,
			String tokenSecret) {

		try {

			this.realm = realm;
			this.consumerKey = consumerKey;
			this.consumerSecret = consumerSecret;
			this.tokenKey = tokenKey;
			this.tokenSecret = tokenSecret;

			oauthService = new ServiceBuilder(this.consumerKey).apiSecret(this.consumerSecret)
					.build(OAuthServiceHelper.instance());

			oauthAccessToken = new OAuth1AccessToken(this.tokenKey, this.tokenSecret);

		} catch (Exception e) {

			ErrorLog errorLog = new ErrorLog(e);

			oauthService = null;
			oauthAccessToken = null;

			logger.error("Error Log {}", errorLog);
		}

	}

	public String getRealm() {
		return realm;
	}

	public OAuth10aService getOuathService() {
		return oauthService;
	}

	public OAuth1AccessToken getOauthAccessToken() {
		return oauthAccessToken;
	}

}

class OAuthServiceHelper extends DefaultApi10a {

	protected OAuthServiceHelper() {

	}

	private static class InstanceHolder {
		private static final OAuthServiceHelper INSTANCE = new OAuthServiceHelper();
	}

	public static OAuthServiceHelper instance() {
		return InstanceHolder.INSTANCE;
	}

	@Override
	public SignatureService getSignatureService() {
		return new HMACSha256SignatureService();
	}

	@Override
	public String getRequestTokenEndpoint() {

		return null;
	}

	@Override
	public String getAccessTokenEndpoint() {

		return null;
	}

	@Override
	protected String getAuthorizationBaseUrl() {

		return null;
	}

}

/**
 * HMAC-SHA256 implementation of {@link SignatureService}
 */
class HMACSha256SignatureService implements SignatureService {

	private static final Logger logger = LoggerFactory.getLogger(HMACSha256SignatureService.class);

	private static final String EMPTY_STRING = "";
	private static final String CARRIAGE_RETURN = "\r\n";
	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final String METHOD = "HMAC-SHA256";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSignature(String baseString, String apiSecret, String tokenSecret) {
		try {
			Preconditions.checkEmptyString(baseString, "Base string can't be null or empty string");
			Preconditions.checkNotNull(apiSecret, "Api secret can't be null");
			return doSign(baseString, OAuthEncoder.encode(apiSecret) + '&' + OAuthEncoder.encode(tokenSecret));
		} catch (NoSuchAlgorithmException | InvalidKeyException | RuntimeException e) {

			ErrorLog errorLog = new ErrorLog(e);
			logger.error("Error Log {}", errorLog);

			throw new OAuthSignatureException(baseString, e);
		}
	}

	private String doSign(String toSign, String keyString) throws NoSuchAlgorithmException, InvalidKeyException {
		final SecretKeySpec key = new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
		final Mac mac = Mac.getInstance(HMAC_SHA256);
		mac.init(key);
		final byte[] bytes = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));
		return Base64.encode(bytes).replace(CARRIAGE_RETURN, EMPTY_STRING);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getSignatureMethod() {
		return METHOD;
	}
}
