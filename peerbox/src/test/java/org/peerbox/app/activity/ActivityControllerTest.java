package org.peerbox.app.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Callback;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.BaseJUnitTest;
import org.peerbox.helper.JavaFXThreadingRule;
import org.peerbox.view.ViewNames;

public class ActivityControllerTest extends BaseJUnitTest {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

	@SuppressWarnings("unused")
	private Parent root;
	private ActivityController controller;
	private ActivityLogger logger;

	@Before
	public void setUp() throws Exception {
		logger = Mockito.mock(ActivityLogger.class);
		Mockito.stub(logger.getActivityItems()).toReturn(FXCollections.observableArrayList());

		controller = new ActivityController(logger);
		root = getRootNode();
	}

	@After
	public void tearDown() throws Exception {
		controller = null;
		logger = null;
	}

	@Test
	public void testInitialize() {
		controller.initialize(null, null);
		assertEquals(
				logger.getActivityItems(),
				controller.getActivityLogListView().getItems()
		);
		logger.getActivityItems().add(ActivityItem.create());
		assertTrue(controller.getActivityLogListView().getItems().size() == 1);
	}

	@Test
	public void testClearAction() {
		controller.clearAction(null);
		Mockito.verify(logger, times(1)).clearActivityItems();
	}

	protected Parent getRootNode() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setControllerFactory(new Callback<Class<?>, Object>() {
				@Override
				public Object call(Class<?> param) {
					return controller;
				}
			});
			loader.setLocation(getClass().getResource(ViewNames.ACTIVITY_VIEW));
			return loader.load();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
