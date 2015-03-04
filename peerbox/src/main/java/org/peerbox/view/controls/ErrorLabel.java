package org.peerbox.view.controls;

import javafx.scene.Node;
import javafx.scene.control.Label;

import org.peerbox.utils.IconUtils;

public class ErrorLabel extends Label {

	public ErrorLabel() {
		this("");
	}

	public ErrorLabel(String text) {
		super(text);
		initialize();
	}

	private static Node createErrorIcon() {
		return IconUtils.createErrorIcon();
	}

	private void initialize() {
		setWrapText(true);
		// error icon
		setGraphic(createErrorIcon());
		// only visible if text is set
		visibleProperty().bind(textProperty().isEmpty().not());
	}

}
