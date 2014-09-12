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
	
	
	public static String getRandomString(int len, String charSet) {
		String result = "";
	 
		while (result.length() < len) {
			result = result + getChar(charSet);
		}
	 
		return result;
	}
	
	public static char getChar(String charSet) {
		int s = getInt(charSet.length());
		return charSet.charAt(s - 1);
	}
	 
	public static int getInt(int max) {
		return (int) (Math.ceil(Math.random() * max));
	}
	
	public static int randomInt(int min, int max) {

	    int randomNum = rnd.nextInt((max - min) + 1) + min;
	    
	    return randomNum;
	}
	
}
