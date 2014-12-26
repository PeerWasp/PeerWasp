package org.peerbox.presenter.settings.synchronization;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Stream;

import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.core.processes.files.list.FileNode;
import org.hive2hive.processframework.ProcessComponent;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.FileManager;
import org.peerbox.UserConfig;
import org.peerbox.model.H2HManager;
import org.peerbox.watchservice.FileComponent;
import org.peerbox.watchservice.FileEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;

public class Synchronization implements Initializable {
    
	private static final Logger logger = LoggerFactory.getLogger(Synchronization.class);
	
	@FXML
	private TreeView<PathItem> fileTreeView;
	
	private UserConfig userConfig;
	private FileEventManager eventManager;
	private FileManager fileManager;
//	private H2HManager manager;
	
	private Set<Path> toSynchronize = new HashSet<Path>();
	private Set<Path> toDesynchronize = new HashSet<Path>();
	
	public Set<Path> getToSynchronize(){
		return toSynchronize;
	}
	
	public Set<Path> getToDesynchronize(){
		return toDesynchronize;
	}
	
	private Vector<FileComponent> toSync = new Vector<FileComponent>();
	
	@Inject
	public Synchronization(FileManager fileManager, FileEventManager eventManager, UserConfig userConfig) {
		this.userConfig = userConfig;
		this.eventManager = eventManager;
		this.fileManager = fileManager;
		
	}
	
	public FileEventManager getFileEventManager(){
		return eventManager;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
//		if(fileManager == null){
//			logger.debug("FileManager not yet initialized.");
//			return;
//		}
		
//		getFileListFromNetwork();
		//(FileNode parent, File file, String path, byte[] md5, Set<UserPermission> userPermissions) {
    FileNode root = new FileNode(null, userConfig.getRootPath().toFile(), userConfig.getRootPath().toString(), null, null);
	        PathTreeItem rootItem = 
	            new PathTreeItem(root, this, false);
	        rootItem.setExpanded(true);                  
	      
	        fileTreeView.setEditable(false);
	        
	        fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());    
	        

	        Path rootFolder = userConfig.getRootPath();
	        
	        try(Stream<Path> dirs = Files.list(rootFolder)){
	        	Iterator<Path> iter = dirs.iterator();
	        	while(iter.hasNext()){
	        		Path folder = iter.next();
	        		FileNode item = new FileNode(null, folder.toFile(), userConfig.getRootPath().toString(), null, null);
	        		PathTreeItem checkBoxTreeItem = new PathTreeItem(item, this, false);
	        		rootItem.getChildren().add(checkBoxTreeItem);
	        	}

	        } catch (IOException ex){
	        	ex.printStackTrace();
	        }
	        
	                       
	        fileTreeView.setRoot(rootItem);
	        fileTreeView.setShowRoot(true);
	}




	private void getFileListFromNetwork() {
		try {
			FileNode filesFromNetwork = fileManager.listFiles();
			listFiles(filesFromNetwork);
		} catch (IllegalArgumentException | NoSessionException | NoPeerConnectionException
				| InvalidProcessStateException | ProcessExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void listFiles(FileNode fileNode){
		boolean isSynched = eventManager.getSynchronizedFiles().contains(fileNode.getFile().toPath());
        PathTreeItem rootItem = new PathTreeItem(fileNode, this, isSynched);
        rootItem.setExpanded(true);     
		for(FileNode child : fileNode.getChildren()){
			System.out.println("File " + child.getFile());
		}
		for(FileNode child : fileNode.getChildren()){
			listFiles(child);
		}
	}

	public void acceptSyncAction(ActionEvent event) {
//		for(Path path : toSynchronize){
//			eventManager.onFileSynchonized(path, isFolder)
//		}
		for(Path path: toDesynchronize){
			eventManager.onFileDesynchronized(path);
		}
	}
//	
//	public void cancelAction(ActionEvent event) {
//		//TODO write button handler
//	}
	
//	private final class FileListDownloadListener implements IProcessComponentListener{
//		
//		@Override
//		public void onExecuting(IProcessEventArgs args) {
//			logger.debug("Receiving file list - STARTED");
//		}
//
//		@Override
//		public void onRollbacking(IProcessEventArgs args) {
//			logger.debug("Receiving file list - ROLLBACKING");
//		}
//
//		@Override
//		public void onPaused(IProcessEventArgs args) {
//			logger.debug("Receiving file list - PAUSED");
//		}
//
//		@Override
//		public void onExecutionSucceeded(IProcessEventArgs args) {
//			logger.debug("Receiving file list - SUCCESSFUL");
//		}
//
//		@Override
//		public void onExecutionFailed(IProcessEventArgs args) {
//			logger.debug("Receiving file list - FAILED");
//		}
//
//		@Override
//		public void onRollbackSucceeded(IProcessEventArgs args) {
//			logger.debug("Receiving file list - ROLLBACK SUCCESSFUL");
//		}
//
//		@Override
//		public void onRollbackFailed(IProcessEventArgs args) {
//			logger.debug("Receiving file list - ROLLBACK FAILED");
//		}
//		
//	}
}