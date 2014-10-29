package org.peerbox.utils;

import static org.junit.Assert.fail;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the registry setters by first setting a value and then querying the registry
 * using the output of the REG application.
 * 
 * @author albrecht
 *
 */
public class WinRegistryTest {

	private static final int NUMBER_ITERATIONS = 100;

	@Test(expected = IllegalArgumentException.class)
	public void testNegativeApiServerPort() {
		WinRegistry.setApiServerPort(-1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTooBigApiServerPort() {
		WinRegistry.setApiServerPort(65536);
	}

	@Test
	public void testSetApiServerPort() {
		for (int i = 0; i < NUMBER_ITERATIONS; ++i) {
			int port = RandomUtils.nextInt(0, 65536);
			WinRegistry.setApiServerPort(port);
			assertPort(port);
		}
	}

	private void assertPort(int port) {
		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"QUERY", /* query a key */
				"HKCU\\Software\\PeerBox", /* registry key */
				"/v", "api_server_port", /* name of the value */
				"/t", "REG_DWORD" /* type of the value */
		);

		StringBuilder output = new StringBuilder();
		ExecuteProcessUtils.executeCommand(builder, output);

		// output contains (among other stuff) the hex string
		String outputStr = output.toString();
		String hexPort = String.format("0x%s", Integer.toHexString(port));
		Assert.assertTrue(outputStr.contains(hexPort));
		Assert.assertTrue(outputStr.contains("REG_DWORD"));
	}

	@Test
	public void testSetRootPath() {
		for (int i = 0; i < NUMBER_ITERATIONS; ++i) {
			String randomFolder = RandomStringUtils.randomAlphanumeric(12);
			String rootPath = String.format("C:\\PeerBox\\%s", randomFolder);
			WinRegistry.setRootPath(rootPath);
			assertRootPath(rootPath);
		}
	}

	private void assertRootPath(String rootPath) {
		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"QUERY", /* query a key */
				"HKCU\\Software\\PeerBox", /* registry key */
				"/v", "rootpath", /* name of the value */
				"/t", "REG_SZ" /* type of the value */
		);

		StringBuilder output = new StringBuilder();
		ExecuteProcessUtils.executeCommand(builder, output);

		String outputStr = output.toString();
		Assert.assertTrue(outputStr.contains(rootPath));
		Assert.assertTrue(outputStr.contains("REG_SZ"));
	}

}
