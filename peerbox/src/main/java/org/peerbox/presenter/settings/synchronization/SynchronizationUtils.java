package org.peerbox.presenter.settings.synchronization;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * This utility class is used to produce tolltips and icons
 * for the TreeView in the {@link org.peerbox.presenter.settings.
 * synchronization.Synchronization Synchronization} view.
 * @author Claudio
 *
 */
public class SynchronizationUtils {

	private static final String successTooltip = "This file is synchronized.\n";
	private static final String errorTooltip = "Synchronization of this file failed.\n";
	private static final String inProgressTooltip = "Synchronization of this file is currently ongoing.\n";
	private static final String softDeletedTooltip = "This file is currently not synchronized.\n"
			+ "Check the box to download it.";
	private static final String sharedFolderTooltip = "This folder is shared with other users.\n";

	public static String getSuccessTooltip(){
		return successTooltip;
	}

	public static String getErrorTooltip(){
		return errorTooltip;
	}

	public static String getInProgressToolTip(){
		return inProgressTooltip;
	}

	public static String getSoftDeletedTooltip(){
		return softDeletedTooltip;
	}

	public static String getSharedFolderTooltip() {
		return sharedFolderTooltip;
	}

	public static ImageView getFolderSuccessIcon() {
		return getImageByName("/images/folder-success.png");
	}

	public static ImageView getFolderInProgressIcon() {
		return getImageByName("/images/folder-synch.png");
	}

	public static ImageView getFolderErrorIcon() {
		return getImageByName("/images/folder-error.png");
	}

	public static ImageView getFolderStandardIcon() {
		return getImageByName("/images/folder-standard.png");
	}

	public static ImageView getFileSuccessIcon() {
		return getImageByName("/images/file-success.png");
	}

	public static ImageView getFileInProgressIcon() {
		return getImageByName("/images/file-synch.png");
	}

	public static ImageView getFileErrorIcon() {
		return getImageByName("/images/file-error.png");
	}

	public static ImageView getFileStandardIcon() {
		return getImageByName("/images/file-standard.png");
	}

	public static ImageView getSharedFolderSuccessIcon(){
		return getImageByName("/images/folder-shared.png");
	}

	public static ImageView getSharedFolderInProgressIcon() {
		return getImageByName("/images/folder-shared-synch.png");
	}

	public static ImageView getSharedFolderErrorIcon() {
		return getImageByName("/images/folder-shared-error.png");
	}

	public static ImageView getSharedFolderStandardIcon() {
		return getImageByName("/images/folder-shared-standard.png");
	}

	/**
	 * Loads the given resource and returns a new ImageView instance.
	 * @param resourceName
	 * @return image view with associated resource.
	 */
	private static ImageView getImageByName(String resourceName) {
		try (InputStream in = SynchronizationUtils.class.getResourceAsStream(resourceName)) {
			if (in != null) {
				return new ImageView(new Image(in));
			}
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(SynchronizationUtils.class);
			logger.warn("Could not load icon '{}'.", resourceName, e);
		}
		return new ImageView();
	}
}
