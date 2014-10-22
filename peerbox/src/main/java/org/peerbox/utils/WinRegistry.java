package org.peerbox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for the Windows Registry.
 * 
 * @author albrecht
 *
 */
public class WinRegistry {

	private static final Logger logger = LoggerFactory.getLogger(WinRegistry.class);

	/**
	 * Set the api_server_port in the registry
	 * @param port
	 * @return
	 */
	public static boolean setApiServerPort(int port) {
		if (port < 0 || port > 65535) {
			throw new IllegalArgumentException("Port out of range (port is: " + 65535 + ")");
		}

		ProcessBuilder builder = new ProcessBuilder();

		builder.command("reg", /* registry command */
				"ADD", /* add a new key */
				"HKCU\\Software\\PeerBox", /* registry key */
				"/v", "api_server_port", /* name of the value */
				"/t", "REG_DWORD", /* type of the value */
				"/d", String.format("%d", port), /* actual data */
				"/f" /* force overwrite if key exists */
		);

		if (!executeCommand(builder)) {
			logger.warn("Could not set the port in the registry");
			return false;
		}
		return true;
	}

	/**
	 * Execute a process built with a ProcessBuilder.
	 * Waits some time such that the process can finish!
	 * @param builder
	 * @return
	 */
	private static boolean executeCommand(ProcessBuilder builder) {
		boolean success = false;
		try {
			Process p = builder.start();

			// output processing (debug purposes...)
			// String out = readOutput(p);
			// logger.debug(out);

			// wait for termination -- should be fast!
			success = p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;

		} catch (InterruptedException | IOException e) {
			logger.info("Exception during command execution", e);
			return false;
		}
		return success;
	}

	/**
	 * buffers std out and std err of a process and returns the string.
	 * 
	 * @param p process to observe
	 * @return string of output and error
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static String readOutput(Process p) throws IOException {
		BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream())); // note: input!!

		StringBuilder strBuilder = new StringBuilder();
		String errLine = null;
		String outLine = null;
		while ((errLine = err.readLine()) != null || (outLine = out.readLine()) != null) {
			if (errLine != null) {
				strBuilder.append(errLine);
				strBuilder.append(System.getProperty("line.separator"));
			}
			if (outLine != null) {
				strBuilder.append(outLine);
				strBuilder.append(System.getProperty("line.separator"));
			}
		}
		return strBuilder.toString();
	}

}
