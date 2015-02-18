package org.peerbox.view.tray;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import net.engio.mbassy.listener.Handler;

import org.peerbox.app.manager.file.FileExecutionFailedMessage;
import org.peerbox.events.IMessageListener;
import org.peerbox.notifications.AggregatedFileEventStatus;
import org.peerbox.notifications.ITrayNotifications;
import org.peerbox.notifications.InformationNotification;
import org.peerbox.presenter.tray.TrayActionHandler;
import org.peerbox.presenter.tray.TrayException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class JSystemTray extends AbstractSystemTray implements ITrayNotifications, IMessageListener {

	private final static Logger logger = LoggerFactory.getLogger(JSystemTray.class);

	private String tooltip;
	private java.awt.TrayIcon trayIcon;
	private JTrayIcons iconProvider;
	private JTrayMenu menu;
	
	private boolean hasFailedOperations = false;

	@Inject
	public JSystemTray(TrayActionHandler actionHandler) {
		super(actionHandler);
		this.iconProvider = new JTrayIcons();
		this.menu = new JTrayMenu(trayActionHandler);
		setTooltip("");
	}

	private TrayIcon create(Image image) throws IOException {
		TrayIcon trayIcon = new java.awt.TrayIcon(image);
		trayIcon.setImageAutoSize(true);
		trayIcon.setToolTip(tooltip);
		trayIcon.setPopupMenu(menu.create());
		return trayIcon;
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
			if(hasFailedOperations){
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
		logger.debug("Information: [{}] - [{}]", in.getTitle(), in.getMessage());
		if(trayIcon != null) {
			trayIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                	menu.getTrayActionHandler().showActivity();
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
            	menu.getTrayActionHandler().showSettings();
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
	
	@Handler
	public void onSynchronizationFailed(FileExecutionFailedMessage message) throws TrayException {
		logger.trace("At least one operation failed. If the application is idle, "
				+ "the error icon is shown.");
		hasFailedOperations = true;
	}
	
	@Handler
	public void onSynchronizationErrorResolved(SynchronizationErrorsResolvedNotification message){
		logger.trace("All failed operations successfully resolved. If the application is idle, "
				+ "the success icon is shown.");
		hasFailedOperations = false;
	}
	
}
