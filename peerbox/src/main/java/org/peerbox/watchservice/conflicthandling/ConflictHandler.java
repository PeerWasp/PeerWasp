package org.peerbox.watchservice.conflicthandling;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FilenameUtils;
import org.peerbox.app.manager.file.LocalFileConflictMessage;
import org.peerbox.events.MessageBus;
import org.peerbox.presenter.settings.synchronization.FileHelper;

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

	public static String currentDate() {
		// get current date and time and set it to specific format
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		Date date = new Date();
		String currentDate = dateFormat.format(date);
		return currentDate;
	}

	public static void resolveConflict(Path file){
		resolveConflict(file, false);
	}

	public static void resolveConflictAndNotifyGUI(Path file, boolean moveFile, MessageBus bus){
		Path renamedFile = ConflictHandler.rename(file);
		try {
			if(moveFile){
				Files.move(file, renamedFile);
				if(bus != null){
					FileHelper fileHelper = new FileHelper(file, true);
					bus.publish(new LocalFileConflictMessage(fileHelper));
				}
			} else {
				Files.copy(file, renamedFile);
				if(bus != null){
					FileHelper fileHelper = new FileHelper(file, true);
					bus.publish(new LocalFileConflictMessage(fileHelper));
				}
			if(bus != null){

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void resolveConflict(Path file, boolean moveFile){
		resolveConflictAndNotifyGUI(file, moveFile, null);
	}

}
