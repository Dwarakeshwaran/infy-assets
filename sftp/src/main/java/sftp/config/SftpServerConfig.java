package sftp.config;

import java.io.File;
import java.io.FileOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import sftp.utils.AppConstants;

public class SftpServerConfig {

	private static final Logger logger = LoggerFactory.getLogger(SftpServerConfig.class);

	public ChannelSftp getSSHConnectionUsingKey(JSch jsch, String username, String keyString, String remoteHost) {

		logger.info("Inside getSSHConnection method...");

		try {

			try (FileOutputStream outputStream = new FileOutputStream(
					new File(AppConstants.TEMP_FOLDER_PATH + AppConstants.SFTP_KEY_NAME))) {

				byte[] keyBytes = keyString.getBytes();
				outputStream.write(keyBytes);

			}

			Session jschSession = jsch.getSession(username, remoteHost, 22);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			jschSession.setConfig(config);
			String privateKey = AppConstants.TEMP_FOLDER_PATH + AppConstants.SFTP_KEY_NAME;

			jsch.addIdentity(privateKey);

			jschSession.connect();

			logger.info("Connected to SFTP server {} Successfully!", remoteHost);

			return (ChannelSftp) jschSession.openChannel("sftp");
		} catch (Exception e) {
			logger.error("Error Occurred While Connecting to SFTP Server {}", e.getMessage());
			return null;
		}

	}

	public ChannelSftp getSSHConnectionUsingPassword(JSch jsch, String username, String password, String remoteHost) {

		logger.info("Inside getSSHConnection method...");

		try {

			Session jschSession = jsch.getSession(username, remoteHost, 22);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			jschSession.setConfig(config);
			jschSession.setPassword(password);

			jschSession.connect();

			logger.info("Connected to SFTP server {} Successfully!", remoteHost);

			return (ChannelSftp) jschSession.openChannel("sftp");
		} catch (Exception e) {
			logger.error("Error Occurred While Connecting to SFTP Server {}", e.getMessage());
			return null;
		}

	}

}
