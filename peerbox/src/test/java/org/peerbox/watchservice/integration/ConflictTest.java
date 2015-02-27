package org.peerbox.watchservice.integration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.peerbox.watchservice.ActionExecutor;
import org.peerbox.watchservice.conflicthandling.ConflictHandler;

public class ConflictTest extends FileIntegrationTest{
	
	private String homeDir = System.getProperty("user.home");
	
	@Test
	public void localCreateRemoteCreateTest() throws IOException, InterruptedException{
		List<Path> paths = new ArrayList<Path>();
		paths.add(addSingleFile(false));
		paths.add(ConflictHandler.rename(paths.get(0)));
		//paths.add(clientRootPath.resolve(paths.get(0).getFileName()));
		Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		FileUtils.writeStringToFile(paths.get(1).toFile(), "_CLIENT1_SECOND");

		waitForExists(paths, WAIT_TIME_SHORT);
		assertCleanedUpState(2);
	}
	
	@Test
	public void localUpdateRemoteUpdateTest() throws IOException, InterruptedException {	
		Path client0File = addSingleFile();
		Path client1File = clientRootPath.resolve(client0File.getFileName());
		assertCleanedUpState(1);
		
		updateSingleFile(client0File, false);
		Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		updateSingleFile(client1File, false);
		Thread.sleep(10000);
		assertCleanedUpState(2);
	}
	
	@Test
	public void remoteCreateLocalCreateTest() throws IOException, InterruptedException{	
		List<Path> paths = new ArrayList<Path>();
		paths.add(addSingleFile(false));
		paths.add(ConflictHandler.rename(paths.get(0)));
		//paths.add(clientRootPath.resolve(paths.get(0).getFileName()));
		Thread.sleep(3 * ActionExecutor.ACTION_WAIT_TIME_MS / 2);
		FileUtils.writeStringToFile(paths.get(1).toFile(), "_CLIENT1_SECOND");

		waitForExists(paths, WAIT_TIME_SHORT);
		assertCleanedUpState(2);
	}
	
	@Test
	public void remoteUpdateLocalUpdateTest() throws IOException, InterruptedException {
		Path client0File = addSingleFile();
		Path client1File = clientRootPath.resolve(client0File.getFileName());
		assertCleanedUpState(1);
		
		updateSingleFile(client0File, false);
		Thread.sleep(ActionExecutor.ACTION_WAIT_TIME_MS * 3 / 2);
		updateSingleFile(client1File, false);
		Thread.sleep(10000);
		assertCleanedUpState(2);
	}
	
	//TODO how can we generate this case? If the state machine works as intended, it never happens
	@Test @Ignore
	public void remoteUpdateLocalCreateTest(){
		
	}
	
	//TODO how can we generate this case? If the state machine works as intended, it never happens
	@Test @Ignore
	public void localUpdateRemoteCreateTest(){
		
	}
	
	//TODO how can we generate this case? If the state machine works as intended, it never happens
	@Test @Ignore
	public void remoteCreateLocalUpdateTest(){
		
	}
	
	//TODO how can we generate this case? If the state machine works as intended, it never happens
	@Test @Ignore
	public void localCreateRemoteUpdateTest() {

	}
	
	// move detection seems not to work yet (no synchronization)
	@Test @Ignore
	public void LocalMove_RemoteUpdateTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		
		Path pathClient0 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathClient0_subDir = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "subDir");
		
		FileUtils.writeStringToFile(pathClient0.toFile(), "CLIENT0_FIRST");
		Thread.sleep(5000); // wait until file is synched
		FileUtils.moveFileToDirectory(pathClient0.toFile(), pathClient0_subDir.toFile(), true);
		Thread.sleep(3000);
		//FileUtils.writeStringToFile(pathClient1.toFile(), "ABCDEFGHIJ0123456789");
		Thread.sleep(10000);
		assertSyncClientPaths();

	}
	
	@Test
	public void LocalUpdate_RemoteUpdateTest() throws IOException, InterruptedException {
		
		String homeDir = System.getProperty("user.home");
		
		Path pathClient0 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-0" + File.separator + "test.txt");
		Path pathClient1 = Paths.get(homeDir + File.separator + "PeerBox_Test" + File.separator + "client-1" + File.separator + "test.txt");
		
		FileUtils.writeStringToFile(pathClient0.toFile(), "CLIENT0_FIRST");
		Thread.sleep(5000);
		FileUtils.writeStringToFile(pathClient0.toFile(), "_CLIENT0_SECOND");
		Thread.sleep(2000);
		FileUtils.writeStringToFile(pathClient1.toFile(), "_CLIENT1_THIRD");
		Thread.sleep(10000);
		assertSyncClientPaths();
	}
}
