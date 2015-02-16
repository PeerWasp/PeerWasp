package org.peerbox.interfaces;

import java.io.IOException;

import com.google.inject.Injector;

import javafx.fxml.FXMLLoader;

public interface IFxmlLoaderProvider {

	FXMLLoader create(String viewName) throws IOException;

	FXMLLoader create(final String fxmlFile, Injector injector) throws IOException;
}
