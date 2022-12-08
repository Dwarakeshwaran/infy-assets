package restlet.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import restlet.service.Service;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

		String realm = "";
		String consumerKey = "";
		String consumerSecret = "";
		String tokenKey = "";
		String tokenSecret = "";

		String restUrl = "";
		String restPath = "";

		Service service = new Service();

		boolean sentStatus = service.netsuiteRestletService(realm, consumerKey, consumerSecret, tokenKey, tokenSecret,
				restUrl, restPath);

		logger.info("Sent Status {}", sentStatus);

	}

}
