package org.peerbox.filerecovery;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.io.FileUtils;
import org.controlsfx.control.StatusBar;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.peerbox.app.manager.ProcessHandle;
import org.peerbox.app.manager.file.IFileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public final class RecoverFileController  implements Initializable, IFileVersionSelectorListener {
	
	private static final Logger logger = LoggerFactory.getLogger(RecoverFileController.class);
	
	@FXML
	private TableView<IFileVersion> tblFileVersions;
	@FXML
	private TableColumn<IFileVersion, Integer> tblColIndex;
	@FXML
	private TableColumn<IFileVersion, String> tblColDate;
	@FXML
	private TableColumn<IFileVersion, String> tblColSize;
	@FXML
	private Label lblNumberOfVersions;
	@FXML
	private Button btnRecover;
	@FXML
	private AnchorPane pane;
	// JavaFX Control, but @FXML is not supported
	private StatusBar statusBar;
	
	private final BooleanProperty busyProperty;
	private final StringProperty fileToRecoverProperty;
	private final StringProperty statusProperty;
	
	private Path fileToRecover;
	
	private final ObservableList<IFileVersion> fileVersions;
	
	private final FileVersionSelector versionSelector;

	private IFileManager fileManager;
	private RecoverFileTask recoverFileTask;
	
	public RecoverFileController() {
		this.fileToRecoverProperty = new SimpleStringProperty();
		this.statusProperty = new SimpleStringProperty();
		this.busyProperty = new SimpleBooleanProperty(false);

		this.fileVersions = FXCollections.observableArrayList();
		this.versionSelector = new FileVersionSelector(this);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeTable();
		initializeStatusBar();
		
		lblNumberOfVersions.textProperty().bind(Bindings.size(fileVersions).asString());
		btnRecover.disableProperty().bind(
				tblFileVersions.getSelectionModel().selectedItemProperty().isNull()
				.or(busyProperty));
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

	private void initializeTable() {
		tblFileVersions.setItems(fileVersions);
		initializeColumns();
		sortTable();
	}

	private void initializeColumns() {
		tblColIndex.setCellValueFactory(
				new Callback<CellDataFeatures<IFileVersion, Integer>, ObservableValue<Integer>>() {
					public ObservableValue<Integer> call(CellDataFeatures<IFileVersion, Integer> p) {
						// p.getValue() returns the IFileVersion instance for a particular TableView row
						return new SimpleIntegerProperty(p.getValue().getIndex()).asObject();
					}
				}
		);
		
		tblColDate.setCellValueFactory(
				new Callback<CellDataFeatures<IFileVersion, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<IFileVersion, String> p) {
						
						Date date = new Date(p.getValue().getDate());
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						String dateFormatted = formatter.format(date);
						return new SimpleStringProperty(dateFormatted);
					}
				}
		);
		
		tblColSize.setCellValueFactory(
				new Callback<CellDataFeatures<IFileVersion, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(CellDataFeatures<IFileVersion, String> p) {
						String humanReadableSize = FileUtils.byteCountToDisplaySize(p.getValue().getSize());
						return new SimpleStringProperty(humanReadableSize);
					}
				}
		);
	}

	private void sortTable() {
		// sorting by index DESC
		tblColIndex.setSortType(TableColumn.SortType.DESCENDING);
		tblFileVersions.getSortOrder().add(tblColIndex);
		tblFileVersions.sort();
	}
	
	public void loadVersions() {
		if (fileToRecover == null || fileToRecover.toString().isEmpty()) {
			throw new IllegalArgumentException("fileToRecover not set, cannot be null or empty");
		}
		recoverFileTask = new RecoverFileTask(fileToRecover);
		new Thread(recoverFileTask).start();
	}

	@Override
	public void onAvailableVersionsReceived(final List<IFileVersion> availableVersions) {
		Runnable versions = new Runnable() {
			@Override
			public void run() {
				setBusy(false);
				if (availableVersions.isEmpty()) {
					setStatus("No versions available for this file.");
				} else {
					setStatus("Select version to recover");
					fileVersions.addAll(availableVersions);
					setStatus("");
					sortTable();
				}
			}
		};
		
		if (Platform.isFxApplicationThread()) {
			versions.run();
		} else {
			Platform.runLater(versions);
		}
	}

	private void onFileRecoverySucceeded() {
		Runnable succeeded = new Runnable() {
			@Override
			public void run() {
				setBusy(false);
				setStatus("");
				
				Alert a = new Alert(AlertType.INFORMATION);
				a.setTitle("File Recovered");
				a.setHeaderText("File recovery finished");
				a.setContentText(String.format("The name of the recovered file is: %s", versionSelector.getRecoveredFileName()));
				a.showAndWait();
				getStage().close();
			}
		};
		
		if (Platform.isFxApplicationThread()) {
			succeeded.run();
		} else {
			Platform.runLater(succeeded);
		}
	}

	private void onFileRecoveryFailed(final String message) {
		Runnable failed = new Runnable() {
			@Override
			public void run() {
				setBusy(false);
				setStatus("");
				
				if(!versionSelector.isCancelled()) {
					// show error if user did not initiate cancel action
					Alert a = new Alert(AlertType.ERROR);
					a.setTitle("File Recovery Failed");
					a.setHeaderText("File recovery did not succeed.");
					a.setContentText(message);
					a.showAndWait();
				}
				getStage().close();
			}
		};
		
		if (Platform.isFxApplicationThread()) {
			failed.run();
		} else {
			Platform.runLater(failed);
		}
	}

	public void recoverAction(ActionEvent event) {
		IFileVersion selectedVersion = tblFileVersions.getSelectionModel().getSelectedItem();
		if(selectedVersion != null) {
			// only allow 1 recovery
			btnRecover.disableProperty().unbind();
			btnRecover.setDisable(true);
			
			setBusy(true);
			setStatus("Downloading file...");
			versionSelector.selectVersion(selectedVersion);
		}
	}

	public void cancelAction(ActionEvent event) {
		cancel();
	}
	
	public void cancel() {
		try {
			if(versionSelector != null) {
				versionSelector.cancel();
			}
			if(recoverFileTask != null) {
				recoverFileTask.cancel();
			}
		} finally {
			Platform.runLater(() -> {
				getStage().close();
			});
		}
	}
	
	@Inject
	public void setFileManager(IFileManager fileManager) {
		this.fileManager = fileManager;
	}

	public String getFileToRecover() {
		return fileToRecoverProperty.get();
	}

	public void setFileToRecover(String fileName) {
		this.fileToRecover = Paths.get(fileName);
		this.fileToRecoverProperty.setValue(fileName);
	}

	public void setFileToRecover(final Path fileToRecover) {
		this.fileToRecover = fileToRecover;
		this.fileToRecoverProperty.setValue(fileToRecover.toString());
	}

	public StringProperty fileToRecoverProperty() {
		return fileToRecoverProperty;
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
	
	private Stage getStage() {
		return (Stage)tblFileVersions.getScene().getWindow();
	}
	
	private class RecoverFileTask extends Task<Void> {
		private ProcessHandle<Void> process;
		private final Path fileToRecover;
		
		public RecoverFileTask(final Path fileToRecover) {
			this.fileToRecover = fileToRecover;
		}
		
		@Override
		protected Void call() throws Exception {
			try {

				setBusy(true);
				setStatus("Retrieving available versions...");

				process = fileManager.recover(this.fileToRecover.toFile(), versionSelector);
				process.execute();

			} catch (Exception e) {
				// FIXME: maybe get specific reason of exception for user?
				logger.warn("Exception while recovering file: {}", e.getMessage(), e);
				throw e;
			}
			return null;
		}
		
		@Override
		protected void succeeded() {
			super.succeeded();
			updateMessage("Done!");
			onFileRecoverySucceeded();
		}

		@Override
		protected void failed() {
			super.failed();
			updateMessage("Failed!");
			onFileRecoveryFailed("File Recovery Failed.");
		}
        
		@Override
		protected void cancelled() {
			super.cancelled();
			updateMessage("Cancelled!");
			try {
				process.getProcess().rollback();
			} catch (InvalidProcessStateException | ProcessRollbackException e) {
				// ignore
			}
		}
	}
}
