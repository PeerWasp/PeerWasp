package org.peerbox.server.servlets;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.peerbox.server.servlets.messages.DeleteMessage;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.ShareMessage;

import com.jayway.restassured.http.ContentType;

/**
 * This test class tests a request where a multiple paths are expected.
 * For instance, a one or several paths are expected in a delete message (see {@link DeleteMessage}).
 * Thus, the corresponding JSON request is different compared to {@link ServletTestSinglePath}.
 *
 * @author albrecht
 *
 */
public class ServletTestMultiplePaths extends ServletTest {

	@Test
	public void testSingleUrl() {
		// single url instead of array
		String message = String.format(
				"{\"paths\":\"%s\"}",
				"/tmp/testpath/file1");

		given().
			contentType(ContentType.JSON).
			content(message).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.DESERIALIZE_ERROR.ordinal()));
	}

	@Test
	public void testPostWrongJson() {
		// files instead of paths
		String msg = String.format(
				"{files:[\"%s\", \"%s\"]",
					"/tmp/PeerWasp_test/f1",
					"/tmp/PeerWasp_test/f2");
		given().
			contentType(ContentType.JSON).
			content(msg).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.DESERIALIZE_ERROR.ordinal()));
	}

	@Test
	public void testPostWrongMsg() {
		// send a wrong message, i.e. one with 1 url
		ShareMessage msg = new ShareMessage();
		Path folder = Paths.get("/tmp/PeerWasp_test/f1");
		msg.setPath(folder);

		given().
			contentType(ContentType.JSON).
			content(msg).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.REQUEST_EXCEPTION.ordinal()));
	}

	@Test
	public void testPostFiles() {
		DeleteMessage msg = new DeleteMessage();
		List<Path> files = new ArrayList<>();
		files.add(Paths.get("/tmp/PeerWasp_Test/folder"));
		files.add(Paths.get("/tmp/PeerWasp_Test/file"));
		msg.setPaths(files);

		given().
			contentType(ContentType.JSON).
			content(msg).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_OK).
			assertThat().contentType(ContentType.JSON);
	}

	@Test
	public void testPostFileAndAdditionalParameter() {
		String message = String.format("{\"paths\":[\"%s\"], \"code\":4}",
				"/tmp/PeerWasp_Test/file");

		given().
			contentType(ContentType.JSON).
			content(message).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_OK).
			assertThat().contentType(ContentType.JSON);
	}



}
