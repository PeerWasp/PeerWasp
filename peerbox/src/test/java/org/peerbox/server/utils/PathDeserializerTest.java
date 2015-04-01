package org.peerbox.server.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.peerbox.BaseJUnitTest;
import org.peerbox.server.servlets.messages.FileRecoveryMessage;
import org.peerbox.utils.OsUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PathDeserializerTest extends BaseJUnitTest {

	@Test
	public void testDeserialize() {
		Path file = Paths.get("/this/is/a/path/to/a/file.txt");
		
		String fileString = file.toString();
		if (OsUtils.isWindows()) {
			// on windows, double excape is required. once for java and once for json.
			fileString = fileString.replace("\\", "\\\\");
		}

		String jsonMessage = String.format("{\"path\":\"%s\"}", fileString);

		FileRecoveryMessage message = null;
		Gson gson = createGsonInstance();
		message = gson.fromJson(jsonMessage, FileRecoveryMessage.class);

		assertNotNull(message);
		assertEquals(file, message.getPath());
	}

	private Gson createGsonInstance() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Path.class, new PathDeserializer());
		return gsonBuilder.create();
	}

}
