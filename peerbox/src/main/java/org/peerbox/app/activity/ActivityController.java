package org.peerbox.app.activity;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

import javax.swing.SwingUtilities;

import org.peerbox.utils.AppData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Controller for the Activity View.
 * Responsible for filtering and sorting the activity items.
 *
 * @author albrecht
 *
 */
public class ActivityController implements Initializable {

	private final static Logger logger = LoggerFactory.getLogger(ActivityController.class);

	@FXML
	private TextField txtFilter;

	@FXML
	private ListView<ActivityItem> lstActivityLog;

	private final ActivityLogger activityLogger;

	@Inject
	public ActivityController(final ActivityLogger activityLogger) {
		this.activityLogger = activityLogger;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadItems();
		Platform.runLater(() -> {
			// request focus works only after initialization
			lstActivityLog.requestFocus();
		});
	}

	/**
	 * Wires the list view with the items source and configures filtering and sorting of the items.
	 */
	private void loadItems() {
		// filtering -- default show all
		FilteredList<ActivityItem> filteredItems = new FilteredList<>(activityLogger.getActivityItems(), p -> true);
		txtFilter.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// filter on predicate
				filteredItems.setPredicate(new ActivityItemFilterPredicate(newValue));
			}
		});

		// sorting
		SortedList<ActivityItem> sortedItems = new SortedList<>(filteredItems, new ActivityItemTimeComparator());

		// set item source
		lstActivityLog.setItems(sortedItems);
		lstActivityLog
				.setCellFactory(new Callback<ListView<ActivityItem>, ListCell<ActivityItem>>() {
					@Override
					public ListCell<ActivityItem> call(ListView<ActivityItem> param) {
						return new ActivityItemCell();
					}
				});
	}

	/**
	 * Clears the activity list view by clearing the underlying list.
	 *
	 * @param event
	 */
	@FXML
	public void clearAction(ActionEvent event) {
		activityLogger.clearActivityItems();
		logger.debug("Clear activity items.");
	}

	protected ListView<ActivityItem> getActivityLogListView() {
		return lstActivityLog;
	}
	/**
	 * Opens the folder in the file browser where the log files are stored.
	 *
	 * @param event
	 */
	@FXML
	public void openLogAction(ActionEvent event) {
		Runnable open = new Runnable() {
			@Override
			public void run() {
				try {
					java.awt.Desktop.getDesktop().open(AppData.getLogFolder().toFile());
					logger.debug("Open log folder... '{}'", AppData.getLogFolder());
				} catch (Exception e) {
					logger.warn("Could not open log folder: {}", e.getMessage(), e);
				}
			}
		};
		// use of swing utilities here because of java.awt.Desktop usage!
		SwingUtilities.invokeLater(open);
	}

	/**
	 * Filter predicate that matches a filter string on title and description
	 */
	private class ActivityItemFilterPredicate implements Predicate<ActivityItem> {

		/* the filter value */
		private final String newValue;

		protected ActivityItemFilterPredicate(String newValue) {
			this.newValue = newValue;
		}

		@Override
		public boolean test(ActivityItem t) {
			if (newValue == null) {
				return true;
			}

			String trimmed = newValue.trim();
			if (trimmed.isEmpty()) {
				return true;
			}

			String lower = trimmed.toLowerCase();
			boolean match = t.getTitle().toLowerCase().contains(lower)
					|| t.getDescription().toLowerCase().contains(lower);
			return match;
		}
	}

	/**
	 * Sorts by time in reverse order, i.e. most recent element first
	 */
	private class ActivityItemTimeComparator implements Comparator<ActivityItem> {
		@Override
		public int compare(ActivityItem o1, ActivityItem o2) {
			return Long.compare(o2.getTimestamp(), o1.getTimestamp());
		}
	}

}
