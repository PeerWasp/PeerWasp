package org.peerbox.guice;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import org.peerbox.interfaces.IFxmlLoaderProvider;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * This class complements the JavaFX FXMLLoader with Google Guice support. The FXML Loader
 * responsible for loading .fxml files does only create the controller instance without
 * further dependencies.
 *
 * The GuiceFxmlLoader uses dependency injection for creating the controller instances (and for
 * providing their annotated dependencies).
 * The GuiceFxmlLoader instance should be reused as it returns new FXMLLoader instances.
 *
 * @author albrecht
 *
 */
public class GuiceFxmlLoader implements IFxmlLoaderProvider {

	/**
	 * The injector to use for creating the controller instances,
	 */
	private Injector injector;

	/**
	 * Creates a new Guice FXML loader.
	 * Note: it may be important which injector instance is passed to this instance,
	 * e.g. due to singletons managed by the container that are then injected into
	 * controller instances.
	 *
	 * @param injector instance to use for creating controllers
	 */
	@Inject
	public GuiceFxmlLoader(Injector injector) {
		if (injector == null) {
			throw new IllegalArgumentException("Injector must not be null.");
		}
		this.injector = injector;
	}

	/**
	 * Creates a new FXMLLoader instance. The location is set to the provided .fxml file name.
	 * The controller will be created using DI. The FXMLLoader is provided ready to be used, e.g.
	 * 		FXMLLoader loader = guiceFxmlLoader.create("/views/form.fxml");
	 * 		Node content = loader.load();
	 * 		FormController controller = (FormController) loader.getController();
	 * 		// do something with the UI element and the controller...
	 *
	 * @param fxmlFile the name of the .fxml file
	 * @return an FXMLLoader instance, ready to call load() on it
	 * @throws IOException in case .fxml file cannot be loaded
	 */
	@Override
	public FXMLLoader create(final String fxmlFile) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(getClass().getResource(fxmlFile));
		loader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> type) {
				return injector.getInstance(type);
			}
		});
		return loader;
	}

}
