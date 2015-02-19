package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

public class PathItem{
	private Path path;
	private boolean isFile;

	public PathItem(Path path) {
		this.path = path;
	}

	public PathItem(Path path, boolean isSynched) {
		this(path, isSynched, true);
	}

	public PathItem(Path path, boolean isSynched, boolean isFile) {
		this.path = path;
		// setSelected(isSynched);
		setIsFile(isFile);

//		setGraphic(view);
//		javafx.application.Platform.runLater(new Runnable() {
//			@Override
//			public void run() {
//
//				final Tooltip tooltip;
//				tooltip = new Tooltip(
//						"Uncheck to remove the file\n from selective synchronization.");
//				setTooltip(tooltip);
//			}
//		});
		// setGraphic(label);
	}

	public Path getPath() {
		return path;
	}

	public boolean isFile() {
		return isFile;
	}

	private void setIsFile(boolean isFile) {
		this.isFile = isFile;
	}

	public boolean isFolder() {
		return !isFile;
	}

	@Override
	public String toString() {
		return "";
		// if (path.getFileName() == null) {
		// return path.toString();
		// } else {
		// return path.getFileName().toString(); // showing file name on the
		// TreeView
		// }
	}
}