package org.peerbox.utils;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

public class ExecuteProcessUtilsTest {

	/**
	 * Create random folder with random file in it.
	 * Execute dir command which should list the file in the folder.
	 *
	 * @throws IOException
	 */
	@Test
	public void testExecuteCommand() throws IOException {
		// create folder
		final String folderName = RandomStringUtils.randomAlphanumeric(16);
		Path tempFolder = Paths.get(FileUtils.getTempDirectoryPath(), folderName);
		if (!Files.exists(tempFolder)) {
			Files.createDirectory(tempFolder);
		}

		// create file in folder
		final String fileName = RandomStringUtils.randomAlphanumeric(16);
		Path tempFile = tempFolder.resolve(fileName);
		Files.createFile(tempFile);

		// execute dir of folder
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("dir", tempFolder.toString());

		StringBuilder output = new StringBuilder();
		ExecuteProcessUtils.executeCommand(builder, output);

		// expect filename in output
		assertTrue(output.toString().contains(fileName));

		// cleanup
		FileUtils.deleteDirectory(tempFolder.toFile());
	}

}
