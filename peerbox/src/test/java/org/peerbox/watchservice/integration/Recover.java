package org.peerbox.watchservice.integration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.junit.Test;
import org.peerbox.client.ClientNode;
import org.peerbox.testutils.FileTestUtils;

public class Recover extends FileIntegrationTest {

	@Test
	public void recoverLastVersionTest() throws Exception {
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, 10);
		Path recoveredFile = srcFile.getParent().resolve("recovered.file");

		waitForExists(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		byte[] fileContent_v0 = Files.readAllBytes(srcFile);

		FileTestUtils.writeRandomData(srcFile, 10);
		waitForUpdate(srcFile, WAIT_TIME_SHORT);
		assertSyncClientPaths();
		byte[] fileContent_v1 = Files.readAllBytes(srcFile);

		System.out.println("Content: " + new String(fileContent_v0));
		System.out.println("Content: " + new String(fileContent_v1));
		ClientNode clientZero = getNetwork().getClientNode(0);
		clientZero.getFileManager().recover(srcFile, new IVersionSelector() {
			@Override
			public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
				return availableVersions.get(0);
			}
			@Override
			public String getRecoveredFileName(String fullName, String name, String extension) {
				return recoveredFile.getFileName().toString();
			}
		}).executeAsync();

		waitForExistsLocally(recoveredFile, WAIT_TIME_SHORT);
		waitForContentEquals(recoveredFile, fileContent_v0, 30);
		waitForExists(recoveredFile, WAIT_TIME_SHORT);
	}


}
