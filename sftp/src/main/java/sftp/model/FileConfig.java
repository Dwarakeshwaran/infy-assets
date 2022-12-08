package sftp.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FileConfig {

	private String sourceServerProtocol;

	private String sourceServerUsername;

	private String sourceServerKeyString;

	private String sourceServerPassword;

	private String sourceServerHostName;

	private String sourceFilePath;

	private String targetServerProtocol;

	private String targetServerUsername;

	private String targetServerKeyString;

	private String targetServerPassword;

	private String targetServerHostName;

	private String targetFilePath;

	private String fileExtension;

}
