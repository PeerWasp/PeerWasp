package org.peerbox.presenter.settings.synchronization;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import org.peerbox.UserConfig;

import com.google.inject.Inject;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
 
public class Synchronization implements Initializable {
    
	@FXML
	private TreeView<PathItem> fileTreeView;
	
	private UserConfig userConfig;
	
	@Inject
	public Synchronization(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
//		 primaryStage.setTitle("Tree View Sample");        
	        PathTreeItem rootItem = 
	            new PathTreeItem(userConfig.getRootPath());
	        rootItem.setExpanded(true);                  
	      
	       // fileTreeView = new TreeView<PathItem>(rootItem);  
	        fileTreeView.setEditable(false);
	        
	        fileTreeView.setCellFactory(CheckBoxTreeCell.<PathItem>forTreeView());    
	        
	        Iterable<Path> rootDirectories=FileSystems.getDefault().getRootDirectories();
	        
	        System.out.println("START");
	        Path rootFolder = userConfig.getRootPath();
	        try(Stream<Path> dirs = Files.list(rootFolder)){
	        	Iterator<Path> iter = dirs.iterator();
	        	while(iter.hasNext()){
	        		Path folder = iter.next();
	        		System.out.println("Path: " + folder);
	        		PathTreeItem checkBoxTreeItem = new PathTreeItem(folder.getFileName());
	        		rootItem.getChildren().add(checkBoxTreeItem);
	        	}

	        } catch (IOException ex){
	        	ex.printStackTrace();
	        }
	        //rootItem.getChildren().setAll(children);
	        
	                       
	        fileTreeView.setRoot(rootItem);
	        fileTreeView.setShowRoot(true);
	 
	      //  StackPane root = new StackPane();
	      //  root.getChildren().add(fileTreeView);
	        //primaryStage.setScene(new Scene(root, 300, 250));
	        //primaryStage.show();
	        
//	        Files.newD
	}

//    @Override
//    public void start(Stage primaryStage) {
//        primaryStage.setTitle("Tree View Sample");        
//        
//        PathTreeItem rootItem = 
//            new PathTreeItem(Paths.get("A:/Dropbox/Dropbox"));
//        rootItem.setExpanded(true);                  
//      
//        final TreeView tree = new TreeView(rootItem);  
//        tree.setEditable(false);
//        
//        tree.setCellFactory(CheckBoxTreeCell.<String>forTreeView());    
//        
//        Iterable<Path> rootDirectories=FileSystems.getDefault().getRootDirectories();
//        
//        System.out.println("START");
//        Path rootFolder = Paths.get("A:/Dropbox/Dropbox");
//        try(Stream<Path> dirs = Files.list(rootFolder)){
//        	Iterator<Path> iter = dirs.iterator();
//        	while(iter.hasNext()){
//        		Path folder = iter.next();
//        		System.out.println("Path: " + folder);
//        		PathTreeItem checkBoxTreeItem = new PathTreeItem(folder.getFileName());
//        		rootItem.getChildren().add(checkBoxTreeItem);
//        	}
//
//        } catch (IOException ex){
//        	ex.printStackTrace();
//        }
//        //rootItem.getChildren().setAll(children);
//        
//                       
//        tree.setRoot(rootItem);
//        tree.setShowRoot(true);
// 
//        StackPane root = new StackPane();
//        root.getChildren().add(tree);
//        primaryStage.setScene(new Scene(root, 300, 250));
//        primaryStage.show();
//        
////        Files.newD
//    }


}