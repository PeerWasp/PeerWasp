package org.peerbox.guice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.peerbox.app.AppContext;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.helper.JavaFxNoOpApp;
import org.peerbox.presenter.LoginController;
import org.peerbox.presenter.MainController;
import org.peerbox.presenter.NavigationService;
import org.peerbox.view.ViewNames;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class GuiceFxmlLoaderTest {

	@BeforeClass
	public static void initJFX() {
		Thread t = new Thread("JavaFX Init Thread") {
			public void run() {
				if(!JavaFxNoOpApp.isInitialized()) {
					Application.launch(JavaFxNoOpApp.class, new String[0]);
				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGuiceFxmlLoader() {
		new GuiceFxmlLoader(null);
	}


	@Test
	public void testCreateLoginViewDependencies() throws Exception {
		/* simple injector */
		Injector injector = Guice.createInjector(
				new AppModule(Mockito.mock(Stage.class)),
				new AppConfigModule(),
				new ApiServerModule()
		);

		IFxmlLoaderProvider fxmlLoaderProvider = new GuiceFxmlLoader(injector);

		FXMLLoader fxmlLoader = fxmlLoaderProvider.create(ViewNames.LOGIN_VIEW);
		Parent loginView = fxmlLoader.load();
		Object loginController = fxmlLoader.getController();
		assertNotNull(loginView);
		assertNotNull(loginController);
		assertTrue(loginController instanceof LoginController);

		/**
		 * Check whether the factory for controller instantiation works and uses Guice to
		 * resolve dependencies
		 */
		Class<?> ctrl = loginController.getClass();
		/* context */
		Field appContextField = ctrl.getDeclaredField("appContext");
		appContextField.setAccessible(true);
		Object appContext = appContextField.get(loginController);
		assertNotNull(appContext);
		assertTrue(appContext instanceof AppContext);

		/* user manager */
		Field userManagerField = ctrl.getDeclaredField("userManager");
		userManagerField.setAccessible(true);
		Object userManager = userManagerField.get(loginController);
		assertNotNull(userManager);
		assertTrue(userManager instanceof IUserManager);

		/* navigation service */
		Field navigationServiceField = ctrl.getDeclaredField("fNavigationService");
		navigationServiceField.setAccessible(true);
		Object navigationService = navigationServiceField.get(loginController);
		assertNotNull(navigationService);
		assertTrue(navigationService instanceof NavigationService);
	}

	@Test
	public void testCreateMainViewMock() throws IOException {
		Injector injector = Mockito.mock(Injector.class);
		when(injector.getInstance(MainController.class)).thenReturn(new MainController());
		IFxmlLoaderProvider fxmlLoaderProvider = new GuiceFxmlLoader(injector);

		FXMLLoader fxmlLoader = fxmlLoaderProvider.create(ViewNames.MAIN_VIEW);
		Parent mainView = fxmlLoader.load();
		Object mainController = fxmlLoader.getController();
		assertNotNull(mainView);
		assertNotNull(mainController);
		// getInstance called to get a controller instance?
		Mockito.verify(injector, Mockito.times(1)).getInstance(MainController.class);
	}


	@SuppressWarnings("unused")
	@Test(expected = IOException.class)
	public void testCreateNoName() throws IOException {
		Injector injector = Guice.createInjector();
		IFxmlLoaderProvider fxmlLoaderProvider = new GuiceFxmlLoader(injector);

		FXMLLoader fxmlLoader;
		fxmlLoader = fxmlLoaderProvider.create("");
		Parent mainView = fxmlLoader.load(); /* should throw exception */
		Object mainController = fxmlLoader.getController();
	}
}
