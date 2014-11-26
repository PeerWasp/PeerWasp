package org.peerbox.presenter;

import java.io.FileNotFoundException;
import java.net.URL;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.io.FileUtils;
import org.hive2hive.core.exceptions.NoPeerConnectionException;
import org.hive2hive.core.exceptions.NoSessionException;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.core.processes.files.recover.IVersionSelector;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.exceptions.InvalidProcessStateException;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.peerbox.FileManager;
import org.peerbox.interfaces.IFileVersionSelectorEventListener;

import com.google.inject.Inject;

public class RecoverFileController  implements Initializable, IFileVersionSelectorEventListener {
	
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
	private Label lblStatus;
	
	private final StringProperty fileToRecoverProperty;
	private final StringProperty statusProperty;
	
	private Path fileToRecover;
	
	private final ObservableList<IFileVersion> fileVersions;
	
	private FileVersionSelector versionSelector;

	private FileManager fileManager;
	
	public RecoverFileController() {
		this.fileToRecoverProperty = new SimpleStringProperty();
		this.statusProperty = new SimpleStringProperty();
		fileVersions = FXCollections.observableArrayList();
		versionSelector = new FileVersionSelector(this);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initializeTable();
		
		lblNumberOfVersions.textProperty().bind(Bindings.size(fileVersions).asString());
		btnRecover.disableProperty().bind(tblFileVersions.getSelectionModel().selectedItemProperty().isNull());
	}

	@Inject
	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}
	
	public void setFileToRecover(final Path fileToRecover) {
		this.fileToRecover = fileToRecover;
		this.fileToRecoverProperty.setValue(fileToRecover.toString());
		loadVersions();
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

	private void showFileRecoverySucceededDialog() {
		Alert a = new Alert(AlertType.INFORMATION);
		a.setTitle("File Recovered");
		a.setHeaderText("File recovery finished");
		a.setContentText(String.format("The name of the recovered file is: %s", versionSelector.getRecoveredFileName()));
		a.showAndWait();
		getStage().close();
	}

	private void showFileRecoveryFailedDialog(String message) {
		
		getStage().close();
	}

	@Override
	public void onAvailableVersionsReceived(List<IFileVersion> availableVersions) {
		Platform.runLater(() -> {
			fileVersions.addAll(availableVersions);
			setStatus("");
			sortTable();
		});
	}

	private void loadVersions() {
		
			try {
				setStatus("Retrieving available versions...");
				
				IProcessComponent component = fileManager.recover(fileToRecover.toFile(), versionSelector);
				component.attachListener(new IProcessComponentListener() {
	
					@Override
					public void onSucceeded() {
						Platform.runLater(() -> {
							showFileRecoverySucceededDialog();
						});
					}
	
					@Override
					public void onFailed(RollbackReason reason) {
						Platform.runLater(() -> {
							showFileRecoveryFailedDialog(reason.getHint());
						});
					}
				});
			
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSessionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoPeerConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidProcessStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	public void recoverAction(ActionEvent event) {
		IFileVersion selectedVersion = tblFileVersions.getSelectionModel().getSelectedItem();
		if(selectedVersion != null) {
			setStatus("Downloading file...");
		}
		versionSelector.selectVersion(selectedVersion);
	}

	public void cancelAction(ActionEvent event) {
		versionSelector.selectVersion((IFileVersion)null);
		getStage().close();
	}
	
	public String getFileToRecover() {
		return fileToRecoverProperty.get();
	}

	public void setFileToRecover(String fileName) {
		this.fileToRecoverProperty.set(fileName);
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
	
	private Stage getStage() {
		return (Stage)tblFileVersions.getScene().getWindow();
	}
	private class FileVersionSelector implements IVersionSelector {
		
		private final Lock selectionLock = new ReentrantLock();
		private final Condition versionSelectedCondition  = selectionLock.newCondition();
		private IFileVersionSelectorEventListener listener; 
		private IFileVersion selectedVersion;
		private String recoveredFileName;
		
		private volatile boolean gotAvailableVersions = false;
		private volatile boolean isCancelled = false;
		
		public FileVersionSelector(IFileVersionSelectorEventListener listener) {
			if(listener == null) {
				throw new IllegalArgumentException("Argument listener must not be null.");
			}
			
			this.recoveredFileName = "";
			this.listener = listener;
		}
		
		public void selectVersion(IFileVersion selectedVersion) {
			if(selectedVersion == null) {
				isCancelled = true;
			}
			
			if(gotAvailableVersions) {
				try {
					selectionLock.lock();
					this.selectedVersion = selectedVersion;
					versionSelectedCondition.signal();
				} finally {
					selectionLock.unlock();
				}
			}
		}

		@Override
		public IFileVersion selectVersion(List<IFileVersion> availableVersions) {
			
			if(isCancelled) {
				// not interested in versions anymore -> select nothing (i.e. cancel)
				return null;
			}
			
			try {
				selectionLock.lock();
				if(availableVersions != null) {
					gotAvailableVersions = true;
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					listener.onAvailableVersionsReceived(availableVersions);
					// wait here until selectVersion(version) is called
					versionSelectedCondition.awaitUninterruptibly();
				}
			} finally {
				selectionLock.unlock();
			}
			
			return selectedVersion;
		}

		public String getRecoveredFileName() {
			return recoveredFileName;
		}

		@Override
		public String getRecoveredFileName(String fullName, String name, String extension) {
			// generate a new file name indicating that the file is restored
			Date versionDate = new Date(selectedVersion.getDate());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
			
			String newFileName = String.format("%s-%s", name, sdf.format(versionDate));
			if(extension!=null && extension.length() > 0) {
				newFileName = String.format("%s.%s", newFileName, extension);
			}
			recoveredFileName = newFileName;
			return newFileName;
		}
	}
	
}
