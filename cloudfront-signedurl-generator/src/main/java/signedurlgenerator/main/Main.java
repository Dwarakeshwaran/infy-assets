package signedurlgenerator.main;

import java.io.File;
import java.util.Date;

import com.amazonaws.util.DateUtils;

import signedurlgenerator.service.SignedUrlGeneratorService;

public class Main {

	public static void main(String[] args) {
		SignedUrlGeneratorService cloudFrontService = new SignedUrlGeneratorService();

		String distributionDomain = "";
		File privateKeyFile = new File("private_key.pem");
		String s3ObjectKey = "";
		String keyPairId = "";
		Date expiryDate = DateUtils.parseISO8601Date("2023-11-11T22:20:00.000Z");

		try {
			String signedUrl = cloudFrontService.generateSignedUrl(distributionDomain, privateKeyFile, s3ObjectKey,
					keyPairId, expiryDate);

			System.out.println(signedUrl);

		} catch (Exception e) {

			System.out.println("Error Occurred while generating SignedUrl for Accessing Coudfront Distribution Domain "
					+ e.getMessage());

		}

	}

}
