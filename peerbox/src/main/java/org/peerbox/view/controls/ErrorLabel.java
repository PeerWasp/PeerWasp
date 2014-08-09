package org.peerbox.view.controls;

import java.io.IOException;
import java.net.URL;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ErrorLabel extends HBox {

	@FXML
	private ImageView imgView;
	private DoubleProperty iconHeightProperty;
	@FXML
	private Label lblMessage;

	public ErrorLabel() {
		try {
			URL icon = getClass().getResource("/org/peerbox/view/controls/error_label.fxml");
			FXMLLoader fxmlLoader = new FXMLLoader(icon);
			fxmlLoader.setRoot(this);
			fxmlLoader.setController(this);
			fxmlLoader.load();
			imgView.fitHeightProperty().bind(iconHeightProperty());
			visibleProperty().bind(lblMessage.textProperty().isNotEmpty());
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
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

	public String getText() {
		return textProperty().get();
	}

	public void setText(String value) {
		textProperty().set(value);
	}

	public StringProperty textProperty() {
		return lblMessage.textProperty();
	}
}
