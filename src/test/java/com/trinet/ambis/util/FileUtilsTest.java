package com.trinet.ambis.util;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FileUtilsTest {

	private FileUtils fileUtils = new FileUtils();

	/**
	 * given file paths </br>
	 * when deleteFiles is called </br>
	 * then delete the files
	 * 
	 * @throws IOException
	 * 
	 */
	@Test
	public void deleteFilesTest() throws IOException {
		Path path1 = Files.write(Files.createTempFile("File1", ".pdf"), "File Content 1".getBytes());
		Path path2 = Files.write(Files.createTempFile("File2", ".pdf"), "File Content 2".getBytes());
		// when
		fileUtils.deleteFiles(path1, path2);
		// then
		// assertions
		assertFalse(path1.toFile().exists());
		assertFalse(path2.toFile().exists());
	}

}
