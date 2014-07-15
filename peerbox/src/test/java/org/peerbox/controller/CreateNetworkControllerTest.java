package org.peerbox.controller;

import javafx.event.ActionEvent;

import org.junit.Test;

import junit.framework.TestCase;

public class CreateNetworkControllerTest {

	@Test
	public void createNetworkTest(){
		CreateNetworkController controller = new CreateNetworkController();
		controller.createNetwork(new ActionEvent());
		assert(controller.getNode() != null);
	}
}