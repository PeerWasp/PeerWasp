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

	@Inject
	public JSystemTray(TrayActionHandler actionHandler) {
		super(actionHandler);
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
			trayIcon.setImage(iconProvider.getSuccessIcon());
		} catch (IOException e) {
			logger.debug("SysTray.show IOException.", e);
			logger.error("Could not change icon (image not found?)");
			throw new TrayException(e);
		}
	}

	@Override
	public void showInformationMessage(String title, String message) {
		System.out.println(title);
		if(trayIcon != null) {
			trayIcon.displayMessage(title, message, MessageType.INFO);
		}
	}

	/**
	 * NOTIFICATIONS - implementation of the ITrayNotifications interface
	 */

	@Override
	@Handler
	public void showInformation(InformationNotification in) {
		logger.debug("information message: [{}] - [{}]", in.getTitle(), in.getMessage());
		if(trayIcon != null) {
			trayIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	trayActionHandler.showActivity();
                    System.out.println("Message Clicked");
                    trayIcon.removeActionListener(this);
                }
            });
			trayIcon.displayMessage(in.getTitle(), in.getMessage(), MessageType.INFO);
		}
	}

	@Override
	@Handler
	public void showFileEvents(AggregatedFileEventStatus event) {
		String msg = generateAggregatedFileEventStatusMessage(event);
		logger.debug("Message received: \n[{}]", msg);
		trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	trayActionHandler.showSettings();
                System.out.println("Message Clicked");
                trayIcon.removeActionListener(this);
            }
        });
		trayIcon.displayMessage("File Synchronization", msg, MessageType.INFO);
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
