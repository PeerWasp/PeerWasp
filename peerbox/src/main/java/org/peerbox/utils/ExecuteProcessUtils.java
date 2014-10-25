package org.peerbox.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuteProcessUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecuteProcessUtils.class);
	
	/**
	 * Execute a process built with a ProcessBuilder.
	 * Waits some time such that the process can finish!
	 * 
	 * @param builder process command to execute
	 * @param output process output, pass null to not read output
	 * @return
	 */
	public static boolean executeCommand(ProcessBuilder builder, StringBuilder output) {
		boolean success = false;
		try {
			Process p = builder.start();

			// output processing
			if(output!=null) {
				readOutput(p, output);
				logger.debug(output.toString());
			}

			// wait for termination -- should be fast!
			success = p.waitFor(5, TimeUnit.SECONDS) && p.exitValue() == 0;
			if (p.isAlive()) {
				p.destroyForcibly();
			}

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
	 * @param output 
	 * @return string of output and error
	 * @throws IOException
	 */
	private static void readOutput(Process p, StringBuilder output) throws IOException {
		BufferedReader err = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		BufferedReader out = new BufferedReader(new InputStreamReader(p.getInputStream())); // note: input!!

		String errLine = null;
		String outLine = null;
		while ((errLine = err.readLine()) != null || (outLine = out.readLine()) != null) {
			if (errLine != null) {
				output.append(errLine);
				output.append(System.getProperty("line.separator"));
			}
			if (outLine != null) {
				output.append(outLine);
				output.append(System.getProperty("line.separator"));
			}
		}
	}
}
