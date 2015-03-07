package org.peerbox.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.peerbox.guice.IFxmlLoaderProvider;
import org.peerbox.helper.JavaFXThreadingRule;


public class NavigationServiceTest {

	@Rule
	public JavaFXThreadingRule jfxRule = new JavaFXThreadingRule();
	
	/**
	 * Controller instance of the view
	 */
	@Mock
	private INavigatable controller;

	/**
	 * Guice loader, does not do DI in this test.
	 */
	@Mock
	private IFxmlLoaderProvider fxmlLoaderProvider;
	
	/**
	 * FXML loader, does not actually load .fxml files
	 */
	@Mock
	private FXMLLoader fxmlLoader;

	/**
	 * Navigation service under test
	 */
	private NavigationService navigationService;

	/**
	 * The pages to load for navigation tests
	 */
	private Label[] elements;


	@Before
	public void setupNavigationService() {
		/* required due to @Mock annotation */
		MockitoAnnotations.initMocks(this);
		
		/* create some ui elements */
		createPages();

		try {
			/* mock the two methods required for navigation: create(.) and load() */
			when(fxmlLoaderProvider.create(anyString())).thenReturn(fxmlLoader);
			when(fxmlLoader.load()).thenReturn(elements[0], elements[1], elements[2], elements[3]);
		} catch (IOException e) {
			/* should not happen as we do not actually load the fxml files */
			e.printStackTrace();
		}
		
		/* initialize the navigation services using the mocks */
		navigationService = new NavigationService(fxmlLoaderProvider);
		navigationService.setNavigationController(controller);
	}
	
	/**
	 * Creates some dummy UI elements to use while testing navigation
	 * Dummy elements need to be of type Node
	 */
	private void createPages() {
		elements = new Label[] { 
			new Label("testpage1.fxml"), 
			new Label("testpage2.fxml"),
			new Label("testpage3.fxml"), 
			new Label("testpage4.fxml") 
		};
	}

	/**
	 * Cleanup after testing
	 */
	@After
	public void destroyNavigationService() {
		controller = null;
		fxmlLoaderProvider = null;
		fxmlLoader = null;
		navigationService = null;
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void testNavigationServiceNull() {
		new NavigationService(null);
	}

	@Test
	public void testCreateLoader() throws IOException {
		final String page = "/testpage1.fxml";
		navigationService.createLoader(page);
		Mockito.verify(fxmlLoaderProvider, Mockito.times(1)).create(page);
	}

	@Test
	public void testNavigate() throws IOException {
		ObservableList<Node> pages = getPages();
		assertNotNull(pages);

		/* first page */
		String page_1 = "/testpage1.fxml";
		navigationService.navigate(page_1);
		Mockito.verify(fxmlLoaderProvider, Mockito.times(1)).create(page_1);
		Mockito.verify(fxmlLoader, Mockito.times(1)).load();
		Mockito.verify(controller, Mockito.times(1)).setContent(elements[0]);
		assertTrue(pages.size() == 1);
		assertEquals(pages.get(0), elements[0]);

		/* second page */
		String page_2 = "/testpage2.fxml";
		navigationService.navigate(page_2);
		Mockito.verify(fxmlLoaderProvider, Mockito.times(1)).create(page_2);
		Mockito.verify(fxmlLoader, Mockito.times(2)).load();
		Mockito.verify(controller, Mockito.times(1)).setContent(elements[1]);
		assertTrue(pages.size() == 2);
		assertEquals(pages.get(0), elements[0]);
		assertEquals(pages.get(1), elements[1]);

		/* third page */
		String page_3 = "/testpage3.fxml";
		navigationService.navigate(page_3);
		Mockito.verify(fxmlLoaderProvider, Mockito.times(1)).create(page_3);
		Mockito.verify(fxmlLoader, Mockito.times(3)).load();
		Mockito.verify(controller, Mockito.times(1)).setContent(elements[2]);
		assertTrue(pages.size() == 3);
		assertEquals(pages.get(0), elements[0]);
		assertEquals(pages.get(1), elements[1]);
		assertEquals(pages.get(2), elements[2]);
	}

	/**
	 * Gives access to the private pages list of the service using reflection
	 * @return the pages list of the navigation service
	 */
	@SuppressWarnings("unchecked")
	private ObservableList<Node> getPages() {
		ObservableList<Node> pages = null;
		try {
			Class<?> ns = navigationService.getClass();
			Field pagesField = ns.getDeclaredField("pages");
			pagesField.setAccessible(true);
			pages = (ObservableList<Node>) pagesField.get(navigationService);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
				| SecurityException e) {
			e.printStackTrace();
			pages = null;
		}
		return pages;
	}

	@Test
	public void testNavigateBack() {
		ObservableList<Node> pages = getPages();
		assertNotNull(pages);
		
		String page_1 = "/testpage1.fxml";
		navigationService.navigate(page_1);
		String page_2 = "/testpage2.fxml";
		navigationService.navigate(page_2);
		String page_3 = "/testpage3.fxml";
		navigationService.navigate(page_3);
		/* correct order after loading is checked by other test case */
		
		/* now navigate back */
		navigationService.navigateBack();
		assertTrue(pages.size() == 2);
		assertEquals(pages.get(0), elements[0]);
		assertEquals(pages.get(1), elements[1]);
		
		navigationService.navigateBack();
		assertTrue(pages.size() == 1);
		assertEquals(pages.get(0), elements[0]);
		assertFalse(navigationService.canNavigateBack());
	}

	@Test
	public void testCanNavigateBack() {
		assertFalse(navigationService.canNavigateBack());

		navigationService.navigate("/testpage1.fxml");
		assertFalse(navigationService.canNavigateBack());

		navigationService.navigate("/testpage2.fxml");
		assertTrue(navigationService.canNavigateBack());

		navigationService.navigateBack();
		assertFalse(navigationService.canNavigateBack());
	}

	@Test(expected = IllegalStateException.class)
	public void testNavigationErrorOnePage() {
		assertFalse(navigationService.canNavigateBack());
		navigationService.navigate("/testpage1.fxml");
		assertFalse(navigationService.canNavigateBack());
		navigationService.navigateBack(); /* throws IllegalStateException */
	}

	@Test(expected = IllegalStateException.class)
	public void testNavigationErrorNoPage() {
		assertFalse(navigationService.canNavigateBack());
		navigationService.navigateBack(); /* throws IllegalStateException */
	}

	@Test
	public void testClearPages() {
		navigationService.navigate("/testpage1.fxml");
		navigationService.navigate("/testpage2.fxml");
		navigationService.navigate("/testpage3.fxml");
		ObservableList<Node> pages = getPages();
		assertTrue(pages.size() == 3);
		navigationService.clearPages();
		assertTrue(pages.size() == 0);
	}

}
