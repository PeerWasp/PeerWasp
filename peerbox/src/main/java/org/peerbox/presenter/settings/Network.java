package org.peerbox.presenter.settings;


import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.util.Duration;

import org.peerbox.app.config.BootstrappingNodes;
import org.peerbox.app.config.BootstrappingNodesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Network settings
 * @author albrecht
 *
 */
public class Network implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Network.class);

	@FXML
	private ListView<String> lwBootstrappingNodes;

	private final Timeline editLastSelection;

	private BootstrappingNodes bootstrappingNodes;
	private BootstrappingNodesFactory bootstrappingNodesFactory;

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
		// bootstrapping nodes
		try {
			bootstrappingNodes = bootstrappingNodesFactory.create();
			bootstrappingNodesFactory.load();
			Set<String> nodes = bootstrappingNodes.getBootstrappingNodes();
			lwBootstrappingNodes.getItems().clear();
			lwBootstrappingNodes.getItems().addAll(nodes);
		} catch (IOException ioex) {
			logger.warn("Could not load settings.");
		}
	}

	@FXML
	public void addAction(ActionEvent event) {
		lwBootstrappingNodes.getItems().add("");
		editLastSelection.play();
	}

	@FXML
	public void removeAction(ActionEvent event) {
		// remove the selected indices in reverse order
		Integer[] indices = lwBootstrappingNodes.getSelectionModel()
				.getSelectedIndices().toArray(new Integer[0]);
		Arrays.sort(indices);
		for(int i = indices.length - 1; i >= 0; --i) {
			lwBootstrappingNodes.getItems().remove(indices[i].intValue());
		}
	}

	@FXML
	public void upAction(ActionEvent event) {
		int lastSelected = lwBootstrappingNodes.getSelectionModel().getSelectedIndex();
		swapBootstrapingNodes(lastSelected, lastSelected-1);
	}

	@FXML
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

	@FXML
	public void saveAction(ActionEvent event) {
		try {
			// update config
			List<String> nodes = lwBootstrappingNodes.getItems();
			bootstrappingNodes.clearNodes();
			for (String n : nodes) {
				bootstrappingNodes.addNode(n);
			}
			bootstrappingNodesFactory.save();

			// reload saved config
			reset();
			logger.debug("Saved network settings.");
		} catch(IOException ioex) {
			logger.warn("Could not save settings: {}", ioex);
		}
	}

	@FXML
	public void resetAction(ActionEvent event) {
		logger.debug("Reset network settings.");
		reset();
	}

	@Inject
	public void setBootstrappingNodesFactory(BootstrappingNodesFactory bootstrappingNodesFactory) {
		this.bootstrappingNodesFactory = bootstrappingNodesFactory;
	}
}
