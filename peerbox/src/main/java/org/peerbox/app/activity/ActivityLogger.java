package org.peerbox.app.activity;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.google.inject.Singleton;

/**
 * Class responsible for collecting activity items.
 * All items are collected in an observable list such that UI elements can directly use this
 * collection. Furthermore, encapsulates the list methods and ensures that operations are
 * executed on the FX Application Thread.
 *
 * @author albrecht
 *
 */
@Singleton
public class ActivityLogger {

	/* upper bound for the activityItems list */
	private final int MAX_ITEMS = 100;

	/* list of items to display */
	private final ObservableList<ActivityItem> activityItems;

	public ActivityLogger() {
		final ObservableList<ActivityItem> list = FXCollections.observableArrayList();
		this.activityItems = FXCollections.synchronizedObservableList(list);
	}

	/**
	 * Adds an activity item.
	 *
	 * @param item to add
	 */
	public void addActivityItem(final ActivityItem item) {
		Runnable add = new Runnable() {
			@Override
			public void run() {
				if (activityItems.size() >= MAX_ITEMS) {
					activityItems.remove(0);
				}
				activityItems.add(item);
			}
		};

		if (Platform.isFxApplicationThread()) {
			add.run();
		} else {
			Platform.runLater(add);
		}
	}

	/**
	 * Returns a read-only view of the activity items.
	 *
	 * @return list of activity items
	 */
	public ObservableList<ActivityItem> getActivityItems() {
		return FXCollections.unmodifiableObservableList(activityItems);
	}

	/**
	 * Clears the list of activity items.
	 */
	public void clearActivityItems() {
		Runnable clear = () -> {
			activityItems.clear();
		};

		if (Platform.isFxApplicationThread()) {
			clear.run();
		} else {
			Platform.runLater(clear);
		}
	}

}
