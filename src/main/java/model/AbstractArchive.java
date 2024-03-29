package model;

import org.apache.commons.compress.archivers.*;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractArchive {

	public void compress(List<Path> files, List<ArchiveEntry> entries, ArchiveOutputStream archiveOutputStream) throws IOException {
		for (int index = 0; index < entries.size(); index++) {
			archiveOutputStream.putArchiveEntry(entries.get(index));
			archiveOutputStream.write(Files.readAllBytes(files.get(index)));
			archiveOutputStream.closeArchiveEntry();
		}
		archiveOutputStream.close();
	}

	public List<Path> decompress(ArchiveInputStream archiveInputStream, Path decompressFolder) throws IOException, ArchiveException {
		List<Path> decompressedFiles = new LinkedList<>();
		ArchiveEntry entry;
		while ((entry = archiveInputStream.getNextEntry()) != null) {
			Path outputFile = Paths.get(decompressFolder.toAbsolutePath().toString(), entry.getName());
			entry.getLastModifiedDate();
			if (entry.isDirectory()) {
				Files.createDirectory(outputFile);
				ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) entry;
				BasicFileAttributeView attributes = Files.getFileAttributeView(
						outputFile, BasicFileAttributeView.class);
				attributes.setTimes(zipArchiveEntry.getLastModifiedTime(),
						zipArchiveEntry.getLastAccessTime(), zipArchiveEntry.getCreationTime());
				System.out.println("Created directory: " + outputFile.toAbsolutePath().toString());
			} else {
				BufferedOutputStream outputStream = new BufferedOutputStream(
						Files.newOutputStream(outputFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND));
				byte[] buffer = new byte[8192];
				int len;
				while ((len = archiveInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, len);
				}
				outputStream.close();

				ZipArchiveEntry zipArchiveEntry = (ZipArchiveEntry) entry;
				BasicFileAttributeView attributes = Files.getFileAttributeView(
						outputFile, BasicFileAttributeView.class);
				attributes.setTimes(zipArchiveEntry.getLastModifiedTime(),
						zipArchiveEntry.getLastAccessTime(), zipArchiveEntry.getCreationTime());
				decompressedFiles.add(outputFile);
				System.out.println("Created file: " + outputFile.toAbsolutePath().toString());
			}
		}
		archiveInputStream.close();
		return decompressedFiles;
	}

	public ArchiveInputStream getArchiveInputStream(String archiverName, Path archive) throws IOException, ArchiveException {
		return new ArchiveStreamFactory()
				.createArchiveInputStream(archiverName, new BufferedInputStream(
						Files.newInputStream(archive)));
	}

	public ArchiveOutputStream getArchiveOutputStream(String archiverName, Path archive) throws IOException, ArchiveException {
		return new ArchiveStreamFactory().createArchiveOutputStream(archiverName, new BufferedOutputStream(
				Files.newOutputStream(archive, StandardOpenOption.CREATE, StandardOpenOption.APPEND)));
	}
}
