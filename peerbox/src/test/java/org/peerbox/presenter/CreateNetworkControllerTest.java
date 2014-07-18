package org.peerbox.presenter;

import java.net.URL;

import javafx.event.ActionEvent;

import org.junit.Test;
import org.peerbox.model.H2HManager;
import org.peerbox.presenter.CreateNetworkController;

import junit.framework.TestCase;

public class CreateNetworkControllerTest {

	@Test
	public void createNetworkTest(){
		CreateNetworkController controller = new CreateNetworkController();
		assert(H2HManager.INSTANCE.getNode() != null);
		
	}
}