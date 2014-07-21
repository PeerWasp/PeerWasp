package org.peerbox.presenter;

import java.io.IOException;

import org.peerbox.interfaces.INavigatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Singleton;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

@Singleton
public class NavigationService {

	private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
	private INavigatable fController;
	private ObservableList<Node> pages = FXCollections.observableArrayList();
	private Injector injector;

	public void setNavigationController(INavigatable controller) {
		fController = controller;
	}

	public void setInjector(Injector injector) {
		this.injector = injector;
	}

	public void navigate(String fxmlFile) {
		Pane content = null;
		try {
			FXMLLoader loader = createGuiceFxmlLoader(fxmlFile);
			content = loader.load();
			fController.setContent(content);
			pages.add(content);
		} catch (IOException e) {
			System.err.println(String.format("Could not load fxml file (%s): %s", e.getCause(), e.getMessage()));
			e.printStackTrace();
		}
	}

	public FXMLLoader createGuiceFxmlLoader(String fxmlFile) throws IOException {
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
	
	public void closeApp(){
		System.out.println("Application closed.");
		System.exit(0);
	}
	
	public void minApp(){
		System.out.println("Application minimized (not yet implemented).");
		//TODO
	}
}
