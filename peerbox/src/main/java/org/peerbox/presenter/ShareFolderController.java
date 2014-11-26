package org.peerbox.presenter;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import org.controlsfx.control.StatusBar;
import org.hive2hive.core.model.PermissionType;
import org.peerbox.presenter.validation.EmptyTextFieldValidator;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

public class ShareFolderController implements Initializable {
	
	@FXML
	private TextField txtFolderPath;
	@FXML
	private TextField txtUsername;
	@FXML
	private Label lblUsernameError;
	@FXML
	private GridPane grdForm;
	@FXML
	private RadioButton rbtnReadWrite;
	@FXML
	private StatusBar statusBar;
	
	private final StringProperty folderToShareProperty;
	private Path folderToShare;
	
	private EmptyTextFieldValidator usernameValidator;
	
	public ShareFolderController() {
		this.folderToShareProperty = new SimpleStringProperty();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
	}
	
	private void initializeValidations() {
		usernameValidator = new EmptyTextFieldValidator(txtUsername, true, ValidationResult.USERNAME_EMPTY);
		usernameValidator.setErrorProperty(lblUsernameError.textProperty());
	}

	private void resetForm() {
		uninstallValidationDecorations();
		txtUsername.clear();
		rbtnReadWrite.setSelected(false);
	}
	
	private void uninstallValidationDecorations() {
		usernameValidator.reset();
	}	

	public void shareAction(ActionEvent event) {
		
	}
	
	public void cancelAction(ActionEvent event) {
		
	}
	
	public void setFolderToShare(Path path) {
		folderToShare = path;
		if(path != null) {
			txtFolderPath.setText(folderToShare.toString());
		}
	}
	
	private PermissionType getPermissionType() {
		if(rbtnReadWrite.isSelected()) {
			return PermissionType.WRITE;
		}
		return PermissionType.READ;
	}

}