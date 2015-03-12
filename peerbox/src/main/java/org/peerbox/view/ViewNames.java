package org.peerbox.view;


/**
 * View names: references to .fxml files in the src/main/resources/ folder
 *
 * @author albrecht
 *
 */
public final class ViewNames {

	private ViewNames () {
		// prevent instances
	}

	public static final String MAIN_VIEW = "/view/MainView.fxml";
	public static final String NETWORK_SELECTION_VIEW = "/view/NetworkSelectionView.fxml";
	public static final String JOIN_NETWORK_VIEW = "/view/JoinNetworkView.fxml";
	public static final String CREATE_NETWORK_VIEW = "/view/CreateNetworkView.fxml";
	public static final String REGISTER_VIEW = "/view/RegisterView.fxml";
	public static final String LOGIN_VIEW = "/view/LoginView.fxml";

	public static final String SETTINGS_MAIN = "/view/settings/Main.fxml";
	public static final String PROPERTIES_VIEW = "/view/settings/Properties.fxml";

	public static final String ACTIVITY_VIEW = "/view/activity/Main.fxml";

	public static final String RECOVER_FILE_VIEW = "/view/RecoverFileView.fxml";
	public static final String SHARE_FOLDER_VIEW = "/view/ShareFolderView.fxml";

}
