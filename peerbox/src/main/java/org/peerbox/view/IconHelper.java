package org.peerbox.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconHelper {

	public static ImageView getErrorIcon() {
		return new ImageView(new Image(IconHelper.class.getResourceAsStream("/images/warning_32.png")));
	}
}
