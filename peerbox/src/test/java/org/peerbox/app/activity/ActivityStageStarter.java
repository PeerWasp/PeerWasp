package org.peerbox.app.activity;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.peerbox.interfaces.IFxmlLoaderProvider;

import com.google.inject.Injector;

/**
 * Starter class for the activity view.
 * Initializes and shows the activity stage.
 *
 * @author albrecht
 *
 */
public class ActivityStageStarter extends Application {

	private ActivityLogger activityLogger;
	private ActivityStage stage;
	private ActivityController controller;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {

			new ActivityStageStarter().run();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void run() throws InterruptedException {
		activityLogger = new ActivityLogger();
		controller = new ActivityController(activityLogger);
		stage = new ActivityStage();
		stage.setFxmlLoaderProvider(new IFxmlLoaderProvider() {
				@Override
				public FXMLLoader create(String fxmlFile) {
					FXMLLoader loader = new FXMLLoader();
					loader.setLocation(getClass().getResource(fxmlFile));
					loader.setControllerFactory( new Callback<Class<?>, Object>() {
						@Override
						public Object call(Class<?> param) {
							return controller;
						}
					});
					return loader;
				}

				@Override
				public FXMLLoader create(String fxmlFile, Injector injector) throws IOException {
					return create(fxmlFile);
				}
			});

		stage.show();

		publishRandomActivityItems();
	}


	private void publishRandomActivityItems() {
		new Thread(new ActivitySimulator()).start();
	}


	/**
	 * Creates random activity items and adds them to the logger.
	 *
	 * @author albrecht
	 *
	 */
	private class ActivitySimulator implements Runnable{

		public void run() {
			int i = 1;
			while(true) {
				try {
					String rndTitle = RandomStringUtils.randomAlphanumeric(10);
					String rndDesc = RandomStringUtils.randomAlphanumeric(20);

					ActivityItem item = ActivityItem.create();
					item.setTitle("Title " + i + rndTitle);
					item.setDescription("Description " + i + rndDesc);
					if(RandomUtils.nextInt(0, 2) == 1) {
						item.setType(ActivityType.WARNING);
					}

					activityLogger.addActivityItem(item);
					Thread.sleep(2000);
					++i;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
