package org.peerbox.guice;


import java.nio.file.Path;
import java.nio.file.Paths;

import javax.sql.DataSource;

import javafx.stage.Stage;

import org.apache.commons.io.FileUtils;
import org.peerbox.app.ClientContext;
import org.peerbox.app.ExitHandler;
import org.peerbox.app.IExitHandler;
import org.peerbox.app.config.BootstrappingNodesFactory;
import org.peerbox.app.config.IUserConfig;
import org.peerbox.app.manager.file.FileManager;
import org.peerbox.app.manager.file.IFileManager;
import org.peerbox.app.manager.node.INodeManager;
import org.peerbox.app.manager.node.NodeManager;
import org.peerbox.app.manager.user.IUserManager;
import org.peerbox.app.manager.user.UserManager;
import org.peerbox.delete.FileDeleteHandler;
import org.peerbox.delete.IFileDeleteHandler;
import org.peerbox.events.MessageBus;
import org.peerbox.filerecovery.FileRecoveryHandler;
import org.peerbox.filerecovery.IFileRecoveryHandler;
import org.peerbox.guice.provider.ClientContextProvider;
import org.peerbox.interfaces.IFxmlLoaderProvider;
import org.peerbox.share.IShareFolderHandler;
import org.peerbox.share.ShareFolderHandler;
import org.peerbox.utils.AppData;
import org.peerbox.view.tray.AbstractSystemTray;
import org.peerbox.view.tray.JSystemTray;
import org.peerbox.watchservice.FileEventManager;
import org.peerbox.watchservice.IFileEventManager;
import org.peerbox.watchservice.filetree.FileTree;
import org.peerbox.watchservice.filetree.persistency.DaoUtils;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class PeerBoxModule extends AbstractModule {

	private final MessageBus messageBus = new MessageBus();
	private final Stage primaryStage;

	public PeerBoxModule(Stage primaryStage) {
		this.primaryStage = primaryStage;
	}

	@Override
	protected void configure() {
		bindMessageBus();
		bindSystemTray();
		bindPrimaryStage();

		bindManagers();

		bindContextMenuHandlers();

		bind(IFxmlLoaderProvider.class).to(GuiceFxmlLoader.class);
		bind(IExitHandler.class).to(ExitHandler.class);
		bind(ClientContext.class).toProvider(ClientContextProvider.class);

	}

	private void bindMessageBus() {
		bind(MessageBus.class).toInstance(messageBus);
		messageBusRegisterRule();
	}

	private void messageBusRegisterRule() {
		bindListener(Matchers.any(), new TypeListener() {
			@Override
	        public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
	            typeEncounter.register(new InjectionListener<I>() {
	                public void afterInjection(I i) {
	                    messageBus.subscribe(i);
	                }
	            });
	        }
	    });
	}

	private void bindPrimaryStage() {
		bind(javafx.stage.Stage.class)
			.annotatedWith(Names.named("PrimaryStage"))
			.toInstance(primaryStage);
	}

	private void bindManagers() {
		bind(INodeManager.class).to(NodeManager.class);
		bind(IUserManager.class).to(UserManager.class);
		bind(IFileManager.class).to(FileManager.class);

		bind(IFileEventManager.class).to(FileEventManager.class);
	}

	private void bindContextMenuHandlers() {
		bind(IFileRecoveryHandler.class).to(FileRecoveryHandler.class);
		bind(IShareFolderHandler.class).to(ShareFolderHandler.class);
		bind(IFileDeleteHandler.class).to(FileDeleteHandler.class);
	}

	private void bindSystemTray() {
		bind(AbstractSystemTray.class).to(JSystemTray.class);
	}

	@Provides
	FileTree providesFileTree(IUserConfig cfg){
		return new FileTree(cfg.getRootPath());
	}

	@Provides
	private BootstrappingNodesFactory providesBootstrappingNodesFactory() {
		BootstrappingNodesFactory f = new BootstrappingNodesFactory();

		f.setLastNodeFile(AppData.getConfigFolder().resolve("lastnode"));
		f.setNodesFile(AppData.getConfigFolder().resolve("bootstrappingnodes"));
		f.setNodesDefaultUrl(getClass().getResource("/config/default_bootstrappingnodes"));

		return f;
	}

	@Provides
	@Singleton
	@Named("userdb")
	private DataSource providesUserDbDataSource() {
		Path dbPath = Paths.get(FileUtils.getUserDirectoryPath(), "peerbox.testdb");
		return DaoUtils.createDataSource(dbPath.toString());
	}

}
