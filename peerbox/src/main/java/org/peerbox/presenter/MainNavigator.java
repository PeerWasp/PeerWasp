package org.peerbox.presenter;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import jidefx.scene.control.decoration.DecorationPane;

public class MainNavigator {

	private static final Logger logger = LoggerFactory.getLogger(MainNavigator.class);
	private static MainController mainController;
	private static ObservableList<Node> pages = FXCollections.observableArrayList();
	private static Injector injector;

	public static void setMainController(MainController controller) {
		mainController = controller;
	}

	public static void setInjector(Injector injector) {
		MainNavigator.injector = injector;
	}

	public static void navigate(String fxmlFile) {
		Pane content = null;
		try {
			FXMLLoader loader = createGuiceFxmlLoader(fxmlFile);
			content = loader.load();
			mainController.setContent(new DecorationPane(content));
			pages.add(content);
		} catch (IOException e) {
			System.err.println(String.format("Could not load fxml file (%s): %s", e.getCause(), e.getMessage()));
			e.printStackTrace();
		}
	}

	public static FXMLLoader createGuiceFxmlLoader(String fxmlFile) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(MainController.class.getResource(fxmlFile));
		loader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> type) {
				return injector.getInstance(type);
			}
		});
		return loader;
	}

	public static boolean canGoBack() {
		return pages.size() >= 2;
	}

	public static void goBack() {
		if (canGoBack()) {
			int previous = pages.size() - 2;
			pages.remove(pages.size() - 1);
			mainController.setContent(pages.get(previous));
		} else {
			// TODO: what should we do here? Throw exception, do nothgin, log, ... :)
			logger.warn("Cannot go back (number of pages: {})", pages.size());
		}
	}

	public static void clearPages() {
		pages.clear();
	}
}
