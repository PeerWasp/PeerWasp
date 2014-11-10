package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.junit.Test;
import org.peerbox.client.ClientNode;
import org.peerbox.h2h.FileRecoveryEvent;
import org.peerbox.utils.FileTestUtils;
import org.peerbox.watchservice.PathUtils;

public class Recover extends FileIntegrationTest implements IProcessComponentListener{

	@Test
	public void recoverLastVersionTest() throws IOException{
		Path srcFile = FileTestUtils.createRandomFile(masterRootPath, 10);
		int recoveredVersion = 0;
		
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
		clientZero.getFileEventManager().onFileRecoveryRequest(new FileRecoveryEvent(srcFile.toFile(), 0));
		//clientZero.getRecoveryService().onLocalFileRecover(srcFile, 0);
		
		Path recoveredFile = PathUtils.getRecoveredFilePath(srcFile.toString(), recoveredVersion);
		waitForExistsLocally(recoveredFile, WAIT_TIME_SHORT);
		waitForContentEquals(recoveredFile, fileContent_v0, 30);
		waitForExists(recoveredFile, WAIT_TIME_SHORT);
	}

	@Override
	public void onSucceeded() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onFailed(RollbackReason reason) {
		// TODO Auto-generated method stub
		
	}
}
