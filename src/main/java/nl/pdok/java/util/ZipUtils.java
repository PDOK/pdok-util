package nl.pdok.java.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author kroonr
 */
public class ZipUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtils.class);

	public static interface ZipEntryProcessor {
		public void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException;

		public void afterProcess();
	}

	public static class ExtractProcessor implements ZipEntryProcessor {
		private File targetDir;

		public ExtractProcessor(File targetDir) {
			this.targetDir = targetDir;
		}

		@Override
		public void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
			if (!zipEntry.isDirectory()) {

				String directory = getDirectory(zipEntry);
				String targetName = getFilename(zipEntry);

				File fileDir = new File(targetDir, directory);
				if (!fileDir.exists()) {
					fileDir.mkdirs();
				}

				File targetFile = new File(fileDir, targetName);

				unzipEntryToFile(zipFile, zipEntry, targetFile);
			}
		}

		@Override
		public void afterProcess() {
			// nothing
		}
	}

	public static class FlattenProcessor implements ZipEntryProcessor {

		private File targetDir;

		public FlattenProcessor(File targetDir) {
			this.targetDir = targetDir;
		}

		@Override
		public void process(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
			if (!zipEntry.isDirectory()) {
				// flatten filename (no dirs)
				String targetName = getFilename(zipEntry);

				File targetFile = new File(targetDir, targetName);

				unzipEntryToFile(zipFile, zipEntry, targetFile);
			}
		}

		@Override
		public void afterProcess() {
			// nothing
		}

	}

	public static String getDirectory(ZipEntry entry) {
		String[] pathParts = FilenameUtils.separatorsToUnix(entry.getName()).split("/");
		return StringUtils.join(Arrays.copyOf(pathParts, pathParts.length - 1), "/");
	}

	public static String getFilename(ZipEntry entry) {
		String[] pathParts = FilenameUtils.separatorsToUnix(entry.getName()).split("/");
		String filename = pathParts[pathParts.length - 1];

		return filename;
	}

	public static void unzip(InputStream zipFile, File targetDir) throws IOException {
		ZipEntryProcessor processor = new ExtractProcessor(targetDir);
		processZipStream(zipFile, processor);
	}

	public static void flattenUnzip(InputStream zipFile, File targetDir) throws IOException {

		ZipEntryProcessor processor = new FlattenProcessor(targetDir);
		processZipStream(zipFile, processor);
	}

	public static void processZipStream(InputStream zipInputStream, ZipEntryProcessor processor) throws IOException {
		File tempFile = File.createTempFile("ZipUtils", "zip");
		tempFile.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(tempFile)) {
			IOUtils.copy(zipInputStream, out);
			LOGGER.debug("Gebruikt tijdelijk bestand \"{}\" om een ZIP stream in te lezen", tempFile.getName());
		}

		processFileWithZipContents(tempFile,processor);
		
		LOGGER.debug("Zip-stream volledige ingelezen. Tijdelijk bestand \"{}\" wordt verwijderd", tempFile.getName());
		tempFile.delete();
	}
	
	public static void processFileWithZipContents(File file, ZipEntryProcessor processor) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			processor.process(zipFile, entry);
		}
		zipFile.close();
		processor.afterProcess();
	}

	public static void unzipEntry(ZipFile zipFile, ZipEntry zipEntry, OutputStream out) throws IOException {
		InputStream in = zipFile.getInputStream(zipEntry);

		IOUtils.copy(in, out);
		IOUtils.closeQuietly(in);

		out.close();

		LOGGER.debug("Bestand {} ingelezen uit zip-bestand. Grootte: {}", zipEntry.getName(),
				FileUtils.byteCountToDisplaySize(zipEntry.getSize()));
	}

	public static void unzipEntryToFile(ZipFile zipFile, ZipEntry zipEntry, File target) throws IOException {

		try (FileOutputStream newFileStream = new FileOutputStream(target)) {
			unzipEntry(zipFile, zipEntry, newFileStream);
		}
	}
        
        public final static byte[] MAGIC = { 'P', 'K', 0x3, 0x4 };
        
        public static boolean isZip(File file) throws IOException {
            try (InputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[MAGIC.length];
                in.read(buffer);
                return ArrayUtils.isEquals(MAGIC, buffer);
            }
        }
        
        public static boolean isZip(BufferedInputStream in) throws IOException {
            
            if (!in.markSupported()) {
                throw new IllegalArgumentException("marksupported stream required");
            }
            
            in.mark(MAGIC.length);
            byte[] buffer = new byte[MAGIC.length];
            in.read(buffer);
            in.reset();
            return ArrayUtils.isEquals(MAGIC, buffer);
            
        }
}
