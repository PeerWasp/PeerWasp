package org.peerbox.watchservice;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class WatchServiceTestHelpers {
	private static Random rnd = new Random();
	
	public static void writeRandomData(FileWriter out, int numCharacters) throws IOException {
		for(int i = 0; i < numCharacters; ++i) {
			out.write(getRandomCharacter());
		}
		out.flush();
	}
	
	private static char getRandomCharacter() {
		char c = (char)(rnd.nextInt(26) + 'a');
		return c;
	}
}
