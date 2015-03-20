package org.peerbox;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class TestParameter {
	public static void main(String[] args) {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
	    long max = memoryBean.getHeapMemoryUsage().getMax();
	    System.out.println(max);
	}
}
