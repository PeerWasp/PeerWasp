package org.peerbox.presenter;

import org.peerbox.interfaces.INavigatable;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class MainController implements INavigatable {

	@FXML
	private Pane mainPane;

	/* (non-Javadoc)
	 * @see org.peerbox.presenter.INavigatable#setContent(javafx.scene.Node)
	 */
	@Override
	public void setContent(Node content) {
		mainPane.getChildren().clear();
		mainPane.getChildren().add(content);
		mainPane.requestLayout();
	}
	
	public void closeApp(){
		System.out.println("Application closed.");
		System.exit(0);
	}
	
	public void minApp(){
		System.out.println("Application minimized (not yet implemented).");
		//TODO
	}
}
