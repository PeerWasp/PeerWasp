package org.peerbox.presenter;

import javafx.scene.Node;

/**
 * Defines the interface for controllers that are navigatable using the 
 * navigation service.
 * @author albrecht
 *
 */
public interface INavigatable {

	public abstract void setContent(Node content);

}