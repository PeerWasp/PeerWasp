package org.peerbox.presenter.settings.synchronization;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends CheckBoxTreeItem<PathItem> {
    private boolean isLeaf = false;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeft = true;
    private Path path;

    public PathTreeItem(Path pathItem) {
        super(new PathItem(pathItem));
    	path = pathItem;
    }

    public static PathTreeItem createNode(Path pathItem) {
        return new PathTreeItem(pathItem);
    }

    @Override
    public ObservableList<TreeItem<PathItem>> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(buildChildren(this));
        }
        return super.getChildren();
    }  

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeft) {
            isFirstTimeLeft = false;
            Path path = getValue().getPath();
            isLeaf = !Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS);
        }
        return isLeaf;
    }

    private ObservableList<TreeItem<PathItem>> buildChildren(TreeItem<PathItem> treeItem) {
        Path path = treeItem.getValue().getPath();
        if (path != null && Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            ObservableList<TreeItem<PathItem>> children = FXCollections.observableArrayList();
            try (DirectoryStream<Path> dirs = Files.newDirectoryStream(path)) {
                for (Path dir : dirs) {
                    //path pathItem = new PathItem(dir);
                    children.add(createNode(dir));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return children;
        }
        return FXCollections.emptyObservableList();
    }
}