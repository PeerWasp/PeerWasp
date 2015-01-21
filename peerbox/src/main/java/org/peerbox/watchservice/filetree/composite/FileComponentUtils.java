package org.peerbox.watchservice.filetree.composite;

public class FileComponentUtils {

	private FileComponentUtils() {
		// prevent instantiation
		// only static methods
	}

	/**
	 * Recursively goes up the tree and sets the isUploaded property. Starts from the child and
	 * sets the property on all ancestors (parent relationship).
	 *
	 * @param child the first node from which the recursion starts.
	 * @param isUploaded new value to set
	 */
	public static void setIsUploadedWithAncestors(final FileComponent child, final boolean isUploaded) {
		FileComponent current = child;
		while (current != null) {
			current.setIsUploaded(isUploaded);
			current = current.getParent();
		}
	}
}
