package org.peerbox.server.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.peerbox.BaseJUnitTest;
import org.peerbox.server.servlets.messages.FileRecoveryMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class PathSerializerTest extends BaseJUnitTest {

	@Test
	public void testSerialize() {
		Path file = Paths.get("/this/is/a/path/to/a/file.txt");

		String jsonExpected = String.format("{\"path\":\"%s\"}", file.toString());

		FileRecoveryMessage message = new FileRecoveryMessage();
		message.setPath(file);

		Gson gson = createGsonInstance();
		String jsonSerialized = gson.toJson(message);

		assertNotNull(jsonSerialized);
		assertEquals(jsonExpected, jsonSerialized);
	}

	private Gson createGsonInstance() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Path.class, new PathSerializer());
		return gsonBuilder.create();
	}


}
