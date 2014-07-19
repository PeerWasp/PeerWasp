package org.peerbox.presenter;


import org.junit.Before;
import org.junit.Test;
import org.peerbox.model.H2HManager;
import org.peerbox.presenter.CreateNetworkController;

public class CreateNetworkControllerTest {
	
	private H2HManager h2hmanager;
	
	@Before
	public void initializeVariables(){
		h2hmanager = new H2HManager();
	}
	
	@Test
	public void createNetworkTest(){
		CreateNetworkController controller = new CreateNetworkController(h2hmanager);
		assert(h2hmanager.getNode() != null);
		// TODO: why should the node be != null after constructor?
	}
}