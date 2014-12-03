package org.peerbox.presenter;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import org.peerbox.interfaces.INavigatable;

public class MainController implements INavigatable {

	@FXML
	private Pane mainPane;
	
	//offset values are needed for calculating position of application
	private double xOffset = 0;
	private double yOffset = 0;

	/* (non-Javadoc)
	 * @see org.peerbox.presenter.INavigatable#setContent(javafx.scene.Node)
	 */
	@Override
	public void setContent(Node content) {
		mainPane.getChildren().clear();
		mainPane.getChildren().add(content);
		mainPane.requestLayout();
	}
	
//	public void closeApp(ActionEvent event){
//		System.out.println("Application closed.");
//		Platform.exit();
//        System.exit(0);
//	}
//	
//	public void minApp(ActionEvent event){
//		System.out.println("Application minimized.");
//		Stage stage = (Stage) mainPane.getScene().getWindow();
//		stage.setIconified(true);
//	}
//	
//	// provides ability to move application window wherever the user clicks & drags with the mouse
//	public void onMousePressed(MouseEvent event) {
//		xOffset = event.getSceneX();
//		yOffset = event.getSceneY();
//	}
//
//	// releases window at position when mouse click is released
//	public void onMouseDragged(MouseEvent event) {
//		Stage stage = (Stage) mainPane.getScene().getWindow();
//		stage.setX(event.getScreenX() - xOffset);
//		stage.setY(event.getScreenY() - yOffset);
//	}
}
