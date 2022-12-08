package signedurlgenerator.service;

import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol;

public class SignedUrlGeneratorService {

	public String generateSignedUrl(String distributionDomain, File privateKeyFile, String s3ObjectKey,
			String keyPairId, Date expiryDate) throws InvalidKeySpecException, IOException {

		Protocol protocol = Protocol.https;

		return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(protocol, distributionDomain, privateKeyFile,
				s3ObjectKey, keyPairId, expiryDate);

	}

}
