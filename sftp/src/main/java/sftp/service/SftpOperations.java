package sftp.service;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import sftp.model.FileInfo;
import sftp.utils.AppConstants;
import sftp.utils.ErrorLog;

public class SftpOperations {

	private static final Logger logger = LoggerFactory.getLogger(SftpOperations.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<FileInfo> getSftpSourceFileList(ChannelSftp sftpChannel, String sftpPath, String fileExtension) {

		logger.info("Inside getSftpSourceFileList method...");

		List<FileInfo> fileList = new ArrayList<>();

		if (sftpChannel != null) {

			/*
			 * 1. Download all the files from SFTP sftp Path to Lambda's /tmp/ Folder
			 */

			List<ChannelSftp.LsEntry> sftpFiles = new ArrayList();
			try {
				sftpChannel.connect();
				sftpFiles = sftpChannel.ls(sftpPath);

			} catch (SftpException | JSchException e) {

				String errorMessage = "Error While Downloading Folder to /tmp/ folder of Lambda \n \n ";
				ErrorLog errorLog = new ErrorLog(e);

				logger.error("{} Exception Message {}", errorMessage, e.getMessage());
				logger.error("Error Log {}", errorLog);

			}

			logger.info("Number of Files in {} is {}", sftpPath, sftpFiles.size());

			/*
			 * 2. Read the file names from the /tmp/ folder in Lambda and get all the files
			 * in a File Object and store it in FileInfo list object.
			 */

			for (ChannelSftp.LsEntry sftpFile : sftpFiles) {

				if (sftpFile.getFilename().contains(fileExtension)) {

					FileInfo fileInfo = new FileInfo();

					try {

						String fileName = sftpFile.getFilename();
						sftpChannel.get(sftpPath + fileName, AppConstants.TEMP_FOLDER_PATH);
						logger.info("File {}{} downloaded to {}", sftpPath, fileName, AppConstants.TEMP_FOLDER_PATH);

						fileInfo.setFileName(fileName);
						fileInfo.setProcessingStartTimestamp(new Timestamp(System.currentTimeMillis()));
						fileInfo.setModifiedDate(getDate(sftpFile.getAttrs().getMTime()));
						fileInfo.setFile(new File(AppConstants.TEMP_FOLDER_PATH + fileName));
						logger.info("File Info {}", fileInfo);

						fileList.add(fileInfo);

					} catch (SftpException ioException) {

						String errorMessage = "Error occurred while downloading files from SFTP Server to Lamda's /tmp/ folder \n \n ";
						ErrorLog errorLog = new ErrorLog(ioException);

						logger.error("{} Exception Message {}", errorMessage, ioException.getMessage());
						logger.error("Error Log {}", errorLog);
					}
				}

			}

		} else
			logger.error("sftpChannel in getSftpSourceFileList is null");

		return fileList;

	}

	public boolean sendToSftp(ChannelSftp sftpChannel, List<FileInfo> sftpFileList, String sftpPath) {

		logger.info("Inside sendToSftp method...");

		int flag = 0;

		if (sftpChannel != null) {

			try {
				sftpChannel.connect();
			} catch (JSchException e) {
				logger.error("Error While connecting to SFTP Channel");
				e.printStackTrace();
			}

			for (FileInfo fileInfo : sftpFileList) {
				try {

					sftpChannel.put(fileInfo.getFile().getPath(), sftpPath + fileInfo.getFileName());

					fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
					fileInfo.setFileTransferStatus(AppConstants.TRANSFER_SUCCESS);
					logger.info("{} Uploaded to {}", fileInfo.getFileName(), sftpPath);

				} catch (Exception e) {

					flag = 1;
					fileInfo.setProcessingEndTimestamp(new Timestamp(System.currentTimeMillis()));
					fileInfo.setFileTransferStatus(AppConstants.TRANSFER_FAILED);

					String errorMessage = "Error occurred while uploading file to SFTP Server \n \n ";
					ErrorLog errorLog = new ErrorLog(e);

					logger.error(errorMessage + "Exception Message {}", e.getMessage());

					logger.error("{} Exception Message {}", errorMessage, e.getMessage());
					logger.error("Error Log {}", errorLog);
				}
			}

		} else {
			flag = 1;
			logger.error("sftpChannel is null");
		}

		return flag == 0;
	}

	private Date getDate(Integer epochTime) {

		long milliseconds = Long.parseLong(epochTime.toString());
		Date date = new Date(milliseconds * 1000L);

		String pattern = "yyyy-MM-dd HH:mm:ss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

		try {
			return new SimpleDateFormat(pattern).parse(simpleDateFormat.format(date));
		} catch (ParseException e) {

			String errorMessage = "Error occurred while getting the Modified Date from the file \n \n ";
			ErrorLog errorLog = new ErrorLog(e);

			logger.error("{} Exception Message {}", errorMessage, e.getMessage());
			logger.error("Error Log {}", errorLog);

			return null;
		}

	}

}
