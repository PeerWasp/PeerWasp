package org.peerbox.guice;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.util.Callback;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class GuiceFxmlLoader {
	
	private Injector injector;
	
	@Inject
	public GuiceFxmlLoader(Injector injector) {
		this.injector = injector;
	}
	
	public FXMLLoader create(String fxmlFile) throws IOException {
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
