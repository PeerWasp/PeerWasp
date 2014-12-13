package org.peerbox.filerecovery;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
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
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.exceptions.ProcessExecutionException;
import org.hive2hive.processframework.exceptions.ProcessRollbackException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.hive2hive.processframework.interfaces.IProcessEventArgs;
import org.peerbox.FileManager;
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
	private StatusBar statusBar;
	
	private final BooleanProperty busyProperty;
	private final StringProperty fileToRecoverProperty;
	private final StringProperty statusProperty;
	
	private Path fileToRecover;
	
	private final ObservableList<IFileVersion> fileVersions;
	
	private FileVersionSelector versionSelector;

	private FileManager fileManager;
	private IProcessComponent<Void> process;
	
	public RecoverFileController() {
		this.fileToRecoverProperty = new SimpleStringProperty();
		this.statusProperty = new SimpleStringProperty();
		this.busyProperty = new SimpleBooleanProperty(false);
		
		fileVersions = FXCollections.observableArrayList();
		versionSelector = new FileVersionSelector(this);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeTable();
		initializeStatusBar();
		
		lblNumberOfVersions.textProperty().bind(Bindings.size(fileVersions).asString());
		btnRecover.disableProperty().bind(
				tblFileVersions.getSelectionModel().selectedItemProperty().isNull().or(
				busyProperty));
	
		
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

	private void loadVersions() {
	
		try {
			setBusy(true);
			setStatus("Retrieving available versions...");
	
			process = fileManager.recover(fileToRecover.toFile(), versionSelector);
			process.attachListener(new RecoveryProcessListener());
	
		} catch (NoSessionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoPeerConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidProcessStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onAvailableVersionsReceived(List<IFileVersion> availableVersions) {
		Platform.runLater(() -> {
			setBusy(false);
			if (availableVersions.isEmpty()) {
				setStatus("No versions available for this file.");
			} else {
				setStatus("Select version to recover");
				fileVersions.addAll(availableVersions);
				setStatus("");
				sortTable();
			}
		});
	}

	private void onFileRecoverySucceeded() {
		Platform.runLater(() -> {
			setBusy(false);
			setStatus("");
			
			Alert a = new Alert(AlertType.INFORMATION);
			a.setTitle("File Recovered");
			a.setHeaderText("File recovery finished");
			a.setContentText(String.format("The name of the recovered file is: %s", versionSelector.getRecoveredFileName()));
			a.showAndWait();
			getStage().close();
		});
	}

	private void onFileRecoveryFailed(String message) {
		Platform.runLater(() -> {
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
		});
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
			if(process != null) {
				process.rollback();
			}
		} catch (InvalidProcessStateException | ProcessRollbackException e) {
			logger.warn("Could not cancel process.", e);
		} finally {
			getStage().close();
		}
	}
	
	@Inject
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public String getFileToRecover() {
		return fileToRecoverProperty.get();
	}

	public void setFileToRecover(String fileName) {
		this.fileToRecoverProperty.set(fileName);
	}

	public void setFileToRecover(final Path fileToRecover) {
		this.fileToRecover = fileToRecover;
		this.fileToRecoverProperty.setValue(fileToRecover.toString());
		loadVersions();
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
	
	
	private class RecoveryProcessListener implements IProcessComponentListener {

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
			onFileRecoverySucceeded();
		}

		@Override
		public void onExecutionFailed(IProcessEventArgs args) {
			onFileRecoveryFailed("Recovery failed.");
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
	
}
