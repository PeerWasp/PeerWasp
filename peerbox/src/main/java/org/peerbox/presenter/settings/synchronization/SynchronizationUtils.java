package org.peerbox.presenter.settings.synchronization;

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
	
	private static final String successTooltip = "This file is synchronized.";
	private static final String errorTooltip = "Synchronization of this file failed.";
	private static final String inProgressTooltip = "Synchronization of this file is currently ongoing.";
	private static final String softDeletedTooltip = "This file is currently not synchronized.\n"
			+ "Check the box to download it.";
	
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
	public static ImageView getFolderSuccessIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/folder-success.png")));
	}
	
	public static ImageView getFolderInProgressIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/folder-synch.png")));
	}
	
	public static ImageView getFolderErrorIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/folder-error.png")));
	}
	
	public static ImageView getFolderStandardIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/folder-standard.png")));
	}
	
	public static ImageView getFileSuccessIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/file-success.png")));
	}
	
	public static ImageView getFileInProgressIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/file-synch.png")));
	}
	
	public static ImageView getFileErrorIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/file-error.png")));
	}
	
	public static ImageView getFileStandardIcon(){
		return new ImageView(new Image(SynchronizationUtils.class.getResourceAsStream("/images/file-standard.png")));
	}
}
