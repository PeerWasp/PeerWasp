package org.peerbox.presenter.settings;



import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextInputDialog;

import org.peerbox.app.config.BootstrappingNodes;
import org.peerbox.app.config.BootstrappingNodesFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Network settings tab controller.
 * Allows adding, editing and removing the set of bootstrapping nodes.
 *
 * @author albrecht
 *
 */
public class Network implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(Network.class);


	@FXML
	private Button btnAdd;
	@FXML
	private Button btnEdit;
	@FXML
	private Button btnRemove;
	@FXML
	private Button btnUp;
	@FXML
	private Button btnDown;
	@FXML
	private ListView<String> lwBootstrappingNodes;

	private BootstrappingNodes bootstrappingNodes;
	private BootstrappingNodesFactory bootstrappingNodesFactory;

	private ObservableList<String> addresses;

	@Inject
	public Network(BootstrappingNodesFactory bootstrappingNodesFactory) {
		this.bootstrappingNodesFactory = bootstrappingNodesFactory;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lwBootstrappingNodes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		addresses = lwBootstrappingNodes.getItems();

		// disable buttons if no item selected
		BooleanBinding isNoItemSelected = lwBootstrappingNodes.getSelectionModel().selectedItemProperty().isNull();
		btnEdit.disableProperty().bind(isNoItemSelected);
		btnRemove.disableProperty().bind(isNoItemSelected);
		btnUp.disableProperty().bind(isNoItemSelected);
		btnDown.disableProperty().bind(isNoItemSelected);

		reset();
	}

	private void reset() {
		// bootstrapping nodes
		try {
			bootstrappingNodes = bootstrappingNodesFactory.create();
			bootstrappingNodesFactory.load();
			Set<String> nodes = bootstrappingNodes.getBootstrappingNodes();
			addresses.clear();
			nodes.forEach(item -> addItemIgnoreDuplicate(item));
		} catch (IOException ioex) {
			logger.warn("Could not load settings.");
		}
	}

	@FXML
	public void addAction(ActionEvent event) {
		// request node address from user and add
		TextInputDialog input = new TextInputDialog();
		input.getEditor().setPromptText("Enter address");
		input.setTitle("New Node Address");
		input.setHeaderText("Enter new node address");
		Optional<String> result = input.showAndWait();

		if (result.isPresent()) {
			String nodeAddress = result.get().trim();
			addItemIgnoreDuplicate(nodeAddress);
		}
	}

	@FXML
	public void editAction(ActionEvent event) {
		// load current selection and allow user to change value
		int index = lwBootstrappingNodes.getSelectionModel().getSelectedIndex();
		String nodeAddress = lwBootstrappingNodes.getSelectionModel().getSelectedItem();

		TextInputDialog input = new TextInputDialog();
		input.getEditor().setText(nodeAddress);
		input.setTitle("Edit Node Address");
		input.setHeaderText("Enter node address");
		Optional<String> result = input.showAndWait();

		if(result.isPresent()) {
			String newNodeAddress = result.get().trim();
			if (!newNodeAddress.isEmpty() && !containsAddress(newNodeAddress)) {
				addresses.add(index, newNodeAddress);
				addresses.remove(nodeAddress);
			}
		}
	}

	@FXML
	public void removeAction(ActionEvent event) {
		// remove the selected indices in reverse order
		Integer[] indices = lwBootstrappingNodes.getSelectionModel()
				.getSelectedIndices().toArray(new Integer[0]);
		Arrays.sort(indices);
		for(int i = indices.length - 1; i >= 0; --i) {
			addresses.remove(indices[i].intValue());
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
		int size = addresses.size();
		if(indexCurrent >= 0 && indexCurrent < size && indexNew >= 0 && indexNew < size) {
			Collections.swap(addresses, indexCurrent, indexNew);
			lwBootstrappingNodes.getSelectionModel().clearAndSelect(indexNew);
			lwBootstrappingNodes.requestFocus();
		}
	}

	@FXML
	public void saveAction(ActionEvent event) {
		try {
			// update config
			bootstrappingNodes.clearNodes();
			for (String n : addresses) {
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

	/**
	 * Adds an item to the list view collection.
	 * Check that there are no duplicates (ignore case!)
	 *
	 * @param toAdd
	 */
	private void addItemIgnoreDuplicate(String toAdd) {
		if (!containsAddress(toAdd)) {
			addresses.add(toAdd);
		}
	}

	/**
	 * Checks whether the given address is in the list of addresses.
	 * Note: ignore case!
	 *
	 * @param address
	 * @return true if element is present, otherwise false.
	 */
	private boolean containsAddress(String address) {
		boolean contains = false;
		for (String item : addresses) {
			if (item.equalsIgnoreCase(address)) {
				contains = true;
				break;
			}
		}
		return contains;
	}

}
