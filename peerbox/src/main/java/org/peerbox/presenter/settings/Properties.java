package org.peerbox.presenter.settings;

import java.net.URL;
import java.util.ResourceBundle;

import org.hive2hive.core.model.PermissionType;
import org.hive2hive.core.model.UserPermission;
import org.peerbox.presenter.settings.synchronization.PathItem;
import org.peerbox.presenter.settings.synchronization.Synchronization;
import org.peerbox.presenter.settings.synchronization.SynchronizationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.stage.Window;

public class Properties implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Properties.class);
	
	@FXML private ListView<Label> sharedListView;
	@FXML private Button okButton;
	
	private PathItem item;
	
	public Properties(PathItem item) {
		this.item = item;
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("Initialize Properties!");
		if(item != null && item.getUserPermissions() != null){
			for(UserPermission perm : item.getUserPermissions()){
				StringBuilder sb = new StringBuilder();
				sb.append(perm.getUserId());
				PermissionType type = perm.getPermission();
				if(type == PermissionType.READ){
					sb.append(" (Read)");
				} else {
					sb.append(" (Read + Write)");
				}
				
				ImageView icon = SynchronizationUtils.getSharedFolderSuccessIcon();
				Label label = new Label(sb.toString());
				label.setGraphic(icon);
				sharedListView.getItems().add(label);
			}
		}
	}
	
	@FXML
	public void okAction(ActionEvent event) {
		if(event.getTarget() != null && event.getTarget() instanceof Button){
			Button okButton = (Button)event.getTarget();
			Window window = okButton.getScene().getWindow();
			window.hide();
		}
	}
}
