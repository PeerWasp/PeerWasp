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

import org.controlsfx.tools.Platform;
import org.hive2hive.core.api.interfaces.IFileManager;
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
import org.peerbox.IPeerboxFileManager;
import org.peerbox.IUserConfig;
import org.peerbox.UserConfig;
import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.presenter.settings.synchronization.DummyFileEventManager;
import org.peerbox.presenter.settings.synchronization.DummyFileManager;
import org.peerbox.presenter.settings.synchronization.DummyUserConfig;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.composite.FileComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Stage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Window;

public class Synchronization implements Initializable {
    
	private static final Logger logger = LoggerFactory.getLogger(Synchronization.class);
	private Set<Path> synchronizedFiles;
	
	@FXML
	private TreeView<PathItem> fileTreeView;
	@FXML
	private Button okButton;
	@FXML
	private Button cancelButton;
	
	private IUserConfig userConfig;
	private IFileEventManager eventManager;
	private IPeerboxFileManager fileManager;
	
	private Set<FileNode> toSynchronize = new HashSet<FileNode>();
	private Set<FileNode> toDesynchronize = new HashSet<FileNode>();
	
	public Set<FileNode> getToSynchronize(){
		return toSynchronize;
	}
	
	public Set<FileNode> getToDesynchronize(){
		return toDesynchronize;
	}
	
	private Vector<FileComponent> toSync = new Vector<FileComponent>();
	
	@Inject
	public Synchronization(FileManager fileManager, FileEventManager eventManager, UserConfig userConfig) {

		this.userConfig = userConfig;
		this.eventManager = eventManager;
		this.fileManager = fileManager;

	}
	
	public IFileEventManager getFileEventManager(){
		return eventManager;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		//showDummyData();		
		logger.debug("Initialize Synchronization!");
		synchronizedFiles = eventManager.getFileTree().getSynchronizedPathsAsSet();
		createTreeWithFilesFromNetwork();
	}




	private void createTreeWithFilesFromNetwork() {
		try {
			FileNode filesFromNetwork = fileManager.listFiles();
			listFiles(filesFromNetwork);
		} catch (IllegalArgumentException | NoSessionException | NoPeerConnectionException
				| InvalidProcessStateException | ProcessExecutionException e) {
			e.printStackTrace();
		}
	}
	
	private void listFiles(FileNode fileNode){
		boolean isSynched = synchronizedFiles.contains(fileNode.getFile().toPath());
		logger.debug("File {} is selected: {}", fileNode.getFile().toPath(), isSynched);
		
		
		PathTreeItem dummyRoot = new PathTreeItem(fileNode, this, false, true);
//	    TreeView<String> tree = new TreeView<>(dummyRoot);
		fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());    
	    fileTreeView.setRoot(dummyRoot);
        fileTreeView.setEditable(false);
        fileTreeView.setShowRoot(false);
		
        for(FileNode topLevelNode : fileNode.getChildren()){
			PathTreeItem rootItem = new PathTreeItem(topLevelNode, this, isSynched);
			dummyRoot.getChildren().add(rootItem);
		}

        for(FileNode child : fileNode.getChildren()){
			System.out.println("File " + child.getFile());
		}
	}
	
	private void listFilesRec(FileNode fileNode){
		boolean isSynched = synchronizedFiles.contains(fileNode.getFile().toPath());
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
		
		for(FileNode node : toSynchronize){
			if(!synchronizedFiles.contains(node.getFile().toPath()))
				logger.debug("Synchronize file {}", node.getFile().toPath());
				eventManager.onFileSynchronized(node.getFile().toPath(), node.isFolder());
		}
		for(FileNode node: toDesynchronize){
			logger.debug("Desynchronize file {}", node.getFile().toPath());
			eventManager.onFileDesynchronized(node.getFile().toPath());
		}
		
		if(event.getTarget() != null && event.getTarget() instanceof Button){
			Button okButton = (Button)event.getTarget();
			Window window = okButton.getScene().getWindow();
			window.hide();
		}
		
	}
	
	@FXML
	public void cancelAction(ActionEvent event) {
		if(event.getTarget() != null && event.getTarget() instanceof Button){
			Button cancelButton = (Button)event.getTarget();
			Window window = cancelButton.getScene().getWindow();
			window.hide();
		}
	}
	
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
	
	private void showDummyData(){
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
}