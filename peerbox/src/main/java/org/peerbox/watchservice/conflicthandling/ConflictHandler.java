package org.peerbox.watchservice.conflicthandling;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;

public class ConflictHandler {
	
	public static Path rename(Path path){
		
		String pathString = path.toString();
		String renamedFileString = null;
		String conflictWarning = "_CONFLICT_";
		
		// get index of extension (even works with files like "hello.world.txt")
		int indexOfExtension = FilenameUtils.indexOfExtension(pathString);
		
		String fileName = pathString.substring(0, indexOfExtension);
		String fileExtension = pathString.substring(indexOfExtension);

		renamedFileString = fileName + conflictWarning + currentDate() + fileExtension;
		Path renamedFile = Paths.get(renamedFileString);
		
		return renamedFile;
	}
	
	public static String currentDate(){		
		// get current date and time and set it to specific format
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();		
		
		String currentDate;
		
		return currentDate = dateFormat.format(date);
	}
	
	public static void resolveConflict(Path file){
		Path renamedFile = ConflictHandler.rename(file);
		try {
			Files.copy(file, renamedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
