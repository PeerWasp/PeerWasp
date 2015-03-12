package org.peerbox.server.servlets;

import static com.jayway.restassured.config.ObjectMapperConfig.objectMapperConfig;

import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.peerbox.BaseJUnitTest;
import org.peerbox.guice.ApiServerModule;
import org.peerbox.server.IServer;
import org.peerbox.server.helper.ApiServerTestModule;
import org.peerbox.server.utils.PathDeserializer;
import org.peerbox.server.utils.PathSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.mapper.factory.GsonObjectMapperFactory;

public class ServletTest extends BaseJUnitTest {

	protected static IServer server;
	protected static String baseUrl;

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

	protected static Gson createGsonInstance() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Path.class, new PathDeserializer());
		gsonBuilder.registerTypeAdapter(Path.class, new PathSerializer());
		return gsonBuilder.create();
	}

	protected String getBaseUrl() {
		return baseUrl;
	}

	protected String getUrl(String path) {
		return String.format("%s%s", getBaseUrl(), path);
	}

}
