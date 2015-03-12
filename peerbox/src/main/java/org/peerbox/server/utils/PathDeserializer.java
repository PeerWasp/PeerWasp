package org.peerbox.server.utils;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * PathDeserializer helper that creates a Path instance out of a json string.
 *
 * @author albrecht
 *
 */
public class PathDeserializer implements JsonDeserializer<Path> {

	@Override
	public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		return Paths.get(json.getAsJsonPrimitive().getAsString());
	}
}
