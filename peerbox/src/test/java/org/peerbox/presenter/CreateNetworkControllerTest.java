package org.peerbox.presenter;


import org.junit.Before;
import org.junit.Test;
import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.presenter.CreateNetworkController;

public class CreateNetworkControllerTest {
	
	private INodeManager nodeManager;
	
	@Before
	public void initialize() {
		nodeManager = new NodeManager(null);
	}
	
	@Test
	public void createNetworkTest(){
		// TODO: mock navigation service.
		CreateNetworkController controller = new CreateNetworkController(null, nodeManager);
		assert(nodeManager.getNode() != null);
		// TODO: why should the node be != null after constructor?
	}
}