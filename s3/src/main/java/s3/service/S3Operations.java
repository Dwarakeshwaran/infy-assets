package s3.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import s3.model.FileInfo;
import s3.utils.AppConstants;

public class S3Operations {

	private static final Logger logger = LoggerFactory.getLogger(S3Operations.class);

	public List<FileInfo> getS3SourceFileList(AmazonS3 s3Client, String s3BucketName, String s3Path,
			String fileExtension) throws IOException {

		logger.debug("Inside getS3SourceFileList method {} {} {} {}", s3Client, s3BucketName, s3Path, fileExtension);

		List<S3ObjectSummary> s3Objects = null;
		String key = null;
		String fileName = null;
		GetObjectRequest getObjectRequest = null;
		InputStream s3ObjectStream = null;
		File s3File = null;

		List<FileInfo> fileList = new ArrayList<>();

		s3Objects = s3Client.listObjects(s3BucketName, s3Path).getObjectSummaries();
		s3Objects.sort((a, b) -> a.getLastModified().compareTo(b.getLastModified()));

		for (S3ObjectSummary s3Object : s3Objects) {

			FileInfo fileInfo = new FileInfo();

			/*
			 * 1. Get all the file info from sourceBucketName and sourcePath and store it in
			 * the FileInfo class List.
			 */

			try {

				key = s3Object.getKey();
				fileName = getFileName(key, fileExtension);

				fileInfo.setFileName(fileName);
				fileInfo.setProcessingStartTimestamp(new Timestamp(System.currentTimeMillis()));
				fileInfo.setModifiedDate(s3Object.getLastModified());

			} catch (Exception e) {
				logger.error("Error occurred while getting File Name from S3 Bucket {}", e.getMessage());
				key = null;
			}

			/* 2. Download the S3 Object (In InputStream format) from BucketName and Key */

			if (fileName != null) {
				try {

					getObjectRequest = new GetObjectRequest(s3BucketName, key);
					s3ObjectStream = s3Client.getObject(getObjectRequest).getObjectContent();

				} catch (Exception e) {
					logger.error("Error occurred while downloading the file from S3 Bucket {}", e.getMessage());
					s3ObjectStream = null;
				}
			}

			/*
			 * 3. Copy the contents of the Input Stream to a file and store it in /tmp/
			 * Folder
			 */

			if (s3ObjectStream != null) {

				logger.info("s3ObjectStream is successfully downloaded to Lambda's /tmp/ folder {}", s3ObjectStream);

				try {
					s3File = new File(AppConstants.TEMP_FOLDER_PATH + fileName);

					Files.copy(s3ObjectStream, s3File.toPath(), StandardCopyOption.REPLACE_EXISTING);
					IOUtils.closeQuietly(s3ObjectStream);

					fileInfo.setFile(s3File);
					logger.info("File Info {}", fileInfo);

					fileList.add(fileInfo);

				} catch (Exception e) {
					logger.error("Error occurred while writing the stream data to the file in temp folder {}",
							e.getMessage());
				}

			} else
				logger.info("s3ObjectStream is null File path is {}", key);

		}

		return fileList;

	}

	private String getFileName(String key, String fileExtension) {

		String[] pathNames = key.split("/");

		String fileName = pathNames[pathNames.length - 1];
		if (key.contains(fileExtension)) {
			logger.info("File Name {}", fileName);
			return fileName;
		} else
			return null;

	}

	public boolean sendToS3(List<FileInfo> s3FilesList, AmazonS3 s3Client, String s3BucketName, String s3Path) {

		logger.debug("Inside sendToS3 method ...");

		int flag = 0;

		String key = null;

		for (FileInfo fileInfo : s3FilesList) {

			try {

				key = s3Path + fileInfo.getFileName();

				PutObjectRequest request = new PutObjectRequest(s3BucketName, key, fileInfo.getFile());
				s3Client.putObject(request);

				fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
				fileInfo.setFileTransferStatus(AppConstants.TRANSFER_SUCCESS);

				logger.info("Successfully Uploaded file {} to AWS S3 Bucket {}", key, s3BucketName);

			} catch (Exception e) {
				flag = 1;
				fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
				fileInfo.setFileTransferStatus(AppConstants.TRANSFER_FAILED);
				logger.error("Error while uploading file {} to AWS S3 Bucket {} {}", key, s3BucketName, e);

			}

		}

		return flag == 0;

	}

}
