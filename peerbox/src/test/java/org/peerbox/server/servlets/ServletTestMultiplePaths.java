package org.peerbox.server.servlets;

import static com.jayway.restassured.RestAssured.get;
import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.post;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

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
 * This test class tests a request where a multiple paths are expected.
 * For instance, a one or several paths are expected in a delete message (see {@link DeleteMessage}).
 * Thus, the corresponding JSON request is different compared to {@link ServletTestSinglePath}.
 *
 * @author albrecht
 *
 */
public class ServletTestMultiplePaths extends ServletTest {

	protected String url;

	@Test
	public void test() {
		fail("Not implemented yet");
	}
}
