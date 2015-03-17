package org.peerbox.app.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.peerbox.view.FontAwesomeOffline;

/**
 * UI representation of an ActivityItem in a ListView.
 * This cell maps the properties of a ActivityItem to a GUI control.
 *
 * @author albrecht
 *
 */
class ActivityItemCell extends ListCell<ActivityItem> {

	/* CSS style class for the title */
	private static final String STYLE_CLASS_ITEM_TITLE = "activity-item-title";

	/* CSS style class for ActivityItem of type "warning" */
	private static final String STYLE_CLASS_ITEM_WARNING = "activity-item-warning";

	/* formatting of timestamp */
	private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/* controls of this cell */
	private final GridPane grid;
    private final Label icon;
    private final Label title;
    private final Label dateTime;
    private final Label description;

    /* font that provides icons */
    private final GlyphFont fontAwesome;

	public ActivityItemCell() {
		super();

		fontAwesome = FontAwesomeOffline.getGlyphFont();

		grid = new GridPane();
		icon = new Label();
		title = new Label();
		title.getStyleClass().add(STYLE_CLASS_ITEM_TITLE);
		dateTime = new Label();
		description = new Label();

		initializeGrid();
		addControlsToGrid();
	}

	private void initializeGrid() {
		grid.setHgap(10);
		grid.setVgap(5);
		grid.setPadding(new Insets(0, 10, 0, 10));

		// icon column
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setFillWidth(false);
		col1.setHgrow(Priority.NEVER);
		grid.getColumnConstraints().add(col1);

		// title column: grows
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setFillWidth(true);
		col2.setHgrow(Priority.ALWAYS);
		grid.getColumnConstraints().add(col2);

		// date column
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setFillWidth(false);
		col3.setHgrow(Priority.NEVER);
		grid.getColumnConstraints().add(col3);
	}

	private void addControlsToGrid() {
        grid.add(icon, 			0, 0, 1, 2);
        grid.add(title, 		1, 0);
        grid.add(dateTime, 		2, 0);
        GridPane.setHalignment(dateTime, HPos.RIGHT);
        grid.add(description, 	1, 1, 2, 1);
	}

	@Override
	public void updateItem(ActivityItem item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			clearContent();
		} else {
			addContent(item);
		}
	}

	private void clearContent() {
		setText(null);
		setGraphic(null);
	}

	private void addContent(ActivityItem item) {
		setText(null);

		title.setText(item.getTitle());
		dateTime.setText(formatTimestamp(item.getTimestamp()));
		description.setText(item.getDescription());

		icon.setGraphic(getIconByType(item.getType()));

		switch(item.getType()) {
			case WARNING:
				grid.getStyleClass().add(STYLE_CLASS_ITEM_WARNING);
				break;
			default:
				// remove all style classes of other types
				grid.getStyleClass().removeAll(STYLE_CLASS_ITEM_WARNING);
				break;
		}

		setGraphic(grid);
	}

	private Node getIconByType(ActivityType type) {
		Node ico = null;
		switch (type) {
			case INFORMATION:
				ico = fontAwesome.create(FontAwesome.Glyph.INFO_CIRCLE);
				break;
			case WARNING:
				ico = fontAwesome.create(FontAwesome.Glyph.WARNING);
				break;
			default:
				break;
		}
		return ico;
	}

	private String formatTimestamp(long timestamp) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_PATTERN);
		Date date = new Date(timestamp);
		String formatted = dateFormat.format(date);
		return formatted;
	}
}
