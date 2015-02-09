package org.peerbox.watchservice.filetree;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.peerbox.watchservice.PathUtils;

public class PathUtilsTest {

	private String filename = "file1.txt";
	private String directory = "exampledir";
	private Path oneLevel = Paths.get(File.separator + filename);
	private Path twoLevels = Paths.get(File.separator + directory + File.separator + filename);
	private Path twoLevelsWithoutFS = Paths.get(directory + File.separator + filename);
	private Path zeroLevels = Paths.get("");

//	@Test
//	public void getNextPathFragmentTest(){
//
//
//		String result = PathUtils.getNextPathFragment(oneLevel.toString());
//		assertEquals(result, filename);
//
//		result = PathUtils.getNextPathFragment(twoLevels.toString());
//		assertEquals(result, directory);
//
//		result = PathUtils.getNextPathFragment(twoLevelsWithoutFS.toString());
//		assertEquals(result, directory);
//
//		result = PathUtils.getNextPathFragment(zeroLevels.toString());
//		assertEquals(result, "");
//	}
//
//	@Test
//	public void getRemainingPathFragmentTest(){
//		String result = PathUtils.getRemainingPathFragment(oneLevel.toString());
//		assertEquals(result, "");
//
//		result = PathUtils.getRemainingPathFragment(twoLevels.toString());
//		assertEquals(result, File.separator + filename);
//
//		result = PathUtils.getRemainingPathFragment(twoLevelsWithoutFS.toString());
//		assertEquals(result, File.separator + filename);
//
//		result = PathUtils.getRemainingPathFragment(zeroLevels.toString());
//		assertEquals(result, "");
//	}

}
