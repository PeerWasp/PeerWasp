package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RepeatedUpdate extends FileIntegrationTest{

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[30][0]);
    }
    
	@Test
	public void manyFilesInManyFoldersTest() throws IOException {
		int nrFolders = 10;
		int nrFilesPerFolder = 10;
		int totalFiles = nrFolders + nrFolders * nrFilesPerFolder;
		List<Path> files = addManyFilesInManyFolders(nrFolders, nrFilesPerFolder);
		assertCleanedUpState(totalFiles);
		
		updateManyFiles(files);
		assertCleanedUpState(totalFiles);
	}
	
	@Test @Ignore
	public void singleFileInManyFoldersTest() throws IOException {
		int nrFolders = 20;
		int nrFilesPerFolder = 1;
		int totalFiles = nrFolders + nrFolders * nrFilesPerFolder;
		List<Path> files = addManyFilesInManyFolders(nrFolders, nrFilesPerFolder);
		assertCleanedUpState(totalFiles);
		
		updateManyFiles(files);
		assertCleanedUpState(totalFiles);
	}

	private List<Path> updateManyFiles(List<Path> files) throws IOException {
		List<Path> modified = new ArrayList<>();
		for(int i = 0; i < files.size()/2; ++i) {
			Path f = files.get(i);
			// ignore directories
			if(Files.isDirectory(f)) { 
				continue;
			}
			
			// modify with probability 0.5
			boolean modify = true; //(RandomUtils.nextInt(0, 100) % 2) == 0;
			if (modify) {
				updateSingleFile(f, false);
				modified.add(f);
			}
		}
		waitForUpdate(modified, WAIT_TIME_LONG);
		return modified;
	}
}
