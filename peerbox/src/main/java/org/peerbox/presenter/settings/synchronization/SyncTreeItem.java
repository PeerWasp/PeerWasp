package org.peerbox.presenter.settings.synchronization;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

public class SyncTreeItem extends CheckBoxTreeItem<PathItem> implements PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(SyncTreeItem.class);
	private ImageView defaultIcon;
	private ImageView successIcon;
	private ImageView inProgressIcon;
	private ImageView errorIcon;
	
	private String sharingTooltip = "";
	
	private void setSharingTooltip(String sharingTooltip){
		this.sharingTooltip = sharingTooltip;
	}

	private PropertyChangeSupport mPcs = new PropertyChangeSupport(this);

	
    public final boolean getIsShared(){
    	return isShared.get();
    }
    
    public final void setIsShared(Boolean newValue){
   	 	isShared.set(newValue);
    }
    
	private BooleanProperty isShared = new SimpleBooleanProperty(false);
    public BooleanProperty isSharedProperty() {
        if(isShared == null){
            
        }
        return isShared;
    }
    
    private ObjectProperty<ProgressState> progressState;
    
    public final ProgressState getProgressState(){
    	return progressState.get();
    }
    
    public final void setProgressState(ProgressState newValue){
   	 	ProgressState oldProgressState = getProgressState();
   	 	progressState.set(newValue);
   	 	mPcs.firePropertyChange("progressState", oldProgressState, progressState);
    }
    
    public ObjectProperty<ProgressState> progressStateProperty() {
    	if(progressState == null){
    		progressState = new SimpleObjectProperty<ProgressState>();
    		progressState.addListener(new ChangeListener<ProgressState>() {

				@Override
				public void changed(
						ObservableValue<? extends ProgressState> arg0,
						ProgressState oldValue, ProgressState newValue) {
					handleProgressStateChange(oldValue, newValue);
				}
    			
			});
    	}
    	return progressState;
    }
    
	public SyncTreeItem(PathItem pathItem){
		super(pathItem);
		
		setGraphic(new Label(pathItem.getPath().getFileName().toString()));
		
		initializeProgressStateProperty();
		initializeIsSharedProperty();
		
		initializeIcons();
		
		setProgressState(ProgressState.DEFAULT);
	}
	
	private void initializeIsSharedProperty() {
		isShared = new SimpleBooleanProperty(false);
	    isShared.addListener(new ChangeListener<Boolean>(){
	
			@Override
			public void changed(ObservableValue<? extends Boolean> observable,
					Boolean oldValue, Boolean newValue) {
				if(getValue().isFolder()){
					if(newValue && getValue().isFolder()){
						defaultIcon = SynchronizationUtils.getSharedFolderStandardIcon();
						errorIcon = SynchronizationUtils.getSharedFolderErrorIcon();
						successIcon = SynchronizationUtils.getSharedFolderSuccessIcon();
						inProgressIcon = SynchronizationUtils.getSharedFolderInProgressIcon();
						
						sharingTooltip = SynchronizationUtils.getSharedFolderTooltip();
					} else {
						defaultIcon = SynchronizationUtils.getFolderStandardIcon();
						errorIcon = SynchronizationUtils.getFolderErrorIcon();
						successIcon = SynchronizationUtils.getFolderSuccessIcon();
						inProgressIcon = SynchronizationUtils.getFolderInProgressIcon();
					}
				}
				//set to default to trigger change handler
				ProgressState temp = getProgressState();
				setProgressState(ProgressState.DEFAULT);
				setProgressState(temp);

			}
	    });
	}
    
    private void initializeProgressStateProperty() {
    	progressState = new SimpleObjectProperty<ProgressState>();
		progressState.addListener(new ChangeListener<ProgressState>() {

			@Override
			public void changed(
					ObservableValue<? extends ProgressState> arg0,
					ProgressState oldValue, ProgressState newValue) {
				handleProgressStateChange(oldValue, newValue);
			}
			
		});
	}

	private void initializeIcons() {
    	if(getValue().isFile()){
			defaultIcon = SynchronizationUtils.getFileStandardIcon();
			errorIcon = SynchronizationUtils.getFileErrorIcon();
			successIcon = SynchronizationUtils.getFileSuccessIcon();
			inProgressIcon = SynchronizationUtils.getFileInProgressIcon();
		} else {
			defaultIcon = SynchronizationUtils.getFolderStandardIcon();
			errorIcon = SynchronizationUtils.getFolderErrorIcon();
			successIcon = SynchronizationUtils.getFolderSuccessIcon();
			inProgressIcon = SynchronizationUtils.getFolderInProgressIcon();
		}
	}

	public void setDefaultIcon(ImageView newDefaultIcon){
    	 ImageView oldDefaultIcon = defaultIcon;
         defaultIcon = newDefaultIcon;
         mPcs.firePropertyChange("defaultIcon",
         		oldDefaultIcon, defaultIcon);
    }
    
    public void
    addPropertyChangeListener(String property, PropertyChangeListener listener) {
        mPcs.addPropertyChangeListener(property, listener);
    }
    
    public void
    removePropertyChangeListener(String property, PropertyChangeListener listener) {
        mPcs.removePropertyChangeListener(property, listener);
    }
    
    private void handleProgressStateChange(ProgressState oldValue, ProgressState newValue){
    	logger.trace("{} : Changed progressState from {} to {} ", getValue().getPath(), oldValue, newValue);

    	if(oldValue == newValue){
    		updateIconInUIThread();
    		return;
    	}
    	switch(newValue){
        	case IN_PROGRESS:
        		updateIconInUIThread(inProgressIcon);
        		updateTooltipInUIThread(SynchronizationUtils.getInProgressToolTip());
          		break;
        	case SUCCESSFUL:
        		updateIconInUIThread(successIcon);
        		updateTooltipInUIThread(SynchronizationUtils.getSuccessTooltip());
          		break;
        	case FAILED:
        		updateIconInUIThread(errorIcon);
        		updateTooltipInUIThread(SynchronizationUtils.getErrorTooltip());
          		break;
          	default:
        		updateIconInUIThread(defaultIcon);
        		updateTooltipInUIThread(SynchronizationUtils.getSuccessTooltip());
    	}
    }
    
    @Override
	public void propertyChange(PropertyChangeEvent event) {
    	logger.trace("{}: Changed property {} to {}", getValue().getPath(), event.getPropertyName(), event.getNewValue());
        if (event.getPropertyName().equals("progressState")) {
        	ProgressState eventState = ((ObjectProperty<ProgressState>)event.getNewValue()).get();
        	switch(getProgressState()){
	        	case IN_PROGRESS:
	        		handleWhenInProgress(eventState);
	          		break;
	        	case SUCCESSFUL:
	        		handleWhenSuccessful(eventState);
	          		break;
	        	case FAILED:
	          		break;
	          	default:
	          		handleWhenDefault(eventState);
        	}
        
        }
	}

	private void handleWhenDefault(ProgressState eventState) {
		switch(eventState){
			case FAILED:
				//ignore
				break;
			default:
				setProgressState(eventState);
		}
	}

	private void handleWhenSuccessful(ProgressState eventState) {
		switch(eventState){
			case IN_PROGRESS:
				setProgressState(eventState);
				break;
			default:
		}
	}

	private void handleWhenInProgress(ProgressState eventState) {
		switch(eventState){
			case SUCCESSFUL:
				for(TreeItem<PathItem> child : getChildren()){
	        		if(child instanceof SyncTreeItem){
	        			SyncTreeItem castedChild = (SyncTreeItem) child;
	        			if(castedChild.getProgressState() == ProgressState.IN_PROGRESS){
	        				return;
	        			}
	        		}
	        	}
				setProgressState(eventState);
				break;
			default:
				//ignore
		}
	}
	
	private void updateIconInUIThread(ImageView icon){
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(getGraphic() != null && getGraphic() instanceof Label){
	        		Label oldLabel = (Label)getGraphic();
	        		oldLabel.setGraphic(icon);
	        		setGraphic(oldLabel);
	        	} else {
	        		Label newLabel = new Label(getValue().getPath().getFileName().toString());
	        		newLabel.setGraphic(icon);
	        		setGraphic(newLabel);
	        	}
	        }
		});
	}
	
	private void updateIconInUIThread(){
		ImageView iconToUse = getIconByProgressState();
		updateIconInUIThread(iconToUse);
	}
	
	private ImageView getIconByProgressState() {
		switch(getProgressState()){
			case IN_PROGRESS:
				return inProgressIcon;
			case SUCCESSFUL:
				return successIcon;
			case FAILED:
				return errorIcon;
			default:
				return defaultIcon;
		}
	}

	private void updateTooltipInUIThread(String tooltip){
		
		String completeTooltip = tooltip.concat(sharingTooltip);
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(getGraphic() != null && getGraphic() instanceof Label){
	        		Label oldLabel = (Label)getGraphic();
	        		oldLabel.setTooltip(new Tooltip(completeTooltip));
	        		setGraphic(oldLabel);
	        	} else {
	        		Label newLabel = new Label(getValue().getPath().getFileName().toString());
	        		newLabel.setTooltip(new Tooltip(completeTooltip));
	        		setGraphic(newLabel);
	        	}
	        }
		});
	}
	
	public void bindTo(SyncTreeItem other){
		addPropertyChangeListener("progressState", other);
		getValue().bindPermissionsTo(other.getValue().getPermissionsSetProperty());
	}
	
	public void unbind(){
		mPcs = null;
		setValue(null);
	}
	
}
