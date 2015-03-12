package org.peerbox.presenter.settings.synchronization;

import java.nio.file.Path;

import org.peerbox.app.manager.file.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.EventHandler;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.CheckBoxTreeItem.TreeModificationEvent;

public class ClickEventHandler implements EventHandler<TreeModificationEvent<PathItem>>{

	private static final Logger logger = LoggerFactory.getLogger(Synchronization.class);
	private Synchronization synchronization;

	public ClickEventHandler(Synchronization synchronization){
		this.synchronization = synchronization;
	}

	public Synchronization getSynchronization(){
		return synchronization;
	}

	@Override
	public void handle(TreeModificationEvent<PathItem> arg0) {
		@SuppressWarnings("unchecked")
		CheckBoxTreeItem<PathItem> source = (CheckBoxTreeItem<PathItem>) arg0.getSource();
		PathItem pathItem = source.getValue();
		Path path = pathItem.getPath();
		FileInfo file = new FileInfo(pathItem);
		if(source.isSelected()){
			logger.trace("Add {} to SYNC", path);
			getSynchronization().getToSynchronize().add(file);
			getSynchronization().getToDesynchronize().remove(file);
		} else if(!source.isIndeterminate()){
			logger.trace("Remove {} from SYNC", path);
			getSynchronization().getToSynchronize().remove(file);
			getSynchronization().getToDesynchronize().add(file);
		}
		arg0.consume();
	}
}