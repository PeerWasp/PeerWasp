package org.peerbox.presenter.settings;


import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.peerbox.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Duration;

/**
 * Network settings 
 * @author albrecht
 *
 */
public class Network implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Network.class);
	
	@FXML 
	private CheckBox chbAutoJoin;
	
	@FXML
	private ListView<String> lwBootstrappingNodes;
	
	private final Timeline editLastSelection;
	
	private UserConfig userConfig; 
	
	
	public Network() {
		editLastSelection = new Timeline(new KeyFrame(Duration.seconds(.1), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent actionEvent) {
				lwBootstrappingNodes.getSelectionModel().clearSelection();
				lwBootstrappingNodes.getSelectionModel().selectLast();
				lwBootstrappingNodes.edit(lwBootstrappingNodes.getSelectionModel().getSelectedIndex());
			}
		}));
		editLastSelection.setCycleCount(1);
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lwBootstrappingNodes.setCellFactory(TextFieldListCell.forListView()); 
		lwBootstrappingNodes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		reset();
	}
	
	private void reset() {
		// auto join setting
//		chbAutoJoin.setSelected(userConfig.isAutoJoinEnabled());
		// TODO auto join is no more
		// bootstrapping nodes
		List<String> nodes = userConfig.getBootstrappingNodes();
		lwBootstrappingNodes.getItems().clear();
		lwBootstrappingNodes.getItems().addAll(nodes);
	}
	
	public void addAction(ActionEvent event) {
		lwBootstrappingNodes.getItems().add("");
		editLastSelection.play();
	}
	
	public void removeAction(ActionEvent event) {
		// remove the selected indices in reverse order
		Integer[] indices = lwBootstrappingNodes.getSelectionModel()
				.getSelectedIndices().toArray(new Integer[0]);
		Arrays.sort(indices);
		for(int i = indices.length - 1; i >= 0; --i) {
			lwBootstrappingNodes.getItems().remove(indices[i].intValue());
		}
	}
	
	public void upAction(ActionEvent event) {
		int lastSelected = lwBootstrappingNodes.getSelectionModel().getSelectedIndex();
		swapBootstrapingNodes(lastSelected, lastSelected-1);
	}
	
	public void downAction(ActionEvent event) {
		int lastSelected = lwBootstrappingNodes.getSelectionModel().getSelectedIndex();
		swapBootstrapingNodes(lastSelected, lastSelected+1);
	}
	
	private void swapBootstrapingNodes(int indexCurrent, int indexNew) {
		if(indexCurrent >= 0 && indexCurrent < lwBootstrappingNodes.getItems().size() &&
				indexNew >= 0 && indexNew < lwBootstrappingNodes.getItems().size()) {
			Collections.swap(lwBootstrappingNodes.getItems(), indexCurrent, indexNew);
			lwBootstrappingNodes.getSelectionModel().clearAndSelect(indexNew);
			lwBootstrappingNodes.requestFocus();
		}
	}
	
	public void saveAction(ActionEvent event) {
		try {
			// update config
			List<String> nodes = lwBootstrappingNodes.getItems();
			userConfig.setBootstrappingNodes(nodes);
//			userConfig.setAutoJoin(chbAutoJoin.isSelected()); // TODO auto join is no more
			// reload saved config
			reset();
			logger.debug("Saved network settings.");
		} catch(IOException ioex) {
			logger.warn("Could not save settings: {}", ioex.getMessage());
			// TODO: warn user about this....
		}
	}
	
	public void resetAction(ActionEvent event) {
		logger.debug("Reset network settings.");
		reset();
	}
	
	@Inject
	public void setUserConfig(UserConfig userConfig) {
		this.userConfig = userConfig;
	}
}
