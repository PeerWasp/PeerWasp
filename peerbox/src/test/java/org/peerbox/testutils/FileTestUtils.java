package org.peerbox.testutils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

public class FileTestUtils {
	
	private static int FOLDER_NUMBER = 0;
	private static int FILE_NUMBER = 0;
	
	public static List<Path> createFolderWithFiles(Path basePath, int numberOfFiles, int numberOfChars) throws IOException {
		List<Path> files = new ArrayList<Path>();
		
		// create folder
		Path folder = createRandomFolder(basePath);
		files.add(folder);
		
		// create files
		List<Path> f = createRandomFiles(folder, numberOfFiles, numberOfChars);
		files.addAll(f);
		
		return files;
	}
	
	public static Path createRandomFolder(Path basePath) throws IOException {
		String folderName = "f"+FOLDER_NUMBER++; 
		Path p = basePath.resolve(folderName);
		Files.createDirectory(p);
		return p;
	}
	
	public static List<Path> createRandomFolders(Path basePath, int numberOfFolders) throws IOException {
		List<Path> folders = new ArrayList<>();
		for(int i = 0; i < numberOfFolders; ++i) {
			Path folder = createRandomFolder(basePath);
			folders.add(folder);
		}
		return folders;
	}
	
	public static Path createRandomFile(Path basePath, int numChars) throws IOException {
		String name = String.format("%s.file", FILE_NUMBER++);
		Path file = basePath.resolve(name);
		String data = createRandomData(numChars);
		org.apache.commons.io.FileUtils.writeStringToFile(file.toFile(), data);
		return file;
	}
	
	public static void writeRandomData(Path file, int numChars) throws IOException {
		String data = createRandomData(numChars);
		org.apache.commons.io.FileUtils.writeStringToFile(file.toFile(), data);
	}
	
	public static Path createTestFile(Path basePath, int numChars) throws IOException {
		String name = String.format("%s.txt", "test");
		Path file = basePath.resolve(name);
		String data = "test";
		org.apache.commons.io.FileUtils.writeStringToFile(file.toFile(), data);
		return file;
	}
	
	public static List<Path> createRandomFiles(Path basePath, int numberOfFiles, int numberOfChars) throws IOException {
		List<Path> files = new ArrayList<Path>();
		for(int i = 0; i < numberOfFiles; ++i) {
			Path f = createRandomFile(basePath, numberOfChars);
			files.add(f);
		}
		return files;
	}

	public static String createRandomData(int numChars) {
		return RandomStringUtils.randomAscii(numChars);
	}
}
