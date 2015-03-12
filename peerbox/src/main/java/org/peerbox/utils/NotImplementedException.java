package org.peerbox.utils;

/**
 * NotImplementedException indicates that a method was called that should not be called because
 * there is no implementation available.
 *
 * @author albrecht
 *
 */
public class NotImplementedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NotImplementedException(String message) {
		super(message);
	}
}
