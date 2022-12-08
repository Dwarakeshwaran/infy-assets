package sftp.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

import sftp.config.SftpServerConfig;
import sftp.model.FileConfig;
import sftp.model.FileInfo;
import sftp.utils.AppConstants;

public class Service {

	private static final Logger logger = LoggerFactory.getLogger(Service.class);

	private ChannelSftp sourceSftpChannel = null;
	private ChannelSftp targetSftpChannel = null;

	private SftpServerConfig sftpConfig = new SftpServerConfig();
	private SftpOperations sftpOperations = new SftpOperations();

	public boolean doSftpTransfer() {

		/*
		 * 1. Get Files from the sourceProtocol by using sourceHostName and sourcePath
		 */

		List<FileInfo> sourceFilesList = null;
		FileConfig fileConfig = new FileConfig();

		String sourceProtocol = "";
		String sourceUsername = "";
		String sourceKeyString = "";
		String sourcePassword = "";
		String sourceHostName = "";
		String sourcePath = "";
		String fileExtension = "";

		fileConfig.setSourceServerProtocol(sourceProtocol);
		fileConfig.setSourceServerUsername(sourceUsername);
		fileConfig.setSourceServerKeyString(sourceKeyString);
		fileConfig.setSourceServerPassword(sourcePassword);
		fileConfig.setSourceServerHostName(sourceHostName);
		fileConfig.setSourceFilePath(sourcePath);
		fileConfig.setFileExtension(fileExtension);

		sourceFilesList = getSourceFiles(fileConfig);

		if (sourceFilesList != null) {
			for (FileInfo file : sourceFilesList) {

				logger.info("File Info {}", file);

			}
		}

		/*
		 * 2. Send the Files to their TargetPath using targetProtocol and targetHostName
		 */

		String targetProtocol = "";
		String targetUsername = "";
		String targetKeyString = "";
		String targetPassword = "";
		String targetHostName = "";
		String targetPath = "";

		fileConfig.setTargetServerProtocol(targetProtocol);
		fileConfig.setTargetServerUsername(targetUsername);
		fileConfig.setTargetServerKeyString(targetKeyString);
		fileConfig.setTargetServerPassword(targetPassword);
		fileConfig.setTargetServerHostName(targetHostName);
		fileConfig.setTargetFilePath(targetPath);
		
		boolean sentStatus = false;

		if (sourceFilesList != null) {
			if (!sourceFilesList.isEmpty()) {

				sentStatus = sendSourceFiles(fileConfig, sourceFilesList);

				/*
				 * 3. Delete files in the Lambda's /tmp/ folder to free up Lamda's memory and
				 * Close the server
				 */

				cleanTempFolder(AppConstants.TEMP_FOLDER_PATH);

				disconnectSessions(sourceSftpChannel, targetSftpChannel);

				for (FileInfo file : sourceFilesList)
					logger.info("File Info {}", file);

			} else
				logger.error("Source File List is Empty");
		} else
			logger.error("Source File is Null");
		
		return sentStatus;

	}

	private void disconnectSessions(ChannelSftp sourceSftpChannel, ChannelSftp targetSftpChannel) {
		if (sourceSftpChannel != null) {
			try {
				sourceSftpChannel.disconnect();
				sourceSftpChannel.getSession().disconnect();

			} catch (JSchException e) {

				logger.error("Error occurred while disconnecting sourceSftpChannel {}", e.getMessage());

				e.printStackTrace();
			}
		}
		if (targetSftpChannel != null) {
			try {

				targetSftpChannel.disconnect();
				targetSftpChannel.getSession().disconnect();

			} catch (JSchException e) {

				logger.error("Error occurred while disconnecting targetSftpChannel {}", e.getMessage());

				e.printStackTrace();
			}
		}

	}

	private List<FileInfo> getSourceFiles(FileConfig fileConfig) {

		logger.info("Inside getSourceFiles method...");

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceUsername = fileConfig.getSourceServerUsername();
		String sourceKeyString = fileConfig.getSourceServerKeyString();
		String sourcePassword = fileConfig.getSourceServerPassword();
		String sourceHostName = fileConfig.getSourceServerHostName();
		String sourcePath = fileConfig.getSourceFilePath();
		String fileExtension = fileConfig.getFileExtension();

		List<FileInfo> sourceFilesList = null;

		try {

			if (sourceProtocol != null && sourceHostName != null && sourcePath != null) {

				if (sourceKeyString != null)
					sourceSftpChannel = sftpConfig.getSSHConnectionUsingKey(new JSch(), sourceUsername, sourceKeyString,
							sourceHostName);

				if (sourcePassword != null)
					sourceSftpChannel = sftpConfig.getSSHConnectionUsingPassword(new JSch(), sourceUsername,
							sourcePassword, sourceHostName);

				sourceFilesList = sftpOperations.getSftpSourceFileList(sourceSftpChannel, sourcePath, fileExtension);

			}

			return sourceFilesList;

		} catch (Exception exception) {
			logger.error("Error occurred while connecting to {} {}", sourceProtocol, exception.getMessage());
			return Collections.emptyList();
		}

	}

	private boolean sendSourceFiles(FileConfig fileConfig, List<FileInfo> sourceFilesList) {

		logger.info("Inside sendSourceFiles method...");

		String targetProtocol = fileConfig.getTargetServerProtocol();
		String targetUsername = fileConfig.getTargetServerUsername();
		String targetKeyString = fileConfig.getTargetServerKeyString();
		String targetPassword = fileConfig.getTargetServerPassword();
		String targetHostName = fileConfig.getTargetServerHostName();
		String targetPath = fileConfig.getTargetFilePath();

		boolean sentStatus = false;

		try {

			if (targetKeyString != null)
				targetSftpChannel = sftpConfig.getSSHConnectionUsingKey(new JSch(), targetUsername, targetKeyString,
						targetHostName);

			if (targetPassword != null)
				sourceSftpChannel = sftpConfig.getSSHConnectionUsingPassword(new JSch(), targetUsername, targetPassword,
						targetHostName);

			sentStatus = sftpOperations.sendToSftp(targetSftpChannel, sourceFilesList, targetPath);

			return sentStatus;

		} catch (Exception exception) {

			logger.error("Error occurred while connecting to {} {}", targetProtocol, exception.getMessage());
			return false;

		}

	}

	private void cleanTempFolder(String tempFolderPath) {

		File tempDirectory = new File(tempFolderPath);
		try {
			FileUtils.cleanDirectory(tempDirectory);
			logger.info("All the files inside Lambda's temp folder has been deleted Successfully");
		} catch (IOException e) {
			logger.error("Error occurred while deleting the content in Temp Folder.");
		}

	}

}
