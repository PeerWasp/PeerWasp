package org.peerbox.presenter;

import java.net.URL;
import java.nio.file.Path;
import java.util.ResourceBundle;

import org.hive2hive.core.model.IFileVersion;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableView;

public class RecoverFileController  implements Initializable {

	private final StringProperty fileToRecover;
	
	@FXML
	private TableView<IFileVersion> tblFileVersions;
	
	public RecoverFileController(Path fileToRecover) {
		this.fileToRecover = new SimpleStringProperty(fileToRecover.toString());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO: 
		// - load available revisions from network
		// - add items in table 
		// - user selects an entry
		// - clicks recover -> start downloading
	}
	
}
