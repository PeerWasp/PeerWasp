package org.peerbox.share;

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
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.PermissionType;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.presenter.validation.TextFieldValidator;
import org.peerbox.presenter.validation.ValidationUtils;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.utils.IconUtils;

import com.google.inject.Inject;


public final class ShareFolderController implements Initializable {

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
	private final StringProperty folderToShareProperty;

	private final BooleanProperty busyProperty;
	private final StringProperty statusProperty;

	private IFileManager fileManager;
	private IUserManager userManager;

	@Inject
	public ShareFolderController(IFileManager fileManager, IUserManager userManager) {
		this.statusProperty = new SimpleStringProperty();
		this.busyProperty = new SimpleBooleanProperty(false);
		this.fileManager = fileManager;
		this.userManager = userManager;

		this.folderToShareProperty = new SimpleStringProperty();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
		initializeStatusBar();

		grdForm.disableProperty().bind(busyProperty);
		txtFolderPath.textProperty().bind(folderToShareProperty);
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

				ProcessHandle<Void> handle = fileManager.share(folderToShare, user, permission);
				handle.getProcess().attachListener(new ShareProcessListener());
				handle.executeAsync();
			} catch (IllegalArgumentException | NoSessionException
					| NoPeerConnectionException | InvalidProcessStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ProcessExecutionException e) {
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

	public void setFolderToShare(final Path path) {
		folderToShare = path;
		folderToShareProperty.set(path.toString());
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

			Alert dlg = new Alert(AlertType.INFORMATION);
			IconUtils.decorateDialogWithIcon(dlg);
			dlg.setTitle("Folder Sharing");
			dlg.setHeaderText("Folder sharing finished");
			dlg.setContentText("The user is granted access to the folder.");
			dlg.showAndWait();
			getStage().close();
		});
	}

	public void onFolderShareFailed(String hint) {
		Platform.runLater(() -> {
			setStatus("");
			setBusy(false);

			Alert dlg = new Alert(AlertType.ERROR);
			IconUtils.decorateDialogWithIcon(dlg);
			dlg.setTitle("Folder Sharing");
			dlg.setHeaderText("Folder sharing failed.");
			dlg.setContentText(hint);
			dlg.showAndWait();
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
		public void onExecuting(IProcessEventArgs args) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRollbacking(IProcessEventArgs args) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPaused(IProcessEventArgs args) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onExecutionSucceeded(IProcessEventArgs args) {
			onFolderShareSucceeded();
		}

		@Override
		public void onExecutionFailed(IProcessEventArgs args) {
			onFolderShareFailed("Sharing folder failed.");
		}

		@Override
		public void onRollbackSucceeded(IProcessEventArgs args) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onRollbackFailed(IProcessEventArgs args) {
			// TODO Auto-generated method stub

		}
	}

	public final class UsernameRegisteredValidator extends TextFieldValidator {

		private IUserManager userManager;

		public UsernameRegisteredValidator(TextField txtUsername, StringProperty errorProperty, IUserManager userManager) {
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

	public void cancel() {
		// TODO (AA)

	}

}