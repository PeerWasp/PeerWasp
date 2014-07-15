package org.peerbox.controller;

import java.net.URL;

import javafx.event.ActionEvent;

import org.junit.Test;

import junit.framework.TestCase;

public class CreateNetworkControllerTest {

	@Test
	public void createNetworkTest(){
		CreateNetworkController controller = new CreateNetworkController();
		assert(H2HManager.INSTANCE.getNode() != null);
	}
}