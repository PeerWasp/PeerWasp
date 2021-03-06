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
import org.peerbox.server.servlets.messages.FileRecoveryMessage;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.ShareMessage;

import com.jayway.restassured.http.ContentType;

/**
 * This test class tests a request where a single path is expected.
 * For instance, a single path to a folder is expected in a share folder message (see {@link ShareMessage}).
 * Thus, the corresponding JSON request is very simple.
 *
 * @author albrecht
 *
 */
public class ServletTestSinglePath extends ServletTest {

	@Test
	public void testCollectionOfUrls() {
		StringBuilder sb = new StringBuilder();
		sb.append("{").append("\"path\":").append("[")
		.append("\"").append(Paths.get("/tmp/testpath/file1")).append("\"")
		.append(",\"").append(Paths.get("/tmp/testpath/file2")).append("\"")
		.append("]}");

		given().
			contentType(ContentType.JSON).
			content(sb.toString()).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.DESERIALIZE_ERROR.ordinal()));
	}

	@Test
	public void testPostWrongJson() {
		given().
			contentType(ContentType.JSON).
			content("{file:\"/tmp/PeerWasp_test/f\"").
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.DESERIALIZE_ERROR.ordinal()));
	}

	@Test
	public void testPostWrongMsg() {
		// send a wrong message, i.e. one with more than 1 url (list of urls)
		DeleteMessage msg = new DeleteMessage();
		List<Path> paths = new ArrayList<Path>();
		paths.add(Paths.get("/tmp/PeerWasp_test/f1"));
		paths.add(Paths.get("/tmp/PeerWasp_test/f2"));
		msg.setPaths(paths);

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
	public void testPostFile() {
		FileRecoveryMessage msg = new FileRecoveryMessage();
		msg.setPath(Paths.get("/tmp/PeerWasp_test/file"));

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
		StringBuilder sb = new StringBuilder();
		sb.append("{").append("\"path\":")
		.append("\"").append(Paths.get("/tmp/testpath/file1")).append("\"")
		.append(", \"code\":\"4\"")
		.append("}");

		given().
			contentType(ContentType.JSON).
			content(sb.toString()).
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_OK).
			assertThat().contentType(ContentType.JSON);
	}
}
