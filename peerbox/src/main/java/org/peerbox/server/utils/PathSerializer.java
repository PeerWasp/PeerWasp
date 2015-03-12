package org.peerbox.server.utils;

import java.lang.reflect.Type;
import java.nio.file.Path;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * PathSerializer helper that creates a String out of Path
 *
 * @author albrecht
 *
 */
public class PathSerializer implements JsonSerializer<Path> {

	@Override
	public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
		return new JsonPrimitive(src.toString());
	}

}
