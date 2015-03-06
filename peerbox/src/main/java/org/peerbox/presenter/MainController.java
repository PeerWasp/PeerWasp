package org.peerbox.presenter;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class MainController implements INavigatable {

	@FXML
	private Pane mainPane;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.peerbox.presenter.INavigatable#setContent(javafx.scene.Node)
	 */
	@Override
	public void setContent(Node content) {
		mainPane.getChildren().clear();
		mainPane.getChildren().add(content);
		mainPane.requestLayout();
	}

}
