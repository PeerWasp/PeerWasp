package org.peerbox.server.servlets;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static org.hamcrest.Matchers.equalTo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.peerbox.server.ServerFactory;
import org.peerbox.server.servlets.messages.DeleteMessage;
import org.peerbox.server.servlets.messages.ServerReturnCode;
import org.peerbox.server.servlets.messages.FileRecoveryMessage;

import com.jayway.restassured.http.ContentType;



public class FileRecoveryServletTest extends ServletTest {

	private String url;
	
	public FileRecoveryServletTest() {
		url = getUrl(ServerFactory.getContextMenuVersionsPath());
	}
	
	@Test
	public void testGet() {
		// GET not supported
		get(url).then().assertThat().statusCode(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}
	
	@Test
	public void testPostNoContentType() {
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.WRONG_CONTENT_TYPE.ordinal()));
	}
	
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
	
	@Test
	public void testPostEmptyJson() {	
		given().
			contentType(ContentType.JSON).
			content("{}").
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.DESERIALIZE_ERROR.ordinal()));
	}
	
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
			content("{file:\"/tmp/PeerBox_test/f\"").
		post(url).
		then().
			assertThat().statusCode(HttpServletResponse.SC_BAD_REQUEST).
			assertThat().contentType(ContentType.JSON).
			assertThat().body("returnCode", equalTo(ServerReturnCode.DESERIALIZE_ERROR.ordinal()));
	}
	
	@Test
	public void testPostWrongMsg() {
		// send a wrong message 
		DeleteMessage msg = new DeleteMessage();
		List<Path> paths = new ArrayList<Path>();
		paths.add(Paths.get("/tmp/PeerBox_test/f1"));
		paths.add(Paths.get("/tmp/PeerBox_test/f2"));
		msg.setPaths(paths);
		
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
	public void testPostFile() {
		FileRecoveryMessage msg = new FileRecoveryMessage();
		msg.setPath(Paths.get("/tmp/PeerBox_test/file"));

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
