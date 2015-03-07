package org.peerbox.presenter;

import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;
import org.peerbox.presenter.settings.synchronization.PathItem;
import org.peerbox.presenter.settings.synchronization.ProgressState;
import org.peerbox.presenter.settings.synchronization.SyncTreeItem;

public class SyncTreeItemTest {

	private SyncTreeItem file;
	private SyncTreeItem file2;
	private SyncTreeItem folder;
	@Test
	public void childrenInProgressOnDefaultTest(){
		file.setProgressState(ProgressState.IN_PROGRESS);
		assertTrue(folder.getProgressState() == ProgressState.IN_PROGRESS);
	}
	
	@Test
	public void childrenFailOnDefaultTest(){
		file.setProgressState(ProgressState.FAILED);
		assertTrue(folder.getProgressState() == ProgressState.DEFAULT);
	}
	
	@Test
	public void childrenSuccessOnDefaultTest(){
		file.setProgressState(ProgressState.SUCCESSFUL);
		assertTrue(folder.getProgressState() == ProgressState.SUCCESSFUL);
	}
	
	@Test
	public void childrenSyncOnSuccessTest(){
		folder.setProgressState(ProgressState.SUCCESSFUL);
		file.setProgressState(ProgressState.IN_PROGRESS);
		assertTrue(folder.getProgressState() == ProgressState.IN_PROGRESS);
	}
	
	@Test
	public void childrenFailOnSuccessTest(){
		folder.setProgressState(ProgressState.SUCCESSFUL);
		file.setProgressState(ProgressState.FAILED);
		assertTrue(folder.getProgressState() == ProgressState.SUCCESSFUL);
	}
	
	@Test
	public void childrenSuccessOnSuccessTest(){
		file.setProgressState(ProgressState.SUCCESSFUL);
		assertTrue(folder.getProgressState() == ProgressState.SUCCESSFUL);
	}
	
	@Test
	public void childrenSyncOnInProgressTest(){
		folder.setProgressState(ProgressState.IN_PROGRESS);
		file.setProgressState(ProgressState.IN_PROGRESS);
		assertTrue(folder.getProgressState() == ProgressState.IN_PROGRESS);
	}
	
	@Test
	public void childrenFailOnInProgressTest(){
		folder.setProgressState(ProgressState.IN_PROGRESS);
		file.setProgressState(ProgressState.FAILED);
		assertTrue(folder.getProgressState() == ProgressState.IN_PROGRESS);
	}
	
	@Test
	public void childrenSuccessOnInProgressTest(){
		folder.setProgressState(ProgressState.IN_PROGRESS);
		file.setProgressState(ProgressState.SUCCESSFUL);
		file2.setProgressState(ProgressState.SUCCESSFUL);
		assertTrue(folder.getProgressState() == ProgressState.SUCCESSFUL);
	}
	
	@Test
	public void twoChildrenSuccessOnInProgressTest(){
		folder.setProgressState(ProgressState.IN_PROGRESS);
		file2.setProgressState(ProgressState.IN_PROGRESS);
		file.setProgressState(ProgressState.SUCCESSFUL);
		assertTrue(folder.getProgressState() == ProgressState.IN_PROGRESS);
	}
	
	@Before
	public void setup(){
		PathItem filePathItem = new PathItem(Paths.get("/folder/file1"));
		PathItem file2PathItem = new PathItem(Paths.get("/folder/file2"));
		PathItem folderPathItem = new PathItem(Paths.get("/folder"));
		file = new SyncTreeItem(filePathItem);
		file2 = new SyncTreeItem(file2PathItem);
		folder = new SyncTreeItem(folderPathItem);
		
		file.addPropertyChangeListener("progressState", folder);
		file2.addPropertyChangeListener("progressState", folder);
		folder.getChildren().add(file);
		folder.getChildren().add(file2);
	}
}
