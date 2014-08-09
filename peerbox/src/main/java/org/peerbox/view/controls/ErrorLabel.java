package org.peerbox.view.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ErrorLabel extends Label {

	@FXML
	private ImageView imgView;
	private DoubleProperty iconHeightProperty;

	public ErrorLabel() {
		imgView = new ImageView(new Image(ErrorLabel.class.getResourceAsStream("/images/error-icon.png")));
		imgView.setPreserveRatio(true);
		imgView.setSmooth(true);
		imgView.fitHeightProperty().bind(iconHeightProperty());
		setGraphic(imgView);
		visibleProperty().bind(textProperty().isNotEmpty());
		setWrapText(true);
	}

	public DoubleProperty iconHeightProperty() {
		if (iconHeightProperty == null) {
			iconHeightProperty = new SimpleDoubleProperty(this, "iconHeight", 0);
		}
		return iconHeightProperty;
	}

	public void setIconHeight(double height) {
		iconHeightProperty().set(height);
	}

	public double getIconHeight() {
		return iconHeightProperty().get();
	}

}
