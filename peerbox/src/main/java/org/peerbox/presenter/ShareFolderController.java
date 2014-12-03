package org.peerbox.presenter;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import org.controlsfx.control.StatusBar;
import org.hive2hive.core.exceptions.IllegalFileLocation;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.FileManager;
import org.peerbox.model.UserManager;
import org.peerbox.presenter.validation.TextFieldValidator;
import org.peerbox.presenter.validation.ValidationUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;

import com.google.inject.Inject;


public class ShareFolderController implements Initializable {
	
	@FXML
	private AnchorPane pane;
	@FXML
	private GridPane grdForm;
	@FXML
	private TextField txtFolderPath;
	@FXML
	private TextField txtUsername;
	@FXML
	private Label lblUsernameError;
	@FXML
	private RadioButton rbtnReadWrite;
	@FXML
	private StatusBar statusBar;
	
	private UsernameRegisteredValidator usernameValidator;
	
	private Path folderToShare;
	
	private final BooleanProperty busyProperty;
	private final StringProperty statusProperty;
	
	private FileManager fileManager;
	private UserManager userManager;
	
	@Inject
	public ShareFolderController(FileManager fileManager, UserManager userManager) {
		this.statusProperty = new SimpleStringProperty();
		this.busyProperty = new SimpleBooleanProperty(false);
		this.fileManager = fileManager;
		this.userManager = userManager;
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
		initializeStatusBar();
		
		grdForm.disableProperty().bind(busyProperty);
	}
	
	private void initializeValidations() {
		usernameValidator = new UsernameRegisteredValidator(txtUsername, lblUsernameError.textProperty(), userManager);
	}
	
	private void initializeStatusBar() {
		statusBar = new StatusBar();
		pane.getChildren().add(statusBar);
		AnchorPane.setBottomAnchor(statusBar, 0.0);
		AnchorPane.setLeftAnchor(statusBar, 0.0);
		AnchorPane.setRightAnchor(statusBar, 0.0);
		busyProperty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
					Boolean newValue) {
				if(newValue != null && newValue.booleanValue()) {
					statusBar.setProgress(-1);
				} else {
					statusBar.setProgress(0);
				}
			}
		});
		
		statusBar.textProperty().bind(statusProperty);
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
		
		if(validateAll()) {
			try {
				final String user = txtUsername.getText().trim();
				final PermissionType permission = getPermissionType();
				
				setStatus("Sharing folder...");
				setBusy(true);
				
				IProcessComponent process = fileManager.share(folderToShare.toFile(), user, permission);
				process.attachListener(new ShareProcessListener());
			} catch (IllegalFileLocation | IllegalArgumentException | NoSessionException
					| NoPeerConnectionException | InvalidProcessStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private boolean validateAll() {
		return usernameValidator.validate(true) == ValidationResult.OK;
	}

	public void cancelAction(ActionEvent event) {
		resetForm();
		getStage().close();
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
	
	public void onFolderShareSucceeded() {
		Platform.runLater(() -> {
			setStatus("");
			setBusy(false);
			
			Alert a = new Alert(AlertType.INFORMATION);
			a.setTitle("Folder Sharing");
			a.setHeaderText("Folder sharing finished");
			a.setContentText("The user is granted access to the folder.");
			a.showAndWait();
			getStage().close();
		});
	}

	public void onFolderShareFailed(String hint) {
		Platform.runLater(() -> {
			setStatus("");
			setBusy(false);
			
			Alert a = new Alert(AlertType.ERROR);
			a.setTitle("Folder Sharing");
			a.setHeaderText("Folder sharing failed.");
			a.setContentText(hint);
			a.showAndWait();
		});
	}
	
	private Stage getStage() {
		return (Stage)pane.getScene().getWindow();
	}
	
	public String getStatus() {
		return statusProperty.get();
	}

	public void setStatus(String status) {
		this.statusProperty.set(status);
	}

	public StringProperty statusProperty() {
		return statusProperty;
	}
	
	public Boolean getBusy() {
		return busyProperty.get();
	}

	public void setBusy(Boolean busy) {
		this.busyProperty.set(busy);
	}

	public BooleanProperty busyProperty() {
		return busyProperty;
	}
	
	private class ShareProcessListener implements IProcessComponentListener {
		@Override
		public void onSucceeded() {
			onFolderShareSucceeded();
		}

		@Override
		public void onFailed(RollbackReason reason) {
			onFolderShareFailed(reason.getHint());
		}
	}
	
	public final class UsernameRegisteredValidator extends TextFieldValidator {

		private UserManager userManager;

		public UsernameRegisteredValidator(TextField txtUsername, StringProperty errorProperty, UserManager userManager) {
			super(txtUsername, errorProperty, true);
			this.userManager = userManager;
		}
		
		@Override
		public ValidationResult validate(final String username) {
			return validate(username, false);
		}
		
		public ValidationResult validate(boolean checkIfRegistered) {
			return validate(validateTxtField.getText(), checkIfRegistered);
		}

		public ValidationResult validate(final String username, boolean checkIfRegistered) {
			try {
				
				if(username == null) {
					return ValidationResult.ERROR;
				}

				final String usernameTr = username.trim();
				ValidationResult res = ValidationUtils.validateUserExists(usernameTr, checkIfRegistered, userManager);

				if (res.isError()) {
					setErrorMessage(res.getMessage());
					decorateError();
				} else {
					clearErrorMessage();
					undecorateError();
				}
				
				return res;

			} catch (NoPeerConnectionException e) {
				setErrorMessage("Network connection failed.");
			}

			return ValidationResult.ERROR;
		}

		public void reset() {
			undecorateError();
			clearErrorMessage();
		}
	}

}