package org.peerbox.presenter.settings.synchronization;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import java.util.concurrent.Callable;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class SyncTreeItem extends CheckBoxTreeItem<PathItem> implements PropertyChangeListener {

//	private ProgressState progressState;
	private ImageView defaultIcon;
	private ImageView successIcon;
	private ImageView inProgressIcon;
	private ImageView errorIcon;
	
	private String tooltip;

	private PropertyChangeSupport mPcs = new PropertyChangeSupport(this);

	
	private BooleanProperty isShared;// = new SimpleBooleanProperty(false);
    public BooleanProperty isSharedProperty() {
        if(isShared == null){
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
        					//TODO other changes too!
        				} else {
        					defaultIcon = SynchronizationUtils.getFolderStandardIcon();
        					errorIcon = SynchronizationUtils.getFolderErrorIcon();
        					successIcon = SynchronizationUtils.getFolderSuccessIcon();
        					inProgressIcon = SynchronizationUtils.getFolderInProgressIcon();
        				}
    				}
    				
    				//update in ui thread!
    			}
            });
        }
        return isShared;
    }
    
    private ObjectProperty<ProgressState> progressState;// = new SimpleObjectProperty<ProgressState>();
    
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
		
		progressState = new SimpleObjectProperty<ProgressState>();
		progressState.addListener(new ChangeListener<ProgressState>() {

			@Override
			public void changed(
					ObservableValue<? extends ProgressState> arg0,
					ProgressState oldValue, ProgressState newValue) {
				handleProgressStateChange(oldValue, newValue);
			}
			
		});
		
		setProgressState(ProgressState.DEFAULT);
		if(pathItem.isFile()){
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

//		this.isSharedProperty
	}
	
	
	
//    public ProgressState getProgressState() {
//        return progressState;
//    }
//
//    
//    public void setProgressState(ProgressState newState){
//        ProgressState oldState = progressState;
//        progressState = newState;
//        mPcs.firePropertyChange("progressState",
//        		oldState, progressState);
//    }
//    
    
    
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
    	System.out.println(getValue().getPath() + ": CHANGE progressState to " + newValue);

    	switch(newValue){
        	case IN_PROGRESS:
        		updateIconInUIThread(inProgressIcon);
          		break;
        	case SUCCESSFUL:
        		updateIconInUIThread(successIcon);
          		break;
        	case FAILED:
        		updateIconInUIThread(errorIcon);
          		break;
          	default:
        		updateIconInUIThread(defaultIcon);
    	}
    }
    
    @Override
	public void propertyChange(PropertyChangeEvent event) {
		System.out.println(getValue().getPath() + ": CHANGE!" + event.getNewValue());
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
	        			if(castedChild.getProgressState() != ProgressState.SUCCESSFUL){
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
//		javafx.application.Platform.runLater(new Runnable() {
//	        @Override
//	        public void run() {
//	        	if(getGraphic() != null && getGraphic() instanceof Label){
//	        		Label oldLabel = (Label)getGraphic();
//	        		oldLabel.setGraphic(icon);
//	        		setGraphic(oldLabel);
//	        	} else {
//	        		Label newLabel = new Label(getValue().getPath().getFileName().toString());
//	        		newLabel.setGraphic(icon);
//	        		setGraphic(newLabel);
//	        	}
//	        }
//		});
	}
	
	private void updateTooltipInUIThread(String tooltip){
		javafx.application.Platform.runLater(new Runnable() {
	        @Override
	        public void run() {
	        	if(getGraphic() != null && getGraphic() instanceof Label){
	        		Label oldLabel = (Label)getGraphic();
	        		oldLabel.setTooltip(new Tooltip(tooltip));
	        		setGraphic(oldLabel);
	        	} else {
	        		Label newLabel = new Label(getValue().getPath().getFileName().toString());
	        		newLabel.setTooltip(new Tooltip(tooltip));
	        		setGraphic(newLabel);
	        	}
	        }
		});
	}
//	
//	private class BooleanToStringConverter implements Callable<String>{
//
//		@Override
//		public String call() throws Exception {
//			// TODO Auto-generated method stub
//			return null;
//		}
//		
//	}
}
