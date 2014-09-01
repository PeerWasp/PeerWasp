package org.peerbox.watchservice;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class FolderWatchServiceTest {

	public static void main(String[] args) throws Exception {
		String path = "C:/Users/Claudio/Desktop/WatchServiceTest";
		FolderWatchService service = new FolderWatchService(Paths.get(path));
		service.start();
		System.out.println("Running");
		
//		Thread.sleep(1000*10);
//		service.stop();
//		System.out.println("Stopping");
	}

}
