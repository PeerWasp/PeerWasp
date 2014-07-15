package org.peerbox.controller;


import org.hive2hive.core.api.interfaces.IH2HNode;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class MainController {

	@FXML
	private Pane mainPane;
	
	public void setContent(Node content) {
		mainPane.getChildren().clear();
		mainPane.getChildren().add(content);
	}
	
	
}
