package org.peerbox.view.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import org.peerbox.view.IconHelper;

public class ErrorLabel extends Label {

	@FXML
	private ImageView icon;
	private DoubleProperty iconHeightProperty;

	public ErrorLabel() {
		icon = IconHelper.getErrorIcon();
		icon.setPreserveRatio(true);
		icon.setSmooth(true);
		icon.fitHeightProperty().bind(iconHeightProperty());
		setGraphic(icon);
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
