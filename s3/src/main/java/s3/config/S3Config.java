package s3.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class S3Config {

	public AmazonS3 getS3Config() {

		String region = "us-east-1";

		return AmazonS3ClientBuilder.standard().withCredentials(new DefaultAWSCredentialsProviderChain())
				.withRegion(region).build();

	}

}
