package org.peerbox.presenter;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;

import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.interfaces.INavigatable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This navigation service offers simple page-based navigation similar to website navigation.
 * The service is responsible for loading pages and keeps track of the loaded content.
 * References of old pages are kept in a list in order to be able to navigate to
 * a previously loaded page (navigation backward).
 *
 * The service itself operates on a controller instance which is responsible for actually replacing
 * the content on a view element (e.g. content of a pane).
 *
 * Note: forward navigation is not supported (e.g. navigate back and then forward again to an
 * already loaded page is not possible)
 *
 * Important: the page list needs to be cleared if the history is not required anymore such that
 * they can be freed (garbage collection). The references are kept in the list until removed manually.
 *
 * @author albrecht
 *
 */
@Singleton
public class NavigationService {

	private static final Logger logger = LoggerFactory.getLogger(NavigationService.class);
	/**
	 * the controller of interest which is responsible for displaying and replacing content
	 */
	private INavigatable fController;
	/**
	 * the content history
	 */
	private final ObservableList<Node> pages;
	/**
	 * FXML loader supporting Google Guice (dependency injection),
	 * loads pages and resolves dependencies of controllers using Guice
	 */
	private IFxmlLoaderProvider fxmlLoader;

	/**
	 * Creates a new navigation service instance.
	 *
	 * @param loader the guice fxml loader to use (provides the injector instance)
	 */
	@Inject
	public NavigationService(IFxmlLoaderProvider loader) {
		if (loader == null) {
			throw new IllegalArgumentException("The argument loader must not be null.");
		}
		fxmlLoader = loader;
		pages = FXCollections.observableArrayList();
	}

	/**
	 * Sets the controller instance
	 *
	 * @param controller the instance to use for navigation
	 */
	public void setNavigationController(INavigatable controller) {
		fController = controller;
	}

	/**
	 * Get the controller instance
	 *
	 * @return current controller instance
	 */
	public INavigatable getNavigationController() {
		return fController;
	}

	/**
	 * Creates an FXML loader instance for an .fxml file supporting supporting DI
	 *
	 * @param fxmlFile the name of the resource to load
	 * @return an FXML loader instance, ready to be used by calling the load() method
	 * @throws IOException
	 */
	public FXMLLoader createLoader(final String fxmlFile) throws IOException {
		return fxmlLoader.create(fxmlFile);
	}

	/**
	 * Navigates to a page by loading the page and creating a corresponding controller instance.
	 *
	 * @param fxmlFile the name of the page (resource)
	 * @throws IllegalStateException if no controller is set
	 */
	public synchronized void navigate(final String fxmlFile) {
		if (fController == null) {
			throw new IllegalStateException("Controller must not be null, please set an instance.");
		}

		Node content = null;
		try {
			FXMLLoader loader = createLoader(fxmlFile);
			content = loader.load();
			fController.setContent(content);
			pages.add(content);
		} catch (IOException e) {
			logger.error(String.format("Could not load fxml file (%s).", e.getMessage(), e));
			e.printStackTrace();
		}
	}

	/**
	 * Navigate to the previous page.
	 * Use canNavigateBack() to check whether there is a page that can be loaded.
	 *
	 * @throws IllegalStateException if there is no page to load.
	 */
	public synchronized void navigateBack() {
		if (canNavigateBack()) {
			pages.remove(pages.size() - 1);
			fController.setContent(pages.get(pages.size() - 1));
		} else {
			logger.warn("Cannot go back (number of pages: {})", pages.size());
			throw new IllegalStateException(String.format(
					"Cannot navigate back, number of pages: %s", pages.size()));
		}
	}

	/**
	 * Indicates whether backward navigation is possible.
	 *
	 * @return true if there is a page that can be loaded.
	 */
	public synchronized boolean canNavigateBack() {
		return pages.size() >= 2;
	}

	/**
	 * Clears the page history.
	 */
	public synchronized void clearPages() {
		pages.clear();
	}
}
