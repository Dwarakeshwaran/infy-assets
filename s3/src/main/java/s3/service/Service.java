package s3.service;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;

import s3.config.S3Config;
import s3.model.FileConfig;
import s3.model.FileInfo;
import s3.utils.AppConstants;

public class Service {

	private static final Logger logger = LoggerFactory.getLogger(Service.class);

	private static S3Operations s3Operations = new S3Operations();

	private AmazonS3 s3Client = null;

	public boolean doSftpTransfer() {

		/*
		 * 1. Get Files from the sourceProtocol by using sourceBucketName and sourcePath
		 */

		List<FileInfo> sourceFilesList = null;
		FileConfig fileConfig = new FileConfig();

		String sourceProtocol = "";
		String sourceBucketName = "";
		String sourcePath = "";
		String fileExtension = "";

		fileConfig.setSourceServerProtocol(sourceProtocol);
		fileConfig.setSourceBucketName(sourceBucketName);
		fileConfig.setSourceFilePath(sourcePath);
		fileConfig.setFileExtension(fileExtension);

		sourceFilesList = getSourceFiles(fileConfig);

		if (sourceFilesList != null) {
			for (FileInfo file : sourceFilesList) {

				logger.info("File Info {}", file);

			}
		}

		/*
		 * 2. Send the Files to their TargetPath using targetProtocol and targetBucketName
		 */

		String targetProtocol = "";
		String targetBucketName = "";
		String targetPath = "";

		fileConfig.setTargetServerProtocol(targetProtocol);
		fileConfig.setTargetBucketName(targetBucketName);
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

				for (FileInfo file : sourceFilesList)
					logger.info("File Info {}", file);

			} else
				logger.error("Source File List is Empty");
		} else
			logger.error("Source File is Null");

		return sentStatus;

	}

	private List<FileInfo> getSourceFiles(FileConfig fileConfig) {

		logger.info("Inside getSourceFiles method...");

		String sourceProtocol = fileConfig.getSourceServerProtocol();
		String sourceBucketName = fileConfig.getSourceBucketName();
		String sourcePath = fileConfig.getSourceFilePath();
		String fileExtension = fileConfig.getFileExtension();

		List<FileInfo> sourceFilesList = null;

		try {

			if (sourceProtocol != null && sourceBucketName != null && sourcePath != null) {

				s3Client = new S3Config().getS3Config();

				sourceFilesList = s3Operations.getS3SourceFileList(s3Client, sourceBucketName, sourcePath, fileExtension);
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
		String targetBucketName = fileConfig.getTargetBucketName();
		String targetPath = fileConfig.getTargetFilePath();

		boolean sentStatus = false;

		try {

			s3Client = new S3Config().getS3Config();
			sentStatus = s3Operations.sendToS3(sourceFilesList, s3Client, targetBucketName, targetPath);

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
