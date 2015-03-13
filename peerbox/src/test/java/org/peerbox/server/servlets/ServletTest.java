package org.peerbox.server.servlets;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;

import javax.servlet.http.HttpServletResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.peerbox.BaseJUnitTest;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.server.IServer;
import org.peerbox.server.helper.ApiServerTestModule;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.utils.PathDeserializer;
import org.peerbox.server.utils.PathSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.mapper.factory.GsonObjectMapperFactory;

/**
 * This class contains general test methods for JSON requests.
 * Furthermore, it contains helper functionality for serialization
 * and the configuration of the server.
 *
 * @author albrecht
 *
 */
public class ServletTest extends BaseJUnitTest {

	/* server and base url of server */
	protected static IServer server;
	protected static String baseUrl;

	/* specific url for the test case, i.e. url under test */
	protected String url;

	@BeforeClass
	public static void beforeClass() {

		Injector injector = Guice.createInjector(new ApiServerModule(), new ApiServerTestModule());
		server = injector.getInstance(IServer.class);
		server.start();

		baseUrl = String.format("http://localhost:%d", server.getPort());

		configureRestAssured();

	}

	@AfterClass
	public static void afterClass() {
		server.stop();
	}

	private static void configureRestAssured() {
		RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
				objectMapperConfig().gsonObjectMapperFactory(new GsonObjectMapperFactory() {
					public Gson create(@SuppressWarnings("rawtypes") Class cls, String charset) {
						return createGsonInstance();
					}
				}
		));
	}

	/**
	 * Returns a Gson JSON serializer. It is configured such that it can serialize
	 * {@link Path} instances.
	 *
	 * @return new instance
	 */
	protected static Gson createGsonInstance() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Path.class, new PathDeserializer());
		gsonBuilder.registerTypeAdapter(Path.class, new PathSerializer());
		return gsonBuilder.create();
	}

	/**
	 * Base URL of the server (usually, this is localhost)
	 * @return http url
	 */
	protected String getBaseUrl() {
		return baseUrl;
	}

	/**
	 * Returns an absolute path given a relative URL part.
	 *
	 * @param path relative part (i.e. after localhost:port)
	 * @return http url
	 */
	protected String getUrl(String path) {
		return String.format("%s%s", getBaseUrl(), path);
	}

	/********************************
	 * Standard Test Cases
	 ********************************/

	/**
	 * GET requests are not supported. All requests should be POST
	 */
	@Test
	public void testGet() {
		// GET not supported
		get(url).then().assertThat().statusCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	/**
	 * Require a content type
	 */
	@Test
	public void testPostNoContentType() {
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.WRONG_CONTENT_TYPE.ordinal()));
	}

	/**
	 * The request should have JSON content type specified
	 */
	@Test
	public void testPostTextContentType() {
		given().
			contentType(ContentType.TEXT).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.WRONG_CONTENT_TYPE.ordinal()));
	}

	/**
	 * Empty request
	 */
	@Test
	public void testPostEmpty() {
		given().
			contentType(ContentType.JSON).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.EMPTY_REQUEST.ordinal()));
	}

	/**
	 * Empty JSON request
	 */
	@Test
	public void testPostEmptyJson() {
		given().
			contentType(ContentType.JSON).
			content("{}").
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.REQUEST_EXCEPTION.ordinal()));
	}

}
