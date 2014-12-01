package org.peerbox.interfaces;

import java.io.IOException;

import javafx.fxml.FXMLLoader;

public interface IFxmlLoaderProvider {

	FXMLLoader create(String viewName) throws IOException;

}
