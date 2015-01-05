package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.io.Files;

@RunWith(Parameterized.class)
public class EnhancedMove extends FileIntegrationTest{

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[10][0]);
    }

	@Test
	public void manyNonEmptyFolderMoveTest() throws IOException{
		manyNonEmptyFolderMoveTestFunc();
	}
	
	private void manyNonEmptyFolderMoveTestFunc() throws IOException{
		logger.debug("--------------------START------------");
		int nrFolders = 10;
		int nrFilesPerFolder = 10;
		Path destination = addSingleFolder();
		List<Path> paths = addManyFilesInManyFolders(10, 10);
		int totalFiles = nrFolders + nrFolders * nrFilesPerFolder + 1;
		
		assertCleanedUpState(totalFiles);
		Path lastDestination = null;
		for(Path path: paths){
			if(path.toFile().isDirectory()){
				lastDestination = destination.resolve(path.getFileName());
				if(path.toFile().exists()){
					Files.move(path.toFile(), lastDestination.toFile());
				}
			}
			
		}
		waitForExists(lastDestination, WAIT_TIME_SHORT);
		assertCleanedUpState(totalFiles);
		logger.debug("--------------------END------------");
	}
	
}
