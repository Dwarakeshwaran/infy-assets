package s3.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FileConfig {

	private String sourceServerProtocol;

	private String sourceBucketName;

	private String sourceFilePath;

	private String targetServerProtocol;

	private String targetBucketName;

	private String targetFilePath;

	private String fileExtension;

}
