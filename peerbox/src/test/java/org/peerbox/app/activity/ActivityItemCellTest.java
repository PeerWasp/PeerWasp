package org.peerbox.app.activity;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.peerbox.helper.JavaFXThreadingRule;

public class ActivityItemCellTest {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();

	private ActivityItemCell cell;
	private ActivityItem item;

	@Before
	public void setUp() throws Exception {
		cell = new ActivityItemCell();
		assertNotNull(cell);

		item = ActivityItem.create();
		item.setTitle("Title");
		item.setDescription("Description");
	}

	@After
	public void tearDown() throws Exception {
		cell = null;
	}

	@Test
	public void testActivityItemCell() {
		assertNull(cell.getText());
		assertNull(cell.getGraphic());
	}

	@Test
	public void testUpdate_empty() {
		cell.updateItem(item, true);

		assertNull(cell.getText());
		assertNull(cell.getGraphic());
	}

	@Test
	public void testUpdate_notEmpty() {
		cell.updateItem(item, false);

		assertNull(cell.getText());
		assertNotNull(cell.getGraphic());

		GridPane grid = (GridPane)cell.getGraphic();
		boolean foundTitle = findLabelText("Title", grid.getChildren());
		if(!foundTitle) {
			fail("Did not find title.");
		}

		boolean foundDescription = findLabelText("Description", grid.getChildren());
		if(!foundDescription) {
			fail("Did not find description.");
		}
	}

	private boolean findLabelText(String search, ObservableList<Node> nodes) {
		boolean found = false;
		for (Node n : nodes) {
			if (n instanceof Label) {
				Label l = (Label) n;
				if (l.getText().equals(search)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}

}
