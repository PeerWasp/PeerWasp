package org.peerbox.view;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.hive2hive.core.model.FileVersion;
import org.hive2hive.core.model.IFileVersion;
import org.hive2hive.processframework.RollbackReason;
import org.hive2hive.processframework.interfaces.IProcessComponent;
import org.hive2hive.processframework.interfaces.IProcessComponentListener;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.peerbox.FileManager;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.presenter.FileVersionSelector;
import org.peerbox.presenter.RecoverFileController;

public class RecoverFileStarter extends Application {
	
	@Mock
	private FileManager fileManager;
	
	private RecoverFileController controller;
	private FileVersionSelector versionSelector;
	
	@Mock
	private IProcessComponent process;
	ArgumentCaptor<IProcessComponentListener> processComponentListener;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		MockitoAnnotations.initMocks(this);
		when(fileManager.recover(anyObject(), anyObject())).thenReturn(process);
		
		
		
		RecoverFileStage stage = new RecoverFileStage();
		controller = new RecoverFileController();
		controller.setFileManager(null);
		
		versionSelector = new FileVersionSelector(controller);
		controller.setVersionSelector(versionSelector);
		
		stage.setFxmlLoaderProvider(new IFxmlLoaderProvider() {
			
			@Override
			public FXMLLoader create(String fxmlFile) {
				FXMLLoader loader = new FXMLLoader();
				loader.setLocation(getClass().getResource(fxmlFile));
				loader.setControllerFactory( new Callback<Class<?>, Object>() {
					
					@Override
					public Object call(Class<?> param) {
						controller.setFileManager(fileManager);
						return controller;
					}
				});
				return loader;
			}
		});
		
		stage.onFileVersionRequested(Paths.get("/tmp/peerboxt_test/test"));
		
	
		new Thread(new RecoverySimulation()).start();
		
		// capture the event listener
		processComponentListener = ArgumentCaptor.forClass(IProcessComponentListener.class);
		Mockito.verify(process).attachListener(processComponentListener.capture());
		
		
		
	}
	
	
	private class RecoverySimulation implements Runnable {

		@Override
		public void run() {
			
			try {
				
				Thread.sleep(5000);
				
				List<IFileVersion> versions = new ArrayList<IFileVersion>();
				versions.add(new FileVersion(0, 100, 100, null));
				versions.add(new FileVersion(1, 200, 200, null));
				versions.add(new FileVersion(2, 300, 300, null));
				IFileVersion selected = versionSelector.selectVersion(versions);
				
				Thread.sleep(5000);
				versionSelector.getRecoveredFileName("a", "b", "c");
				
				Thread.sleep(1000);
//				processComponentListener.getValue().onSucceeded();
				processComponentListener.getValue().onFailed(new RollbackReason("Failed"));
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		
	}
}
