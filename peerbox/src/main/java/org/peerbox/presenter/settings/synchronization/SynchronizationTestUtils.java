package org.peerbox.presenter.settings.synchronization;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.eclipse.jetty.util.ConcurrentHashSet;

public class SynchronizationTestUtils {
	
	public static Set<Path> generateLocalFiles(DummyUserConfig userConfig){
		Set<Path> result = new ConcurrentHashSet<Path>();
		Path newFile = userConfig.getRootPath().resolve(Paths.get("Docs"));
		result.add(newFile);

		newFile = userConfig.getRootPath().resolve(Paths.get("Docs" + File.separator + "doc.txt"));
		result.add(newFile);
		for(int i = 0; i < 5; i++){
			newFile = userConfig.getRootPath().resolve(Paths.get("Hello World" + i + ".txt"));
			result.add(newFile);
		}
		
		return result;
	}
	
	public static Set<Path> generateRemoteFiles(DummyUserConfig userConfig){
		Set<Path> result = generateLocalFiles(userConfig);
		for(int i = 0; i < 5; i++){
			Path newFile = userConfig.getRootPath().resolve(Paths.get("Remote File" + i + ".txt"));
			result.add(newFile);
		}
		return result;
	}
}
