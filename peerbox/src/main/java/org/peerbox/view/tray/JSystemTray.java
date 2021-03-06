package org.peerbox.view.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.Constants;
import org.peerbox.app.config.AppConfig;
import org.peerbox.app.manager.file.messages.FileExecutionFailedMessage;
import org.peerbox.app.manager.user.IUserMessageListener;
import org.peerbox.app.manager.user.LoginMessage;
import org.peerbox.app.manager.user.LogoutMessage;
import org.peerbox.app.manager.user.RegisterMessage;
import org.peerbox.events.IMessageListener;
import org.peerbox.notifications.AggregatedFileEventStatus;
import org.peerbox.notifications.ITrayNotifications;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.tray.TrayActionHandler;
import org.peerbox.presenter.tray.TrayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JSystemTray extends AbstractSystemTray implements ITrayNotifications, IMessageListener, IUserMessageListener {

	private final static Logger logger = LoggerFactory.getLogger(JSystemTray.class);

	private String tooltip;
	private java.awt.TrayIcon trayIcon;
	private final JTrayIcons iconProvider;

	private AppConfig appConfig;

	private boolean hasFailedOperations = false;


	@Inject
	public JSystemTray(AppConfig appConfig, TrayActionHandler actionHandler) {
		super(actionHandler);
		this.appConfig = appConfig;
		this.iconProvider = new JTrayIcons();
		setTooltip(Constants.APP_NAME);
	}

	private TrayIcon create(Image image) throws IOException {
		TrayIcon trayIcon = new java.awt.TrayIcon(image);
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip(tooltip);
		trayIcon.setPopupMenu(createMenu(false));
		return trayIcon;
	}

	private PopupMenu createMenu(boolean isUserLoggedIn) {
		JTrayMenu menu = new JTrayMenu(trayActionHandler);
		PopupMenu root = menu.create(isUserLoggedIn);
		return root;
	}

	@Override
	public void show() throws TrayException {
		try {
			trayIcon = create(iconProvider.getDefaultIcon());
			java.awt.SystemTray sysTray = java.awt.SystemTray.getSystemTray();
			sysTray.add(trayIcon);
		} catch (AWTException e) {
			logger.debug("SysTray AWTException.", e);
			logger.error("Could not initialize systray (tray may not be supported?)");
			throw new TrayException(e);
		} catch (IOException e) {
			logger.debug("SysTray.show IOException.", e);
			logger.error("Could not initialize systray (image not found?)");
			throw new TrayException(e);
		}
	}

	@Override
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
		if(trayIcon != null) {
			trayIcon.setToolTip(this.tooltip);
		}
	}

	@Override
	public void showDefaultIcon() throws TrayException {
		try {
			trayIcon.setImage(iconProvider.getDefaultIcon());
		} catch (IOException e) {
			logger.debug("SysTray.show IOException.", e);
			logger.error("Could not change icon (image not found?)");
			throw new TrayException(e);
		}
	}

	@Override
	public void showSyncingIcon() throws TrayException {
		try {
			trayIcon.setImage(iconProvider.getSyncingIcon());
		} catch (IOException e) {
			logger.debug("SysTray.show IOException.", e);
			logger.error("Could not change icon (image not found?)");
			throw new TrayException(e);
		}
	}

	@Override
	public void showSuccessIcon() throws TrayException {
		try {
			if (hasFailedOperations) {
				trayIcon.setImage(iconProvider.getErrorIcon());
			} else {
				trayIcon.setImage(iconProvider.getSuccessIcon());
			}

		} catch (IOException e) {
			logger.debug("SysTray.show IOException.", e);
			logger.error("Could not change icon (image not found?)");
			throw new TrayException(e);
		}
	}

	@Override
	public void showInformationMessage(String title, String message) {
		setNewMessageActionListener(null);
		showMessage(title, message, MessageType.INFO);
	}

	private void showMessage(String title, String message, MessageType type) {
		if (!appConfig.isTrayNotificationEnabled()) {
			return;
		}

		if (title == null) {
			title = "";
		}
		if (message == null) {
			message = "";
		}

		logger.info("{} Message: \n{}\n{}", type.toString(), title, message);
		if (trayIcon != null) {
			trayIcon.displayMessage(title, message, type);
		}

	}

	/**
	 * NOTIFICATIONS - implementation of the ITrayNotifications interface
	 */

	@Override
	@Handler
	public void showInformation(InformationNotification in) {
		ActionListener listener = new ShowActivityActionListener();
		setNewMessageActionListener(listener);
		showMessage(in.getTitle(), in.getMessage(), MessageType.INFO);
	}

	@Override
	@Handler
	public void showFileEvents(AggregatedFileEventStatus event) {
		String msg = generateAggregatedFileEventStatusMessage(event);
		ActionListener listener = new ShowSettingsActionListener();
		setNewMessageActionListener(listener);
		showMessage("File Synchronization", msg, MessageType.INFO);
	}

	/**
	 * Removes all action listeners from the tray icon and adds the given listener.
	 *
	 * @param listener to add. If null, listeners are only and no listener is added.
	 */
	private void setNewMessageActionListener(ActionListener listener) {
		// remove all and add given action listener
		if (trayIcon != null) {
			for (ActionListener l : trayIcon.getActionListeners()) {
				trayIcon.removeActionListener(l);
			}
			if (listener != null) {
				trayIcon.addActionListener(listener);
			}
		}
	}

	/**
	 * Action listener that opens the activity window
	 */
	private class ShowActivityActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			trayActionHandler.showActivity();
		}
	}

	/**
	 * Action listener that opens the settings window
	 */
	private class ShowSettingsActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			trayActionHandler.showSettings();
		}
	}

	private String generateAggregatedFileEventStatusMessage(AggregatedFileEventStatus e) {

		StringBuilder sb = new StringBuilder();
		sb.append("Hi there, some of your files changed.\n\n");
		if(e.getNumFilesAdded() > 0) {
			sb.append("new files: ").append(e.getNumFilesAdded()).append("\n");
		}
		if(e.getNumFilesModified() > 0) {
			sb.append("updated files: ").append(e.getNumFilesModified()).append("\n");
		}
		if(e.getNumFilesDeleted() > 0) {
			sb.append("deleted files: ").append(e.getNumFilesDeleted()).append("\n");
		}
		if(e.getNumFilesMoved() > 0) {
			sb.append("moved files: ").append(e.getNumFilesMoved()).append("\n");
		}
		return sb.toString();
	}

	@Handler
	public void onSynchronizationComplete(SynchronizationCompleteNotification message) throws TrayException {
		logger.trace("Set success icon.");
		showSuccessIcon();
	}

	@Handler
	public void onSynchronizationStart(SynchronizationStartsNotification message) throws TrayException {
		logger.trace("Set synchronization icon.");
		showSyncingIcon();
	}

	@Handler
	public void onSynchronizationFailed(FileExecutionFailedMessage message) throws TrayException {
		logger.trace("At least one operation failed. If the application is idle, "
				+ "the error icon is shown.");
		hasFailedOperations = true;
	}

	@Handler
	public void onSynchronizationErrorResolved(SynchronizationErrorsResolvedNotification message) {
		logger.trace("All failed operations successfully resolved. If the application is idle, "
				+ "the success icon is shown.");
		hasFailedOperations = false;
	}

	/***
	 * User event handling
	 ***/

	@Handler
	@Override
	public void onUserRegister(RegisterMessage register) {
		// nothing to do
	}

	@Handler
	@Override
	public void onUserLogin(LoginMessage login) {
		// refresh menu
		trayIcon.setPopupMenu(null);
		trayIcon.setPopupMenu(createMenu(true));
	}

	@Handler
	@Override
	public void onUserLogout(LogoutMessage logout) {
		// refresh menu
		trayIcon.setPopupMenu(null);
		trayIcon.setPopupMenu(createMenu(false));
	}
}
