package org.peerbox.presenter;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import org.peerbox.guice.GuiceFxmlLoader;
import org.peerbox.interfaces.INavigatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NavigationService {

	private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
	private INavigatable fController;
	private ObservableList<Node> pages = FXCollections.observableArrayList();
	private GuiceFxmlLoader guiceFxmlLoader;

	@Inject
	public NavigationService(GuiceFxmlLoader loader) {
		this.guiceFxmlLoader = loader;
	}

	public void setNavigationController(INavigatable controller) {
		fController = controller;
	}
	
	public FXMLLoader createLoader(String fxmlFile) throws IOException {
		return guiceFxmlLoader.create(fxmlFile);
	}

	public void navigate(String fxmlFile) {
		Pane content = null;
		try {
			FXMLLoader loader = createLoader(fxmlFile);
			content = loader.load();
			fController.setContent(content);
			pages.add(content);
		} catch (IOException e) {
			logger.error(String.format("Could not load fxml file (%s): %s", e.getCause(), e.getMessage()));
			e.printStackTrace();
		}
	}

	public boolean canGoBack() {
		return pages.size() >= 2;
	}

	public void goBack() {
		if (canGoBack()) {
			int previous = pages.size() - 2;
			pages.remove(pages.size() - 1);
			fController.setContent(pages.get(previous));
		} else {
			// TODO: what should we do here? Throw exception, do nothgin, log, ... :)
			logger.warn("Cannot go back (number of pages: {})", pages.size());
		}
	}

	public void clearPages() {
		pages.clear();
	}



}
