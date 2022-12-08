package utilities.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnzipDirectory {

	private static final Logger logger = LoggerFactory.getLogger(UnzipDirectory.class);

	public void unZipDirectory(File file) throws IOException {

		logger.debug("Inside unZipFile method ...");

		String tempLocation = "tmp/";

		// Uncompress the Zip file

		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(file))) {

			ZipEntry zipEntry = zis.getNextEntry();

			File directory = new File(tempLocation);

			if (!directory.exists()) {
				directory.mkdirs();
			}

			Path targetFolder = new File(tempLocation).toPath();

			while (zipEntry != null) {

				Path newUnzipPath = zipSlipVulnerabilityProtect(zipEntry, targetFolder);

				boolean isDirectory = false;

				// check for files or directory
				if (zipEntry.getName().endsWith("/")) {
					isDirectory = true;
				}

				if (isDirectory) {
					Files.createDirectories(newUnzipPath);
				} else {

					if (newUnzipPath.getParent() != null) {
						if (Files.notExists(newUnzipPath.getParent())) {

							Files.createDirectories(newUnzipPath.getParent());
						}
					}

					// copy files using nio
					Files.copy(zis, newUnzipPath, StandardCopyOption.REPLACE_EXISTING);
				}

				zipEntry = zis.getNextEntry();

			}

			zis.closeEntry();

		} catch (Exception e) {

			logger.error("Error Occurred while uncompressing the ZIP File {}", e.getMessage());

		}

	}

	public static Path zipSlipVulnerabilityProtect(ZipEntry zipEntry, Path targetDir) throws IOException {

		Path dirResolved = targetDir.resolve(zipEntry.getName());

		// normalize the path on target directory or else throw exception
		Path normalizePath = dirResolved.normalize();
		if (!normalizePath.startsWith(targetDir)) {
			throw new IOException("Invalid zip: " + zipEntry.getName());
		}

		return normalizePath;
	}
}
