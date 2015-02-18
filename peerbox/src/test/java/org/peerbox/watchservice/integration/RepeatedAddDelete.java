package org.peerbox.watchservice.integration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class RepeatedAddDelete extends FileIntegrationTest{

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[10][0]);
    }
    
	@Test @Ignore
	public void singleFileInManyFoldersTest() throws IOException{
		int nrFolders = 100;
		List<Path> allPathsInOne = addSingleFileInManyFolders(nrFolders);
		assertCleanedUpState(nrFolders * 2);
		
		AddDelete.deleteManyFilesInManyFolders(allPathsInOne);
		assertCleanedUpState(0);
	}
	
	@Test
	public void manyFilesInManyFoldersTest() throws IOException {
		int nrFolders = 10;
		int nrFilesPerFolder = 10;
		
		List<Path> files = addManyFilesInManyFolders(nrFolders, nrFilesPerFolder);
		assertCleanedUpState(nrFolders + nrFolders * nrFilesPerFolder);
		
		AddDelete.deleteManyFilesInManyFolders(files);
		assertCleanedUpState(0);
	}
}
