package org.peerbox.share;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import org.hive2hive.core.model.UserPermission;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.peerbox.ResultStatus;
import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.file.messages.LocalShareFolderMessage;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;
import org.peerbox.presenter.validation.UsernameRegisteredValidator;
import org.peerbox.presenter.validation.ValidationUtils.ValidationResult;
import org.peerbox.utils.IconUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Controller for the Share Folder View.
 * Responsible for configuring sharing and starting process.
 *
 * @author albrecht
 *
 */
public final class ShareFolderController implements Initializable {

	private final static Logger logger = LoggerFactory.getLogger(ShareFolderController.class);

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
	private StatusBar statusBar;

	/* checks whether user exists in the network */
	private UsernameRegisteredValidator usernameValidator;

	private Path folderToShare;
	private final StringProperty folderToShareProperty;

	private final BooleanProperty busyProperty;
	private final StringProperty statusProperty;

	private final IFileManager fileManager;
	private final IUserManager userManager;
	private final MessageBus messageBus;

	@Inject
	public ShareFolderController(IFileManager fileManager, IUserManager userManager, MessageBus messageBus) {
		this.statusProperty = new SimpleStringProperty();
		this.busyProperty = new SimpleBooleanProperty(false);
		this.folderToShareProperty = new SimpleStringProperty();

		this.fileManager = fileManager;
		this.userManager = userManager;
		this.messageBus = messageBus;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeValidations();
		initializeStatusBar();

		grdForm.disableProperty().bind(busyProperty);
		txtFolderPath.textProperty().bind(folderToShareProperty);
	}

	private void initializeValidations() {
		usernameValidator = new UsernameRegisteredValidator(
				txtUsername, lblUsernameError.textProperty(), userManager);
	}

	private void uninstallValidationDecorations() {
		usernameValidator.reset();
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

		// text in status bar
		statusBar.textProperty().bind(statusProperty);
	}

	private void resetForm() {
		uninstallValidationDecorations();
		txtUsername.clear();
		setBusy(false);
		setStatus("");
	}

	@FXML
	public void shareAction(ActionEvent event) {
		boolean inputOk = validateAll();
		if (inputOk) {
			Task<ResultStatus> task = createShareTask();
			new Thread(task).start();
		}
	}

	private Task<ResultStatus> createShareTask() {
		Task<ResultStatus> task = new Task<ResultStatus>() {
			final String username = getUsername();
			final Path toShare = getFolderToShare();

			@Override
			public ResultStatus call() {
				return shareFolder(toShare, username);
			}
		};

		task.setOnScheduled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				setStatus("Sharing folder... Please wait.");
				setBusy(true);
			}
		});

		task.setOnFailed(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				onShareFailed(ResultStatus.error("Could not share folder."));
			}
		});

		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				ResultStatus result = task.getValue();
				if (result.isOk()) {
					onShareSucceeded();
				} else {
					onShareFailed(result);
				}
			}
		});
		return task;
	}

	private ResultStatus shareFolder(Path toShare, String username) {
		ProcessHandle<Void> handle =  null;
		try {

			handle = fileManager.share(toShare, username, PermissionType.WRITE);
			handle.execute();
			return ResultStatus.ok();

		} catch (NoSessionException e) {
			logger.warn("Cannot share folder - no session.", e);
			return ResultStatus.error("The user is not logged in (no session).");
		} catch (NoPeerConnectionException e) {
			logger.warn("Cannot share folder - no connection to the network.", e);
			return ResultStatus.error("There is no connection to the network.");
		} catch (IllegalArgumentException e) {
			logger.warn("Cannot share folder - invalid parameters.", e);
			return ResultStatus.error(String.format(
					"Invalid parameters provided (%s).", e.getMessage()));
		} catch (InvalidProcessStateException | ProcessExecutionException e) {
			logger.warn("Cannot share folder.", e);
			return ResultStatus.error(String.format(
					"Sharing folder failed (%s)", e.getMessage()));
		}
	}

	private boolean validateAll() {
		return usernameValidator.validate(true) == ValidationResult.OK;
	}

	private void onShareSucceeded() {
		Runnable succeeded = new Runnable() {
			@Override
			public void run() {
				setStatus("Sharing succeeded.");
				setBusy(false);

				Alert dlg = new Alert(AlertType.INFORMATION);
				IconUtils.decorateDialogWithIcon(dlg);
				dlg.setTitle("Folder Sharing");
				dlg.setHeaderText("Folder sharing finished");
				dlg.setContentText("The user is granted access to the folder.");
				dlg.showAndWait();
				getStage().close();
			}
		};

		if (Platform.isFxApplicationThread()) {
			succeeded.run();
		} else {
			Platform.runLater(succeeded);
		}
		FileHelper file = new FileHelper(Paths.get(folderToShareProperty.get()), false);
		UserPermission permission = new UserPermission(getUsername(), PermissionType.WRITE);
		messageBus.publish(new LocalShareFolderMessage(file, permission));
	}

	private void onShareFailed(ResultStatus status) {
		Runnable failed = new Runnable() {
			@Override
			public void run() {
				setStatus("Sharing failed.");
				setBusy(false);

				Alert dlg = new Alert(AlertType.ERROR);
				IconUtils.decorateDialogWithIcon(dlg);
				dlg.setTitle("Folder Sharing");
				dlg.setHeaderText("Folder sharing failed.");
				dlg.setContentText(status.getErrorMessage());
				dlg.showAndWait();
			}
		};

		if (Platform.isFxApplicationThread()) {
			failed.run();
		} else {
			Platform.runLater(failed);
		}
	}

	@FXML
	public void cancelAction(ActionEvent event) {
		resetForm();
		getStage().close();
	}

	private Stage getStage() {
		return (Stage)pane.getScene().getWindow();
	}

	private String getUsername() {
		return txtUsername.getText().trim();
	}

	private Path getFolderToShare() {
		return folderToShare;
	}

	public void setFolderToShare(final Path path) {
		folderToShare = path;
		folderToShareProperty.set(path.toString());
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

	public Boolean isBusy() {
		return busyProperty.get();
	}

	public void setBusy(Boolean isBusy) {
		this.busyProperty.set(isBusy);
	}

	public BooleanProperty busyProperty() {
		return busyProperty;
	}

}